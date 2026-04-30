const db = require('../models/db');

function generateOrderId() {
  return 'ORD' + Date.now() + Math.floor(Math.random() * 1000);
}

exports.createVipOrder = async (req, res) => {
  const { userId } = req.user;
  const { planId, payMethod } = req.body;

  let amount = 0;
  let durationMs = 0;

  console.log('[VIP Order] planId:', planId, 'payMethod:', payMethod, 'userId:', userId);

  if (planId === 'lifetime') {
    amount = 18.00;
    durationMs = 100 * 365 * 24 * 3600 * 1000;
  } else {
    return res.status(400).json({ error: `无效的套餐ID: ${planId}` });
  }

  const orderId = generateOrderId();

  try {
    await db.run('INSERT INTO orders (order_id, user_id, plan_id, amount, status, pay_method, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)', [
      orderId, userId, planId, amount, 'pending', payMethod, Date.now()
    ]);

    let vipExpireAt = null;

    // DEV_MODE: auto-activate VIP without real payment
    if (process.env.DEV_MODE === 'true') {
      await db.run('UPDATE orders SET status = "paid", paid_at = ? WHERE order_id = ?', [Date.now(), orderId]);

      const user = await db.get('SELECT vip_expire_at FROM users WHERE user_id = ?', [userId]);
      let newExpireAt = Date.now() + durationMs;
      if (user && user.vip_expire_at > Date.now()) {
        newExpireAt = user.vip_expire_at + durationMs;
      }

      await db.run('UPDATE users SET is_vip = 1, vip_expire_at = ? WHERE user_id = ?', [newExpireAt, userId]);
      vipExpireAt = newExpireAt;
    }

    const mockPayUrl = `alipays://platformapi/startapp?appId=20000067&url=http://mock-pay-success.com`;

    res.json({
      orderId,
      payUrl: process.env.DEV_MODE === 'true' ? null : mockPayUrl,
      vipExpireAt
    });
  } catch (error) {
    console.error('Create order error:', error);
    res.status(500).json({ error: '订单创建失败' });
  }
};

// 支付回调处理逻辑
const handlePaymentSuccess = async (orderId) => {
  const order = await db.get('SELECT * FROM orders WHERE order_id = ? AND status = "pending"', [orderId]);
  if (!order) return false;

  await db.run('UPDATE orders SET status = "paid", paid_at = ? WHERE order_id = ?', [Date.now(), orderId]);

  let durationMs = 0;
  if (order.plan_id === 'lifetime') durationMs = 100 * 365 * 24 * 3600 * 1000;

  const user = await db.get('SELECT vip_expire_at FROM users WHERE user_id = ?', [order.user_id]);
  let newExpireAt = Date.now() + durationMs;
  if (user && user.vip_expire_at > Date.now()) {
    newExpireAt = user.vip_expire_at + durationMs;
  }

  await db.run('UPDATE users SET is_vip = 1, vip_expire_at = ? WHERE user_id = ?', [newExpireAt, order.user_id]);
  return true;
};

// 支付宝异步回调
exports.alipayCallback = async (req, res) => {
  const { out_trade_no, trade_status } = req.body;

  if (trade_status === 'TRADE_SUCCESS') {
    await handlePaymentSuccess(out_trade_no);
    res.send('success');
  } else {
    res.send('fail');
  }
};

// 微信异步回调
exports.wechatCallback = async (req, res) => {
  res.send('<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>');
};
