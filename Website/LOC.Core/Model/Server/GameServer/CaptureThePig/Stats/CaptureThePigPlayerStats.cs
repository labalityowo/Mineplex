namespace LOC.Core.Model.Server.GameServer.CaptureThePig.Stats
{
    public class CaptureThePigPlayerStats
    {
        public int CaptureThePigPlayerStatsId { get; set; }

        public int AccountId { get; set; }

        public string Type { get; set; }

        public long Start { get; set; }

        public int Wins { get; set; }

        public int Losses { get; set; }

        public int Kills { get; set; }

        public int Deaths { get; set; }

        public int Assists { get; set; }

        public int Captures { get; set; }
    }
}
