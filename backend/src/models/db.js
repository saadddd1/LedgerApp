const initSqlJs = require('sql.js');
const path = require('path');
const fs = require('fs');

const dbPath = path.resolve(__dirname, '../../ledger.db');

let db = null;
let sqlModule = null;

function saveToDisk() {
    if (!db) return;
    const data = db.export();
    const buffer = Buffer.from(data);
    fs.writeFileSync(dbPath, buffer);
}

async function initDb() {
    sqlModule = await initSqlJs();

    // Try loading existing database, otherwise create empty one
    if (fs.existsSync(dbPath)) {
        const fileBuffer = fs.readFileSync(dbPath);
        db = new sqlModule.Database(fileBuffer);
    } else {
        db = new sqlModule.Database();
    }

    db.run('PRAGMA journal_mode=WAL');

    db.run(`
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id TEXT UNIQUE,
            phone TEXT UNIQUE,
            email TEXT UNIQUE,
            wechat_openid TEXT UNIQUE,
            wechat_unionid TEXT,
            is_vip INTEGER DEFAULT 0,
            vip_expire_at INTEGER,
            created_at INTEGER
        )
    `);
    db.run(`
        CREATE TABLE IF NOT EXISTS sync_data (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id TEXT UNIQUE,
            data_json TEXT,
            updated_at INTEGER
        )
    `);
    db.run(`
        CREATE TABLE IF NOT EXISTS orders (
            order_id TEXT PRIMARY KEY,
            user_id TEXT,
            plan_id TEXT,
            amount REAL,
            status TEXT,
            pay_method TEXT,
            created_at INTEGER,
            paid_at INTEGER
        )
    `);

    // Migration: add email column to existing databases
    try { db.run('ALTER TABLE users ADD COLUMN email TEXT UNIQUE'); } catch (_) {}

    saveToDisk();
}

// Wrappers compatible with existing controller code
const run = async (sql, params = []) => {
    db.run(sql, params);
    saveToDisk();
    return { changes: db.getRowsModified() };
};

const get = async (sql, params = []) => {
    const stmt = db.prepare(sql);
    stmt.bind(params);
    const row = stmt.step() ? stmt.getAsObject() : null;
    stmt.free();
    return row;
};

const all = async (sql, params = []) => {
    const stmt = db.prepare(sql);
    stmt.bind(params);
    const rows = [];
    while (stmt.step()) {
        rows.push(stmt.getAsObject());
    }
    stmt.free();
    return rows;
};

module.exports = {
    initDb,
    run,
    get,
    all
};
