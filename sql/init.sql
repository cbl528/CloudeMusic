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
