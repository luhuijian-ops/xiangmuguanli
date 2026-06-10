-- 检查数据库是否存在
SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'xiangmuguanli';

-- 如果存在，显示用户表
USE xiangmuguanli;
SHOW TABLES LIKE 'users';

-- 如果用户表存在，显示用户数据
SELECT COUNT(*) as user_count FROM users;