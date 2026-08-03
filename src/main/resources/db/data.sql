-- Initial data for development
-- This script runs after init.sql

-- ============================================
-- USERS
-- ============================================

-- ADMIN: Ivan Ivanov
-- Password: 1234 (hash: $2a$12$9ac0kvKxGDUIvlS1VRLQledAcU3OYn1jeHdOxOoe5OR/Ne8YyvUDe)
INSERT INTO users (id, first_name, last_name, email, password_hash, role, phone)
VALUES (1, 'Ivan', 'Ivanov', 'i@gmail.com', '$2a$12$9ac0kvKxGDUIvlS1VRLQledAcU3OYn1jeHdOxOoe5OR/Ne8YyvUDe', 'ADMIN', '+7-921-111-11-11');

-- TEACHER: Pyotr Petrov
-- Password: 1234 (hash: $2a$12$9ac0kvKxGDUIvlS1VRLQledAcU3OYn1jeHdOxOoe5OR/Ne8YyvUDe)
INSERT INTO users (id, first_name, last_name, email, password_hash, role, phone)
VALUES (2, 'Pyotr', 'Petrov', 'p@gmail.com', '$2a$12$9ac0kvKxGDUIvlS1VRLQledAcU3OYn1jeHdOxOoe5OR/Ne8YyvUDe', 'TEACHER', '+7-921-222-22-22');

-- DISPATCHER: Sergey Sergeev
-- Password: 1234 (hash: $2a$12$9ac0kvKxGDUIvlS1VRLQledAcU3OYn1jeHdOxOoe5OR/Ne8YyvUDe)
INSERT INTO users (id, first_name, last_name, email, password_hash, role, phone)
VALUES (3, 'Sergey', 'Sergeev', 's@gmail.com', '$2a$12$9ac0kvKxGDUIvlS1VRLQledAcU3OYn1jeHdOxOoe5OR/Ne8YyvUDe', 'DISPATCHER', '+7-921-333-33-33');

-- Обновляем sequence для users после ручной вставки
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users), true);