-- RENAME COLUMN `creator` to `creatorId`
ALTER TABLE Account.snapshots CHANGE creator creatorId INT(11);

-- STORE DATETIME SNAPSHOT CREATED (CLEANUP PURPOSES)
ALTER TABLE Account.snapshots ADD created DATETIME DEFAULT NOW() NOT NULL;
ALTER TABLE Account.snapshots
  MODIFY COLUMN creatorId INT(11) AFTER created;

-- CASCADE HANDLERS TABLE
ALTER TABLE Account.reportHandlers DROP FOREIGN KEY reportHandlers_reports_id_fk;
ALTER TABLE Account.reportHandlers
  ADD CONSTRAINT reportHandlers_reports_id_fk
FOREIGN KEY (reportId) REFERENCES reports (id) ON DELETE CASCADE;

-- CASCADE REASONS/REPORTERS TABLE
ALTER TABLE Account.reportReasons DROP FOREIGN KEY reportReasons_reports_id_fk;
ALTER TABLE Account.reportReasons
  ADD CONSTRAINT reportReasons_reports_id_fk
FOREIGN KEY (reportId) REFERENCES reports (id) ON DELETE CASCADE;

-- CASCADE RESULTS TABLE
ALTER TABLE Account.reportResults DROP FOREIGN KEY reportResults_reports_id_fk;
ALTER TABLE Account.reportResults
  ADD CONSTRAINT reportResults_reports_id_fk
FOREIGN KEY (reportId) REFERENCES reports (id) ON DELETE CASCADE;

-- CASCADE SNAPSHOT MESSAGE MAP
ALTER TABLE Account.snapshotMessageMap DROP FOREIGN KEY snapshotMessageMap_snapshots_id_fk;
ALTER TABLE Account.snapshotMessageMap
  ADD CONSTRAINT snapshotMessageMap_snapshots_id_fk
FOREIGN KEY (snapshotId) REFERENCES snapshots (id) ON DELETE CASCADE;
ALTER TABLE Account.snapshotMessageMap DROP FOREIGN KEY snapshotMessageMap_snapshotMessages_id_fk;
ALTER TABLE Account.snapshotMessageMap
  ADD CONSTRAINT snapshotMessageMap_snapshotMessages_id_fk
FOREIGN KEY (messageId) REFERENCES snapshotMessages (id) ON DELETE CASCADE;

-- CASCADE SNAPSHOT RECIPIENTS TABLE
ALTER TABLE Account.snapshotRecipients DROP FOREIGN KEY snapshotRecipients_snapshotMessages_id_fk;
ALTER TABLE Account.snapshotRecipients
  ADD CONSTRAINT snapshotRecipients_snapshotMessages_id_fk
FOREIGN KEY (messageId) REFERENCES snapshotMessages (id) ON DELETE CASCADE;

-- CREATE CLEANUP TASK
DELIMITER //

-- CREATE EVENT TO RUN EVERY DAY AT 00:00
CREATE EVENT `report_cleanup`
  ON SCHEDULE
    EVERY 1 DAY
    -- FORCE TASK TO RUN AT 00:00 DAILY
    STARTS (TIMESTAMP(CURRENT_DATE) + INTERVAL 1 DAY)
  ON COMPLETION PRESERVE
  COMMENT 'Cleans up old report and snapshot data.'
DO BEGIN
  -- DELETE REPORTS (AND ASSOCIATED SNAPSHOT IF ANY) CLOSED > 30 DAYS AGO
  DELETE reports, snapshots FROM reports
    LEFT JOIN reportResults ON reports.id = reportResults.reportId
    LEFT JOIN snapshots ON reports.snapshotId = snapshots.id
  WHERE reportResults.closedTime NOT BETWEEN NOW() - INTERVAL 30 DAY AND NOW();

  -- DELETE SNAPSHOTS NOT LINKED TO REPORT AND OLDER THAN 30 DAYS
  DELETE snapshots FROM snapshots
    LEFT JOIN reports ON snapshots.id = reports.snapshotId
  WHERE reports.id IS NULL
        AND snapshots.created NOT BETWEEN NOW() - INTERVAL 30 DAY AND NOW();

  -- DELETE ORPHANED SNAPSHOT MESSAGES
  DELETE snapshotMessages FROM snapshotMessages
    LEFT JOIN snapshotMessageMap ON snapshotMessages.id = snapshotMessageMap.messageId
  WHERE snapshotMessageMap.snapshotId IS NULL;
END//