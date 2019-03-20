发布流程。

更新 web 项目
```bash
mvn clean package
mvn -pl ./web docker:build docker:push
```

```bash
docker pull registry-vpc.cn-shanghai.aliyuncs.com/mingshz/cs-server:1.8-SNAPSHOT
docker service update --image=registry-vpc.cn-shanghai.aliyuncs.com/mingshz/cs-server:1.8-SNAPSHOT csm_server
```


更新 proxy 项目
```bash
mvn -pl ./web entrance:build entrance:push
```

```bash
docker pull registry-vpc.cn-shanghai.aliyuncs.com/mingshz/cs-proxy:1.8-SNAPSHOT
docker service update --image=registry-vpc.cn-shanghai.aliyuncs.com/mingshz/cs-proxy:1.8-SNAPSHOT csm_proxy
```
