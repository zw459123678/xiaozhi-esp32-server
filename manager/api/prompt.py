import logging
from aiohttp import web
from manager.api.auth import verify_token

logger = logging.getLogger(__name__)


class PromptHandler:
    def __init__(self, config):
        self.config = config

    async def get_prompt(self, request):
        if not await verify_token(self.config, request):
            return web.json_response({'error': 'Unauthorized'}, status=401)

        return web.json_response({
            'prompt': self.config['prompt'],
            'Access-Control-Allow-Origin': '*'
        })

    async def update_prompt(self, request):
        if not await verify_token(self.config, request):
            return web.json_response({'error': 'Unauthorized'}, status=401)

        try:
            data = await request.json()
            if 'prompt' not in data:
                return web.json_response({'error': 'Missing prompt field'}, status=400)

            # 通过config参数回传修改能力
            self.config['prompt'] = data['prompt']
            return web.json_response({'success': True}, headers={'Access-Control-Allow-Origin': '*'})

        except Exception as e:
            logger.error(f"Failed to update prompt: {e}")
            return web.json_response({'error': str(e)}, status=500)
