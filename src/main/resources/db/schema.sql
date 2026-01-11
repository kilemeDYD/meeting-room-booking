-- 1) 用户表（先简单：后面再做注册/加密）
CREATE TABLE IF NOT EXISTS user_account (
                                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                            username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('USER','ADMIN') NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;

-- 2) 会议室表
CREATE TABLE IF NOT EXISTS meeting_room (
                                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                            name VARCHAR(128) NOT NULL,
    location VARCHAR(128) NOT NULL,
    capacity INT NOT NULL,
    equipment VARCHAR(255) NULL,
    status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_room_name (name)
    ) ENGINE=InnoDB;

-- 3) 预约主表
CREATE TABLE IF NOT EXISTS reservation (
                                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                           user_id BIGINT NOT NULL,
                                           room_id BIGINT NOT NULL,
                                           date DATE NOT NULL,
                                           start_time TIME NOT NULL,
                                           end_time TIME NOT NULL,
                                           purpose VARCHAR(255) NOT NULL,
    status ENUM('PENDING','APPROVED','REJECTED','CANCELED') NOT NULL DEFAULT 'APPROVED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_res_room_date (room_id, date),
    INDEX idx_res_user_date (user_id, date),
    CONSTRAINT fk_res_user FOREIGN KEY (user_id) REFERENCES user_account(id),
    CONSTRAINT fk_res_room FOREIGN KEY (room_id) REFERENCES meeting_room(id)
    ) ENGINE=InnoDB;

-- 4) 时间片占用表（B方案核心：唯一约束）
CREATE TABLE IF NOT EXISTS reservation_slot (
                                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                reservation_id BIGINT NOT NULL,
                                                room_id BIGINT NOT NULL,
                                                date DATE NOT NULL,
                                                slot_index INT NOT NULL,
                                                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                UNIQUE KEY uk_room_date_slot (room_id, date, slot_index),
    INDEX idx_slot_res (reservation_id),
    INDEX idx_slot_room_date (room_id, date),
    CONSTRAINT fk_slot_res FOREIGN KEY (reservation_id) REFERENCES reservation(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

-- 5) 审计表（加分）
CREATE TABLE IF NOT EXISTS audit_log (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         reservation_id BIGINT NOT NULL,
                                         action VARCHAR(64) NOT NULL,
    operator_id BIGINT NOT NULL,
    detail VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_res (reservation_id),
    CONSTRAINT fk_audit_res FOREIGN KEY (reservation_id) REFERENCES reservation(id)
    ) ENGINE=InnoDB;
