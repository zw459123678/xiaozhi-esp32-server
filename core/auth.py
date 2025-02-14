# xiaozhi-esp32-server-main/core/auth.py
import logging

logger = logging.getLogger(__name__)

class AuthenticationError(Exception):
    """认证异常"""
    pass

class AuthMiddleware:
    def __init__(self, config):
        self.config = config
        self.auth_config = config["server"].get("auth", {})
        # 构建token查找表
        self.tokens = {
            item["token"]: item["name"] 
            for item in self.auth_config.get("tokens", [])
        }
        # 设备白名单
        self.allowed_devices = set(
            self.auth_config.get("allowed_devices", [])
        )
        
    async def authenticate(self, headers):
        """验证连接请求"""
        # 检查是否启用认证
        if not self.auth_config.get("enabled", False):
            return True
            
        # 验证Authorization header
        auth_header = headers.get("Authorization", "")
        if not auth_header.startswith("Bearer "):
            logger.error("Missing or invalid Authorization header")
            raise AuthenticationError("Missing or invalid Authorization header")
        
        token = auth_header.split(" ")[1]
        if token not in self.tokens:
            logger.error(f"Invalid token: {token}")
            raise AuthenticationError("Invalid token")
            
        # 验证Device-Id
        device_id = headers.get("Device-Id")
        if not device_id:
            logger.error("Missing Device-Id header")
            raise AuthenticationError("Missing Device-Id header")
            
        # 检查设备白名单
        if self.allowed_devices and device_id not in self.allowed_devices:
            logger.error(f"Device not in whitelist: {device_id}")
            raise AuthenticationError("Device not in whitelist")
            
        logger.info(f"Authentication successful - Device: {device_id}, Token: {self.tokens[token]}")
        return True
        
    def get_token_name(self, token):
        """获取token对应的设备名称"""
        return self.tokens.get(token)



