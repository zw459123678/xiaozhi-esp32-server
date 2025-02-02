import json
import logging

logger = logging.getLogger(__name__)


async def handleHelloMessage(conn, text):
    await conn.websocket.send(json.dumps(conn.welcome_msg))
