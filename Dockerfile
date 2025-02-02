FROM python:3.9.21-slim

ENV LC_ALL=zh_CN.UTF-8 \
    LANG=zh_CN.UTF-8 \
    LANGUAGE=zh_CN.UTF-8

# Replace single RUN commands with a single RUN command to reduce layers
RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install -y libgomp1 libgl1-mesa-glx libglib2.0-0 \
    && apt-get clean \
    && apt-get autoremove -y

# Set working directory
WORKDIR /opt/xiaozhi-es32-server

# Clean unnecessary files to reduce image size
RUN pip install -r requirements.txt
#
## Start the application
CMD ["python", "Application.py"]