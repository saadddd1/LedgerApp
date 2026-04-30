// Tencent Cloud SMS service
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

    try {
        const tencentcloud = require('tencentcloud-sdk-nodejs-sms');
        const SmsClient = tencentcloud.sms.v20210111.Client;

        const client = new SmsClient({
            credential: {
                secretId: process.env.SMS_SECRET_ID,
                secretKey: process.env.SMS_SECRET_KEY,
            },
            region: 'ap-guangzhou',
        });

        const params = {
            SmsSdkAppId: process.env.SMS_APP_ID,
            SignName: process.env.SMS_SIGN_NAME,
            TemplateId: process.env.SMS_TEMPLATE_ID,
            TemplateParamSet: [code, '5'],
            PhoneNumberSet: ['+86' + phone],
        };

        const response = await client.SendSms(params);

        if (response.SendStatusSet[0].Code === 'Ok') {
            storeCode(phone, code);
            return { success: true };
        } else {
            console.error('SMS send failed:', response.SendStatusSet[0]);
            return { success: false, error: response.SendStatusSet[0].Message };
        }
    } catch (error) {
        console.error('SMS send error:', error.message);
        return { success: false, error: error.message };
    }
}

module.exports = { sendSms, generateCode, verifyCode };
