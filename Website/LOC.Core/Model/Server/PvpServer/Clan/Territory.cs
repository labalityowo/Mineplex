namespace LOC.Core.Model.Server.PvpServer.Clan
{
    public class Territory
    {
        public int TerritoryId { get; set; }

        public virtual Clan Clan { get; set; }

        public string ServerName { get; set; }

        public string Chunk { get; set; }

        public bool Safe { get; set; }
    }
}
