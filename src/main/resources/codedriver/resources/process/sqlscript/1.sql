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
/*Table structure for table `process` */

CREATE TABLE `process` (
  `uuid` varchar(50) NOT NULL COMMENT '全局唯一id，跨环境导入用',
  `name` varchar(100) DEFAULT NULL COMMENT '名称',
  `is_active` tinyint(4) DEFAULT NULL COMMENT '是否激活',
  `config` longtext COMMENT '流程图配置',
  `fcd` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `fcu` char(32) DEFAULT NULL COMMENT '创建用户',
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `process_draft` */

CREATE TABLE `process_draft` (
  `uuid` varchar(50) NOT NULL COMMENT '全局唯一id，跨环境导入用',
  `process_uuid` varchar(50) NOT NULL COMMENT '流程uuid',
  `name` varchar(100) DEFAULT NULL COMMENT '名称',
  `config` longtext COMMENT '流程图配置',
  `fcd` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `fcu` char(32) DEFAULT NULL COMMENT '创建用户',
  `md5` varchar(100) DEFAULT NULL COMMENT 'md5',
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `process_form` */

CREATE TABLE `process_form` (
  `process_uuid` char(32) NOT NULL COMMENT '流程uuid',
  `form_uuid` char(32) NOT NULL COMMENT '关联的表单uuid',
  PRIMARY KEY (`process_uuid`,`form_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='流程表单';

/*Table structure for table `process_matrix` */

CREATE TABLE `process_matrix` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `uuid` char(32) DEFAULT NULL COMMENT '唯一表示id',
  `name` varchar(50) DEFAULT NULL COMMENT '矩阵名称',
  `type` enum('custom','external') DEFAULT NULL COMMENT 'custom,自定义 external外部数据',
  `fcu` char(32) DEFAULT NULL COMMENT '创建人',
  `fcd` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `lcu` char(32) DEFAULT NULL COMMENT '修改人',
  `lcd` timestamp NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`)
) ENGINE=InnoDB AUTO_INCREMENT=203 DEFAULT CHARSET=utf8;

/*Table structure for table `process_matrix_attribute` */

CREATE TABLE `process_matrix_attribute` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `uuid` char(32) DEFAULT NULL COMMENT '属性uuid',
  `matrix_uuid` char(32) DEFAULT NULL COMMENT '矩阵uuid',
  `type` varchar(50) DEFAULT NULL COMMENT '属性类型',
  `is_required` tinyint(1) DEFAULT NULL COMMENT '属性是否必填',
  `name` varchar(50) DEFAULT NULL COMMENT '属性名称',
  `sort` int(11) DEFAULT NULL COMMENT '排序',
  `config` text COMMENT '属性配置',
  PRIMARY KEY (`id`),
  UNIQUE KEY `matrix_attribute_key` (`matrix_uuid`,`uuid`)
) ENGINE=InnoDB AUTO_INCREMENT=524 DEFAULT CHARSET=utf8;

/*Table structure for table `process_matrix_dispatcher` */

CREATE TABLE `process_matrix_dispatcher` (
  `process_uuid` char(32) DEFAULT NULL COMMENT '流程uuid',
  `matrix_uuid` char(32) DEFAULT NULL COMMENT '矩阵uuid',
  `dispatcher_name` varchar(50) DEFAULT NULL COMMENT '分派器名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
