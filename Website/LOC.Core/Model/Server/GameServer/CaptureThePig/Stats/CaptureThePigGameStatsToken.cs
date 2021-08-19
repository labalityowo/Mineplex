namespace LOC.Core.Model.Server.GameServer.CaptureThePig.Stats
{
    using System.Collections.Generic;

    public class CaptureThePigGameStatsToken
    {
        public long Length { get; set; }

        public List<CaptureThePigPlayerStatsToken> PlayerStats { get; set; }
    }
}
