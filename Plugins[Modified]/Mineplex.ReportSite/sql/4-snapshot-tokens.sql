ALTER TABLE Account.snapshots ADD token CHAR(8);
CREATE UNIQUE INDEX snapshots_token_uindex ON snapshots (token);
ALTER TABLE Account.snapshots
  MODIFY COLUMN creator INT(11) AFTER token;