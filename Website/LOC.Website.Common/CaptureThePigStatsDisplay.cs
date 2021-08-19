namespace LOC.Website.Common
{
    using System.Collections.Generic;
    using Core.Model.Server.GameServer.CaptureThePig.Stats;

    public class CaptureThePigStatsDisplay
    {
        public List<CaptureThePigPlayerStats> MostPoints { get; set; }
        public List<CaptureThePigPlayerStats> MostKills { get; set; }
        public List<CaptureThePigPlayerStats> MostAssists { get; set; }
        public List<CaptureThePigPlayerStats> MostDeaths { get; set; }
    }
}
