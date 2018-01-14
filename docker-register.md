
1 安装docker
https://docs.docker.com/engine/installation/linux/docker-ce/centos/#install-using-the-repository

2 没有证书？ 选择 http 
https://docs.docker.com/registry/insecure/

mkdir -p certs
openssl req \
  -newkey rsa:4096 -nodes -sha256 -keyout certs/domain.key \
  -x509 -days 365 -out certs/domain.crt
生成证书

Linux: Copy the domain.crt file to /etc/docker/certs.d/myregistrydomain.com:5000/ca.crt on every Docker host. You do not need to restart Docker.
让系统信任这个证书
windows https://docs.docker.com/docker-for-windows/faqs/#how-do-i-add-custom-ca-certificates
mac https://docs.docker.com/docker-for-mac/faqs/#how-do-i-add-custom-ca-certificates

sudo docker run -d \
  --restart=always \
  --name registry \
  -v `pwd`/auth:/auth \
  -e "REGISTRY_AUTH=htpasswd" \
  -e "REGISTRY_AUTH_HTPASSWD_REALM=Registry Realm" \
  -e "REGISTRY_AUTH_HTPASSWD_PATH=/auth/htpasswd" \
  -v /mnt/registry:/var/lib/registry \
  -v `pwd`/certs:/certs \
  -e REGISTRY_HTTP_ADDR=0.0.0.0:443 \
  -e REGISTRY_HTTP_TLS_CERTIFICATE=/certs/domain.crt \
  -e REGISTRY_HTTP_TLS_KEY=/certs/domain.key \
  -p 5443:443 \
  registry:2  
启动register

加密register
https://docs.docker.com/registry/deploying/#restricting-access
mkdir auth
docker run \
  --entrypoint htpasswd \
  registry:2 -Bbn cj testpassword > auth/htpasswd
  