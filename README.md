demo 运行:
1. 文件形式
```
curl -X POST http://localhost:8080/agent/run \
  -H "Content-Type: application/json" \
  --data-binary @src/main/resources/req.json
```
2. 硬编码形式
```
curl -X POST http://localhost:8080/agent/run \
  -H "Content-Type: application/json" \
  -d '{"query":"What time is it? Use tool to get server time, answer in one sentence."}'
```