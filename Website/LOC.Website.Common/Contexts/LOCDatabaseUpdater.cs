namespace LOC.Website.Common.Contexts
{
    using System.Data.Entity.Migrations;

    public class LOCDatabaseUpdater
    {
        public void Update()
        {
            var migrator = new DbMigrator(new LocContextConfiguration());
            migrator.Update();
        }
    }
}
