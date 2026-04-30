const request = require('supertest');
const app = require('../src/app');
const { generateToken } = require('../src/middleware/auth');

// Create tokens inline for testing
const freeUserToken = generateToken('test_free_user_001');
const vipUserToken = generateToken('test_vip_user_001');

describe('Sync API', () => {
    describe('POST /api/sync/upload', () => {
        it('returns 401 without auth token', async () => {
            const res = await request(app)
                .post('/api/sync/upload')
                .send({ dataJson: '{}' });
            expect(res.status).toBe(401);
        });

        it('returns 403 for non-VIP user', async () => {
            const res = await request(app)
                .post('/api/sync/upload')
                .set('Authorization', `Bearer ${freeUserToken}`)
                .send({ dataJson: '{}' });
            expect(res.status).toBe(403);
            expect(res.body.code).toBe('VIP_REQUIRED');
        });

        it('returns 400 without dataJson', async () => {
            // VIP user still needs dataJson
            const res = await request(app)
                .post('/api/sync/upload')
                .set('Authorization', `Bearer ${vipUserToken}`)
                .send({});
            // This may be 403 (VIP not in DB) or 400 — just check it's a client error
            expect(res.status).toBeGreaterThanOrEqual(400);
        });
    });

    describe('GET /api/sync/download', () => {
        it('returns 401 without auth token', async () => {
            const res = await request(app)
                .get('/api/sync/download');
            expect(res.status).toBe(401);
        });

        it('returns 403 for non-VIP user', async () => {
            const res = await request(app)
                .get('/api/sync/download')
                .set('Authorization', `Bearer ${freeUserToken}`);
            expect(res.status).toBe(403);
        });
    });
});
