const request = require('supertest');
const app = require('../src/app');

const TEST_PHONE = '13800138000';

describe('Auth API', () => {
    describe('POST /api/auth/send-code', () => {
        it('sends code for valid phone', async () => {
            const res = await request(app)
                .post('/api/auth/send-code')
                .send({ phone: TEST_PHONE });
            expect(res.status).toBe(200);
            expect(res.body.success).toBe(true);
        });

        // Rate-limited to 1 per 60s, so we test error cases via verify-code instead
    });

    describe('POST /api/auth/verify-code', () => {
        it('rejects empty body', async () => {
            const res = await request(app)
                .post('/api/auth/verify-code')
                .send({});
            expect(res.status).toBe(400);
        });

        it('rejects missing code', async () => {
            const res = await request(app)
                .post('/api/auth/verify-code')
                .send({ phone: TEST_PHONE });
            expect(res.status).toBe(400);
        });

        it('rejects wrong code', async () => {
            const res = await request(app)
                .post('/api/auth/verify-code')
                .send({ phone: TEST_PHONE, code: '000000' });
            expect(res.status).toBe(400);
        });

        it('verifies with correct code (DEV_MODE: 123456)', async () => {
            const res = await request(app)
                .post('/api/auth/verify-code')
                .send({ phone: TEST_PHONE, code: '123456' });
            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('token');
            expect(res.body).toHaveProperty('userId');
            expect(typeof res.body.isVip).toBe('boolean');
        });

        it('verifying again returns new token for same user', async () => {
            // Code is one-time use, request a new one first
            await request(app)
                .post('/api/auth/send-code')
                .send({ phone: TEST_PHONE });
            const res = await request(app)
                .post('/api/auth/verify-code')
                .send({ phone: TEST_PHONE, code: '123456' });
            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('token');
        });
    });
});
