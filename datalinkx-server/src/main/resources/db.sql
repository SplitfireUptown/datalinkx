CREATE database if NOT EXISTS `datalinkx` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `datalinkx`;

SET NAMES utf8mb4;

CREATE TABLE `DS` (
                      `id` int unsigned NOT NULL AUTO_INCREMENT,
                      `ds_id` char(35) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'ds_[32位uuid]',
                      `name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
                      `type` int DEFAULT NULL COMMENT '1MYSQL|2ORACLE|3SQLSERVER|4OPEN_DS',
                      `host` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
                      `port` int DEFAULT NULL,
                      `username` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
                      `password` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
                      `database` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
                      `schema` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '数据库的原始schema',
                      `config` text CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '附加配置',
                      `status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '最后一次同步状态，状态代码用统一的状态代码',
                      `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      `utime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      `is_del` int DEFAULT '0',
                      PRIMARY KEY (`id`) USING BTREE,
                      KEY `ds_id` (`ds_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='数据库信息';


CREATE TABLE `JOB` (
                       `id` int unsigned NOT NULL AUTO_INCREMENT,
                       `job_id` char(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
                       `reader_ds_id` char(40) NOT NULL DEFAULT '' COMMENT '来源数据源id',
                       `writer_ds_id` char(40) NOT NULL DEFAULT '' COMMENT '目标数据源id',
                       `config` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
                       `crontab` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
                       `from_tb_id` char(40) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '来源数据表id',
                       `to_tb_id` char(40) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '目标数据表id',
                       `count` text CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '导出数据统计',
                       `xxl_id` char(40) NOT NULL DEFAULT '' COMMENT 'xxl_job_id',
                       `task_id` char(40) NOT NULL DEFAULT '' COMMENT 'flink_job_id',
                       `sync_mode` longtext COMMENT 'mode: overwrite(全量)/increment(增量)，increate_field(增量字段)，increate_value(增量值)',
                       `is_del` int NOT NULL DEFAULT '0',
                       `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       `utime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       `status` int NOT NULL DEFAULT '0' COMMENT '0:CREATE|1:SYNCING|2:SYNC_FINISH|3:SYNC_ERROR|4:QUEUING',
                       `error_msg` longtext CHARACTER SET utf8 COLLATE utf8_general_ci,
                       PRIMARY KEY (`id`) USING BTREE,
                       KEY `job_id` (`job_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='流转任务配置信息';

CREATE TABLE `JOB_RELATION` (
                                `id` int unsigned NOT NULL AUTO_INCREMENT,
                                `relation_id` char(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '依赖关系id',
                                `job_id` char(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '流转任务id',
                                `sub_job_id` char(40) NOT NULL DEFAULT '' COMMENT '流转任务子id',
                                `priority` int COMMENT '同级任务优先级，越高优先级越高',
                                `is_del` int NOT NULL DEFAULT '0',
                                `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `utime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`) USING BTREE,
                                KEY `job_id` (`job_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='流转任务级联配置表';


CREATE TABLE `JOB_LOG` (
                           `id` int unsigned NOT NULL AUTO_INCREMENT,
                           `is_del` int DEFAULT NULL,
                           `cost_time` int unsigned NOT NULL,
                           `count` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
                           `end_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           `error_msg` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
                           `job_id` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
                           `start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '任务启动时间',
                           `status` tinyint unsigned DEFAULT NULL,
                           PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT COMMENT='流转任务日志';

CREATE TABLE `MESSAGEHUB_TOPIC` (
                                    `id` int NOT NULL AUTO_INCREMENT COMMENT '自增id',
                                    `topic` varchar(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '消息主题',
                                    `fields` text COLLATE utf8_unicode_ci COMMENT '字段描述',
                                    `info_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT 'REDIS_QUEUE' COMMENT '消息类型',
                                    `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `utime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除 0否1是',
                                    `desc` text COLLATE utf8_unicode_ci NOT NULL COMMENT 'topic描述',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `topic_type_key` (`topic`,`info_type`,`is_del`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='消息队列白名单';

INSERT INTO `MESSAGEHUB_TOPIC` (`topic`, `fields`, `info_type`, `desc`) VALUES ('JOB_PROGRESS', '', 'REDIS_STREAM', '任务状态刷新');


alter table JOB ADD COLUMN  `name` varchar(64) DEFAULT NULL COMMENT '任务名称';;

alter table JOB ADD COLUMN `cover` tinyint NOT NULL DEFAULT '0' COMMENT '是否开启覆盖';

-- 流式任务表结构变更

alter table JOB ADD COLUMN `type` tinyint NOT NULL DEFAULT '0' COMMENT '是否是流式任务';

alter table JOB ADD COLUMN `checkpoint` longtext CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '流式任务端点续传';

alter table JOB ADD COLUMN `start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP;

alter table JOB ADD COLUMN `retry_time` int NOT NULL DEFAULT '0' COMMENT '流式任务重试次数';

alter table JOB ADD COLUMN `graph` longtext CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '计算画布';