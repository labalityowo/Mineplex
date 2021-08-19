namespace LOC.Core.Model.Server.GameServer.Dominate.Stats
{
    public class DominatePlayerStats
    {
        public int DominatePlayerStatsId { get; set; }

        public string Type { get; set; }

        public long Start { get; set; }

        public int Points { get; set; }

        public int Kills { get; set; }

        public int Deaths { get; set; }

        public int Assists { get; set; }

        public int Wins { get; set; }

        public int Losses { get; set; }
    }
}
