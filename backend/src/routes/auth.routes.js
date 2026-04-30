const express = require('express');
const router = express.Router();
const authController = require('../controllers/auth.controller');
const rateLimit = require('express-rate-limit');

// Rate limit: max 1 SMS per 60 seconds per IP (production only)
const smsLimiter = rateLimit({
    windowMs: 60 * 1000,
    max: 1,
    message: { error: '请60秒后再获取验证码' },
    skip: () => process.env.DEV_MODE === 'true',
});

router.post('/send-code', smsLimiter, authController.sendCode);
router.post('/verify-code', authController.verifyCode);

module.exports = router;
