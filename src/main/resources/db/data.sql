-- ===============================
-- Initial Data for Meeting Room Booking
-- ===============================

-- ---------- 管理员账号 ----------
-- 说明：
-- 这里使用 {noop} 只是为了开发期能直接登录
-- 后面接 Spring Security / BCrypt 再统一替换
INSERT INTO app_user (
    username,
    password_hash,
    role,
    enabled,
    created_at,
    updated_at
) VALUES (
             'admin',
             '{noop}admin123',
             'ADMIN',
             1,
             NOW(),
             NOW()
         )
ON DUPLICATE KEY UPDATE
    updated_at = VALUES(updated_at);

-- ---------- 普通用户 ----------
INSERT INTO app_user (
    username,
    password_hash,
    role,
    enabled,
    created_at,
    updated_at
) VALUES (
             'user1',
             '{noop}user123',
             'USER',
             1,
             NOW(),
             NOW()
         )
ON DUPLICATE KEY UPDATE
    updated_at = VALUES(updated_at);

-- ---------- 会议室 ----------
INSERT INTO room (
    name,
    location,
    capacity,
    equipment,
    enabled,
    created_at,
    updated_at
) VALUES
      (
          '第一会议室',
          '1号楼 3层',
          10,
          '投影仪,白板',
          1,
          NOW(),
          NOW()
      ),
      (
          '第二会议室',
          '1号楼 5层',
          20,
          '投影仪,视频会议',
          1,
          NOW(),
          NOW()
      ),
      (
          '大会议室',
          '2号楼 1层',
          50,
          '投影仪,视频会议,音响',
          1,
          NOW(),
          NOW()
      )
ON DUPLICATE KEY UPDATE
    updated_at = VALUES(updated_at);

-- ---------- 系统配置 ----------
INSERT INTO system_config (
    cfg_key,
    cfg_value,
    updated_at
) VALUES
      ('slot_minutes', '15', NOW()),
      ('bookable_days_ahead', '30', NOW()),
      ('max_duration_minutes', '240', NOW())
ON DUPLICATE KEY UPDATE
                     cfg_value = VALUES(cfg_value),
                     updated_at = VALUES(updated_at);
