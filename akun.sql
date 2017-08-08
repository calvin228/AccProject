CREATE DATABASE  IF NOT EXISTS `akun` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `akun`;
-- MySQL dump 10.13  Distrib 5.7.17, for Linux (x86_64)
--
-- Host: localhost    Database: akun
-- ------------------------------------------------------
-- Server version	5.7.17-0ubuntu0.16.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `chart`
--

DROP TABLE IF EXISTS `chart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chart` (
  `Chart_no` int(11) NOT NULL,
  `Chart_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`Chart_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chart`
--

LOCK TABLES `chart` WRITE;
/*!40000 ALTER TABLE `chart` DISABLE KEYS */;
INSERT INTO `chart` VALUES (1000,'Asset'),(1010,'Cash'),(1020,'Bank'),(1030,'Account Receivable'),(1040,'Inventory'),(1050,'Tax Out'),(1060,'Cost in Advances'),(1061,'Insurance in Advances'),(1070,'Giro Receivable'),(1100,'Fixed Asset'),(2000,'Liability'),(2010,'Bank Credit'),(2020,'Account Payable'),(2022,'Purchase Return'),(2030,'Giro Payable'),(2100,'Long Term Liability'),(2110,'Long Term Loan'),(3000,'Capital'),(3010,'Owner Investment'),(3020,'Dividend'),(3030,'Privet'),(3040,'Retained Earning'),(4000,'Sales'),(4010,'Sales Income'),(4020,'Sales Discount'),(4030,'Sales Return'),(4040,'Service Income'),(5000,'Cost of Good Sold'),(5110,'Purchasing'),(5120,'Purchasing Discount'),(5130,'Purchasing Return'),(6000,'Operational Expenditure'),(6010,'General Expenditure'),(6011,'Office Supplies'),(6012,'Phone Bills'),(6020,'Marketing Expenditure'),(6022,'Electricity Bills'),(6023,'Telephone Bills'),(6030,'Human Resource Expenditure'),(6031,'Salary Expenses'),(7000,'Others'),(7010,'Others Income');
/*!40000 ALTER TABLE `chart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory`
--

DROP TABLE IF EXISTS `inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inventory` (
  `inv_date` date DEFAULT NULL,
  `inv_id` int(11) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `opening` int(11) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `inv_in` int(11) DEFAULT NULL,
  `inv_out` int(11) DEFAULT NULL,
  `ending` int(11) DEFAULT NULL,
  `inv_value` double DEFAULT NULL,
  PRIMARY KEY (`inv_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory`
--

LOCK TABLES `inventory` WRITE;
/*!40000 ALTER TABLE `inventory` DISABLE KEYS */;
INSERT INTO `inventory` VALUES ('2017-01-31',1,'Book Mickey	',0,17900,200,120,80,1432000),('2017-01-31',2,'Book Donald',0,16722,180,90,90,1504980);
/*!40000 ALTER TABLE `inventory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory_master`
--

DROP TABLE IF EXISTS `inventory_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inventory_master` (
  `id_inventory` int(11) NOT NULL,
  `id_produk` int(11) DEFAULT NULL,
  `avg_harga` double DEFAULT NULL,
  PRIMARY KEY (`id_inventory`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_master`
--

LOCK TABLES `inventory_master` WRITE;
/*!40000 ALTER TABLE `inventory_master` DISABLE KEYS */;
/*!40000 ALTER TABLE `inventory_master` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `journal_detail`
--

DROP TABLE IF EXISTS `journal_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `journal_detail` (
  `id_detail` int(11) NOT NULL AUTO_INCREMENT,
  `journal_id` int(11) DEFAULT NULL,
  `Chart_no` int(11) DEFAULT NULL,
  `Chart_name` varchar(40) DEFAULT NULL,
  `Debit` double DEFAULT NULL,
  `Credit` double DEFAULT NULL,
  PRIMARY KEY (`id_detail`),
  KEY `journal_id` (`journal_id`),
  CONSTRAINT `journal_detail_ibfk_1` FOREIGN KEY (`journal_id`) REFERENCES `journal_master` (`journal_id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `journal_detail`
--

LOCK TABLES `journal_detail` WRITE;
/*!40000 ALTER TABLE `journal_detail` DISABLE KEYS */;
INSERT INTO `journal_detail` VALUES (1,1,1020,'Bank',50000000,0),(2,2,3010,'Owner Investment',0,50000000),(3,3,1010,'Cash',15000000,0),(4,4,3010,'Owner Investment',0,15000000),(5,5,5110,'Purchasing',3160000,0),(8,8,2020,'Account Payable',0,3160000),(9,9,6011,'Office Supplies',120000,0),(10,10,1010,'Cash',0,120000),(11,11,1030,'Account Receivable',1000000,0),(12,12,4010,'Sales Income',0,1000000),(13,13,1030,'Account Receivable',1140000,0),(14,14,4010,'Sales Income',0,1140000),(15,15,5110,'Purchasing',3430000,0),(16,16,2020,'Account Payable',0,3430000),(17,17,2020,'Account Payable',3160000,0),(18,18,1020,'Bank',0,3000000),(19,19,1010,'Cash',0,160000),(20,20,1030,'Account Receivable',1900000,0),(21,21,4010,'Sales Income',0,1900000),(22,22,1020,'Bank',1000000,0),(23,23,1030,'Account Receivable',0,1000000),(24,24,6012,'Phone Bills',500000,0),(25,25,1020,'Bank',0,500000);
/*!40000 ALTER TABLE `journal_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `journal_master`
--

DROP TABLE IF EXISTS `journal_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `journal_master` (
  `journal_id` int(11) NOT NULL,
  `journal_date` date DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`journal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `journal_master`
--

LOCK TABLES `journal_master` WRITE;
/*!40000 ALTER TABLE `journal_master` DISABLE KEYS */;
INSERT INTO `journal_master` VALUES (1,'2017-01-05','Mr.Amir Investment'),(2,'2017-01-05','Mr.Amir Investment'),(3,'2017-01-05','Ms.Bella Investment'),(4,'2017-01-05','Ms.Bella Investment'),(5,'2017-01-05','Purchasing No. P.01'),(8,'2017-01-05','Purchasing No. P.01'),(9,'2017-01-05','Office Supplies'),(10,'2017-01-05','Office Supplies'),(11,'2017-01-06','Sales: AB Logs, No. S.01\n'),(12,'2017-01-06','Sales: AB Logs, No. S.01\n'),(13,'2017-01-07','Sales: Med. Ltd, No. S.01\n'),(14,'2017-01-07','Sales: Med. Ltd, No. S.01\n'),(15,'2017-01-08','Purchasing No. P.02\n'),(16,'2017-01-08','Purchasing No. P.02\n'),(17,'2017-01-09','Paid No. P.01\n'),(18,'2017-01-09','Paid No. P.01\n'),(19,'2017-01-09','Paid No. P.01\n'),(20,'2017-01-09','Sales: AB Logs, No. S.03\n'),(21,'2017-01-09','Sales: AB Logs, No. S.03\n'),(22,'2017-01-09','Paid No. S.01\n'),(23,'2017-01-09','Paid No. S.01\n'),(24,'2017-01-10','Phone Bills'),(25,'2017-01-10','Phone Bills');
/*!40000 ALTER TABLE `journal_master` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `login`
--

DROP TABLE IF EXISTS `login`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `login` (
  `user_id` varchar(255) DEFAULT NULL,
  `pass` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `login`
--

LOCK TABLES `login` WRITE;
/*!40000 ALTER TABLE `login` DISABLE KEYS */;
INSERT INTO `login` VALUES ('abc','123'),('calvin','calvin123');
/*!40000 ALTER TABLE `login` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `totaldebittrial`
--

DROP TABLE IF EXISTS `totaldebittrial`;
/*!50001 DROP VIEW IF EXISTS `totaldebittrial`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `totaldebittrial` AS SELECT 
 1 AS `sum(Debit)`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `totalgl`
--

DROP TABLE IF EXISTS `totalgl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `totalgl` (
  `journal_date` date DEFAULT NULL,
  `Chart_name` varchar(255) DEFAULT NULL,
  `total_debit` double DEFAULT NULL,
  `total_credit` double DEFAULT NULL,
  `total_balance` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `totalgl`
--

LOCK TABLES `totalgl` WRITE;
/*!40000 ALTER TABLE `totalgl` DISABLE KEYS */;
/*!40000 ALTER TABLE `totalgl` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `userlogin`
--

DROP TABLE IF EXISTS `userlogin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `userlogin` (
  `userid` varchar(255) DEFAULT NULL,
  `pass` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `userlogin`
--

LOCK TABLES `userlogin` WRITE;
/*!40000 ALTER TABLE `userlogin` DISABLE KEYS */;
INSERT INTO `userlogin` VALUES ('abc','123');
/*!40000 ALTER TABLE `userlogin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Final view structure for view `totaldebittrial`
--

/*!50001 DROP VIEW IF EXISTS `totaldebittrial`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `totaldebittrial` AS select sum(`journal_detail`.`Debit`) AS `sum(Debit)` from `journal_detail` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-07-31 17:54:45
