namespace LOC.Website.Common
{
    using System.Collections.Generic;
    using Core.Model.Server.GameServer.Dominate.Stats;

    public class DominateStatsDisplay
    {
        public List<DominatePlayerStats> MostPoints { get; set; }
        public List<DominatePlayerStats> MostKills { get; set; }
        public List<DominatePlayerStats> MostAssists { get; set; }
        public List<DominatePlayerStats> MostDeaths { get; set; }
    }
}
