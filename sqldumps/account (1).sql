-- phpMyAdmin SQL Dump
-- version 5.1.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th8 19, 2021 lúc 11:32 AM
-- Phiên bản máy phục vụ: 10.4.20-MariaDB
-- Phiên bản PHP: 7.4.21

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `account`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accountcrowns`
--

CREATE TABLE `accountcrowns` (
  `accountId` int(11) NOT NULL,
  `crownCount` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accountcustomdata`
--

CREATE TABLE `accountcustomdata` (
  `accountId` int(11) NOT NULL,
  `customDataId` int(11) NOT NULL,
  `data` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accountfriend`
--

CREATE TABLE `accountfriend` (
  `id` int(11) NOT NULL,
  `uuidSource` varchar(100) DEFAULT NULL,
  `uuidTarget` varchar(100) DEFAULT NULL,
  `status` varchar(100) DEFAULT NULL,
  `favourite` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accountfrienddata`
--

CREATE TABLE `accountfrienddata` (
  `accountId` int(11) NOT NULL,
  `status` int(11) NOT NULL,
  `favourite` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accountignore`
--

CREATE TABLE `accountignore` (
  `uuidIgnorer` varchar(255) NOT NULL,
  `uuidIgnored` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accountinventory`
--

CREATE TABLE `accountinventory` (
  `id` int(11) NOT NULL,
  `accountId` int(11) NOT NULL,
  `itemId` int(11) NOT NULL,
  `count` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accountkits`
--

CREATE TABLE `accountkits` (
  `accountId` int(11) NOT NULL,
  `kitId` int(11) NOT NULL,
  `active` bit(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Đang đổ dữ liệu cho bảng `accountkits`
--

INSERT INTO `accountkits` (`accountId`, `kitId`, `active`) VALUES
(1, 101, b'1'),
(1, 102, b'0'),
(1, 103, b'0');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accountkitstats`
--

CREATE TABLE `accountkitstats` (
  `accountId` int(11) NOT NULL,
  `kitId` int(11) NOT NULL,
  `statId` int(11) NOT NULL,
  `value` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accountlevelreward`
--

CREATE TABLE `accountlevelreward` (
  `accountId` int(11) NOT NULL,
  `level` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accountmissions`
--

CREATE TABLE `accountmissions` (
  `accountId` int(11) NOT NULL,
  `missionId` int(11) NOT NULL,
  `length` int(11) NOT NULL,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `startTime` bigint(11) NOT NULL,
  `progress` int(11) NOT NULL DEFAULT 0,
  `complete` bit(1) NOT NULL DEFAULT b'0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Đang đổ dữ liệu cho bảng `accountmissions`
--

INSERT INTO `accountmissions` (`accountId`, `missionId`, `length`, `x`, `y`, `startTime`, `progress`, `complete`) VALUES
(1, 1101, 0, 5, 0, 1628076085654, 0, b'0'),
(1, 1001, 0, 23, 0, 1628076085654, 0, b'0'),
(1, 703, 0, 102, 0, 1628076085654, 0, b'0'),
(1, 1003, 0, 25, 0, 1628076085654, 0, b'0'),
(1, 502, 0, 20, 0, 1628076085654, 0, b'0'),
(1, 901, 1, 40, 0, 1628076085654, 0, b'0'),
(1, 201, 1, 360, 0, 1628076085654, 0, b'0'),
(1, 1200, 1, 75, 0, 1628076085654, 0, b'0'),
(3, 202, 0, 60, 0, 1628079940971, 0, b'0'),
(3, 900, 0, 1, 0, 1628079940971, 0, b'0'),
(3, 1000, 0, 2, 0, 1628079940971, 0, b'0'),
(3, 505, 0, 504, 0, 1628079940971, 0, b'0'),
(3, 802, 0, 4, 0, 1628079940971, 0, b'0'),
(3, 504, 1, 1255, 0, 1628079940972, 0, b'0'),
(3, 701, 1, 10, 0, 1628079940972, 0, b'0'),
(3, 901, 1, 35, 0, 1628079940972, 0, b'0'),
(4, 301, 0, 1, 0, 1629307611699, 0, b'0'),
(4, 102, 0, 18, 0, 1629307611700, 0, b'0'),
(4, 104, 0, 29, 0, 1629307611700, 0, b'0'),
(4, 1002, 0, 23, 0, 1629307611700, 0, b'0'),
(4, 600, 0, 2, 0, 1629307611700, 0, b'0'),
(4, 5, 1, 1, 56, 1629307611700, 0, b'0'),
(4, 408, 1, 14510, 0, 1629307611700, 0, b'0'),
(4, 1100, 1, 105230, 0, 1629307611701, 0, b'0'),
(5, 703, 0, 116, 0, 1629307620849, 0, b'0'),
(5, 2, 0, 16, 0, 1629307620849, 0, b'0'),
(5, 1302, 0, 33, 0, 1629307620849, 0, b'0'),
(5, 500, 0, 13, 0, 1629307620849, 0, b'0'),
(5, 100, 0, 14, 0, 1629307620849, 1, b'0'),
(5, 506, 1, 90, 0, 1629307620849, 0, b'0'),
(5, 406, 1, 10, 0, 1629307620849, 0, b'0'),
(5, 300, 1, 15, 24, 1629307620849, 0, b'0');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accountpolls`
--

CREATE TABLE `accountpolls` (
  `id` int(11) NOT NULL,
  `accountId` int(11) NOT NULL,
  `pollId` int(11) NOT NULL,
  `value` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accountpunishments`
--

CREATE TABLE `accountpunishments` (
  `id` int(11) NOT NULL,
  `target` varchar(20) NOT NULL,
  `category` varchar(255) NOT NULL,
  `sentence` varchar(255) NOT NULL,
  `reason` varchar(255) NOT NULL,
  `duration` double NOT NULL,
  `admin` varchar(255) NOT NULL,
  `severity` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Đang đổ dữ liệu cho bảng `accountpunishments`
--

INSERT INTO `accountpunishments` (`id`, `target`, `category`, `sentence`, `reason`, `duration`, `admin`, `severity`) VALUES
(1, 'Labality', 'ChatOffense', 'Mute', '1', 720, 'Labality', 3),
(2, 'Labality', 'Other', 'Ban', '1', -1, 'Labality', 1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accountranks`
--

CREATE TABLE `accountranks` (
  `id` int(11) NOT NULL,
  `accountId` int(11) NOT NULL,
  `rankIdentifier` varchar(40) DEFAULT NULL,
  `primaryGroup` tinyint(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Đang đổ dữ liệu cho bảng `accountranks`
--

INSERT INTO `accountranks` (`id`, `accountId`, `rankIdentifier`, `primaryGroup`) VALUES
(3, -1, 'PLAYER', 1),
(5, 1, 'ADMIN', 0),
(1, 1, 'ADMIN', 1),
(2, 1, 'QA', 0),
(4, 1, 'SRMOD', 0),
(7, 3, 'PLAYER', 1),
(8, 4, 'PLAYER', 1),
(9, 5, 'ADMIN', 1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accounts`
--

CREATE TABLE `accounts` (
  `id` int(11) NOT NULL,
  `uuid` varchar(100) DEFAULT NULL,
  `name` varchar(40) DEFAULT NULL,
  `gems` int(11) DEFAULT 0,
  `coins` int(11) NOT NULL DEFAULT 0,
  `lastLogin` mediumtext DEFAULT NULL,
  `totalPlayTime` mediumtext DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Đang đổ dữ liệu cho bảng `accounts`
--

INSERT INTO `accounts` (`id`, `uuid`, `name`, `gems`, `coins`, `lastLogin`, `totalPlayTime`) VALUES
(1, '0a8cd98e-4b11-4c53-9e34-fc37cbaa7e0c', 'Labality', 0, 0, '2021-08-19 00:12:08', NULL),
(3, 'c3949f1c-739b-4eb0-859a-852cb8d352ee', 'MymagicCraft', 0, 0, '2021-08-04 19:29:15', NULL),
(4, 'fba6d312-a7d2-3e7e-8cfa-dc00ae963bcd', 'Pheonix_121', 0, 0, '2021-08-19 00:32:48', NULL),
(5, 'ccb16cbc-ad4c-3307-8774-6f4b685b3c95', 'MrEarthCreeper', 0, 0, '2021-08-19 00:32:50', NULL);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accountstatsalltime`
--

CREATE TABLE `accountstatsalltime` (
  `accountId` int(11) NOT NULL,
  `statId` int(11) NOT NULL,
  `value` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Đang đổ dữ liệu cho bảng `accountstatsalltime`
--

INSERT INTO `accountstatsalltime` (`accountId`, `statId`, `value`) VALUES
(1, 1, 4077),
(3, 1, 15),
(1, 2, 10672500),
(1, 4, 20),
(1, 5, 20),
(1, 6, 20),
(1, 12, 100),
(1, 13, 100),
(1, 14, 100),
(5, 1, 27),
(5, 4, 1),
(5, 5, 1),
(5, 6, 1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accounttasks`
--

CREATE TABLE `accounttasks` (
  `accountId` int(11) NOT NULL,
  `taskId` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accountthanktransactions`
--

CREATE TABLE `accountthanktransactions` (
  `receiverId` int(11) NOT NULL,
  `senderId` int(11) NOT NULL,
  `thankAmount` int(11) NOT NULL,
  `reason` varchar(255) NOT NULL,
  `ignoreCooldown` bit(1) NOT NULL,
  `claimed` bit(1) NOT NULL,
  `sentTime` time NOT NULL,
  `claimTime` time NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accounttitle`
--

CREATE TABLE `accounttitle` (
  `accountId` int(11) NOT NULL,
  `trackName` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `accountwinstreak`
--

CREATE TABLE `accountwinstreak` (
  `accountId` int(11) NOT NULL,
  `gameId` int(11) NOT NULL,
  `value` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `bonus`
--

CREATE TABLE `bonus` (
  `accountId` int(11) NOT NULL,
  `dailytime` timestamp NULL DEFAULT NULL,
  `clansdailytime` timestamp NULL DEFAULT NULL,
  `ranktime` date DEFAULT NULL,
  `votetime` date DEFAULT NULL,
  `clansvotetime` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Đang đổ dữ liệu cho bảng `bonus`
--

INSERT INTO `bonus` (`accountId`, `dailytime`, `clansdailytime`, `ranktime`, `votetime`, `clansvotetime`) VALUES
(1, NULL, NULL, NULL, NULL, NULL),
(4, NULL, NULL, NULL, NULL, NULL),
(5, NULL, NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `communities`
--

CREATE TABLE `communities` (
  `id` int(11) NOT NULL,
  `name` varchar(15) NOT NULL,
  `region` varchar(5) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Đang đổ dữ liệu cho bảng `communities`
--

INSERT INTO `communities` (`id`, `name`, `region`) VALUES
(1, 'order', 'US');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `communityinvites`
--

CREATE TABLE `communityinvites` (
  `accountId` int(11) NOT NULL,
  `communityId` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `communityjoinrequests`
--

CREATE TABLE `communityjoinrequests` (
  `accountId` int(11) NOT NULL,
  `communityId` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `communitymembers`
--

CREATE TABLE `communitymembers` (
  `accountId` int(11) NOT NULL,
  `communityId` int(11) NOT NULL,
  `communityRole` varchar(20) NOT NULL,
  `readingChat` bit(1) NOT NULL DEFAULT b'1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Đang đổ dữ liệu cho bảng `communitymembers`
--

INSERT INTO `communitymembers` (`accountId`, `communityId`, `communityRole`, `readingChat`) VALUES
(1, 1, 'LEADER', b'1');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `communitysettings`
--

CREATE TABLE `communitysettings` (
  `settingId` int(11) NOT NULL,
  `communityId` int(11) NOT NULL,
  `settingValue` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Đang đổ dữ liệu cho bảng `communitysettings`
--

INSERT INTO `communitysettings` (`settingId`, `communityId`, `settingValue`) VALUES
(5, 1, 'Gladiators'),
(5, 1, 'Speed Builders'),
(5, 1, 'Skyfall'),
(5, 1, 'Heroes of GWEN'),
(5, 1, 'Cake Wars Standard'),
(5, 1, 'Bacon Brawl'),
(5, 1, 'The Bridges'),
(6, 1, 'Closed'),
(8, 1, 'false'),
(6, 1, 'Open to Join'),
(8, 1, 'false'),
(6, 1, 'Accepting Join Requests'),
(8, 1, 'false'),
(6, 1, 'Closed'),
(8, 1, 'false'),
(6, 1, 'Open to Join'),
(8, 1, 'false'),
(6, 1, 'Accepting Join Requests'),
(8, 1, 'false'),
(3, 1, 'AQUA'),
(5, 1, 'Castle Assault'),
(5, 1, 'Castle Siege'),
(5, 1, 'Champions Domination'),
(5, 1, 'Champions TDM'),
(3, 1, 'BLUE');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `customdata`
--

CREATE TABLE `customdata` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `elorating`
--

CREATE TABLE `elorating` (
  `accountId` int(11) NOT NULL,
  `gameType` int(11) NOT NULL,
  `elo` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `eternalgiveaway`
--

CREATE TABLE `eternalgiveaway` (
  `accountId` int(11) NOT NULL,
  `uuid` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `incognitostaff`
--

CREATE TABLE `incognitostaff` (
  `accountId` int(11) NOT NULL,
  `status` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `items`
--

CREATE TABLE `items` (
  `id` int(11) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `rarity` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `kitprogression`
--

CREATE TABLE `kitprogression` (
  `uuid` varchar(255) NOT NULL,
  `kitId` varchar(255) NOT NULL,
  `xp` int(11) NOT NULL,
  `level` int(11) NOT NULL,
  `upgrade_level` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `newnpcsnew`
--

CREATE TABLE `newnpcsnew` (
  `id` int(11) NOT NULL,
  `entity_type` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `info` varchar(255) NOT NULL,
  `world` varchar(255) NOT NULL,
  `x` double NOT NULL,
  `y` double NOT NULL,
  `z` double NOT NULL,
  `yaw` int(11) NOT NULL,
  `pitch` int(11) NOT NULL,
  `in_hand` varchar(255) NOT NULL,
  `in_hand_data` bit(1) NOT NULL,
  `helmet` varchar(255) NOT NULL,
  `chestplate` varchar(255) NOT NULL,
  `leggings` varchar(255) NOT NULL,
  `boots` varchar(255) NOT NULL,
  `metadata` varchar(255) NOT NULL,
  `skin_value` varchar(255) NOT NULL,
  `skin_signature` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `playermap`
--

CREATE TABLE `playermap` (
  `accountId` int(11) NOT NULL,
  `playerName` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `polls`
--

CREATE TABLE `polls` (
  `id` int(11) NOT NULL,
  `enabled` bit(1) DEFAULT NULL,
  `question` varchar(256) NOT NULL,
  `answerA` varchar(256) NOT NULL,
  `answerB` varchar(256) DEFAULT NULL,
  `answerC` varchar(256) DEFAULT NULL,
  `answerD` varchar(256) DEFAULT NULL,
  `coinReward` int(11) NOT NULL,
  `displayType` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `powerplayclaims`
--

CREATE TABLE `powerplayclaims` (
  `accountId` int(11) NOT NULL,
  `claimYear` int(11) NOT NULL,
  `claimMonth` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `powerplaysubs`
--

CREATE TABLE `powerplaysubs` (
  `accountId` int(11) NOT NULL,
  `startDate` date NOT NULL,
  `duration` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `preferences`
--

CREATE TABLE `preferences` (
  `accountId` int(11) NOT NULL,
  `uuid` varchar(256) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `specificyoutube`
--

CREATE TABLE `specificyoutube` (
  `accountId` int(11) NOT NULL,
  `clicktime` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `stats`
--

CREATE TABLE `stats` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Đang đổ dữ liệu cho bảng `stats`
--

INSERT INTO `stats` (`id`, `name`) VALUES
(1, 'Global.TimeInGame'),
(2, 'Global.ExpEarned'),
(3, 'track.warrior'),
(4, 'track.gem-collector'),
(5, 'Bacon Brawl.GemsEarned'),
(6, 'Global.GemsEarned'),
(7, 'Snake.GemsEarned'),
(8, 'Draw My Thing.GemsEarned'),
(9, 'Alien Invasion.GemsEarned'),
(10, 'Runner.GemsEarned'),
(11, 'Sneaky Assassins.GemsEarned'),
(12, 'luckpoints'),
(13, 'luck'),
(14, 'gem-collector'),
(15, 'Dragon Escape.GemsEarned'),
(16, 'Bacon Brawl.TrackWins'),
(17, 'Bacon Brawl.ExpEarned'),
(18, 'Bacon Brawl.Damage Dealt'),
(19, 'Bacon Brawl.Kills'),
(20, 'Bacon Brawl.Wins');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `tasks`
--

CREATE TABLE `tasks` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `twofactor`
--

CREATE TABLE `twofactor` (
  `accountId` int(11) NOT NULL,
  `secretKey` varchar(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `twofactor_history`
--

CREATE TABLE `twofactor_history` (
  `accountId` int(11) NOT NULL,
  `ip` int(11) NOT NULL,
  `loginTime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `youtube`
--

CREATE TABLE `youtube` (
  `accountId` int(11) NOT NULL,
  `clicktime` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `accountcrowns`
--
ALTER TABLE `accountcrowns`
  ADD PRIMARY KEY (`accountId`);

--
-- Chỉ mục cho bảng `accountfriend`
--
ALTER TABLE `accountfriend`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uuidIndex` (`uuidSource`,`uuidTarget`);

--
-- Chỉ mục cho bảng `accountinventory`
--
ALTER TABLE `accountinventory`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `accountItemIndex` (`accountId`,`itemId`),
  ADD KEY `itemId` (`itemId`);

--
-- Chỉ mục cho bảng `accountpolls`
--
ALTER TABLE `accountpolls`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `accountPollIndex` (`accountId`,`pollId`),
  ADD KEY `pollId` (`pollId`);

--
-- Chỉ mục cho bảng `accountpunishments`
--
ALTER TABLE `accountpunishments`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `accountranks`
--
ALTER TABLE `accountranks`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `additionalIndex` (`accountId`,`rankIdentifier`,`primaryGroup`),
  ADD KEY `accountIndex` (`accountId`),
  ADD KEY `rankIndex` (`rankIdentifier`);

--
-- Chỉ mục cho bảng `accounts`
--
ALTER TABLE `accounts`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uuidIndex` (`uuid`),
  ADD UNIQUE KEY `nameIndex` (`name`);

--
-- Chỉ mục cho bảng `bonus`
--
ALTER TABLE `bonus`
  ADD PRIMARY KEY (`accountId`);

--
-- Chỉ mục cho bảng `communities`
--
ALTER TABLE `communities`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `incognitostaff`
--
ALTER TABLE `incognitostaff`
  ADD PRIMARY KEY (`accountId`);

--
-- Chỉ mục cho bảng `items`
--
ALTER TABLE `items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `mameIndex` (`name`);

--
-- Chỉ mục cho bảng `polls`
--
ALTER TABLE `polls`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `stats`
--
ALTER TABLE `stats`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `accountfriend`
--
ALTER TABLE `accountfriend`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `accountinventory`
--
ALTER TABLE `accountinventory`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `accountpolls`
--
ALTER TABLE `accountpolls`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `accountpunishments`
--
ALTER TABLE `accountpunishments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT cho bảng `accountranks`
--
ALTER TABLE `accountranks`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT cho bảng `accounts`
--
ALTER TABLE `accounts`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT cho bảng `bonus`
--
ALTER TABLE `bonus`
  MODIFY `accountId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT cho bảng `communities`
--
ALTER TABLE `communities`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT cho bảng `items`
--
ALTER TABLE `items`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `polls`
--
ALTER TABLE `polls`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `stats`
--
ALTER TABLE `stats`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `accountcrowns`
--
ALTER TABLE `accountcrowns`
  ADD CONSTRAINT `accountcrowns_ibfk_1` FOREIGN KEY (`accountId`) REFERENCES `accounts` (`id`);

--
-- Các ràng buộc cho bảng `accountinventory`
--
ALTER TABLE `accountinventory`
  ADD CONSTRAINT `accountinventory_ibfk_1` FOREIGN KEY (`accountId`) REFERENCES `accounts` (`id`),
  ADD CONSTRAINT `accountinventory_ibfk_2` FOREIGN KEY (`itemId`) REFERENCES `items` (`id`);

--
-- Các ràng buộc cho bảng `accountpolls`
--
ALTER TABLE `accountpolls`
  ADD CONSTRAINT `accountpolls_ibfk_1` FOREIGN KEY (`accountId`) REFERENCES `accounts` (`id`),
  ADD CONSTRAINT `accountpolls_ibfk_2` FOREIGN KEY (`pollId`) REFERENCES `polls` (`id`);

--
-- Các ràng buộc cho bảng `bonus`
--
ALTER TABLE `bonus`
  ADD CONSTRAINT `bonus_ibfk_1` FOREIGN KEY (`accountId`) REFERENCES `accounts` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
