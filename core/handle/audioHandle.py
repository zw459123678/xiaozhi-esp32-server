import logging
import json
import asyncio
import time
from core.utils.util import remove_punctuation_and_length, get_string_no_punctuation_or_emoji

logger = logging.getLogger(__name__)


async def handleAudioMessage(conn, audio):
    if not conn.asr_server_receive:
        logger.debug(f"前期数据处理中，暂停接收")
        return
    have_voice = conn.vad.is_vad(conn, audio)

    # 如果本次没有声音，本段也没声音，就把声音丢弃了
    if have_voice == False and conn.client_have_voice == False:
        conn.asr_audio.clear()
        return
    conn.asr_audio.append(audio)
    # 如果本段有声音，且已经停止了
    if conn.client_voice_stop:
        conn.asr_server_receive = False
        text, file_path = conn.asr.speech_to_text(conn.asr_audio, conn.session_id)
        logger.info(f"识别文本: {text}")
        text_len = remove_punctuation_and_length(text)
        if text_len > 0:
            stt_text = get_string_no_punctuation_or_emoji(text)
            await conn.websocket.send(json.dumps({
                "type": "stt",
                "text": stt_text,
                "session_id": conn.session_id}
            ))
            await conn.websocket.send(
                json.dumps({
                    "type": "llm",
                    "text": "😊",
                    "emotion": "happy",
                    "session_id": conn.session_id}
                ))
            conn.executor.submit(conn.chat, text)
        else:
            conn.asr_server_receive = True
        conn.asr_audio.clear()
        conn.reset_vad_states()


async def sendAudioMessage(conn, audios, duration, text):
    base_delay = conn.tts_duration

    if text == conn.tts_first_text:
        conn.tts_start_speak_time = time.time()
        await conn.websocket.send(json.dumps({
            "type": "tts",
            "state": "start",
            "session_id": conn.session_id
        }))

    # 调度文字显示任务
    text_task = asyncio.create_task(
        schedule_with_interrupt(
            base_delay - 0.5,
            send_sentence_start(conn, text)
        )
    )
    conn.scheduled_tasks.append(text_task)

    conn.tts_duration = conn.tts_duration + duration

    # 发送音频数据
    for opus_packet in audios:
        await conn.websocket.send(opus_packet)

    if conn.llm_finish_task and text == conn.tts_last_text:
        stop_duration = conn.tts_duration - (time.time() - conn.tts_start_speak_time)
        stop_task = asyncio.create_task(
            schedule_with_interrupt(stop_duration, send_tts_stop(conn, text))
        )
        conn.scheduled_tasks.append(stop_task)


async def send_sentence_start(conn, text):
    await conn.websocket.send(json.dumps({
        "type": "tts",
        "state": "sentence_start",
        "text": text,
        "session_id": conn.session_id
    }))


async def send_tts_stop(conn, text):
    await conn.websocket.send(json.dumps({
        "type": "tts",
        "state": "sentence_end",
        "text": text,
        "session_id": conn.session_id
    }))
    await conn.websocket.send(json.dumps({
        "type": "tts",
        "state": "stop",
        "session_id": conn.session_id
    }))
    conn.clearSpeakStatus()


async def schedule_with_interrupt(delay, coro):
    """可中断的延迟调度"""
    try:
        await asyncio.sleep(delay)
        await coro
    except asyncio.CancelledError:
        pass
