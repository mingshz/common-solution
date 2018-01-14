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
