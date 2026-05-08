-- 为现有客户端添加 mobile 授权类型
-- 如果你的 clientId 不是 mobile-app，而是其他值（如 bf755b6a2a608b8dc8372d64f3ce970d）

UPDATE sys_client 
SET grant_type = CONCAT(grant_type, ',mobile')
WHERE client_id = 'bf755b6a2a608b8dc8372d64f3ce970d'
  AND grant_type NOT LIKE '%mobile%';

-- 验证修改结果
SELECT client_id, client_key, grant_type, device_type, status 
FROM sys_client 
WHERE client_id = 'bf755b6a2a608b8dc8372d64f3ce970d';

-- 说明：
-- 1. 执行此 SQL 前，请确保数据库中已存在该 clientId 的记录
-- 2. 如果不存在，需要先插入记录或更换为已存在的 clientId
-- 3. grant_type 应该是逗号分隔的字符串，如："password,sms,mobile"
