namespace LOC.Website.Common.Models
{
    using System;
    using System.Collections.Generic;
    using System.Data.Entity;
    using System.Linq;
    using Core.Data;
    using Core.Model.Account;
    using Core.Model.GameServer.Stats;
    using Core.Model.Server.GameServer.Dominate.Stats;
    using Core.Tokens.Client;
    using Data;

    public class DominateAdministrator : PvpAdministrator, IDominateAdministrator
    {
        public DominateAdministrator(INautilusRepositoryFactory repositoryFactory) : base(repositoryFactory) { }

        public List<GemRewardToken> UploadStats(DominateGameStatsToken token)
        {
            var updateTokenList = new List<GemRewardToken>();
            var timeDifference = token.Duration - ((long)new TimeSpan(DateTime.Now.ToUniversalTime().Ticks - new DateTime(1970, 1, 1).Ticks).TotalMilliseconds) - new TimeSpan(0, 15, 0).TotalMilliseconds;
            var direction = timeDifference / Math.Abs(timeDifference);
            var pointsAccordingToDuration = Math.Min(Math.Abs(timeDifference), 5) * direction + 35;

            using (var repository = RepositoryFactory.CreateRepository())
            {
                foreach (var playerStats in token.PlayerStats)
                {
                    var account = repository.Where<Account>(x => x.Name == playerStats.Name).Include(x => x.DominateStats).First(); ;
                    var earnedPoints = (int)pointsAccordingToDuration;

                    UpdateStatsForAccount(repository, account, earnedPoints, playerStats);

                    updateTokenList.Add(new GemRewardToken { Name = account.Name, Amount = earnedPoints });
                }

                repository.CommitChanges();
            }

            return updateTokenList;
        }

        private void UpdateStatsForAccount(IRepository repository, Account account, int earnedPoints, DominatePlayerStatsToken playerStats)
        {
            account.Gems += earnedPoints;

            if (account.DominateStats == null)
            {
                account.DominateStats = new List<DominatePlayerStats>();
            }

            System.Diagnostics.Debug.WriteLine("Dominate Stats : " + account.DominateStats.Count);

            if (!account.DominateStats.Exists(x => x.Type == "Week"))
            {
                var stats = new DominatePlayerStats();
                stats.Type = "Week";

                account.DominateStats.Add(stats);
                System.Diagnostics.Debug.WriteLine("No Week");
            }

            if (!account.DominateStats.Exists(x => x.Type == "Month"))
            {
                var stats = new DominatePlayerStats();
                stats.Type = "Month";

                account.DominateStats.Add(stats);
                System.Diagnostics.Debug.WriteLine("No Month");
            }

            if (!account.DominateStats.Exists(x => x.Type == "All"))
            {
                var stats = new DominatePlayerStats();
                stats.Type = "All";

                account.DominateStats.Add(stats);
                System.Diagnostics.Debug.WriteLine("No All");
            }

            var weeklyStats = account.DominateStats.FirstOrDefault(x => x.Type == "Week");
            var monthStats = account.DominateStats.FirstOrDefault(x => x.Type == "Month");
            var allStats = account.DominateStats.FirstOrDefault(x => x.Type == "All");

            UpdateStats(weeklyStats, playerStats);
            UpdateStats(monthStats, playerStats);
            UpdateStats(allStats, playerStats);

            repository.Edit(account);

            System.Diagnostics.Debug.WriteLine("Done with stats.");
        }

        private void UpdateStats(DominatePlayerStats statToUpdate, DominatePlayerStatsToken statAdding)
        {
            statToUpdate.Kills += statAdding.PlayerStats.Kills;
            statToUpdate.Assists += statAdding.PlayerStats.Assists;
            statToUpdate.Deaths += statAdding.PlayerStats.Deaths;
            statToUpdate.Points += statAdding.PlayerStats.Points;

            if (statAdding.Won)
                statToUpdate.Wins += 1;
            else
                statToUpdate.Losses += 1;
        }
    }
}
