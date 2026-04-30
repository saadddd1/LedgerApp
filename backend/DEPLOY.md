# TwoLedger1 后端部署指南（Ubuntu）

---

## 1. 域名需要吗？

**不需要。** 先用服务器 IP 跑起来。App 里配 `http://你的IP:8080` 就能用。
以后想上线再买域名配 HTTPS。

---

## 2. 服务器准备

### 安全组（防火墙）放行端口

在云服务器控制台 → 安全组 → 添加规则：

| 端口 | 用途 |
|------|------|
| 22   | SSH 连接 |
| 8080 | 后端 API |
| 80   | HTTP（可选，配 Nginx/HTTPS 时开） |
| 443  | HTTPS（可选） |

### 连接服务器

```bash
ssh ubuntu@你的服务器IP
```

> 腾讯云 Ubuntu 默认用户是 `ubuntu`，不是 `root`。命令不需要加 `sudo` 的都别加。

---

## 3. 安装软件

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# Node.js 22
curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt install -y nodejs

# Git
sudo apt install -y git

# 验证
node -v   # 应显示 v22.x
npm -v
```

### 安装 PM2（进程守护，崩了自动重启）

```bash
sudo npm install -g pm2
pm2 startup   # 复制它输出的命令，照着执行一遍
```

---

## 4. 部署代码

```bash
# 放在家目录，不需要 root 权限
mkdir -p ~/projects
cd ~/projects
git clone https://github.com/saadddd1/LedgerApp.git
cd LedgerApp/backend
npm install
```

### 创建环境变量

**生产环境：**
```bash
cat > .env << 'EOF'
PORT=8080
JWT_SECRET=替换成随机字符串
DEV_MODE=false
EMAIL_HOST=smtp.qq.com
EMAIL_PORT=465
EMAIL_USER=你的QQ邮箱
EMAIL_PASS=你的QQ邮箱授权码
EOF
```

**开发测试（跳过短信验证）：**
```bash
cat > .env << 'EOF'
PORT=8080
JWT_SECRET=dev_test_key_12345
DEV_MODE=true
EOF
```

生成随机密钥：`openssl rand -hex 32`

---

## 5. 启动服务

```bash
cd ~/projects/LedgerApp/backend
pm2 start src/index.js --name twoledger --time
pm2 save

# 验证
curl http://localhost:8080/api/health
# 应返回 {"status":"ok","uptime":...}
```

---

## 6. 客户端配置

修改 Android 端 `app/src/main/java/com/example/ledger/network/ApiClient.kt`：

```kotlin
const val BASE_URL = "http://你的服务器IP:8080"
```

重新编译 APK。

---

## 7. （可选）Nginx + 域名 + HTTPS

```bash
# 安装 Nginx
sudo apt install -y nginx
# 创建配置
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
# 启用站点
sudo ln -s /etc/nginx/sites-available/twoledger /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx

# 安装 certbot 并申请免费 SSL
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d api.你的域名.com
```

---

## 8. 数据库备份

数据库文件位置：`~/projects/LedgerApp/backend/ledger.db`

```bash
# 创建备份目录
mkdir -p ~/backups

# 手动备份
cp ~/projects/LedgerApp/backend/ledger.db ~/backups/ledger_$(date +%Y%m%d).db

# 每日凌晨 3 点自动备份（cron 里必须用绝对路径）
echo "0 3 * * * cp /home/ubuntu/projects/LedgerApp/backend/ledger.db /home/ubuntu/backups/ledger_\$(date +\%Y\%m\%d).db" | crontab -
```

---

## 9. 常用命令

```bash
pm2 status              # 查看进程
pm2 logs twoledger      # 实时日志
pm2 restart twoledger   # 重启

# 更新代码
cd ~/projects/LedgerApp/backend
git pull
npm install
pm2 restart twoledger

# 查看端口
netstat -tlnp | grep 8080
```

---

## 10. 故障排查

| 现象 | 检查 |
|------|------|
| apt 报锁错误 | `rm -rf /var/lib/dpkg/lock-frontend` 后重试 |
| npm 找不到 | 重新登录 SSH，或 `source ~/.bashrc` |
| 启动失败 | `pm2 logs twoledger` 看日志 |
| 连不上 | 安全组是否放行 8080；`netstat -tlnp \| grep 8080` 看端口 |
| 端口被占 | `lsof -i :8080` 找 PID，`kill -9 PID` 关掉 |
| git clone 太慢 | 挂代理或改用 Gitee 镜像 |
