/**
These queries must be executed before attempting to use the report-v2 branch otherwise the software
will not function at all.
 */

/**
Drop old tables created by previous developers of the report and chatsnap system.
 */
DROP TABLE IF EXISTS reportTickets;
DROP TABLE IF EXISTS chatsnap;

/**
Create new schema used for v2
 */

CREATE TABLE snapshots
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  creator INT(11),
  CONSTRAINT snapshots_accounts_id_fk FOREIGN KEY (creator) REFERENCES accounts (id)
);
CREATE INDEX snapshots_accounts_id_fk ON snapshots (creator);

CREATE TABLE snapshotTypes
(
  id TINYINT(3) unsigned PRIMARY KEY NOT NULL,
  name VARCHAR(25) NOT NULL
);
CREATE UNIQUE INDEX reportMessageTypes_id_uindex ON snapshotTypes (id);

CREATE TABLE snapshotMessages
(
  id BIGINT(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  senderId INT(11) NOT NULL,
  server VARCHAR(25) NOT NULL,
  time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
  message VARCHAR(150) NOT NULL,
  snapshotType TINYINT(3) unsigned NOT NULL,
  CONSTRAINT reportChatLog_accounts_id_fk FOREIGN KEY (senderId) REFERENCES accounts (id),
  CONSTRAINT reportChatMessages_reportMessageTypes_id_fk FOREIGN KEY (snapshotType) REFERENCES snapshotTypes (id)
);
CREATE INDEX reportChatLog_accounts_id_fk ON snapshotMessages (senderId);
CREATE INDEX reportChatMessages_reportMessageTypes_id_fk ON snapshotMessages (snapshotType);

CREATE TABLE snapshotRecipients
(
  messageId BIGINT(20) NOT NULL,
  recipientId INT(11) NOT NULL,
  CONSTRAINT `PRIMARY` PRIMARY KEY (messageId, recipientId),
  CONSTRAINT snapshotRecipients_snapshotMessages_id_fk FOREIGN KEY (messageId) REFERENCES snapshotMessages (id),
  CONSTRAINT reportMessageRecipients_accounts_id_fk FOREIGN KEY (recipientId) REFERENCES accounts (id)
);
CREATE INDEX reportMessageRecipients_accounts_id_fk ON snapshotRecipients (recipientId);

CREATE TABLE snapshotMessageMap
(
  snapshotId INT(11) NOT NULL,
  messageId BIGINT(20) NOT NULL,
  CONSTRAINT `PRIMARY` PRIMARY KEY (snapshotId, messageId),
  CONSTRAINT snapshotMessageMap_snapshots_id_fk FOREIGN KEY (snapshotId) REFERENCES snapshots (id),
  CONSTRAINT snapshotMessageMap_snapshotMessages_id_fk FOREIGN KEY (messageId) REFERENCES snapshotMessages (id)
);
CREATE INDEX snapshotMessageMap_snapshotMessages_id_fk ON snapshotMessageMap (messageId);

CREATE TABLE reportTeams
(
  id TINYINT(4) PRIMARY KEY NOT NULL,
  name VARCHAR(50) NOT NULL
);

CREATE TABLE reportTeamMemberships
(
  accountId INT(11) NOT NULL,
  teamId TINYINT(4) NOT NULL,
  CONSTRAINT `PRIMARY` PRIMARY KEY (accountId, teamId),
  CONSTRAINT reportTeams_accounts_id_fk FOREIGN KEY (accountId) REFERENCES accounts (id),
  CONSTRAINT reportTeams_reportTeamTypes_id_fk FOREIGN KEY (teamId) REFERENCES reportTeams (id)
);
CREATE INDEX reportTeams_accountId_index ON reportTeamMemberships (accountId);
CREATE INDEX reportTeams_reportTeamTypes_id_fk ON reportTeamMemberships (teamId);

CREATE TABLE reportCategoryTypes
(
  id TINYINT(4) unsigned PRIMARY KEY NOT NULL,
  name VARCHAR(16) NOT NULL
);

CREATE TABLE reports
(
  id INT(11) unsigned PRIMARY KEY NOT NULL AUTO_INCREMENT,
  suspectId INT(11) NOT NULL,
  categoryId TINYINT(4) unsigned NOT NULL,
  snapshotId INT(11),
  assignedTeam TINYINT(4),
  CONSTRAINT accounts_accountsId_id_fk FOREIGN KEY (suspectId) REFERENCES accounts (id),
  CONSTRAINT reportCategoryTypes_categoryId_id_fk FOREIGN KEY (categoryId) REFERENCES reportCategoryTypes (id),
  CONSTRAINT reports_snapshots_id_fk FOREIGN KEY (snapshotId) REFERENCES snapshots (id),
  CONSTRAINT reports_reportTeams_teamId_fk FOREIGN KEY (assignedTeam) REFERENCES reportTeams (id)
);
CREATE INDEX reportCategoryTypes_categoryId_id_fk ON reports (categoryId);
CREATE INDEX reports_reportTeams_teamId_fk ON reports (assignedTeam);
CREATE INDEX reports_snapshots_id_fk ON reports (snapshotId);
CREATE INDEX reports_suspect_id_index ON reports (suspectId);

CREATE TABLE reportResultTypes
(
  id TINYINT(4) unsigned PRIMARY KEY NOT NULL,
  globalStat TINYINT(1) NOT NULL,
  name VARCHAR(16) NOT NULL
);

CREATE TABLE reportReasons
(
  reportId INT(11) unsigned NOT NULL,
  reporterId INT(11) NOT NULL,
  reason VARCHAR(100) NOT NULL,
  server VARCHAR(50) NOT NULL,
  weight INT(11) DEFAULT '0' NOT NULL,
  time DATETIME NOT NULL,
  CONSTRAINT `PRIMARY` PRIMARY KEY (reportId, reporterId),
  CONSTRAINT reportReasons_reports_id_fk FOREIGN KEY (reportId) REFERENCES reports (id),
  CONSTRAINT reportReasons_accounts_id_fk FOREIGN KEY (reporterId) REFERENCES accounts (id)
);
CREATE INDEX reportReasons_accounts_id_fk ON reportReasons (reporterId);

CREATE TABLE reportHandlers
(
  reportId INT(10) unsigned NOT NULL,
  handlerId INT(11) NOT NULL,
  aborted TINYINT(1) DEFAULT '0' NOT NULL,
  CONSTRAINT `PRIMARY` PRIMARY KEY (reportId, handlerId),
  CONSTRAINT reportHandlers_reports_id_fk FOREIGN KEY (reportId) REFERENCES reports (id),
  CONSTRAINT reportHandlers_accountStat_accountId_fk FOREIGN KEY (handlerId) REFERENCES accounts (id)
);
CREATE INDEX reportHandlers_accountStat_accountId_fk ON reportHandlers (handlerId);
CREATE INDEX reportHandlers_reportId_index ON reportHandlers (reportId);

CREATE TABLE reportResults
(
  reportId INT(11) unsigned PRIMARY KEY NOT NULL,
  resultId TINYINT(4) NOT NULL,
  reason VARCHAR(50),
  closedTime DATETIME NOT NULL,
  CONSTRAINT reportResults_reports_id_fk FOREIGN KEY (reportId) REFERENCES reports (id)
);
CREATE INDEX reportResults_reportResultTypes_id_fk ON reportResults (resultId);

/**
Insert "enums"
 */

INSERT INTO Account.reportCategoryTypes (id, name) VALUES (0, 'GLOBAL');
INSERT INTO Account.reportCategoryTypes (id, name) VALUES (1, 'HACKING');
INSERT INTO Account.reportCategoryTypes (id, name) VALUES (2, 'CHAT_ABUSE');
INSERT INTO Account.reportCategoryTypes (id, name) VALUES (3, 'GAMEPLAY');

INSERT INTO Account.reportResultTypes (id, globalStat, name) VALUES (0, 0, 'ACCEPTED');
INSERT INTO Account.reportResultTypes (id, globalStat, name) VALUES (1, 0, 'DENIED');
INSERT INTO Account.reportResultTypes (id, globalStat, name) VALUES (2, 1, 'ABUSIVE');
INSERT INTO Account.reportResultTypes (id, globalStat, name) VALUES (3, 1, 'EXPIRED');

INSERT INTO Account.snapshotTypes (id, name) VALUES (0, 'CHAT');
INSERT INTO Account.snapshotTypes (id, name) VALUES (1, 'PM');
INSERT INTO Account.snapshotTypes (id, name) VALUES (2, 'PARTY');

INSERT INTO Account.reportTeams (id, name) VALUES (0, 'RC');