const db = require('../models/db');

exports.uploadSyncData = async (req, res) => {
    try {
        const { userId } = req.user;
        const { dataJson } = req.body;

        if (!dataJson) {
            return res.status(400).json({ error: '没有需要同步的数据' });
        }

        const existing = await db.get('SELECT id FROM sync_data WHERE user_id = ?', [userId]);
        if (existing) {
            await db.run('UPDATE sync_data SET data_json = ?, updated_at = ? WHERE user_id = ?', [dataJson, Date.now(), userId]);
        } else {
            await db.run('INSERT INTO sync_data (user_id, data_json, updated_at) VALUES (?, ?, ?)', [userId, dataJson, Date.now()]);
        }

        res.json({ success: true, message: '数据同步成功' });
    } catch (error) {
        console.error('Sync upload error:', error);
        res.status(500).json({ error: '数据同步失败' });
    }
};

exports.downloadSyncData = async (req, res) => {
    try {
        const { userId } = req.user;

        const record = await db.get('SELECT data_json, updated_at FROM sync_data WHERE user_id = ?', [userId]);
        if (!record) {
            return res.json({ dataJson: null, message: '云端暂无备份数据' });
        }

        res.json({ success: true, dataJson: record.data_json, updatedAt: record.updated_at });
    } catch (error) {
        console.error('Sync download error:', error);
        res.status(500).json({ error: '数据拉取失败' });
    }
};
