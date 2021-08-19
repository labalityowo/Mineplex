namespace LOC.Website.Common.Models
{
    using System.Collections.Generic;
    using Core.Model.Server.GameServer.CaptureThePig.Stats;
    using Core.Tokens.Client;

    public interface ICaptureThePigAdministrator : IPvpAdministrator
    {
        List<GemRewardToken> UploadStats(CaptureThePigGameStatsToken token);
    }
}
