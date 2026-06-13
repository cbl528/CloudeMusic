-- 创建数据库
CREATE DATABASE IF NOT EXISTS music
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE music;

-- 用户表
CREATE TABLE IF NOT EXISTS `t_user`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`        VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`        VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname`        VARCHAR(50)  NULL DEFAULT NULL COMMENT '昵称',
    `avatar`          VARCHAR(512)  NULL DEFAULT NULL COMMENT '头像URL',
    `email`           VARCHAR(100) NULL DEFAULT NULL COMMENT '邮箱',
    `phone`           VARCHAR(20)  NULL DEFAULT NULL COMMENT '手机号',
    `gender`          TINYINT      NULL DEFAULT 0 COMMENT '性别：0未知 1男 2女',
    `signature`       VARCHAR(255) NULL DEFAULT NULL COMMENT '个性签名',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
    `last_login_time` DATETIME     NULL DEFAULT NULL COMMENT '最后登录时间',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`) USING BTREE,
    UNIQUE KEY `uk_email` (`email`) USING BTREE,
    INDEX `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';

-- 收藏表
CREATE TABLE IF NOT EXISTS `t_favorite`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `song_id`     BIGINT       NOT NULL COMMENT '网易云歌曲ID',
    `song_name`   VARCHAR(255) NOT NULL DEFAULT '' COMMENT '歌曲名（缓存）',
    `artist`      VARCHAR(255) NOT NULL DEFAULT '' COMMENT '歌手名（缓存）',
    `cover`       VARCHAR(500) NOT NULL DEFAULT '' COMMENT '封面URL（缓存）',
    `duration`    BIGINT       NOT NULL DEFAULT 0 COMMENT '时长毫秒（缓存）',
    `type`        VARCHAR(20)  NOT NULL DEFAULT 'song' COMMENT '类型：song/playlist/album',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_song` (`user_id`, `song_id`) COMMENT '同一用户不重复收藏同一首歌',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_user_type` (`user_id`, `type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='收藏表';

-- 播放历史表
CREATE TABLE IF NOT EXISTS `t_history`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `song_id`     BIGINT       NOT NULL COMMENT '网易云歌曲ID',
    `song_name`   VARCHAR(255) NOT NULL DEFAULT '' COMMENT '歌曲名（缓存）',
    `artist`      VARCHAR(255) NOT NULL DEFAULT '' COMMENT '歌手名（缓存）',
    `cover`       VARCHAR(500) NOT NULL DEFAULT '' COMMENT '封面URL（缓存）',
    `duration`    BIGINT       NOT NULL DEFAULT 0 COMMENT '时长毫秒（缓存）',
    `played_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '播放时间',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_user_played` (`user_id`, `played_at`) COMMENT '按用户和时间排序查询'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='播放历史表';
