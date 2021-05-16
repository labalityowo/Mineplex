-- MariaDB dump 10.17  Distrib 10.4.14-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: player_stats
-- ------------------------------------------------------
-- Server version       10.4.14-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ipinfo`
--

DROP TABLE IF EXISTS `ipinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ipinfo` (
  `id` int(11) NOT NULL,
  `ipAddress` text COLLATE latin1_bin NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ipinfo`
--

LOCK TABLES `ipinfo` WRITE;
/*!40000 ALTER TABLE `ipinfo` DISABLE KEYS */;
/*!40000 ALTER TABLE `ipinfo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `playerinfo`
--

DROP TABLE IF EXISTS `playerinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `playerinfo` (
  `uuid` varchar(100) NOT NULL,
  `name` varchar(40) NOT NULL,
  `version` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `playerinfo`
--

LOCK TABLES `playerinfo` WRITE;
/*!40000 ALTER TABLE `playerinfo` DISABLE KEYS */;
/*!40000 ALTER TABLE `playerinfo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `playerips`
--

DROP TABLE IF EXISTS `playerips`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `playerips` (
  `playerInfoId` int(11) NOT NULL,
  `ipInfoId` int(11) NOT NULL,
  `date` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `playerips`
--

LOCK TABLES `playerips` WRITE;
/*!40000 ALTER TABLE `playerips` DISABLE KEYS */;
/*!40000 ALTER TABLE `playerips` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `playerloginsessions`
--

DROP TABLE IF EXISTS `playerloginsessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `playerloginsessions` (
  `playerInfoId` int(11) NOT NULL,
  `loginTime` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `playerloginsessions`
--

LOCK TABLES `playerloginsessions` WRITE;
/*!40000 ALTER TABLE `playerloginsessions` DISABLE KEYS */;
/*!40000 ALTER TABLE `playerloginsessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `playeruniquelogins`
--

DROP TABLE IF EXISTS `playeruniquelogins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `playeruniquelogins` (
  `playerInfoId` int(11) NOT NULL,
  `day` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `playeruniquelogins`
--

LOCK TABLES `playeruniquelogins` WRITE;
/*!40000 ALTER TABLE `playeruniquelogins` DISABLE KEYS */;
/*!40000 ALTER TABLE `playeruniquelogins` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-05-16 10:39:44