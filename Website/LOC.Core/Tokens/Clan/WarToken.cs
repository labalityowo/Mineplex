namespace LOC.Core.Tokens.Clan
{
    using Model.Server.PvpServer.Clan;

    public class WarToken
    {
        public WarToken()
        {
            
        }

        public WarToken(War war)
        {
            ClanId = war.Clan.ClanId;
            ClanName = war.Clan.Name;
            Dominance = war.Dominance;
            Ended = war.Ended;
            Cooldown = war.Cooldown;
        }

        public int ClanId { get; set; }
        public string ClanName { get; set; }

        public int Dominance { get; set; }
        public bool Ended { get; set; }
        public long Cooldown { get; set; }
    }
}