FROM python:3.10-slim

RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install -y libopus-dev ffmpeg \
    && apt-get clean \
    && apt-get autoremove -y

# Set working directory
WORKDIR /opt/xiaozhi-es32-server

COPY . /opt/xiaozhi-es32-server

# Clean unnecessary files to reduce image size
RUN pip install -r requirements.txt -i https://mirrors.aliyun.com/pypi/simple/

## Start the application
CMD ["python", "app.py"]