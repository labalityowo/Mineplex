namespace LOC.Website.Common.Models
{
    using System.Collections.Generic;
    using Core.Model.GameServer.Stats;
    using Core.Tokens.Client;

    public interface IDominateAdministrator : IPvpAdministrator
    {
        List<GemRewardToken> UploadStats(DominateGameStatsToken token);
    }
}
