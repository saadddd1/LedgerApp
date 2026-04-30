const db = require('../models/db');

// Middleware that checks if the authenticated user has an active VIP subscription
async function vipOnly(req, res, next) {
    const { userId } = req.user;
    const user = await db.get('SELECT is_vip, vip_expire_at FROM users WHERE user_id = ?', [userId]);

    if (!user) return res.status(403).json({ error: '此功能仅限VIP会员使用', code: 'VIP_REQUIRED' });
    if (user.is_vip !== 1 || !user.vip_expire_at || user.vip_expire_at < Date.now()) {
        return res.status(403).json({
            error: '此功能仅限VIP会员使用',
            code: 'VIP_REQUIRED',
        });
    }

    next();
}

module.exports = { vipOnly };
