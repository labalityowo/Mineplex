namespace LOC.Core.Model.Server.PvpServer.Clan
{
    using Account;
    using System.Collections.Generic;

    public class Clan
    {
        public int ClanId { get; set; }
        public string Name { get; set; }
        public string Description { get; set; }
        public int Power { get; set; }
        public string Home { get; set; }
        public bool Admin { get; set; }
        public long DateCreated { get; set; }
        public long LastTimeOnline { get; set; }

        public string Generator { get; set; }
        public int GeneratorStock { get; set; }
        public long GeneratorTime { get; set; }

        public List<Alliance> Alliances { get; set; }
        public List<War> Wars { get; set; }
        public List<Territory> Territories { get; set; }
        public List<Account> Members { get; set; } 
    }
}