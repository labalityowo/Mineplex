ALTER TABLE Account.reports ADD region VARCHAR(5) NULL;
ALTER TABLE Account.reports
  MODIFY COLUMN region VARCHAR(5) AFTER categoryId;