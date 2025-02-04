![图片](docs/images/banner.png)

# Xiaozhi ESP-32 Back-end Service (xiaozhi-esp32-server)

（[中文](README.md) | English）

This project provides backend services for the open-source smart hardware
project [xiaozhi-esp32](https://github.com/78/xiaozhi-esp32)。Implemented in Python following
the[Xiaozhi Communication Protocol](https://ccnphfhqs21z.feishu.cn/wiki/M0XiwldO9iJwHikpXD5cEx71nKh)。

## Target Audience

This project requires compatible esp32 hardware devices. If you have purchased esp32 hardware, successfully connected to
Xiage's deployed backend, and wish to independently set up the `xiaozhi-esp32` backend service, this project is for
you.

To see a demo, watch this
video: [Xiaozhi ESP32 Connecting to Custom Backend Model](https://www.bilibili.com/video/BV1FMFyejExX)

To fully experience this project, follow these steps:

- Prepare hardware compatible with the `xiaozhi-esp32` project. For supported
  models, [click here](https://rcnv1t9vps13.feishu.cn/wiki/DdgIw4BUgivWDPkhMj1cGIYCnRf).
- Use a computer/server with at least 4-core CPU and 8GB RAM to run this project. After deployment, you'll see the
  service endpoint address in the console.
- Download the `xiaozhi-esp32` project, replace the default `endpoint address` with your own, compile, and flash the
  firmware to your device.
- Start the device and check your server console logs to verify successful connection.

## Feature List

## Implemented

- `xiaozhi-esp32` WebSocket communication protocol
- Supports wake-word initiated dialogue, manual dialogue, and real-time interruption of dialogue.
- Support for 5 languages: Mandarin, Cantonese, English, Japanese, Korean (FunASR - default)
- Flexible LLM switching (ChatGLM - default, Dify, DeepSeek)
- Flexible TTS switching (EdgeTTS - default, ByteDance Doubao TTS)

## In Progress

- Sleep mode after inactivity
- Dialogue memory
- Change the mood mode

## Dependencies

| Type | Service    |  Usage   | Pricing Model	 | Notes                                                              |
|:-----|:-----------|:--------:|:---------------|:-------------------------------------------------------------------|
| LLM  | DeepSeek   | API call | Token-based    | [Apply for API Key](https://platform.deepseek.com/)                |
| LLM  | Dify       | API call | Token-based    | Self-hosted                                                        |
| LLM  | ChatGLMLLM | API call | Free           | [Create API Key](https://bigmodel.cn/usercenter/proj-mgmt/apikeys) |
| TTS  | DoubaoTTS  | API call | Token-based    | [Create API Key](https://console.volcengine.com/speech/service/8)  |
| TTS  | EdgeTTS    | API call | 免费             |                                                                    |
| VAD  | SileroVAD  |  Local   | Free           |                                                                    |
| ASR  | FunASR     |  Local   | Free           |                                                                    |

# Deployment

Currently supports local source code execution. Docker deployment coming soon.

## Local Source Code Deployment

### 1.Install Prerequisites

Requires `Python` and `Conda` environments:

```
conda remove -n xiaozhi-esp32-server --all -y
conda create -n xiaozhi-esp32-server python=3.10 -y
conda activate xiaozhi-esp32-server
```

### 2.Install Dependencies

```
cd xiaozhi-esp32-server
conda activate xiaozhi-esp32-server
pip config set global.index-url https://mirrors.aliyun.com/pypi/simple/
pip install -r requirements.txt
```

### 3.Download ASR Model

Download [SenseVoiceSmall](https://modelscope.cn/models/iic/SenseVoiceSmall/resolve/master/model.pt) to
`model/SenseVoiceSmall`.

### 4.Configure Project

Modify the `Config.yaml` file to configure the various parameters required for this project. The default LLM uses
`Chatglmllm`, you need to configure the key to start.
The default TTS uses `Edgetts`. This does not require configuration. If you need to replace it with` TTS`, you need to
configure the key.

Configuration description: This is the default component of each function, such as LLM default to use the `Chatglmllm`
model. If you need to switch the model, it is the corresponding name.

```
selected_module:
  ASR: FunASR
  VAD: SileroVAD
  LLM: ChatGLMLLM
  TTS: EdgeTTS
```

For example, modify the components used by `llm`, depending on which` llm` supports this project, as follows, it
supports `Deepseekllm` and` Chatglmllm`. You are modified to the corresponding LLM in `selectd_module`

```
LLM:
  DeepSeekLLM:
    ...
  ChatGLMLLM:
    ...
  DifyLLM:
    ...
```

Some services, for example, if you use the TTS` of the `dify` and` bean bags, you need a key, remember to add the
configuration file!

### 5.Run the Project

Run the Project

```
python app.py
```

You'll see the WebSocket endpoint in logs:

```
2025-xx-xx xx:51:59,492 - core.server - INFO - Server is running at ws://192.168.1.25:8000
2025-xx-xx xx:51:59,516 - websockets.server - INFO - server listening on 0.0.0.0:8000
```

Among them, the `ws://192.168.1.25:8000` is the interface address provided by this project. Of course, your own machine
is different from mine. Remember to find your own address.

### 6.Compile ESP32 Firmware

1. Download `xiaozhi-esp32` project, configure the project environment according to this
   tutorial [" Windows builds ESP IDF 5.3.2 Development Environment and Compiles Xiaozhi "](https://icnynnzcwou8.feishu.cn/wiki/JEYDwTTALi5s2zkGlFGcDiRknXf)
   Cure

2. Open the `xiaozhi-esp32/main/kconfig.projbuild` file, find the content of the` websocket_url` `default`, change the
   ` wss: // api.tenclass.net` to your own address, such as

Before modification:

```
config WEBSOCKET_URL
    depends on CONNECTION_TYPE_WEBSOCKET
    string "Websocket URL"
    default "wss://api.tenclass.net/xiaozhi/v1/"
    help
        Communication with the server through websocket after wake up.
```

After modification (example):

```
config WEBSOCKET_URL
    depends on CONNECTION_TYPE_WEBSOCKET
    string "Websocket URL"
    default "ws://192.168.1.25:8000/xiaozhi/v1/"
    help
        Communication with the server through websocket after wake up.
```

3. Configure build settings:

```
# The terminal command line enters the root directory of xiaozhi-esp32
cd xiaozhi-esp32
# For example, the board I use is ESP32S3, so the compile target is ESP32S3. If your board is other models, please replace it with the corresponding model
idf.py set-target esp32s3
# Enter the menu configuration
idf.py menuconfig
```

![图片](docs/images/build_setting01.png)

After entering the menu configuration, then enter `xiaozhi assistant`, set the` connection_type` to `websocket`
Go back to the main menu, then enter `xiaozhi assistant`, set the `BOARD_TYPE` of your board
Save exit and return to the terminal command line.

![图片](docs/images/build_setting02.png)

4. Build and package:

```
idf.py build
cd scripts
python release.py
```

After the compilation is successful, the firmware file `merged-binary.bin` is generated in the` build` directory in the
project root directory.
This `merged-binary.bin` is the firmware file that will be recorded on the hardware.

6. Flash
   Connect the ESP32 device to the computer, use the Chrome browser, and open the following URL

```
https://espressif.github.io/esp-launchpad/
```

Open this
tutorial, [Flash Tools/Web -side Burning Folding Step (No IDF Development Environment)] (https://ccnphfhqs21z.feishu.cn/wiki/Zpz4wXBtdimBrLk25WdcXzxcnNS).
Turn to: `Method 2: ESP-LAUNCHPAD browser web-end burning`, start from
`3. Burning firmware/download to the development board`, follow the tutorial operation.

# FAQ

## 1、TTS often fails, often overtime

Suggestion: If the `Edgetts` is slow or often fails, you can replace it with a bean bag TTS` with a volcanic engine. If
both are slow, the network environment may need to be optimized.

## 2、Big model reply is a bit slow

Suggestions: Both big models and TTS are dependent interfaces. If the network environment is not good, you can consider
changing the local model. Or try to switch different interface models.

## 3、Why is my ChatGLMLLM replying to a bit? Obviously it is Xiaozhi, but treats me as Xiaozhi.

Suggestion: The first step can be to adjust the prompt words in the configuration file. The second step, the model used
in the configuration file is the free model: `glm-4-flash`. You might consider switching to the paid version.

## 4、For more questions, contact us to feedback

![图片](docs/images/wechat.jpg)

# Acknowledgments

- This project is inspired by the [Bailin Voice Dialogue Robot] (https://github.com/wwbin2017/bailing) project, and the
  basic idea of the project is completed.