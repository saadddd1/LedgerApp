const express = require('express');
const router = express.Router();
const db = require('../models/db');

function requireAdminKey(req, res, next) {
    const key = req.query.key;
    if (!key || key !== process.env.ADMIN_KEY) {
        return res.status(401).send('<h1>401</h1><p>管理员密钥错误</p>');
    }
    next();
}

router.get('/', requireAdminKey, async (req, res) => {
    try {
        const users = await db.all('SELECT user_id, email, phone, is_vip, vip_expire_at, created_at FROM users ORDER BY created_at DESC');
        const syncs = await db.all('SELECT user_id, updated_at FROM sync_data');
        const orders = await db.all('SELECT order_id, user_id, plan_id, amount, status, created_at, paid_at FROM orders ORDER BY created_at DESC');

        const syncMap = {};
        for (const s of syncs) syncMap[s.user_id] = s.updated_at;

        const rows = users.map(u => {
            const createDate = new Date(u.created_at).toLocaleString('zh-CN');
            const syncDate = syncMap[u.user_id]
                ? new Date(syncMap[u.user_id]).toLocaleString('zh-CN')
                : '未同步';
            const isVip = u.is_vip === 1 && u.vip_expire_at && u.vip_expire_at > Date.now();
            return `<tr>
                <td>${u.user_id}</td>
                <td>${u.email || u.phone || '-'}</td>
                <td>${isVip ? 'VIP' : '普通'}</td>
                <td>${createDate}</td>
                <td>${syncDate}</td>
            </tr>`;
        }).join('');

        const orderRows = orders.map(o => {
            const orderDate = new Date(o.created_at).toLocaleString('zh-CN');
            const paidDate = o.paid_at ? new Date(o.paid_at).toLocaleString('zh-CN') : '-';
            const statusColor = o.status === 'paid' ? 'green' : o.status === 'pending' ? 'orange' : 'gray';
            return `<tr>
                <td>${o.order_id}</td>
                <td>${o.user_id}</td>
                <td>${o.plan_id}</td>
                <td>¥${o.amount.toFixed(2)}</td>
                <td style="color:${statusColor}">${o.status}</td>
                <td>${orderDate}</td>
                <td>${paidDate}</td>
            </tr>`;
        }).join('');

        const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>账本管理后台</title>
<style>
* { margin:0; padding:0; box-sizing:border-box; }
body { font-family:-apple-system,BlinkMacSystemFont,sans-serif; background:#f5f5f5; padding:20px; }
h1 { margin-bottom:8px; }
.card { background:#fff; border-radius:12px; padding:20px; margin-bottom:20px; box-shadow:0 1px 3px rgba(0,0,0,0.1); }
table { width:100%; border-collapse:collapse; }
th, td { padding:10px 12px; text-align:left; border-bottom:1px solid #eee; font-size:14px; }
th { background:#fafafa; font-weight:600; }
tr:hover { background:#f8f9ff; }
.stats { display:flex; gap:16px; margin-bottom:20px; }
.stat { background:#fff; border-radius:12px; padding:16px 24px; box-shadow:0 1px 3px rgba(0,0,0,0.1); }
.stat .num { font-size:28px; font-weight:700; color:#3B7CFF; }
.stat .label { font-size:13px; color:#999; margin-top:4px; }
</style>
</head>
<body>
<h1>📊 账本管理后台</h1>
<p style="color:#999;margin-bottom:20px;">总览用户和数据同步情况</p>

<div class="stats">
    <div class="stat"><div class="num">${users.length}</div><div class="label">总用户数</div></div>
    <div class="stat"><div class="num">${syncs.length}</div><div class="label">有云端数据</div></div>
    <div class="stat"><div class="num">${users.filter(u => u.is_vip === 1).length}</div><div class="label">VIP 用户</div></div>
</div>

<div class="card">
    <h2 style="margin-bottom:16px;">用户列表</h2>
    <table>
        <thead><tr><th>用户ID</th><th>账号</th><th>身份</th><th>注册时间</th><th>最近同步</th></tr></thead>
        <tbody>${rows || '<tr><td colspan="5" style="color:#999;text-align:center;">暂无用户</td></tr>'}</tbody>
    </table>
</div>

<div class="card">
    <h2 style="margin-bottom:16px;">订单记录</h2>
    <table>
        <thead><tr><th>订单号</th><th>用户ID</th><th>套餐</th><th>金额</th><th>状态</th><th>创建时间</th><th>支付时间</th></tr></thead>
        <tbody>${orderRows || '<tr><td colspan="7" style="color:#999;text-align:center;">暂无订单</td></tr>'}</tbody>
    </table>
</div>
</body>
</html>`;
        res.send(html);
    } catch (e) {
        console.error('Admin error:', e);
        res.status(500).send('服务器错误');
    }
});

module.exports = router;
