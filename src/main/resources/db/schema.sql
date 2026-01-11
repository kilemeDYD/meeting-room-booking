-- ===============================
-- Meeting Room Booking Schema
-- MySQL 8 / InnoDB
-- ===============================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ===============================
-- 1. 会议室表
-- ===============================
CREATE TABLE IF NOT EXISTS room (
                                    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    name         VARCHAR(64)  NOT NULL,
                                    location     VARCHAR(128),
                                    capacity     INT          NOT NULL DEFAULT 0,
                                    equipment    VARCHAR(256),
                                    enabled      TINYINT(1)   NOT NULL DEFAULT 1,
                                    created_at   DATETIME     NOT NULL,
                                    updated_at   DATETIME     NOT NULL,
                                    UNIQUE KEY uk_room_name (name),
                                    INDEX idx_room_enabled (enabled)
) ENGINE=InnoDB;

-- ===============================
-- 2. 用户表（Session 鉴权用）
-- ===============================
CREATE TABLE IF NOT EXISTS app_user (
                                        id             BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        username       VARCHAR(64)  NOT NULL,
                                        password_hash  VARCHAR(128) NOT NULL,
                                        role           VARCHAR(32)  NOT NULL,   -- USER / ADMIN
                                        enabled        TINYINT(1)   NOT NULL DEFAULT 1,
                                        created_at     DATETIME     NOT NULL,
                                        updated_at     DATETIME     NOT NULL,
                                        UNIQUE KEY uk_user_username (username),
                                        INDEX idx_user_role (role)
) ENGINE=InnoDB;

-- ===============================
-- 3. 预约主表
-- ===============================
CREATE TABLE IF NOT EXISTS reservation (
                                           id           BIGINT PRIMARY KEY AUTO_INCREMENT,
                                           room_id      BIGINT       NOT NULL,
                                           user_id      BIGINT       NOT NULL,
                                           title        VARCHAR(128) NOT NULL,
                                           description  VARCHAR(512),
                                           start_time   DATETIME     NOT NULL,
                                           end_time     DATETIME     NOT NULL,
                                           status       VARCHAR(32)  NOT NULL,   -- PENDING / APPROVED / REJECTED / CANCELED
                                           version      BIGINT       NOT NULL DEFAULT 0,
                                           created_at   DATETIME     NOT NULL,
                                           updated_at   DATETIME     NOT NULL,

                                           CONSTRAINT fk_res_room FOREIGN KEY (room_id) REFERENCES room(id),
                                           CONSTRAINT fk_res_user FOREIGN KEY (user_id) REFERENCES app_user(id),

                                           INDEX idx_res_room_start (room_id, start_time),
                                           INDEX idx_res_user_start (user_id, start_time),
                                           INDEX idx_res_status (status)
) ENGINE=InnoDB;

-- ===============================
-- 4. 时间片占用表（强一致核心）
-- ===============================
CREATE TABLE IF NOT EXISTS reservation_slot (
                                                id              BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                reservation_id  BIGINT   NOT NULL,
                                                room_id         BIGINT   NOT NULL,
                                                slot_start      DATETIME NOT NULL,
                                                created_at      DATETIME NOT NULL,

                                                CONSTRAINT fk_slot_res FOREIGN KEY (reservation_id)
                                                    REFERENCES reservation(id) ON DELETE CASCADE,
                                                CONSTRAINT fk_slot_room FOREIGN KEY (room_id)
                                                    REFERENCES room(id),

                                                UNIQUE KEY uk_room_slot (room_id, slot_start),
                                                INDEX idx_slot_res (reservation_id),
                                                INDEX idx_slot_room_time (room_id, slot_start)
) ENGINE=InnoDB;

-- ===============================
-- 5. 审计日志表（加分）
-- ===============================
CREATE TABLE IF NOT EXISTS audit_log (
                                         id           BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         entity_type  VARCHAR(32) NOT NULL,   -- RESERVATION / ROOM / USER / CONFIG
                                         entity_id    BIGINT      NOT NULL,
                                         action       VARCHAR(32) NOT NULL,   -- CREATE / UPDATE / APPROVE / REJECT / CANCEL
                                         operator_id  BIGINT      NOT NULL,
                                         from_status  VARCHAR(32),
                                         to_status    VARCHAR(32),
                                         detail       VARCHAR(512),
                                         request_id   VARCHAR(64),
                                         created_at   DATETIME    NOT NULL,

                                         CONSTRAINT fk_audit_operator FOREIGN KEY (operator_id)
                                             REFERENCES app_user(id),

                                         INDEX idx_audit_entity (entity_type, entity_id),
                                         INDEX idx_audit_created (created_at),
                                         INDEX idx_audit_operator (operator_id)
) ENGINE=InnoDB;

-- ===============================
-- 6. 系统配置表
-- ===============================
CREATE TABLE IF NOT EXISTS system_config (
                                             cfg_key     VARCHAR(64) PRIMARY KEY,
                                             cfg_value   VARCHAR(256) NOT NULL,
                                             updated_at  DATETIME     NOT NULL
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;
