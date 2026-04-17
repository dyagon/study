
# Java 并发学习项目



## netty

生成证书，放到 resources 里
```bash
openssl req -x509 -newkey rsa:2048 -nodes \
  -keyout server.key \
  -out server.crt \
  -days 365 \
  -subj "/C=CN/ST=GD/L=SZ/O=MyOrg/OU=MyUnit/CN=localhost"

```
