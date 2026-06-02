/*
Navicat Premium Dump SQL

Source Server         : xiaoyu
Source Server Type    : MySQL
Source Server Version : 80039 (8.0.39)
Source Host           : localhost:3306
Source Schema         : xiaoyu

Target Server Type    : MySQL
Target Server Version : 80039 (8.0.39)
File Encoding         : 65001

Date: 10/10/2025 16:28:25
*/

SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for blacklists
-- ----------------------------
DROP TABLE IF EXISTS `blacklists`;

CREATE TABLE `blacklists` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `owner_id` bigint NOT NULL COMMENT '拉黑者 UID',
    `target_id` bigint NOT NULL COMMENT '被拉黑者 UID',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '拉黑时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_ot` (
        `owner_id` ASC,
        `target_id` ASC
    ) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '黑名单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for campuses
-- ----------------------------
DROP TABLE IF EXISTS `campuses`;

CREATE TABLE `campuses` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '校区 ID',
    `name` enum(
        'NANHU',
        'YUJIATOU',
        'MAFANGSHAN'
    ) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '校区名称',
    `province` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '省',
    `city` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '市',
    `address` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '详细地址',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `name` (`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '校区字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;

CREATE TABLE `comments` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论 ID',
    `user_id` bigint NULL DEFAULT NULL COMMENT '评论者 UID',
    `item_id` bigint NULL DEFAULT NULL COMMENT '业务对象 ID',
    `item_type` enum('POST', 'TASK') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务类型',
    `parent_id` bigint NULL DEFAULT 0 COMMENT '父评论 ID，0 为一级',
    `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评论内容',
    `at_users` json NULL COMMENT '@用户 JSON 数组',
    `status` enum(
        'VISIBLE',
        'HIDDEN',
        'AUDITING',
        'REJECTED'
    ) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'VISIBLE' COMMENT '可见状态',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    PRIMARY KEY (`id`) USING BTREE,
    FULLTEXT INDEX `content` (`content`) COMMENT '内容全文索引'
) ENGINE = InnoDB AUTO_INCREMENT = 62 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for favorites
-- ----------------------------
DROP TABLE IF EXISTS `favorites`;

CREATE TABLE `favorites` (
    `user_id` bigint NOT NULL COMMENT '收藏者 UID',
    `item_id` bigint NOT NULL COMMENT '业务对象 ID',
    `item_type` enum('POST', 'TASK') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏 ID',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for files
-- ----------------------------
DROP TABLE IF EXISTS `files`;

CREATE TABLE `files` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件主键',
    `user_id` bigint NOT NULL COMMENT '上传者 UID',
    `biz_type` enum(
        'AVATAR',
        'BG',
        'POST',
        'TASK',
        'COMMENT'
    ) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务类型',
    `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'OSS 原图地址',
    `thumb_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'CDN 缩略图地址',
    `size` int NULL DEFAULT NULL COMMENT '文件大小（字节）',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for friend_messages
-- ----------------------------
DROP TABLE IF EXISTS `friend_messages`;

CREATE TABLE `friend_messages` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '私信 ID',
    `from_id` bigint NULL DEFAULT NULL COMMENT '发送者 UID',
    `to_id` bigint NULL DEFAULT NULL COMMENT '接收者 UID',
    `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息内容',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息类型: TEXT/POST/TASK',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 380 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '好友私信表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for friendships
-- ----------------------------
DROP TABLE IF EXISTS `friendships`;

CREATE TABLE `friendships` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '好友关系 ID',
    `user_id` bigint NULL DEFAULT NULL COMMENT '用户 UID',
    `friend_id` bigint NULL DEFAULT NULL COMMENT '好友 UID',
    `requester_id` bigint NOT NULL COMMENT '发起申请者 UID',
    `status` enum(
        'PENDING',
        'ACCEPTED',
        'REFUSED',
        'DELETED'
    ) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT '好友状态',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_uf` (
        `user_id` ASC,
        `friend_id` ASC
    ) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '好友关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for homepages
-- ----------------------------
DROP TABLE IF EXISTS `homepages`;

CREATE TABLE `homepages` (
    `user_id` bigint NOT NULL COMMENT '用户 ID',
    `visit_cnt` int NULL DEFAULT 0 COMMENT '被访问次数',
    `last_visit` datetime NULL DEFAULT NULL COMMENT '最近一次访问时间',
    PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '个人主页统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of homepages
-- ----------------------------

-- ----------------------------
-- Table structure for likes
-- ----------------------------
DROP TABLE IF EXISTS `likes`;

CREATE TABLE `likes` (
    `user_id` bigint NOT NULL COMMENT '点赞者 UID',
    `item_id` bigint NOT NULL COMMENT '业务对象 ID',
    `item_type` enum('POST', 'TASK', 'COMMENT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (
        `user_id`,
        `item_id`,
        `item_type`
    ) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;

CREATE TABLE `notifications` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知 ID',
    `user_id` bigint NOT NULL COMMENT '接收者 UID',
    `type` enum(
        'INTERACTION',
        'SYSTEM',
        'CHAT'
    ) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知类型',
    `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
    `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '正文',
    `ref_id` bigint NULL DEFAULT NULL COMMENT '关联业务对象 ID',
    `ref_type` enum(
        'POST',
        'TASK',
        'COMMENT',
        'CHAT',
        'SYSTEM'
    ) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `status` enum('UNREAD', 'READ') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'UNREAD' COMMENT '阅读状态',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '通知时间',
    `from_user_id` bigint NULL DEFAULT NULL COMMENT '发送者id状态(系统通知为空)',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 113 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for post_files
-- ----------------------------
DROP TABLE IF EXISTS `post_files`;

CREATE TABLE `post_files` (
    `post_id` bigint NOT NULL COMMENT '动态 ID',
    `file_id` bigint NOT NULL COMMENT '文件 ID',
    `sort` tinyint NULL DEFAULT 0 COMMENT '顺序号',
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '动态-文件表主键',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '动态-文件关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for post_stats
-- ----------------------------
DROP TABLE IF EXISTS `post_stats`;

CREATE TABLE `post_stats` (
    `post_id` bigint NOT NULL COMMENT '动态 ID',
    `view_cnt` int NULL DEFAULT 0 COMMENT '浏览量',
    `like_cnt` int NULL DEFAULT 0 COMMENT '点赞数',
    `comment_cnt` int NULL DEFAULT 0 COMMENT '评论数',
    `share_cnt` int NULL DEFAULT 0 COMMENT '转发数',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `fav_cnt` int NULL DEFAULT 0 COMMENT '收藏数',
    PRIMARY KEY (`post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '动态统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for posts
-- ----------------------------
DROP TABLE IF EXISTS `posts`;

CREATE TABLE `posts` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '动态 ID',
    `user_id` bigint NOT NULL COMMENT '发布者 UID',
    `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文本内容',
    `visibility` enum('PUBLIC', 'FRIEND', 'CAMPUS') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PUBLIC' COMMENT '可见范围',
    `poi_lat` decimal(10, 6) NULL DEFAULT NULL COMMENT '纬度',
    `poi_lng` decimal(10, 6) NULL DEFAULT NULL COMMENT '经度',
    `poi_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'POI 名称',
    `is_top` tinyint NULL DEFAULT 0 COMMENT '是否置顶：0否 1是',
    `status` enum(
        'DRAFT',
        'PUBLISHED',
        'HIDDEN',
        'AUDITING',
        'REJECTED'
    ) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PUBLISHED' COMMENT '状态',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `campus_id` bigint NULL DEFAULT NULL COMMENT '校区ID',
    `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动态标题',
    PRIMARY KEY (`id`) USING BTREE,
    FULLTEXT INDEX `content` (`content`) COMMENT '内容全文索引'
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '朋友圈动态表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for reports
-- ----------------------------
DROP TABLE IF EXISTS `reports`;

CREATE TABLE `reports` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '举报 ID',
    `user_id` bigint NULL DEFAULT NULL COMMENT '举报人 UID',
    `item_id` bigint NULL DEFAULT NULL COMMENT '被举报对象 ID',
    `item_type` enum(
        'POST',
        'TASK',
        'COMMENT',
        'USER'
    ) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '被举报类型',
    `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '举报理由',
    `status` enum(
        'PENDING',
        'ACCEPTED',
        'REFUSED'
    ) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT '处理状态',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '举报记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;

CREATE TABLE `roles` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '角色 ID',
    `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色名称：USER、ADMIN 等',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `name` (`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for search_blacklist
-- ----------------------------
DROP TABLE IF EXISTS `search_blacklist`;

CREATE TABLE `search_blacklist` (
    `keyword` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '屏蔽关键词',
    PRIMARY KEY (`keyword`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '搜索黑名单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for search_hotwords
-- ----------------------------
DROP TABLE IF EXISTS `search_hotwords`;

CREATE TABLE `search_hotwords` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '热词 ID',
    `keyword` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关键词',
    `search_cnt` int NULL DEFAULT 0 COMMENT '搜索次数',
    `weight` int NULL DEFAULT 0 COMMENT '后台手动权重',
    `is_hot` tinyint NULL DEFAULT 0 COMMENT '是否上榜：0否 1是',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `keyword` (`keyword` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '热搜词库表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sensitive_words
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_words`;

CREATE TABLE `sensitive_words` (
    `word` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '敏感词',
    `level` tinyint NULL DEFAULT 1 COMMENT '危险等级：1低 2中 3高',
    PRIMARY KEY (`word`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '敏感词库表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for shares
-- ----------------------------
DROP TABLE IF EXISTS `shares`;

CREATE TABLE `shares` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '转发 ID',
    `user_id` bigint NULL DEFAULT NULL COMMENT '转发者 UID',
    `item_id` bigint NULL DEFAULT NULL COMMENT '业务对象 ID',
    `item_type` enum('POST', 'TASK', 'COMMENT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务类型',
    `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '转发附言',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '转发时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 74 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '转发记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for system_notices
-- ----------------------------
DROP TABLE IF EXISTS `system_notices`;

CREATE TABLE `system_notices` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告 ID',
    `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '公告标题',
    `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '公告正文',
    `start_time` datetime NULL DEFAULT NULL COMMENT '生效开始时间',
    `end_time` datetime NULL DEFAULT NULL COMMENT '生效结束时间',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tag_items
-- ----------------------------
DROP TABLE IF EXISTS `tag_items`;

CREATE TABLE `tag_items` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tag_id` int NULL DEFAULT NULL COMMENT '标签 ID',
    `item_id` bigint NULL DEFAULT NULL COMMENT '业务对象 ID',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签与业务对象关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tags
-- ----------------------------
DROP TABLE IF EXISTS `tags`;

CREATE TABLE `tags` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '标签 ID',
    `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签名称',
    `weight` int NULL DEFAULT 0 COMMENT '后台排序权重',
    `is_hot` tinyint NULL DEFAULT 0 COMMENT '是否热门：0否 1是',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `name` (`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for task_files
-- ----------------------------
DROP TABLE IF EXISTS `task_files`;

CREATE TABLE `task_files` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `task_id` bigint NOT NULL COMMENT '任务 ID',
    `file_id` bigint NOT NULL COMMENT '文件 ID',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_task_file` (`task_id` ASC, `file_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务-文件关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for task_orders
-- ----------------------------
DROP TABLE IF EXISTS `task_orders`;

CREATE TABLE `task_orders` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单主键',
    `task_id` bigint NOT NULL COMMENT '任务 ID',
    `receiver_id` bigint NOT NULL COMMENT '接单者 UID',
    `status` enum(
        'WAIT_ACCEPT',
        'ACCEPTED',
        'REFUSED',
        'CANCELLED',
        'DELIVER',
        'FINISH'
    ) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'WAIT_ACCEPT' COMMENT '订单状态',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_task_recv` (
        `task_id` ASC,
        `receiver_id` ASC
    ) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for task_reviews
-- ----------------------------
DROP TABLE IF EXISTS `task_reviews`;

CREATE TABLE `task_reviews` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价 ID',
    `task_id` bigint NULL DEFAULT NULL COMMENT '任务 ID',
    `reviewer_id` bigint NULL DEFAULT NULL COMMENT '评价者 UID',
    `reviewee_id` bigint NULL DEFAULT NULL COMMENT '被评价者 UID',
    `role_type` enum('PUBLISHER', 'RECEIVER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评价方角色',
    `score` tinyint NULL DEFAULT NULL COMMENT '1~5 星',
    `tags` json NULL COMMENT '评价标签 JSON 数组',
    `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文字评价',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    PRIMARY KEY (`id`) USING BTREE,
    CONSTRAINT `task_reviews_chk_1` CHECK (`score` between 1 and 5)
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务双向评价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for task_stats
-- ----------------------------
DROP TABLE IF EXISTS `task_stats`;

CREATE TABLE `task_stats` (
    `task_id` bigint NOT NULL COMMENT '任务 ID',
    `view_cnt` int NULL DEFAULT 0 COMMENT '浏览量',
    `order_cnt` int NULL DEFAULT 0 COMMENT '接单数',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务统计id',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tasks
-- ----------------------------
DROP TABLE IF EXISTS `tasks`;

CREATE TABLE `tasks` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务 ID',
    `publisher_id` bigint NOT NULL COMMENT '发布者 UID',
    `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务标题',
    `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '任务详情',
    `reward` decimal(10, 2) NULL DEFAULT NULL COMMENT '悬赏金额',
    `status` enum(
        'AUDITING',
        'RECRUIT',
        'RUNNING',
        'FINISH',
        'CLOSED',
        'ARBITRATED'
    ) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'AUDITING' COMMENT '生命周期状态',
    `visibility` enum('PUBLIC', 'FRIEND', 'CAMPUS') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PUBLIC' COMMENT '可见范围',
    `expire_at` datetime NULL DEFAULT NULL COMMENT '截止报名时间',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    FULLTEXT INDEX `title` (`title`, `content`) COMMENT '标题+详情全文索引'
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for topic_posts
-- ----------------------------
DROP TABLE IF EXISTS `topic_posts`;

CREATE TABLE `topic_posts` (
    `topic_id` bigint NOT NULL COMMENT '话题 ID',
    `post_id` bigint NOT NULL COMMENT '动态 ID',
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '帖子_话题关联id',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '话题-动态关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for topics
-- ----------------------------
DROP TABLE IF EXISTS `topics`;

CREATE TABLE `topics` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '话题 ID',
    `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '话题名称（带 #）',
    `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '话题描述',
    `post_cnt` int NULL DEFAULT 0 COMMENT '关联动态数',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `name` (`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '话题字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_auths
-- ----------------------------
DROP TABLE IF EXISTS `user_auths`;

CREATE TABLE `user_auths` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户 ID',
    `identity_type` enum(
        'QQ',
        'WECHAT',
        'MOBILE',
        'PASSWORD'
    ) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录方式',
    `identifier` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '唯一标识：openid/手机号',
    `credential` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码或 access_token',
    `verified_at` datetime NULL DEFAULT NULL COMMENT '验证通过时间',
    `account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_type_id` (
        `identity_type` ASC,
        `identifier` ASC
    ) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户授权表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_roles
-- ----------------------------
DROP TABLE IF EXISTS `user_roles`;

CREATE TABLE `user_roles` (
    `user_id` bigint NOT NULL COMMENT '用户 ID',
    `role_id` int NOT NULL COMMENT '角色 ID',
    PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户主键',
    `nickname` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
    `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像 OSS 地址',
    `birthday` date NULL DEFAULT NULL COMMENT '生日',
    `gender` tinyint NULL DEFAULT 0 COMMENT '性别：0未知 1男 2女',
    `campus_id` bigint NULL DEFAULT NULL COMMENT '所属校区 ID',
    `qq_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'QQ 授权 openid',
    `mobile` char(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
    `real_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '实名姓名',
    `id_card_no` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号',
    `is_real_name` tinyint NULL DEFAULT 0 COMMENT '是否已实名：0否 1是',
    `privacy_mobile` tinyint NULL DEFAULT 0 COMMENT '手机号可见范围：0公开 1好友 2仅自己',
    `privacy_birthday` tinyint NULL DEFAULT 0 COMMENT '生日可见范围',
    `privacy_fans` tinyint NULL DEFAULT 0 COMMENT '粉丝列表可见范围',
    `status` tinyint NULL DEFAULT 0 COMMENT '账号状态：0正常 1封号',
    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `qq_openid` (`qq_openid` ASC) USING BTREE,
    UNIQUE INDEX `mobile` (`mobile` ASC) USING BTREE,
    FULLTEXT INDEX `nickname` (`nickname`) COMMENT '昵称全文索引'
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for visit_logs
-- ----------------------------
DROP TABLE IF EXISTS `visit_logs`;

CREATE TABLE `visit_logs` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志 ID',
    `user_id` bigint NOT NULL COMMENT '被访问者 UID',
    `visitor_id` bigint NOT NULL COMMENT '访问者 UID',
    `visit_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '主页访问日志表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;