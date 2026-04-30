require('dotenv').config();
const express = require('express');
const cors = require('cors');
const rateLimit = require('express-rate-limit');
const db = require('./models/db');

const app = express();

app.use(cors());
app.use(express.json({ limit: '50mb' }));

app.use(rateLimit({
    windowMs: 60 * 1000,
    max: 100,
    message: { error: '请求太频繁，请稍后再试' },
}));

// Lazy-init DB on first request
let dbReady = false;
const ensureDb = async (req, res, next) => {
    if (!dbReady) {
        try {
            await db.initDb();
            dbReady = true;
        } catch (e) {
            console.error('DB init failed:', e);
            return res.status(500).json({ error: '数据库初始化失败' });
        }
    }
    next();
};

app.get('/api/health', ensureDb, (req, res) => {
    res.json({ status: 'ok', uptime: process.uptime(), devMode: process.env.DEV_MODE === 'true' });
});

app.use('/api/auth', ensureDb, require('./routes/auth.routes'));
app.use('/api/sync', ensureDb, require('./routes/sync.routes'));
app.use('/api/vip', ensureDb, require('./routes/vip.routes'));

app.use((err, req, res, next) => {
    console.error('Error:', err);
    res.status(500).json({ error: err.message || '内部服务器错误' });
});

module.exports = app;
