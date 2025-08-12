# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

XiaoZhi ESP32 Server is a voice assistant server that provides WebSocket and HTTP APIs for ESP32 voice devices. The system supports multiple AI providers (ASR, LLM, TTS, VLLM), plugin extensibility, and device management.

## Development Commands

### Running the Application
```bash
python app.py
```

### Environment Setup
Install dependencies:
```bash
pip install -r requirements.txt
```

### Docker Deployment
```bash
docker-compose up -d
```

## Architecture Overview

### Core Components
- **WebSocket Server** (`core/websocket_server.py`): Handles real-time communication with ESP32 devices
- **HTTP Server** (`core/http_server.py`): Provides OTA and vision analysis APIs
- **Message Handlers** (`core/handle/`): Process different message types (audio, text, hello, abort)
- **Provider System** (`core/providers/`): Modular AI service providers

### Provider Architecture
The system uses a modular provider pattern for AI services:
- **ASR** (Speech-to-Text): FunASR, Doubao, Aliyun, Baidu, OpenAI, etc.
- **LLM** (Language Models): ChatGLM, Doubao, DeepSeek, Qwen, OpenAI, etc.
- **TTS** (Text-to-Speech): EdgeTTS, Doubao, Aliyun, Minimax, etc.
- **VLLM** (Vision Language Models): ChatGLM-VL, Qwen-VL
- **VAD** (Voice Activity Detection): Silero VAD
- **Memory**: mem0ai, local short-term memory, or no memory
- **Intent Recognition**: LLM-based or function calling

### Configuration System
- Primary config: `config.yaml` (template with defaults)
- User config: `data/.config.yaml` (overrides, git-ignored for security)
- API config: `config_from_api.yaml` (when using management API)
- Settings loader: `config/settings.py` and `config/config_loader.py`

### Plugin System
- Plugin functions: `plugins_func/functions/`
- Auto-loading: `plugins_func/loadplugins.py`
- Built-in plugins: weather, news, music playback, Home Assistant integration
- Function calling support for compatible LLMs

### Tool System
- **Unified Tool Manager** (`core/providers/tools/unified_tool_manager.py`): Orchestrates all tool types
- **Device IoT Tools** (`core/providers/tools/device_iot/`): Hardware device control
- **MCP Tools** (`core/providers/tools/device_mcp/`, `server_mcp/`, `mcp_endpoint/`): Model Context Protocol integration
- **Server Plugins** (`core/providers/tools/server_plugins/`): Server-side function execution

## Key Configuration Files

### Main Configuration (`config.yaml`)
Contains all provider configurations, server settings, and plugin configurations. Users should create `data/.config.yaml` to override specific settings without modifying the main config.

### Dependencies (`requirements.txt`)
Key dependencies include:
- `torch`, `torchaudio`: PyTorch for model inference
- `websockets`: WebSocket server
- `aiohttp`: HTTP server and client
- `funasr`: Local ASR model
- `silero_vad`: Voice activity detection
- `openai`, `dashscope`, `cozepy`: Various AI provider clients
- `mcp`: Model Context Protocol support

### Docker Configuration
- `docker-compose.yml`: Production deployment
- `docker-compose_all.yml`: Full stack with dependencies

## Audio Processing Pipeline
1. **Audio Reception**: ESP32 devices send Opus-encoded audio via WebSocket
2. **VAD Processing**: Silero VAD detects voice activity
3. **ASR Processing**: Convert speech to text using configured provider
4. **Intent Recognition**: Optional intent classification (music, IoT commands, etc.)
5. **LLM Processing**: Generate responses using configured language model
6. **TTS Processing**: Convert response text to speech
7. **Audio Transmission**: Send synthesized audio back to device

## Message Flow
- **Hello Message**: Device authentication and capability negotiation
- **Audio Messages**: Real-time voice data streaming
- **Text Messages**: Control commands (listen states, story requests, etc.)
- **Abort Messages**: Cancel ongoing operations
- **Report Messages**: Device status and metrics

## Development Notes

### Adding New Providers
1. Create provider class inheriting from appropriate base class (`core/providers/{type}/base.py`)
2. Implement required methods (`process`, `setup`, etc.)
3. Add configuration section to `config.yaml`
4. Import provider in provider module's `__init__.py`

### Plugin Development
1. Create function in `plugins_func/functions/`
2. Use `@register_function` decorator
3. Add to `selected_module.Intent.{type}.functions` configuration list
4. Function signature must include `conn` parameter for device context

### Testing
- Manual WebSocket testing: Open `test/test_page.html` in Chrome
- Performance testing: `performance_tester.py` and `performance_tester_vllm.py`
- Audio testing: Use test files in `test/` directory

## Security Considerations
- API keys and tokens should be configured in `data/.config.yaml`
- Device authentication via token system (optional)
- JWT-based API authentication for vision analysis endpoints
- MAC address whitelisting support

## Port Configuration
- WebSocket: 8000 (configurable via `server.port`)
- HTTP: 8003 (configurable via `server.http_port`)
- Ensure both ports are accessible for ESP32 devices