import logging
import yaml
from aiohttp import web
from core.utils.util import get_project_dir

logger = logging.getLogger(__name__)

class ConfigServer:
    def __init__(self, config: dict):
        self.config = config
        self.app = web.Application()
        self.setup_routes()

    def setup_routes(self):
        self.app.router.add_get('/', self.index)
        self.app.router.add_get('/api/prompt', self.get_prompt)
        self.app.router.add_post('/api/prompt', self.update_prompt)
        self.app.router.add_options('/api/prompt', self.handle_options)

    async def index(self, request):
        html = '''
        <!DOCTYPE html>
        <html>
        <head>
            <title>小智提示词配置</title>
            <meta charset="utf-8">
            <style>
                body { max-width: 800px; margin: 20px auto; padding: 0 20px; font-family: Arial, sans-serif; }
                textarea { width: 100%; height: 300px; margin: 20px 0; padding: 10px; }
                button { padding: 10px 20px; font-size: 16px; }
                .success { color: green; }
                .error { color: red; }
            </style>
        </head>
        <body>
            <h1>小智提示词配置</h1>
            <textarea id="prompt"></textarea>
            <div>
                <button onclick="updatePrompt()">更新提示词</button>
                <span id="status"></span>
            </div>
            <script>
                // 加载当前prompt
                fetch('/api/prompt')
                    .then(response => response.json())
                    .then(data => {
                        document.getElementById('prompt').value = data.prompt;
                    });

                function updatePrompt() {
                    const prompt = document.getElementById('prompt').value;
                    fetch('/api/prompt', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify({prompt: prompt})
                    })
                    .then(response => response.json())
                    .then(data => {
                        const status = document.getElementById('status');
                        if(data.success) {
                            status.textContent = '更新成功!';
                            status.className = 'success';
                        } else {
                            status.textContent = '更新失败: ' + data.error;
                            status.className = 'error';
                        }
                        setTimeout(() => status.textContent = '', 3000);
                    });
                }
            </script>
        </body>
        </html>
        '''
        return web.Response(text=html, content_type='text/html')

    async def handle_options(self, request):
        headers = {
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
            'Access-Control-Allow-Headers': 'Content-Type, Authorization'
        }
        return web.Response(headers=headers)

    async def verify_token(self, request):
        if 'token' not in self.config['server']['http']:
            return True
            
        expected_token = self.config['server']['http']['token']
        token = request.headers.get('Authorization', '').replace('Bearer ', '')
        
        if not token or token != expected_token:
            return False
        return True

    async def get_prompt(self, request):
        if not await self.verify_token(request):
            return web.json_response({'error': 'Unauthorized'}, status=401)

        headers = {'Access-Control-Allow-Origin': '*'}
        return web.json_response({
            'prompt': self.config['prompt']
        }, headers=headers)

    async def update_prompt(self, request):
        if not await self.verify_token(request):
            return web.json_response({'error': 'Unauthorized'}, status=401)

        try:
            data = await request.json()
            if 'prompt' not in data:
                return web.json_response({'error': 'Missing prompt field'}, status=400)

            # 更新内存中的配置
            self.config['prompt'] = data['prompt']

            # 更新配置文件
            config_path = get_project_dir() + 'config.yaml'
            with open(config_path, 'w', encoding='utf-8') as f:
                yaml.dump(self.config, f, allow_unicode=True)

            headers = {'Access-Control-Allow-Origin': '*'}
            return web.json_response({'success': True}, headers=headers)

        except Exception as e:
            logger.error(f"Failed to update prompt: {e}")
            return web.json_response({'error': str(e)}, status=500)

    async def start(self):
        try:
            http_config = self.config['server']['http']
            if not http_config.get('enabled', False):
                logger.info("HTTP server is disabled")
                return

            runner = web.AppRunner(self.app)
            await runner.setup()
            site = web.TCPSite(runner, http_config['ip'], http_config['port'])
            await site.start()
            logger.info(f"Config HTTP server is running at http://{http_config['ip']}:{http_config['port']}")
            return runner  # 返回runner以便后续清理
        except Exception as e:
            logger.error(f"Failed to start HTTP server: {e}")
            raise
