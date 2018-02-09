部署线路图

mysql
worker

前端包editor nginx /editor
前端包cs-manager nginx /
配置fallback
配置proxy to worker
开发一个nginx image 可以在 部署时读取json配置 并为此渲染proxy文件

优先级如下
proxy
editor
other  

export 80


删除所有已关闭的容器
docker rm `docker ps -a|grep Exited|awk '{print $1}'`
删除所有未标记的镜像
docker rmi `docker image ls|grep '<none>'|awk '{print $3}'`

删除正在关闭中的容器
docker ps -a|grep 'Removal In Progress'
docker rm `docker ps -a|grep 'Removal In Progress'|awk '{print $1}'`

无法删除的问题
第一步 直接rm 会发现错误 比如文件mount 错误
第二步 查找进程信息
grep -l 错误的mount-id /proc/*/mountinfo
获得结果: /proc/27877/mountinfo
ps -f 4388


最终解决 /etc/systemd/system/multi-user.target.wants/docker.service
```conf
# 在 Service 段最后加入私有挂在参数
[Service]
MountFlags=private
```