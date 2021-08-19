namespace LOC.Core.Model.Server.GameServer.CaptureThePig.Stats
{
    public class CaptureThePigPlayerStatsToken
    {
        public string Name { get; set; }

        public bool Won { get; set; }

        public long TimePlayed { get; set; }

        public CaptureThePigPlayerStats PlayerStats { get; set; }
    }
}
