namespace LOC.Website.Common.Models
{
    using System;
    using System.Collections.Generic;
    using System.Data.Entity;
    using System.Linq;
    using Core.Data;
    using Core.Model.Account;
    using Core.Model.Server.GameServer.CaptureThePig.Stats;
    using Core.Tokens.Client;
    using Data;

    public class CaptureThePigAdministrator : PvpAdministrator, ICaptureThePigAdministrator
    {
        public CaptureThePigAdministrator(INautilusRepositoryFactory repositoryFactory) : base(repositoryFactory) { }
       
        public List<GemRewardToken> UploadStats(CaptureThePigGameStatsToken token)
        {
            var updateTokenList = new List<GemRewardToken>();
            var pointsPerMinute = Math.Min(2 / (token.Length / 60000), 2);

            using (var repository = RepositoryFactory.CreateRepository())
            {               
                foreach (var playerStats in token.PlayerStats)
                {                    
                    var account = repository.Where<Account>(x => x.Name == playerStats.Name).Include(x => x.CaptureThePigStats).First();;
                    var earnedPoints = (int)((playerStats.TimePlayed / 60000) * (pointsPerMinute + (playerStats.Won ? 5 : 0)));

                    UpdateStatsForAccount(repository, account, earnedPoints, playerStats);

                    updateTokenList.Add(new GemRewardToken { Name = account.Name, Amount = earnedPoints });
                }

                repository.CommitChanges();
            }

            return updateTokenList;
        }

        private void UpdateStatsForAccount(IRepository repository, Account account, int earnedPoints, CaptureThePigPlayerStatsToken playerStats)
        {
            account.Gems += earnedPoints;

            if (account.CaptureThePigStats == null)
            {
                account.CaptureThePigStats = new List<CaptureThePigPlayerStats>();
            }

            var weeklyStats = account.CaptureThePigStats.FirstOrDefault(x => x.Type == "Week");
            var monthStats = account.CaptureThePigStats.FirstOrDefault(x => x.Type == "Month");
            var allStats = account.CaptureThePigStats.FirstOrDefault(x => x.Type == "All");

            if (weeklyStats == null)
            {
                weeklyStats = new CaptureThePigPlayerStats();
                weeklyStats.Type = "Week";

                account.CaptureThePigStats.Add(weeklyStats);
            }

            if (monthStats == null)
            {
                monthStats = new CaptureThePigPlayerStats();
                monthStats.Type = "Month";

                account.CaptureThePigStats.Add(monthStats);
            }

            if (allStats == null)
            {
                allStats = new CaptureThePigPlayerStats();
                allStats.Type = "All";

                account.CaptureThePigStats.Add(allStats);
            }


            UpdateStats(weeklyStats, playerStats);
            repository.Edit(weeklyStats);

            UpdateStats(monthStats, playerStats);
            repository.Edit(monthStats);

            UpdateStats(allStats, playerStats);
            repository.Edit(allStats);

            repository.Edit(account);
        }

        private void UpdateStats(CaptureThePigPlayerStats statToUpdate, CaptureThePigPlayerStatsToken statAdding)
        {
            statToUpdate.Captures += statAdding.PlayerStats.Captures;
            statToUpdate.Kills += statAdding.PlayerStats.Kills;
            statToUpdate.Assists += statAdding.PlayerStats.Assists;
            statToUpdate.Deaths += statAdding.PlayerStats.Deaths;

            if (statAdding.Won)
                statToUpdate.Wins += 1;
            else
                statToUpdate.Losses += 1;
        }
    }
}
