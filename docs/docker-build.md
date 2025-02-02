# 编译docker镜像
1、安装docker
```
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```
2、编译docker镜像
```
# 普通运行
docker build -t xiaozhi-esp32-server:local -f ./Dockerfile .
```
3、测试本地镜像
```
docker stop xiaozhi-esp32-server
docker rm xiaozhi-esp32-server

docker run -d --name xiaozhi-esp32-server -p 8000:8000 xiaozhi-esp32-server:local
# 或者挂载本地目录，方便更新代码
docker run -d --name xiaozhi-esp32-server -p 8000:8000 -v /home/system/xiaozhi-esp32-server:/opt/xiaozhi-esp32-server xiaozhi-esp32-server:local
```
5、发布腾讯云镜像
```
# amd64
docker tag xiaozhi-esp32-server:local ccr.ccs.tencentyun.com/xinnan/xiaozhi-esp32-server:latest-amd64
docker push ccr.ccs.tencentyun.com/xinnan/xiaozhi-esp32-server:latest-amd64

# arm64
docker tag xiaozhi-esp32-server:local ccr.ccs.tencentyun.com/xinnan/xiaozhi-esp32-server:latest-arm64
docker push ccr.ccs.tencentyun.com/xinnan/xiaozhi-esp32-server:latest-arm64

# 合并版本号
docker manifest create ccr.ccs.tencentyun.com/xinnan/xiaozhi-esp32-server:1.0.0 ccr.ccs.tencentyun.com/xinnan/xiaozhi-esp32-server:latest-amd64 ccr.ccs.tencentyun.com/xinnan/xiaozhi-esp32-server:latest-arm64 --amend
docker manifest inspect ccr.ccs.tencentyun.com/xinnan/xiaozhi-esp32-server:1.0.0
docker manifest push ccr.ccs.tencentyun.com/xinnan/xiaozhi-esp32-server:1.0.0

# 推送最新版本
docker manifest rm ccr.ccs.tencentyun.com/xinnan/xiaozhi-esp32-server:latest
docker manifest create ccr.ccs.tencentyun.com/xinnan/xiaozhi-esp32-server:latest ccr.ccs.tencentyun.com/xinnan/xiaozhi-esp32-server:latest-amd64 ccr.ccs.tencentyun.com/xinnan/xiaozhi-esp32-server:latest-arm64 --amend
docker manifest inspect ccr.ccs.tencentyun.com/xinnan/xiaozhi-esp32-server:latest
docker manifest push ccr.ccs.tencentyun.com/xinnan/xiaozhi-esp32-server:latest

```
6、运行线上镜像
```
docker run -d --name xiaozhi-esp32-server --restart unless-stopped -p 8000:8000 ccr.ccs.tencentyun.com/xinnan/xiaozhi-esp32-server:latest-amd64
# 或者挂载本地目录，方便更新代码
docker run -d --name xiaozhi-esp32-server --restart unless-stopped -p 8000:8000 -v /home/system/xiaozhi-esp32-server:/opt/xiaozhi-esp32-server ccr.ccs.tencentyun.com/xinnan/xiaozhi-esp32-server:latest-amd64
```