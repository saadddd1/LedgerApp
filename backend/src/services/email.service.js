// Email verification code service
// Uses SMTP (QQ/Gmail/163 etc.), no third-party review needed.
// In DEV_MODE, codes are printed to console instead.

const nodemailer = require('nodemailer');

const inMemoryCodes = new Map(); // email -> { code, expiresAt }

let transporter = null;

function getTransporter() {
    if (!transporter) {
        if (process.env.DEV_MODE === 'true') {
            return null;
        }
        transporter = nodemailer.createTransport({
            host: process.env.EMAIL_HOST,
            port: parseInt(process.env.EMAIL_PORT || '465'),
            secure: process.env.EMAIL_SECURE !== 'false',
            auth: {
                user: process.env.EMAIL_USER,
                pass: process.env.EMAIL_PASS,
            },
        });
    }
    return transporter;
}

function generateCode() {
    if (process.env.DEV_MODE === 'true') {
        return '123456';
    }
    return String(Math.floor(100000 + Math.random() * 900000));
}

function storeCode(email, code, ttlMinutes = 5) {
    inMemoryCodes.set(email, { code, expiresAt: Date.now() + ttlMinutes * 60 * 1000 });
}

function verifyCode(email, code) {
    const record = inMemoryCodes.get(email);
    if (!record) return false;
    if (Date.now() > record.expiresAt) {
        inMemoryCodes.delete(email);
        return false;
    }
    if (record.code !== code) return false;
    inMemoryCodes.delete(email);
    return true;
}

async function sendCode(email, code) {
    if (process.env.DEV_MODE === 'true') {
        console.log(`[DEV MODE] Email to ${email}: verification code is ${code}`);
        storeCode(email, code);
        return { success: true, devMode: true };
    }

    const mailer = getTransporter();
    if (!mailer) {
        console.error('Email transporter not configured');
        return { success: false, error: '邮件服务未配置' };
    }

    try {
        await mailer.sendMail({
            from: process.env.EMAIL_USER,
            to: email,
            subject: '您的登录验证码 - 账本',
            text: `您的验证码是 ${code}，5分钟内有效。`,
            html: `<p>您的验证码是 <strong>${code}</strong>，5分钟内有效。</p>`,
        });
        storeCode(email, code);
        return { success: true };
    } catch (error) {
        console.error('Email send error:', error.message);
        return { success: false, error: error.message };
    }
}

module.exports = { sendCode, generateCode, verifyCode };
