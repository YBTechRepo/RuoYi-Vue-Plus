-- 移动端客户端配置 SQL
-- 用于手机号登录功能

INSERT INTO sys_client (
    client_id, 
    client_key, 
    client_secret, 
    grant_type, 
    device_type, 
    status, 
    timeout, 
    active_timeout,
    create_time, 
    update_time
) VALUES (
    'mobile-app',                    -- 客户端 ID（前端传参使用）
    'mobile-app',                    -- 客户端标识
    '$2a$10$abc123def456ghi789jkl',   -- 客户端密钥（需加密，此处仅为示例）
    'mobile,sms',                    -- 支持的授权类型：mobile=手机号密码登录，sms=短信验证码登录
    'APP',                           -- 设备类型
    '0',                             -- 状态：0=正常，1=停用
    86400000,                        -- Token 有效期：24 小时（毫秒）
    7200000,                         -- Token 活跃超时时间：2 小时（毫秒）
    NOW(),
    NOW()
);

-- 说明：
-- 1. client_secret 需要使用 BCrypt 加密，实际部署时请替换为真实加密值
-- 2. grant_type 包含 mobile 表示支持手机号密码登录
-- 3. 前端登录请求示例：
--    POST /api/auth/login
--    {
--      "clientId": "mobile-app",
--      "grantType": "mobile",
--      "phone": "13800138000",
--      "password": "123456"
--    }
