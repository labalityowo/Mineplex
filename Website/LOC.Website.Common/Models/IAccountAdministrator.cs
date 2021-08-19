namespace LOC.Website.Common.Models
{
    using System;
    using System.Collections.Generic;
    using Core.Model.Account;
    using Core.Model.Sales;
    using Core.Tokens;
    using Core.Tokens.Client;
    using LOC.Core;

    public interface IAccountAdministrator
    {
        List<String> GetAccountNames();
        List<Account> GetAllAccountsMatching();
        List<Account> GetAllAccountsMatching(string name);
        List<String> GetAllAccountNamesMatching(string name);
        Account GetAccountByName(string name);
        List<AccountNameToken> GetAccounts(AccountBatchToken token);
        Account GetAccountById(int id);
        Account CreateAccount(string name);
        bool GemReward(GemRewardToken token);
        void ApplySalesPackage(SalesPackage salesPackage, int accountId, decimal gross, decimal fee);
        Account Login(LoginRequestToken loginToken);
        void Logout(string name);
        bool ApplyKits(string name);

        PunishmentResponse Punish(PunishToken punish);
        PunishmentResponse RemovePunishment(RemovePunishmentToken ban);
        List<Punishment> GetAdminPunishments(String adminName);

        string PurchaseGameSalesPackage(PurchaseToken token);
        bool AccountExists(string name);
        void SaveCustomBuild(CustomBuildToken token);
        void Ignore(string accountName, string ignoredPlayer);
        void RemoveIgnore(string accountName, string ignoredPlayer);
        string PurchaseUnknownSalesPackage(UnknownPurchaseToken token);
        string UpdateRank(RankUpdateToken token);
        void RemoveBan(RemovePunishmentToken token);

        void UpdateAccountUUIDs(List<AccountNameToken> tokens);

        bool CoinReward(GemRewardToken token);

        ClientToken GetAccountByUUID(string uuid);
    }
}
