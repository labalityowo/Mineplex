namespace LOC.Core.Model.GameServer.Stats
{
    using System.Collections.Generic;
    using Model.Server.GameServer.Dominate.Stats;

    public class DominateGameStatsToken
    {
        public List<DominatePlayerStatsToken> PlayerStats { get; set; }

        public long Duration { get; set; }
    }
}
