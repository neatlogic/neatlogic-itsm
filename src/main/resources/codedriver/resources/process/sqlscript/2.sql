/*
SQLyog Ultimate
MySQL - 5.7.23-log : Database - codedriver_master
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*Table structure for table `process_matrix_form_component` */

CREATE TABLE `process_matrix_form_component` (
  `form_version_uuid` char(32) DEFAULT NULL COMMENT '表单版本uuid',
  `matrix_uuid` char(32) DEFAULT NULL COMMENT '矩阵uuid',
  `form_attribute_label` varchar(50) DEFAULT NULL COMMENT '表单组件名称',
  `form_attribute_uuid` varchar(32) DEFAULT NULL COMMENT '表单组件uuid'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `process_notifytemplate` */

CREATE TABLE `process_notifytemplate` (
  `uuid` varchar(50) NOT NULL COMMENT '全局唯一id，跨环境导入用',
  `name` varchar(100) NOT NULL COMMENT '模板名称',
  `title` text COMMENT '标题',
  `content` text COMMENT '内容',
  `type` varchar(100) DEFAULT NULL COMMENT '分类',
  `notify_handler` varchar(255) DEFAULT NULL COMMENT '通知插件',
  `trigger` enum('active','start','failed','succeed','hang','aborted','recover','accept','transfer','assign','timeout','urge','retreat','back') DEFAULT NULL COMMENT '触发事件类型',
  `fcd` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `fcu` char(32) DEFAULT NULL COMMENT '创建用户',
  `lcd` timestamp NULL DEFAULT NULL COMMENT '最后一次修改时间',
  `lcu` char(32) DEFAULT NULL COMMENT '最后一次修改用户',
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `process_sla` */

CREATE TABLE `process_sla` (
  `uuid` char(32) NOT NULL COMMENT 'uuid',
  `process_uuid` char(32) NOT NULL COMMENT '流程uuid',
  `name` varchar(100) DEFAULT NULL COMMENT '名称',
  `config` text COMMENT '配置规则,json格式',
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `process_step` */

CREATE TABLE `process_step` (
  `process_uuid` char(32) NOT NULL,
  `uuid` char(32) NOT NULL,
  `name` varchar(100) NOT NULL,
  `type` enum('start','end','process','converge') NOT NULL COMMENT '类型',
  `handler` enum('start','end','omnipotent','distributary','condition') NOT NULL COMMENT '处理器',
  `config` longtext COMMENT '额外配置,json格式',
  `description` text,
  PRIMARY KEY (`uuid`),
  KEY `idx_process_step` (`process_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `process_step_formattribute` */

CREATE TABLE `process_step_formattribute` (
  `process_uuid` varchar(50) NOT NULL,
  `process_step_uuid` varchar(50) NOT NULL,
  `form_uuid` varchar(50) NOT NULL,
  `attribute_uuid` varchar(50) NOT NULL,
  `action` enum('read','hide') NOT NULL COMMENT '授权类型',
  PRIMARY KEY (`process_uuid`,`process_step_uuid`,`form_uuid`,`attribute_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `process_step_handler` */

CREATE TABLE `process_step_handler` (
  `handler` enum('start','end','omnipotent','distributary','condition','automatic') NOT NULL COMMENT '节点组件',
  `config` text COMMENT '组件配置信息',
  PRIMARY KEY (`handler`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `process_step_notifytemplate` */

CREATE TABLE `process_step_notifytemplate` (
  `process_uuid` varchar(50) NOT NULL COMMENT '流程uuid',
  `process_step_uuid` varchar(50) NOT NULL COMMENT '流程步骤uuid',
  `template_uuid` varchar(50) NOT NULL COMMENT '通知模板uuid',
  PRIMARY KEY (`process_uuid`,`process_step_uuid`,`template_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `process_step_rel` */

CREATE TABLE `process_step_rel` (
  `process_uuid` char(32) NOT NULL,
  `uuid` char(32) NOT NULL COMMENT '全局唯一id',
  `from_step_uuid` char(32) NOT NULL,
  `to_step_uuid` char(32) NOT NULL,
  `condition` varchar(500) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `type` enum('forward','backward') DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `idx_process_uuid` (`process_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `process_step_sla` */

CREATE TABLE `process_step_sla` (
  `sla_uuid` char(32) NOT NULL,
  `step_uuid` char(32) NOT NULL,
  PRIMARY KEY (`sla_uuid`,`step_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
