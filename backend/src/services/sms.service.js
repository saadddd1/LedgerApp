// Alibaba Cloud SMS service
// In DEV_MODE, codes are printed to console instead of sending real SMS.

const inMemoryCodes = new Map(); // phone -> { code, expiresAt }

function generateCode() {
    if (process.env.DEV_MODE === 'true') {
        return '123456';
    }
    return String(Math.floor(100000 + Math.random() * 900000));
}

function storeCode(phone, code, ttlMinutes = 5) {
    inMemoryCodes.set(phone, { code, expiresAt: Date.now() + ttlMinutes * 60 * 1000 });
}

function verifyCode(phone, code) {
    const record = inMemoryCodes.get(phone);
    if (!record) return false;
    if (Date.now() > record.expiresAt) {
        inMemoryCodes.delete(phone);
        return false;
    }
    if (record.code !== code) return false;
    inMemoryCodes.delete(phone); // one-time use
    return true;
}

async function sendSms(phone, code) {
    if (process.env.DEV_MODE === 'true') {
        console.log(`[DEV MODE] SMS to ${phone}: verification code is ${code}`);
        storeCode(phone, code);
        return { success: true, devMode: true };
    }

    // Production: Alibaba Cloud SMS API
    try {
        const Dysmsapi = require('@alicloud/dysmsapi20170525');
        const { default: OpenApi, Config } = require('@alicloud/openapi-client');

        const config = new Config({
            accessKeyId: process.env.SMS_ACCESS_KEY_ID,
            accessKeySecret: process.env.SMS_ACCESS_KEY_SECRET,
        });
        config.endpoint = 'dysmsapi.aliyuncs.com';

        const client = new Dysmsapi.default(config);
        const sendReq = new Dysmsapi.SendSmsRequest({
            phoneNumbers: phone,
            signName: process.env.SMS_SIGN_NAME,
            templateCode: process.env.SMS_TEMPLATE_CODE,
            templateParam: JSON.stringify({ code }),
        });

        const response = await client.sendSms(sendReq);
        if (response.body.code === 'OK') {
            storeCode(phone, code);
            return { success: true };
        } else {
            console.error('SMS send failed:', response.body);
            return { success: false, error: response.body.message };
        }
    } catch (error) {
        console.error('SMS send error:', error.message);
        // Fallback: still store code in dev-liked mode when SMS fails
        if (process.env.DEV_MODE === 'true') {
            storeCode(phone, code);
            return { success: true, devMode: true };
        }
        return { success: false, error: error.message };
    }
}

module.exports = { sendSms, generateCode, verifyCode };
