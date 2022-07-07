namespace LOC.Website.Common.Models
{
    using System;
    using System.Collections.Generic;
    using System.Data.Entity;
    using System.Linq;
    using Core;
    using Core.Data;
    using Core.Model.Account;
    using Core.Model.Sales;
    using Core.Model.Server.GameServer;
    using Core.Model.Server.PvpServer;
    using Core.Tokens;
    using Core.Tokens.Client;
    using Data;
    using LOC.Website.Common.Contexts;
    using System.Data.Entity.Infrastructure;
    using System.Transactions;
    using System.Runtime.CompilerServices;

    public class AccountAdministrator : IAccountAdministrator
    {
        private readonly INautilusRepositoryFactory _repositoryFactory;
        private readonly IGameServerMonitor _gameServerMonitor;
        private readonly ILogger _logger;

        private static ConditionalWeakTable<string, object> _accountLocks = new ConditionalWeakTable<string, object>();

        public AccountAdministrator(INautilusRepositoryFactory nautilusRepositoryFactory, ILogger logger)
        {
            _repositoryFactory = nautilusRepositoryFactory;
            _gameServerMonitor = GameServerMonitor.Instance;
            _logger = logger;
        }

        public List<String> GetAccountNames()
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                return repository.GetAll<Account>().Select(x => x.Name).ToList();
            }
        }

        public List<Account> GetAllAccountsMatching()
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                return repository.GetAll<Account>().Include(x => x.Rank).ToList();
            }
        }

        public List<Account> GetAllAccountsMatching(string name)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                return repository.GetAll<Account>().Where(c => c.Name == name).Include(x => x.Rank).ToList();
            }
        }

        public List<String> GetAllAccountNamesMatching(string name)
        {
            var accounts = new List<String>();

            using (var repository = _repositoryFactory.CreateRepository())
            {
                var account = repository.Where<Account>(x => x.Name == name).OrderByDescending(y => y.LastLogin).FirstOrDefault();

                if (account != null)
                    accounts.Add(account.Name);
            }

            return accounts;
        }

        /*
        public List<AccountTask> GetTasksByCount(SearchConf searchConf)
        {
            var tasks = new List<AccountTask>();

            try
            {
                using (var repository = _repositoryFactory.CreateRepository())
                {
                    var gameTasks = repository.GetAll<GameTask>().Where(x => x.GameTaskId > searchConf.IdIndex).OrderBy(x => x.GameTaskId).Take(searchConf.Count).Include(x => x.Account).ToList();

                    foreach (var task in gameTasks)
                    {
                        AccountTask accountTask = new AccountTask();
                        accountTask.Id = task.GameTaskId;
                        accountTask.Task = task.TaskName;
                        accountTask.UUID = task.Account.Uuid;

                        tasks.Add(accountTask);
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.Log("ERROR", ex.Message + " : " + ex.StackTrace);
            }

            return tasks;
        }
        */

        private object getAccountLock(string name)
        {
            object lockObject = null;

            if (!_accountLocks.TryGetValue(name, out lockObject))
            {
                lockObject = new object();
                _accountLocks.Add(name, lockObject);
            }

            return lockObject;
        }

        public Account Login(LoginRequestToken loginToken)
        {
            lock (getAccountLock(loginToken.Name))
            {
                using (var repository = _repositoryFactory.CreateRepository())
                {
                    var account = repository.Where<Account>(x => x.Uuid == loginToken.Uuid).FirstOrDefault();
                    
                    if (account == default(Account))
                        account = CreateAccount(loginToken, repository);

                    account.LoadNavigationProperties(repository.Context);
                    account.LastLogin = DateTime.Now.Ticks;

                    if (String.IsNullOrEmpty(account.Uuid))
                        account.Uuid = loginToken.Uuid;

                    // Expire punishments
                    if (account.Punishments != null)
                    {
                        foreach (var expiredPunishment in account.Punishments.Where(x => x.Active && (x.Duration - 0d) > 0 && TimeUtil.GetCurrentMilliseconds() > (x.Time + (x.Duration * 3600000))))
                        {
                            expiredPunishment.Active = false;
                        }
                    }

                    // Insert UUID if not there
                    if (String.IsNullOrEmpty(account.Uuid))
                    {
                        if (!String.IsNullOrEmpty(loginToken.Uuid))
                            account.Uuid = loginToken.Uuid;
                    }

                    // Update account name if changed
                    if (!String.Equals(account.Name, loginToken.Name))
                    {
                        account.Name = loginToken.Name;

                        var oldAccount = repository.Where<Account>(x => x.Name == loginToken.Name).FirstOrDefault();

                        if (oldAccount != null && oldAccount != default(Account))
                        {

                        }
                    }

                    /*
                    // Expire ranks
                    if ((account.Rank.Name == "ULTRA" || account.Rank.Name == "HERO") && !account.RankPerm && DateTime.Now.CompareTo(account.RankExpire) >= 0)
                    {
                        account.Rank = repository.Where<Rank>(x => x.Name == "ALL").First();
                        repository.Attach(account.Rank);
                    }
                        * */

                    repository.Edit(account);
                    repository.CommitChanges();

                    return account;
                }
            }
        }
        
        public void Logout(string name)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                var account = GetAccountByName(name, repository);

                if (account.Logins.Any())
                {
                    account.LastLogin = account.Logins.OrderBy(x => x.Time).Last().Time;
                    account.TotalPlayingTime += DateTime.Now.Subtract(TimeSpan.FromTicks(account.LastLogin)).Ticks;
                }

                repository.CommitChanges();

                _gameServerMonitor.PlayerLoggedOut(account);
            }
        }

        public Account GetAccountByName(string name)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                return GetAccountByName(name, repository);
            }
        }

        public Account GetAccountById(int id)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                return GetAccountByName(repository.GetAll<Account>().First(x => x.AccountId == id).Name, repository);
            }
        }

        public Account CreateAccount(string name)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                var account = repository.Add(new Account
                {
                    Gems = 1200,
                    Coins = 0,
                    Name = name,
                    Rank = repository.Where<Rank>(x => x.RankId == 1).First(),
                    LastVote = DateTime.Today.Subtract(TimeSpan.FromDays(5)),
                    RankExpire = DateTime.Now
                });
                repository.CommitChanges();

                return account;
            }
        }

        public void UpdateAccountUUIDs(List<AccountNameToken> tokens)
        {
            try
            {
                using (var repository = _repositoryFactory.CreateRepository())
                {
                    foreach (AccountNameToken token in tokens)
                    {
                        var account = GetAccountByName(token.Name);
                        account.Uuid = token.UUID;

                        repository.Context.Database.ExecuteSqlCommand("UPDATE dbo.Accounts SET Uuid = {0} WHERE Name = {1}", token.UUID, token.Name);
                    }

                    repository.CommitChanges();
                }
            }
            catch (Exception exception)
            {
                _logger.Log("Error", exception.Message);
            }
        }

        public List<AccountNameToken> GetAccounts(AccountBatchToken token)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                return repository.GetAll<Account>().OrderBy(x => x.AccountId).Skip(token.Start).Take(token.End - token.Start).Select(x => new AccountNameToken { Name = x.Name }).ToList();
            } 
        }

        public bool GemReward(GemRewardToken token)
        {
            lock (getAccountLock(token.Name))
            {
                using (var repository = _repositoryFactory.CreateRepository())
                {
                    var account = repository.Where<Account>(x => x.Name == token.Name).OrderByDescending(x => x.LastLogin).FirstOrDefault();

                    if (account == null)
                        return false;

                    token.OriginalBalance = account.Gems;
                    account.Gems += token.Amount;

                    repository.Edit(account);
                    repository.CommitChanges();
                }

                using (var repository = _repositoryFactory.CreateRepository())
                {
                    var account = repository.Where<Account>(x => x.Name == token.Name).OrderByDescending(x => x.LastLogin).FirstOrDefault();

                    if (account == null)
                        return false;

                    if (account.Gems != token.OriginalBalance + token.Amount)
                        return false;
                }
            }

            return true;
        }

        public bool CoinReward(GemRewardToken token)
        {
            lock (getAccountLock(token.Name))
            {
                using (var repository = _repositoryFactory.CreateRepository())
                {
                    var account = repository.Where<Account>(x => x.Name == token.Name).OrderByDescending(x => x.LastLogin).FirstOrDefault();

                    if (account == null)
                        return false;

                    token.OriginalBalance = account.Coins;
                    account.Coins += token.Amount;

                    repository.Edit(account);
                    repository.CommitChanges();
                }

                using (var repository = _repositoryFactory.CreateRepository())
                {
                    var account = repository.Where<Account>(x => x.Name == token.Name).OrderByDescending(x => x.LastLogin).FirstOrDefault();

                    if (account == null)
                        return false;

                    if (account.Coins != token.OriginalBalance + token.Amount)
                        return false;

                    if (!token.Source.Contains("Earned") && !token.Source.Contains("Tutorial") && !token.Source.Contains("Parkour"))
                    {
                        var coinTransaction = new CoinTransaction
                        {
                            Source = token.Source,
                            Account = account,
                            Amount = token.Amount,
                            Date = (long)TimeUtil.GetCurrentMilliseconds()
                        };

                        repository.Add<CoinTransaction>(coinTransaction);
                        repository.CommitChanges();
                    }
                }
            }

            return true;
        }

        public PunishmentResponse Punish(PunishToken punish)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                var account = repository.Where<Account>(x => x.Name == punish.Target).OrderByDescending(x => x.LastLogin).Include(x => x.Rank).FirstOrDefault();

                if (account == null)
                    return PunishmentResponse.AccountDoesNotExist;

                if (!String.Equals(punish.Admin, "Mineplex Enjin Server"))
                {
                    var punisher =
                        repository.Where<Account>(x => x.Name == punish.Admin).OrderByDescending(x => x.LastLogin).Include(x => x.Rank).FirstOrDefault();

                    if (punisher == null)
                        return PunishmentResponse.NotPunished;
                }

                var punishment = new Punishment
                {
                    UserId = account.AccountId,
                    Admin = punish.Admin,
                    Category = punish.Category,
                    Sentence = punish.Sentence,
                    Time = (long)TimeUtil.GetCurrentMilliseconds(),
                    Reason = punish.Reason,
                    Duration = punish.Duration,
                    Severity = punish.Severity,
                    Active = true
                };

                if (account.Punishments == null)
                    account.Punishments = new List<Punishment>();

                account.Punishments.Add(punishment);
                repository.Edit(account);

                repository.CommitChanges();
            }

            return PunishmentResponse.Punished;
        }

        public PunishmentResponse RemovePunishment(RemovePunishmentToken token)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                var account = repository.Where<Account>(x => x.Name == token.Target).OrderByDescending(x => x.LastLogin).Include(x => x.Punishments).FirstOrDefault();

                if (account == null)
                    return PunishmentResponse.AccountDoesNotExist;

                if (account.Punishments == null || account.Punishments.Count == 0)
                    return PunishmentResponse.NotPunished;

                var activePunishment = account.Punishments.FirstOrDefault(x => x.PunishmentId == token.PunishmentId && x.Active);

                if (activePunishment == null)
                    return PunishmentResponse.NotPunished;

                var punishment = repository.Where<Punishment>(x => x.UserId == account.AccountId && x.PunishmentId == token.PunishmentId && x.Active).First();
                punishment.Active = false;
                punishment.Removed = true;
                punishment.RemoveAdmin = token.Admin;
                punishment.RemoveTime = (long)TimeUtil.GetCurrentMilliseconds();
                punishment.RemoveReason = token.Reason;

                repository.Edit(punishment);

                repository.CommitChanges();
            }

            return PunishmentResponse.PunishmentRemoved;
        }

        public List<Punishment> GetAdminPunishments(String adminName)
        {
            String lowerName = adminName.ToLower();

            using (var repository = _repositoryFactory.CreateRepository())
            {
                return repository.Where<Punishment>(p => p.Admin.ToLower() == lowerName).ToList();
            }
        }

        public string PurchaseGameSalesPackage(PurchaseToken token)
        {
            try
            {
                lock (getAccountLock(token.AccountName))
                {
                    using (var repository = _repositoryFactory.CreateRepository())
                    {
                        var account =
                            repository.Where<Account>(x => x.Name == token.AccountName).OrderByDescending(x => x.LastLogin)
                                      .Include(x => x.PvpTransactions)
                                      .First();

                        var salesPackage =
                            repository.Where<GameSalesPackage>(x => x.GameSalesPackageId == token.SalesPackageId)
                                      .FirstOrDefault();

                        if (account == null || salesPackage == null)
                            return TransactionResponse.Failed.ToString();

                        if (account.Gems < salesPackage.Gems)
                            return TransactionResponse.InsufficientFunds.ToString();

                        var accountTransaction = new GameTransaction
                            {
                                Account = account,
                                GameSalesPackageId = salesPackage.GameSalesPackageId,
                                Gems = salesPackage.Gems,
                            };

                        repository.Attach(account);
                        repository.Edit(account);

                        if (account.PvpTransactions == null)
                            account.PvpTransactions = new List<GameTransaction> { accountTransaction };
                        else
                        {
                            account.PvpTransactions.Add(accountTransaction);
                        }

                        account.Gems -= salesPackage.Gems;

                        repository.CommitChanges();

                        return TransactionResponse.Success.ToString();
                    }
                }
            }
            catch (Exception exception)
            {
                return TransactionResponse.Failed.ToString() + ":" + exception.Message;
            }
        }

        public bool AccountExists(string name)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                return repository.Any<Account>(x => x.Name == name);
            }
        }

        public void SaveCustomBuild(CustomBuildToken token)
        {
            lock (getAccountLock(token.PlayerName))
            {
                using (var repository = _repositoryFactory.CreateRepository())
                {
                    var account =
                        repository.Where<Account>(x => x.Name == token.PlayerName).OrderByDescending(x => x.LastLogin).Include(x => x.CustomBuilds).First();

                    var customBuild =
                        account.CustomBuilds.FirstOrDefault(
                            x => String.Equals(x.PvpClass, token.PvpClass) && x.CustomBuildNumber == token.CustomBuildNumber);

                    if (customBuild == null)
                    {
                        customBuild = repository.Add(token.GetCustomBuild());
                        account.CustomBuilds.Add(customBuild);
                    }
                    else
                    {
                        token.UpdateCustomBuild(customBuild);
                        repository.Edit(customBuild);
                    }

                    if (customBuild.Active)
                    {
                        foreach (
                            var otherClassBuild in
                                account.CustomBuilds.Where(
                                    x =>
                                    String.Equals(x.PvpClass, token.PvpClass) && x.CustomBuildNumber != customBuild.CustomBuildNumber)
                                       .ToList())
                        {
                            otherClassBuild.Active = false;
                            repository.Edit(otherClassBuild);
                        }
                    }

                    repository.Edit(account);

                    repository.CommitChanges();
                }
            }
        }

        public void Ignore(string accountName, string ignoredPlayer)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                var account = repository.Where<Account>(x => x.Name == accountName).OrderByDescending(x => x.LastLogin).First();

                account.IgnoredPlayers.Add(ignoredPlayer);

                repository.CommitChanges();
            }
        }

        public void RemoveIgnore(string accountName, string ignoredPlayer)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                var account = repository.Where<Account>(x => x.Name == accountName).OrderByDescending(x => x.LastLogin).First();

                account.IgnoredPlayers.Remove(ignoredPlayer);

                repository.CommitChanges();
            }
        }

        public string PurchaseUnknownSalesPackage(UnknownPurchaseToken token)
        {
            try
            {
                lock (getAccountLock(token.AccountName))
                {
                    using (var repository = _repositoryFactory.CreateRepository())
                    {
                        var account =
                            repository.Where<Account>(x => x.Name == token.AccountName)
                                      .OrderByDescending(x => x.LastLogin)
                                      .Include(x => x.AccountTransactions)
                                      .First();

                        if (account == null)
                            return TransactionResponse.Failed.ToString();

                        if (token.CoinPurchase ? account.Coins < token.Cost : account.Gems < token.Cost)
                            return TransactionResponse.InsufficientFunds.ToString();

                        var accountTransaction = new AccountTransaction
                        {
                            Account = account,
                            SalesPackageName = token.SalesPackageName,
                            Date = (long)TimeUtil.GetCurrentMilliseconds(),
                            Gems = token.CoinPurchase ? 0 : token.Cost,
                            Coins = token.CoinPurchase ? token.Cost : 0
                        };

                        repository.Attach(account);
                        repository.Edit(account);

                        if (account.AccountTransactions == null)
                            account.AccountTransactions = new List<AccountTransaction> { accountTransaction };
                        else
                        {
                            account.AccountTransactions.Add(accountTransaction);
                        }

                        if (token.CoinPurchase)
                            account.Coins -= token.Cost;
                        else
                            account.Gems -= token.Cost;

                        repository.CommitChanges();

                        return TransactionResponse.Success.ToString();
                    }
                }
            }
            catch (Exception exception)
            {
                return TransactionResponse.Failed.ToString() + ":" + exception.Message;
            }
        }

        public bool ApplyKits(String name)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                var account = repository.Where<Account>(x => String.Equals(x.Name, name)).OrderByDescending(x => x.LastLogin).Include(x => x.Rank).FirstOrDefault();
                account.LoadNavigationProperties(repository.Context);

                addAccountTransaction(repository, account, "Bacon Brawl Bebe Piggles", 0, 0);
                addAccountTransaction(repository, account, "Bacon Brawl `Pig`", 0, 0);
                addAccountTransaction(repository, account, "A Barbarians Life Barbarian Archer", 0, 0);
                addAccountTransaction(repository, account, "A Barbarians Life Bomber", 0, 0);
                addAccountTransaction(repository, account, "The Bridges Archer", 0, 0);
                addAccountTransaction(repository, account, "The Bridges Bomber", 0, 0);
                addAccountTransaction(repository, account, "The Bridges Brawler", 0, 0);
                addAccountTransaction(repository, account, "The Bridges Miner", 0, 0);
                addAccountTransaction(repository, account, "Castle Siege Castle Assassin", 0, 0);
                addAccountTransaction(repository, account, "Castle Siege Castle Brawler", 0, 0);
                addAccountTransaction(repository, account, "Castle Siege Castle Knight", 0, 0);
                addAccountTransaction(repository, account, "Castle Siege Undead Archer", 0, 0);
                addAccountTransaction(repository, account, "Castle Siege Undead Zombie", 0, 0);
                addAccountTransaction(repository, account, "Death Tag Runner Archer", 0, 0);
                addAccountTransaction(repository, account, "Death Tag Runner Traitor", 0, 0);
                addAccountTransaction(repository, account, "Dragon Escape Disruptor", 0, 0);
                addAccountTransaction(repository, account, "Dragon Escape Warper", 0, 0);
                addAccountTransaction(repository, account, "Dragons Marksman", 0, 0);
                addAccountTransaction(repository, account, "Dragons Pyrotechnic", 0, 0);
                addAccountTransaction(repository, account, "Block Hunt Instant Hider", 0, 0);
                addAccountTransaction(repository, account, "Block Hunt Shocking Hider", 0, 0);
                addAccountTransaction(repository, account, "Block Hunt Radar Hunter", 0, 0);
                addAccountTransaction(repository, account, "Block Hunt TNT Hunter", 0, 0);
                addAccountTransaction(repository, account, "Super Paintball Machine Gun", 0, 0);
                addAccountTransaction(repository, account, "Super Paintball Shotgun", 0, 0);
                addAccountTransaction(repository, account, "One in the Quiver Brawler", 0, 0);
                addAccountTransaction(repository, account, "One in the Quiver Enchanter", 0, 0);
                addAccountTransaction(repository, account, "Runner Archer", 0, 0);
                addAccountTransaction(repository, account, "Runner Frosty", 0, 0);
                addAccountTransaction(repository, account, "Sheep Quest Archer", 0, 0);
                addAccountTransaction(repository, account, "Sheep Quest Brute", 0, 0);
                addAccountTransaction(repository, account, "Super Smash Mobs Blaze", 0, 0);
                addAccountTransaction(repository, account, "Super Smash Mobs Chicken", 0, 0);
                addAccountTransaction(repository, account, "Super Smash Mobs Mad Cow", 0, 0);
                addAccountTransaction(repository, account, "Super Smash Mobs Creeper", 0, 0);
                addAccountTransaction(repository, account, "Super Smash Mobs Enderman", 0, 0);
                addAccountTransaction(repository, account, "Super Smash Mobs Undead Knight", 0, 0);
                addAccountTransaction(repository, account, "Super Smash Mobs Magma Cube", 0, 0);
                addAccountTransaction(repository, account, "Super Smash Mobs Pig", 0, 0);
                addAccountTransaction(repository, account, "Super Smash Mobs Skeletal Horse", 0, 0);
                addAccountTransaction(repository, account, "Super Smash Mobs Sky Squid", 0, 0);
                addAccountTransaction(repository, account, "Super Smash Mobs Snowman", 0, 0);
                addAccountTransaction(repository, account, "Super Smash Mobs Witch", 0, 0);
                addAccountTransaction(repository, account, "Super Smash Mobs Wither", 0, 0);
                addAccountTransaction(repository, account, "Super Smash Mobs Wither Skeleton", 0, 0);
                addAccountTransaction(repository, account, "Super Smash Mobs Wolf", 0, 0);
                addAccountTransaction(repository, account, "Snake Super Snake", 0, 0);
                addAccountTransaction(repository, account, "Snake Other Snake", 0, 0);
                addAccountTransaction(repository, account, "Sneaky Assassins Ranged Assassin", 0, 0);
                addAccountTransaction(repository, account, "Sneaky Assassins Revealer", 0, 0);
                addAccountTransaction(repository, account, "Super Spleef Archer", 0, 0);
                addAccountTransaction(repository, account, "Super Spleef Brawler", 0, 0);
                addAccountTransaction(repository, account, "Squid Shooter Squid Blaster", 0, 0);
                addAccountTransaction(repository, account, "Squid Shooter Squid Sniper", 0, 0);
                addAccountTransaction(repository, account, "Survival Games Archer", 0, 0);
                addAccountTransaction(repository, account, "Survival Games Assassin", 0, 0);
                addAccountTransaction(repository, account, "Survival Games Beastmaster", 0, 0);
                addAccountTransaction(repository, account, "Survival Games Bomber", 0, 0);
                addAccountTransaction(repository, account, "Survival Games Brawler", 0, 0);
                addAccountTransaction(repository, account, "Survival Games Necromancer", 0, 0);
                addAccountTransaction(repository, account, "Turf Wars Infiltrator", 0, 0);
                addAccountTransaction(repository, account, "Turf Wars Shredder", 0, 0);
                addAccountTransaction(repository, account, "Zombie Survival Survivor Archer", 0, 0);
                addAccountTransaction(repository, account, "Zombie Survival Survivor Rogue", 0, 0);

                repository.CommitChanges();
            }

            return true;
        }

        public string UpdateRank(RankUpdateToken token)
        {
            Rank rank = null;
            var expire = DateTime.Now.AddMonths(1).AddMilliseconds(-DateTime.Now.Millisecond);

            try
            {
                using (var repository = _repositoryFactory.CreateRepository())
                {
                    var account = repository.Where<Account>(x => String.Equals(x.Name, token.Name)).OrderByDescending(x => x.LastLogin).Include(x => x.Rank).FirstOrDefault();
                    rank = repository.Where<Rank>(x => String.Equals(x.Name, token.Rank)).FirstOrDefault();

                    if (account == null)
                        return "ALL";

                    if (account.Rank.Name == "LT" || account.Rank.Name == "OWNER")
                        return account.Rank.Name;

                    if (rank == null)
                        return account.Rank.ToString();

                    account.Rank = rank;
                    account.RankExpire = expire;
                    account.RankPerm = token.Perm;

                    repository.Edit(account);
                    repository.CommitChanges();

                    _logger.Log("INFO", "TOKEN " + token.Name + "'s rank has been updated to " + token.Rank + " " + (token.Perm ? "Permanently" : "Monthly") + "." + " Rank expire : " + account.RankExpire.ToString());
                }

                using (var repository = _repositoryFactory.CreateRepository())
                {
                    var account = repository.Where<Account>(x => String.Equals(x.Name, token.Name)).OrderByDescending(x => x.LastLogin).Include(x => x.Rank).FirstOrDefault();

                    if (token.Retries >= 3)
                        _logger.Log("ERROR", "Applying UpdateRank, retried 3 times and something didn't stick.");
                    else if (!account.Rank.Name.Equals(token.Rank) || account.RankPerm != token.Perm || account.RankExpire.Equals(expire))
                    {
                        token.Retries++;
                        UpdateRank(token);
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.Log("ERROR", "Applying UpdateRank : " + String.Join("; ", ex.InnerException.InnerException.Message));
            }

            return rank == null ? token.Rank : rank.Name.ToString();
        }

        public void RemoveBan(RemovePunishmentToken token)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                var account = repository.Where<Account>(x => x.Name == token.Target).OrderByDescending(x => x.LastLogin).Include(x => x.Punishments).FirstOrDefault();

                if (account == null)
                    return;

                if (account.Punishments == null || account.Punishments.Count == 0)
                    return;

                var activePunishments = account.Punishments.Where(x => x.Active);

                if (!activePunishments.Any())
                    return;

                foreach (Punishment punishment in activePunishments)
                {
                    punishment.Active = false;
                    punishment.Removed = true;
                    punishment.RemoveAdmin = token.Admin;
                    punishment.RemoveTime = DateTime.Now.Ticks;
                    punishment.RemoveReason = token.Reason;

                    repository.Edit(punishment);
                }

                repository.CommitChanges();
            }
        }

        public void ApplySalesPackage(SalesPackage salesPackage, int accountId, decimal gross, decimal fee)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                var account = repository.Where<Account>(x => x.AccountId == accountId).Include(x => x.Transactions).First();

                var accountTransaction = new LOC.Core.Model.Sales.Transaction {Account = account, SalesPackage = salesPackage, Fee = fee, Profit = (gross - fee), Time = DateTime.Now};

                repository.Attach(salesPackage);

                repository.Attach(account);
                repository.Edit(account);

                if (account.Transactions == null)
                    account.Transactions = new List<LOC.Core.Model.Sales.Transaction>();

                account.Transactions.Add(accountTransaction);
                account.Gems += salesPackage.Gems;
                account.Donated = true;
                account.RankPerm = salesPackage.RankPerm;

                if (salesPackage.Rank.RankId != 1 && !salesPackage.RankPerm)
                {
                    account.Rank = salesPackage.Rank;
                    account.RankExpire = DateTime.Now.AddDays(salesPackage.Length);
                }

                repository.CommitChanges();
            }
        }

        protected Account GetAccountByName(string name, IRepository repository)
        {
            var account = repository.Where<Account>(x => x.Name == name).OrderByDescending(y => y.LastLogin).FirstOrDefault();
            account.LoadNavigationProperties(repository.Context);

            return account;
        }

        protected Account CreateAccount(LoginRequestToken loginToken, IRepository repository)
        {
            var newAccount = new Account
            {
                Name = loginToken.Name,
                Uuid = loginToken.Uuid,
                Rank = repository.Where<Rank>(x => x.RankId == 1).First(),
                Gems = 0,
                Coins = 0,
                Transactions = new List<LOC.Core.Model.Sales.Transaction>(),
                PvpTransactions = new List<GameTransaction>(),
                IpAddresses = new List<LoginAddress>(),
                MacAddresses = new List<MacAddress>(),
                Logins = new List<Login>(),
                CustomBuilds = new List<CustomBuild>(),
                LastVote = DateTime.Today.Subtract(TimeSpan.FromDays(5)),
                RankExpire = DateTime.Now
            };

            newAccount = repository.Add(newAccount);
            repository.CommitChanges();

            newAccount = repository.Where<Account>(x => x.AccountId == newAccount.AccountId)
                                   .Include(x => x.Rank)
                                   .Include(x => x.Punishments)
                                   .Include(x => x.Clan)
                                   .Include(x => x.ClanRole)
                                   .Include(x => x.CustomBuilds)
                                   .Include(x => x.FishCatches)
                                   .Include(x => x.PvpTransactions)
                                   .Include(x => x.Pets)
                                   .First();

            return newAccount;
        }

        protected LoginAddress CreateIpAddress(LoginRequestToken loginToken, IRepository repository)
        {
            var newLoginAddress = new LoginAddress { Address = loginToken.IpAddress };

            newLoginAddress = repository.Add(newLoginAddress);
            repository.CommitChanges();

            return newLoginAddress;
        }

        protected void CreateMacAddress(LoginRequestToken loginToken, IRepository repository)
        {
            var newMacAddress = new MacAddress { Address = loginToken.MacAddress, Accounts = new List<Account>() };

            repository.Add(newMacAddress);
            repository.CommitChanges();
        }

        public ClientToken GetAccountByUUID(string uuid)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                var account = repository.Where<Account>(x => x.Uuid == uuid).FirstOrDefault();

                if (account == null)
                    return null;

                account.LoadNavigationProperties(repository.Context);

                ClientToken clientToken = null;

                clientToken = new ClientToken(account);

                foreach (var trans in repository.Where<CoinTransaction>(x => x.Account.AccountId == account.AccountId).ToList())
                {
                    clientToken.DonorToken.CoinRewards.Add(new CoinTransactionToken(trans));
                }

                return clientToken;
            }
        }

        private void addAccountTransaction(IRepository repository, Account account, string salesPackageName, int gems, int coins)
        {
            var accountTransaction = new AccountTransaction
            {
                Account = account,
                SalesPackageName = salesPackageName,
                Date = (long)TimeUtil.GetCurrentMilliseconds(),
                Gems = gems,
                Coins = coins
            };

            if (account.AccountTransactions == null)
                account.AccountTransactions = new List<AccountTransaction> { accountTransaction };
            else
            {
                account.AccountTransactions.Add(accountTransaction);
            }
        }
    }
}