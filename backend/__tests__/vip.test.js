const request = require('supertest');
const app = require('../src/app');
const { generateToken } = require('../src/middleware/auth');

const token = generateToken('test_user_vip_order');

describe('VIP API', () => {
    describe('POST /api/vip/pay', () => {
        it('returns 401 without auth token', async () => {
            const res = await request(app)
                .post('/api/vip/pay')
                .send({ planId: 'lifetime', payMethod: 'alipay' });
            expect(res.status).toBe(401);
        });

        it('returns 400 with invalid planId', async () => {
            const res = await request(app)
                .post('/api/vip/pay')
                .set('Authorization', `Bearer ${token}`)
                .send({ planId: '99', payMethod: 'alipay' });
            expect(res.status).toBe(400);
        });

        it('creates order for lifetime plan', async () => {
            const res = await request(app)
                .post('/api/vip/pay')
                .set('Authorization', `Bearer ${token}`)
                .send({ planId: 'lifetime', payMethod: 'alipay' });
            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('orderId');
            expect(res.body.orderId).toMatch(/^ORD/);
            // DEV_MODE auto-activates VIP
            expect(res.body).toHaveProperty('vipExpireAt');
        });
    });

    describe('POST /api/vip/callback/alipay', () => {
        it('accepts successful callback', async () => {
            const res = await request(app)
                .post('/api/vip/callback/alipay')
                .send({ out_trade_no: 'ORD123', trade_status: 'TRADE_SUCCESS' });
            expect(res.status).toBe(200);
        });
    });
});
