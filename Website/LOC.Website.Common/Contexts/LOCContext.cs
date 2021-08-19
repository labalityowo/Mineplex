using LOC.Core.Model.PvpServer;

namespace LOC.Website.Common.Contexts
{
    using System.Data.Entity;
    using Core;
    using Core.GameServer;
    using Core.Model.Account;
    using Core.Model.GameServer;
    using Core.Model.Sales;
    using Core.Model.Server;
    using Core.Model.Server.GameServer.CaptureThePig.Stats;
    using Core.Model.Server.GameServer.Dominate.Stats;
    using Core.Model.Server.GameServer.MineKart;
    using Core.Model.Server.PvpServer;
    using Core.Model.Server.PvpServer.Clan;

    public class LocContext : DbContext
    {
        public DbSet<Account> Accounts { get; set; }
        public DbSet<Punishment> Bans { get; set; }
        public DbSet<RemovedPunishment> Unbans { get; set; }
        public DbSet<Login> Logins { get; set; }
        public DbSet<LoginAddress> IpAddresses { get; set; }
        public DbSet<MacAddress> MacAddresses { get; set; }
        public DbSet<Rank> Ranks { get; set; }
        public DbSet<GameSalesPackage> PvpSalesPackages { get; set; }
        public DbSet<PvpClass> PvpClasses { get; set; }
        public DbSet<BenefitItem> BenefitItems { get; set; }
        public DbSet<Weapon> Weapons { get; set; }
        public DbSet<Skill> Skills { get; set; }
        public DbSet<Item> Items { get; set; }
        public DbSet<Pet> Pets { get; set; }
        public DbSet<PetExtra> PetExtras { get; set; }
        public DbSet<Server> Servers { get; set; }
        public DbSet<ServerHistory> ServerHistory { get; set; }
        public DbSet<ServerStatus> ServerStatuses { get; set; }
        public DbSet<Transaction> Transactions { get; set; }
        public DbSet<SalesPackage> SalesPackages { get; set; }
        public DbSet<GameTransaction> PvpTransactions { get; set; }

        public DbSet<DominatePlayerStats> DominatePlayerStats { get; set; }
        public DbSet<CaptureThePigPlayerStats> CaptureThePigPlayerStats { get; set; }

        public DbSet<GemTransaction> GemTransactions { get; set; }
        public DbSet<CoinTransaction> CoinTransactions { get; set; }

        public DbSet<MineKart> MineKarts { get; set; }

        public DbSet<Clan> Clans { get; set; }
        public DbSet<ClanRole> ClanRoles { get; set; }
        public DbSet<Territory> ClanTerritories { get; set; }
        public DbSet<Alliance> Alliances { get; set; }
        public DbSet<War> Wars { get; set; }

        public DbSet<FieldBlock> Fields { get; set; }
        public DbSet<FieldOre> FieldOres { get; set; }
        public DbSet<FieldMonster> FieldMonsters { get; set; }

        public DbSet<FishCatch> FishCatches { get; set; }

        public DbSet<FilteredWord> FilteredWords { get; set; }

        public DbSet<LogEntry> Log { get; set; }

        protected override void OnModelCreating(DbModelBuilder modelBuilder)
        {
            modelBuilder.Entity<Account>()
                        .HasMany(x => x.DominateStats)
                        .WithMany();

            modelBuilder.Entity<Account>()
                        .HasMany(x => x.IpAddresses)
                        .WithMany();

            modelBuilder.Entity<Account>()
                        .HasMany(x => x.CaptureThePigStats)
                        .WithRequired();

            modelBuilder.Entity<Account>()
                        .HasMany(x => x.MacAddresses)
                        .WithMany();

            modelBuilder.Entity<Account>()
                        .HasOptional(x => x.Clan)
                        .WithMany(x => x.Members);

            modelBuilder.Entity<Clan>()
                        .HasMany(x => x.Alliances)
                        .WithMany();

            modelBuilder.Entity<Clan>()
                        .HasMany(x => x.Wars)
                        .WithMany();

            modelBuilder.Entity<Clan>()
                        .HasMany(x => x.Territories)
                        .WithRequired(x => x.Clan);
        }
    }
}  
