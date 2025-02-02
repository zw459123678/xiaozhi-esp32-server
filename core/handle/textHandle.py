import logging

logger = logging.getLogger(__name__)


async def handleTextMessage(conn, message):
    await conn.websocket.send(message)
