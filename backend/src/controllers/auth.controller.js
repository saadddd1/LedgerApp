const db = require('../models/db');
const { generateToken } = require('../middleware/auth');
const smsService = require('../services/sms.service');

function generateUserId() {
    return 'usr_' + Date.now().toString(36) + Math.random().toString(36).substr(2, 5);
}

// POST /api/auth/send-code
exports.sendCode = async (req, res) => {
    const { phone } = req.body;
    if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
        return res.status(400).json({ error: '请输入正确的手机号' });
    }

    const code = smsService.generateCode();
    const result = await smsService.sendSms(phone, code);

    if (result.success) {
        res.json({ success: true, message: result.devMode ? 'DEV_MODE: 验证码已打印到控制台' : '验证码已发送' });
    } else {
        res.status(500).json({ error: '验证码发送失败，请稍后重试' });
    }
};

// POST /api/auth/verify-code
exports.verifyCode = async (req, res) => {
    const { phone, code } = req.body;
    if (!phone || !code) {
        return res.status(400).json({ error: '手机号和验证码必填' });
    }

    if (!smsService.verifyCode(phone, code)) {
        return res.status(400).json({ error: '验证码错误或已过期' });
    }

    try {
        let user = await db.get('SELECT * FROM users WHERE phone = ?', [phone]);
        if (!user) {
            const userId = generateUserId();
            await db.run(
                'INSERT INTO users (user_id, phone, created_at) VALUES (?, ?, ?)',
                [userId, phone, Date.now()]
            );
            user = await db.get('SELECT * FROM users WHERE user_id = ?', [userId]);
        }

        const isVip = user.is_vip === 1 && user.vip_expire_at && user.vip_expire_at > Date.now();
        const token = generateToken(user.user_id);

        res.json({
            token,
            userId: user.user_id,
            isVip,
            vipExpireAt: user.vip_expire_at || null,
        });
    } catch (error) {
        console.error('Verify code error:', error);
        res.status(500).json({ error: '服务器内部错误' });
    }
};
