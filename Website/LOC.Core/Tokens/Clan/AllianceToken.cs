namespace LOC.Core.Tokens.Clan
{
    using Model.Server.PvpServer.Clan;

    public class AllianceToken
    {
        public AllianceToken()
        {
            
        }

        public AllianceToken(Alliance alliance)
        {
            ClanId = alliance.Clan.ClanId;
            ClanName = alliance.Clan.Name;
            Trusted = alliance.Trusted;
        }

        public int ClanId { get; set; }
        public string ClanName { get; set; }

        public bool Trusted { get; set; }
    }
}