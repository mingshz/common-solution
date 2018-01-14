这个项目将api-mocker相关的访问进行转发的 ngxin

约定配置如下
wx匹配功能
url-json => server:port
单页应用转发

https support 

或者调整下思路，将host一个文件映射过去，我们只管理这个文件
生成一个Dockerfile 并且足够的丰富
name: 生成的imageName

最终呈现见过是 完成了image的构建

-----
执行目录需要一个
entrance.json 文件
    name: 生成的imageName
    ssl: 是否开启ssl
    servers:
        host:
        port: 默认8080
        localApiJsonPath: 本地的api json文件
        localApiFile: 本地api json文件
        apiServerUrlPrefix: 可选 url地址，默认是 http://cs.ming.com
        projectId: 项目id
        useGitBranch: 默认true 使用当前代码分支作为分支名
        branch: 可选
    staticServers: 特指单页面应用程序
        locationUri: https://nginx.org/en/docs/http/ngx_http_core_module.html#location
        host: 
        port: 默认80
           
certificate 文件夹 如果开启了ssl的话，里面包含的文件都取名为host;比如 host.key host.pem

最终生成一个image
docker build -t produce-mocker-nginx .
