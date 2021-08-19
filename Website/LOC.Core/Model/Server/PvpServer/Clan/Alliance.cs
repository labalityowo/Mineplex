namespace LOC.Core.Model.Server.PvpServer.Clan
{
    public class Alliance
    {
        public int AllianceId { get; set; }

        public Clan Clan { get; set; }

        public bool Trusted { get; set; }
    }
}