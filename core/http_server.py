import logging
import os
from aiohttp import web
from core.utils.util import get_local_ip, get_project_dir
from manager.api.prompt import PromptHandler

logger = logging.getLogger(__name__)


async def handle_options(request):
    headers = {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type, Authorization'
    }
    return web.Response(headers=headers)


class ConfigServer:
    def __init__(self, config: dict):
        self.config = config
        self.app = web.Application()

        # 初始化接口处理器
        self.prompt_handler = PromptHandler(config)
        self.setup_routes()

    def setup_routes(self):
        # 注册prompt接口
        self.app.router.add_get('/api/prompt', self.prompt_handler.get_prompt)
        self.app.router.add_post('/api/prompt', self.prompt_handler.update_prompt)
        self.app.router.add_options('/api/prompt', handle_options)

        # 注册静态文件路由
        static_dir = os.path.join(get_project_dir(), 'manager/static')  # 获取static目录绝对路径
        self.app.router.add_static(
            prefix='/manager/',  # 匹配前缀
            path=static_dir,  # 静态文件目录
            name='static'
        )
        self.app.router.add_get('/manager', self.redirect_to_index)

    async def redirect_to_index(self, _):
        raise web.HTTPFound('/manager/')

    async def start(self):
        try:
            http_config = self.config['manager']
            if not http_config.get('enabled', False):
                logger.info("HTTP server is disabled")
                return

            runner = web.AppRunner(self.app)
            await runner.setup()
            site = web.TCPSite(runner, http_config['ip'], http_config['port'])
            await site.start()
            logger.info(
                f"Config HTTP server is running at http://{get_local_ip()}:{http_config['port']}/manager/index.html")
            return runner  # 返回runner以便后续清理
        except Exception as e:
            logger.error(f"Failed to start HTTP server: {e}")
            raise
