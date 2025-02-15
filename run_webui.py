import os
import sys

# 添加项目根目录到Python路径
project_root = os.path.dirname(os.path.abspath(__file__))
sys.path.append(project_root)

from core.webui import WebUI

if __name__ == '__main__':
    webui = WebUI()
    webui.run()
