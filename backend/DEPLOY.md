# TwoLedger1 后端部署指南（新手版）

---

## 1. 域名需要吗？

**不需要。** 先用服务器 IP 跑起来。App 里配 `http://你的IP:8080` 就能用。
等以后想上线应用商店再买域名配 HTTPS。

---

## 2. 服务器准备

### 系统：Ubuntu Server 22.04 LTS

### 安全组（防火墙）放行端口：
| 端口 | 用途 |
|------|------|
| 22 | SSH 连接 |
| 8080 | 后端 API |

在云服务器控制台的"安全组"里添加规则，放行 TCP 8080。

### 连接服务器

```bash
ssh root@你的服务器IP
```

---

## 3. 安装软件（一条条执行）

```bash
# 更新系统
apt update && apt upgrade -y

# 安装 Node.js 22
curl -fsSL https://deb.nodesource.com/setup_22.x | bash -
apt install -y nodejs

# 检查版本
node -v    # 应显示 v22.x
npm -v

# 安装 Git
apt install -y git

# 安装 PM2（进程守护，崩了自动重启）
npm install -g pm2
pm2 startup   # 复制它输出的命令执行一遍，实现开机自启
```

---

## 4. 部署代码

```bash
# 在服务器上创建目录
mkdir -p /opt/twoledger
cd /opt/twoledger

# 方式一：用 Git 拉代码（推荐）
git clone https://github.com/你的仓库/twoledger.git .
# 或者
git init && git remote add origin <仓库地址> && git pull origin main

# 方式二：从本地上传（在你自己电脑上执行）
scp -r backend/* root@服务器IP:/opt/twoledger/

# 安装依赖
npm install

# 创建环境变量文件
cat > .env << 'EOF'
PORT=8080
JWT_SECRET=改成随机字符串
DEV_MODE=false
EOF

# 生成随机密钥
JWT_SECRET=$(openssl rand -hex 32)
# 用这个值更新 .env
```

### 如果是开发测试，保持 DEV_MODE=true 跳过短信验证：

```bash
cat > .env << 'EOF'
PORT=8080
JWT_SECRET=dev_test_key_12345
DEV_MODE=true
EOF
```

---

## 5. 启动服务

```bash
# 启动
pm2 start src/index.js --name twoledger --time

# 保存 PM2 进程列表（重启后自动恢复）
pm2 save

# 查看状态
pm2 status

# 查看日志
pm2 logs twoledger

# 测试是否跑起来了
curl http://localhost:8080/api/health
# 应返回 {"status":"ok","uptime":...}
```

---

## 6. 客户端配置

修改 Android 端 `ApiClient.kt`：

```kotlin
const val BASE_URL = "http://你的服务器IP:8080"
```

重新编译 APK 安装即可。

---

## 7. （可选）配 Nginx + 域名 + HTTPS

等有了域名再做这一步：

```bash
apt install -y nginx

# 配置反向代理
nano /etc/nginx/sites-available/twoledger
```

```nginx
server {
    listen 80;
    server_name api.你的域名.com;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    client_max_body_size 10m;
}
```

```bash
ln -s /etc/nginx/sites-available/twoledger /etc/nginx/sites-enabled/
nginx -t && systemctl reload nginx

# SSL 证书
apt install -y certbot python3-certbot-nginx
certbot --nginx -d api.你的域名.com
```

---

## 8. 数据库备份

SQLite 数据库文件：`/opt/twoledger/ledger.db`

```bash
# 手动备份
cp /opt/twoledger/ledger.db ~/backup_$(date +%Y%m%d).db

# 每日自动备份（crontab）
echo "0 3 * * * cp /opt/twoledger/ledger.db /opt/backups/ledger_\$(date +\%Y\%m\%d).db" | crontab -
```

---

## 9. 常用维护命令

```bash
pm2 status              # 查看进程
pm2 logs twoledger      # 实时日志
pm2 restart twoledger   # 重启
pm2 stop twoledger      # 停止

# 更新代码
cd /opt/twoledger
git pull
npm install
pm2 restart twoledger

# 查看端口占用
netstat -tlnp | grep 8080
```
