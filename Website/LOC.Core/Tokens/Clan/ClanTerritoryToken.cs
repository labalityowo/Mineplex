namespace LOC.Core.Tokens.Clan
{
    using Model.Server.PvpServer.Clan;

    public class ClanTerritoryToken
    {
        public ClanTerritoryToken()
        {
            
        }

        public ClanTerritoryToken(Territory territory)
        {
            ClanId = territory.Clan.ClanId;
            ClanName = territory.Clan.Name;
            ServerName = territory.ServerName;
            Chunk = territory.Chunk;
            Safe = territory.Safe;
        }

        public int ClanId { get; set; }
        public string ClanName { get; set; }
        public string ServerName { get; set; }
        public string Chunk { get; set; }
        public bool Safe { get; set; }
    }
}