namespace LOC.Website.Common.Contexts
{
    using System;
    using System.Data.Entity.Migrations;
    using System.Linq;
    using Core;
    using Core.GameServer;
    using Core.Model.Account;
    using Core.Model.Server.PvpServer.Clan;

    internal sealed class LocContextConfiguration : DbMigrationsConfiguration<LocContext>
    {
        public LocContextConfiguration()
        {
            AutomaticMigrationsEnabled = true;
            AutomaticMigrationDataLossAllowed = true;
        }

        protected override void Seed(LocContext context)
        {
            AddDefaultRanks(context);
            AddDefaultServerStatuses(context);
            AddDefaultLogEntry(context);
            AddDefaultClanRoles(context);

            base.Seed(context);
        }

        private static void AddDefaultServerStatuses(LocContext context)
        {
            var serverStatuses = context.ServerStatuses.ToList();

            if (!serverStatuses.Any())
            {
                context.ServerStatuses.Add(new ServerStatus { Name = "Offline" });
                context.ServerStatuses.Add(new ServerStatus { Name = "Online" });

                context.SaveChanges();
            }
        }

        private static void AddDefaultRanks(LocContext context)
        {
            var ranks = context.Ranks.ToList();

            if (!ranks.Any())
            {
                context.Ranks.Add(new Rank { Name = "ALL" });
                context.Ranks.Add(new Rank { Name = "ULTRA" });
                context.Ranks.Add(new Rank { Name = "HELPER" });
                context.Ranks.Add(new Rank { Name = "MODERATOR" });
                context.Ranks.Add(new Rank { Name = "ADMIN" });
                context.Ranks.Add(new Rank { Name = "DEVELOPER" });
                context.Ranks.Add(new Rank { Name = "OWNER" });

                context.SaveChanges();
            }
        }

        private static void AddDefaultClanRoles(LocContext context)
        {
            var clanRoles = context.ClanRoles.ToList();

            if (!clanRoles.Any())
            {
                context.ClanRoles.Add(new ClanRole { Name = "NONE" });
                context.ClanRoles.Add(new ClanRole { Name = "RECRUIT" });
                context.ClanRoles.Add(new ClanRole { Name = "MEMBER" });
                context.ClanRoles.Add(new ClanRole { Name = "ADMIN" });
                context.ClanRoles.Add(new ClanRole { Name = "LEADER" });

                context.SaveChanges();
            }
        }

        private void AddDefaultLogEntry(LocContext context)
        {
            //context.LogEntries.Add(new LogEntry {Date = DateTime.Now, Category = "Log", Message = "test"});
            //context.SaveChanges();
        }
    }
}
