namespace LOC.Core.Model.Account
{
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using Sales;
    using Server.GameServer;
    using Server.GameServer.CaptureThePig.Stats;
    using Server.GameServer.Dominate.Stats;
    using Server.PvpServer;
    using Server.PvpServer.Clan;

    public class Account
    {
        public int AccountId { get; set; }

        [StringLength(40)]
        public string Name { get; set; }

        [StringLength(100)]
        public string Uuid { get; set; }

        public Rank Rank { get; set; }
        public bool RankPerm { get; set; }
        public DateTime RankExpire { get; set; }

        public int LoginCount { get; set; }
        public long LastLogin { get; set; }
        public long TotalPlayingTime { get; set; }

        public List<Punishment> Punishments { get; set; }

        public bool FilterChat { get; set; }

        public int Gems { get; set; }
        public int Coins { get; set; }
        public bool Donated { get; set; }

        public DateTime LastVote { get; set; }
        public int VoteModifier { get; set; }

        public int EconomyBalance { get; set; }

        public List<string> IgnoredPlayers { get; set; }

        public virtual Clan Clan { get; set; }
        public virtual ClanRole ClanRole { get; set; }

        public virtual List<OwnedPet> Pets { get; set; }
        public int PetNameTagCount { get; set; }
        public virtual List<FishCatch> FishCatches { get; set; }

        public virtual List<Transaction> Transactions { get; set; }
        public virtual List<GameTransaction> PvpTransactions { get; set; }

        public virtual List<CustomBuild> CustomBuilds { get; set; }

        public virtual List<LoginAddress> IpAddresses { get; set; }
        public virtual List<MacAddress> MacAddresses { get; set; }

        public virtual List<Login> Logins { get; set; }

        public List<DominatePlayerStats> DominateStats { get; set; }
        public List<CaptureThePigPlayerStats> CaptureThePigStats { get; set; }

        public List<AccountTransaction> AccountTransactions { get; set; }
    }
}
