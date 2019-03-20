发布流程。

更新 web 项目
```bash
mvn clean install
mvn -pl ./web docker:push
```

```bash
docker pull registry-vpc.cn-shanghai.aliyuncs.com/mingshz/cs-server:latest
docker service update --image=registry-vpc.cn-shanghai.aliyuncs.com/mingshz/cs-server:latest csm_server
```


更新 proxy 项目
```bash
mvn -pl ./web entrance:build entrance:push
```

```bash
docker pull registry-vpc.cn-shanghai.aliyuncs.com/mingshz/cs-proxy:1.8-SNAPSHOT
docker service update --image=registry-vpc.cn-shanghai.aliyuncs.com/mingshz/cs-proxy:1.8-SNAPSHOT csm_proxy
```
