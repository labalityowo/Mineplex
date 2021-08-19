/**
These queries should only be run once the report-v2 branch has been put fully into production.

This is because this query will break servers which run a version of the Mineplex software which doesn't include
the report-v2 branch.
 */

ALTER TABLE Account.accountPreferences DROP showUserReports;