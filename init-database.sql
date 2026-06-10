-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS xiangmuguanli DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE xiangmuguanli;

-- 创建用户表（如果不存在）
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 插入管理员用户（如果不存在）
INSERT IGNORE INTO users (username, password, name, email, status, is_admin) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPkU2Cslq', '管理员', 'admin@example.com', 'ACTIVE', true),
('user', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPkU2Cslq', '普通用户', 'user@example.com', 'ACTIVE', false);

-- 密码是 "123456" 的 BCrypt 加密值

-- 显示创建的用户
SELECT 'Users created successfully:' as message;
SELECT id, username, name, email, status, is_admin FROM users;