const express = require('express');
const router = express.Router();
const syncController = require('../controllers/sync.controller');
const { authenticateToken } = require('../middleware/auth');

router.post('/upload', authenticateToken, syncController.uploadSyncData);
router.get('/download', authenticateToken, syncController.downloadSyncData);

module.exports = router;
