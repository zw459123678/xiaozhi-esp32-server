![图片](docs/images/banner.png)
本项目为开源智能硬件项目 [xiaozhi-esp32](https://github.com/78/xiaozhi-esp32) 
提供后端服务。根据[小智通信协议](https://ccnphfhqs21z.feishu.cn/wiki/M0XiwldO9iJwHikpXD5cEx71nKh)使用`Python`实现。

## 适用人群

本项目需要配合esp32硬件设备使用，如果童鞋们已经购买了esp32相关硬件，且成功对接虾哥部署的后端，并且想自己独立搭建
`xiaozhi-esp32`后端服务，可学习本项目。

要想完整体验本项目，需要以下总体步骤：

- 准备一套兼容`xiaozhi-esp32`
  项目的硬件设备，具体型号可[点击这里](https://rcnv1t9vps13.feishu.cn/wiki/DdgIw4BUgivWDPkhMj1cGIYCnRf)。
- 拥有一台至少4核CPU 8G内存的普通电脑或服务器，运行本项目。部署后可以在控制台看到本项目服务的接口地址。
- 下载`xiaozhi-esp32`项目，把`接口地址`修改成本项目地址，然后编译，把新固件烧录到硬件设备上。
- 启动设备，查看电脑或服务器的控制台，如果看到日志，说明成功连到本项目的接口了。

## 功能清单

## 已实现

- `xiaozhi-esp32` 通信 WebSocket 协议
- 支持国语、粤语、英语、日语、韩语 5 种语言识别（FunASR（默认））
- 自由更换 LLM（支持ChatGLM（默认）、Dify、DeepSeek）
- 自由更换 TTS（支持EdgeTTS（默认）、火山引擎豆包TTS）

## 正在实现

- 打断对话
- 按键手动对话
- 长时间不聊天进入休眠状态
- 对话记忆

## 本项目依赖服务

| 类型  | 服务名称       | 使用方式 | 收费模式    | 备注                                                         |
|:----|:-----------|:----:|:--------|:-----------------------------------------------------------|
| LLM | DeepSeek   | 接口调用 | 消耗token | [点击申请密钥](https://platform.deepseek.com/)                   |
| LLM | Dify       | 接口调用 | 消耗token | 本地化部署                                                      |
| LLM | ChatGLMLLM | 接口调用 | 免费      | [点击创建密钥](https://bigmodel.cn/usercenter/proj-mgmt/apikeys) |
| TTS | DoubaoTTS  | 接口调用 | 消耗token | [点击创建密钥](https://console.volcengine.com/speech/service/8)  |
| TTS | EdgeTTS    | 接口调用 | 免费      |                                                            |
| VAD | SileroVAD  | 本地使用 | 免费      |                                                            |
| ASR | FunASR     | 本地使用 | 免费      |                                                            |

# 部署方式

本项目暂时只支持本地源码运行，未来将支持docker快速部署。

## 本地源码运行

### 1.安装基础环境

本项目使用`python`语言开发，依赖`python`、`conda`环境，运行本项目需安装`python`、`conda`。

安装后使用`conda`创建以下环境

```
conda remove -n xiaozhi-esp32-server --all -y
conda create -n xiaozhi-esp32-server python=3.10 -y
conda activate xiaozhi-esp32-server
```

### 2.安装本项目依赖

```
# 拉取本项目后进入本项目根目录
cd xiaozhi-esp32-server
conda activate xiaozhi-esp32-server
pip config set global.index-url https://mirrors.aliyun.com/pypi/simple/
pip install -r requirements.txt
```

### 3.下载语音识别模型

下载模型文件[SenseVoiceSmall](https://modelscope.cn/models/iic/SenseVoiceSmall/resolve/master/model.pt)到
`model/SenseVoiceSmall`目录下

### 4.配置项目

修改`config.yaml`文件，配置本项目所需的各种参数。默认的LLM使用的是`ChatGLMLLM`，你需要配置密钥，才能启动。
默认的TTS使用的是`EdgeTTS`，这个无需配置，如果你需要更换成`豆包TTS`，则需要配置密钥。

配置说明：这里是各个功能使用的默认组件，例如LLM默认使用`ChatGLMLLM`模型。如果需要切换模型，就是改对应的名称。

```
selected_module:
  ASR: FunASR
  VAD: SileroVAD
  LLM: ChatGLMLLM
  TTS: EdgeTTS
```

比如修改`LLM`使用的组件，就看本项目支持哪些`LLM`，如下就是支持`DeepSeekLLM`、`ChatGLMLLM`。你们在`selected_module`修改成对应的LLM

```
LLM:
  DeepSeekLLM:
    ...
  ChatGLMLLM:
    ...
  DifyLLM:
    ...
```

有些服务，比如如果你使用`Dify`、`豆包的TTS`，是需要密钥的，记得在配置文件加上哦！

### 5.运行项目

启动项目

```
python app.py
```

### 6.编译esp32固件

# 常见问题

## 1、TTS 经常失败、大模型反应慢

建议：如果`EdgeTTS`慢或经常失败，可以更换成`火山引擎的豆包TTS`，如果两个都慢，可能所处的网络环境需要优化一下。

## 2、大模型回复有点慢

建议：大模型和TTS都是依赖接口，如果网络环境不佳，可以考虑换成本地模型。或多尝试切换不同的接口模型。

## 3、更多问题，可联系我们反馈

![图片](docs/images/wechat.jpg)

# 鸣谢

- 本项目受[百聆语音对话机器人](https://github.com/wwbin2017/bailing)项目启发，基于该项目的基础思路完成实现。