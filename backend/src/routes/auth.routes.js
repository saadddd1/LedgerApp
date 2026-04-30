const express = require('express');
const router = express.Router();
const authController = require('../controllers/auth.controller');
const rateLimit = require('express-rate-limit');

// Rate limit: max 3 requests per 60 seconds per IP
const sendCodeLimiter = rateLimit({
    windowMs: 60 * 1000,
    max: 3,
    message: { error: '发送太频繁，请稍后再试' },
});

router.post('/send-code', sendCodeLimiter, authController.sendCode);
router.post('/verify-code', authController.verifyCode);

module.exports = router;
