namespace LOC.Core.Model.Server.PvpServer.Clan
{
    public class War
    {
        public int WarId { get; set; }

        public Clan Clan { get; set; }

        public int Dominance { get; set; }

        public bool Ended { get; set; }

        public long Cooldown { get; set; }
    }
}
