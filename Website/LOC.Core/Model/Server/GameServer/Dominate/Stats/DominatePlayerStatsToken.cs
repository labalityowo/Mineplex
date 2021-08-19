namespace LOC.Core.Model.Server.GameServer.Dominate.Stats
{
    public class DominatePlayerStatsToken
    {
        public string Name { get; set; }

        public bool Won { get; set; }

        public long TimePlayed { get; set; }

        public DominatePlayerStats PlayerStats { get; set; }
    }
}
