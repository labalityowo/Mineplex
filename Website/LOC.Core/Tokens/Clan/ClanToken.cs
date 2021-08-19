namespace LOC.Core.Tokens.Clan
{
    using System.Collections.Generic;
    using Model.Server.PvpServer.Clan;

    public class ClanToken
    {
        public ClanToken()
        {
            
        }

        public ClanToken(Clan clan)
        {
            ClanId = clan.ClanId;
            Name = clan.Name;
            Description = clan.Description;
            Power = clan.Power;
            Home = clan.Home;
            Admin = clan.Admin;
            DateCreated = clan.DateCreated;
            LastTimeOnline = clan.LastTimeOnline;

            Generator = new ClanGeneratorToken(Name, clan.Generator, clan.GeneratorStock, clan.GeneratorTime);

            Members = new List<ClanMemberToken>();
            foreach (var member in clan.Members)
            {
                Members.Add(new ClanMemberToken(member));
            }

            Alliances = new List<AllianceToken>();
            foreach (var alliance in clan.Alliances)
            {
                Alliances.Add(new AllianceToken(alliance));
            }

            Wars = new List<WarToken>();
            foreach (var war in clan.Wars)
            {
                Wars.Add(new WarToken(war));
            }
            
            Territories = new List<ClanTerritoryToken>();
            foreach (var territory in clan.Territories)
            {
                Territories.Add(new ClanTerritoryToken(territory));
            }
        }

        public int ClanId { get; set; }
        public string Name { get; set; }
        public string Description { get; set; }
        public int Power { get; set; }
        public string Home { get; set; }
        public bool Admin { get; set; }
        public long DateCreated { get; set; }
        public long LastTimeOnline { get; set; }

        public ClanGeneratorToken Generator { get; set; }
        public List<ClanMemberToken> Members { get; set; }
        public List<ClanTerritoryToken> Territories { get; set; }
        public List<AllianceToken> Alliances;
        public List<WarToken> Wars;
    }
}
