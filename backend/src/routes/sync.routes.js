const express = require('express');
const router = express.Router();
const syncController = require('../controllers/sync.controller');
const { authenticateToken } = require('../middleware/auth');
const { vipOnly } = require('../middleware/vipOnly');

// All sync endpoints require auth + VIP
router.post('/upload', authenticateToken, vipOnly, syncController.uploadSyncData);
router.get('/download', authenticateToken, vipOnly, syncController.downloadSyncData);

module.exports = router;
