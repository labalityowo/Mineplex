namespace LOC.Core.Tokens.Client
{
    using System;
    using System.Collections.Generic;
    using Model.Account;
    using Model.Sales;
    using Model.Server.PvpServer;

    public class ClientToken
    {
        public ClientToken()
        {
        }

        public ClientToken(Account account)
        {
            AccountId = account.AccountId;

            FilterChat = account.FilterChat;
            Name = account.Name;
            Uuid = account.Uuid;
            Rank = account.Rank.Name;
            RankPerm = account.RankPerm;
            RankExpire = account.RankExpire.ToShortDateString();

            Time = (long)TimeUtil.GetCurrentMilliseconds();

            EconomyBalance = account.EconomyBalance;

            FishTokens = new List<FishToken>();

            if (account.FishCatches != null)
            {
                foreach (var fishCatch in account.FishCatches)
                {
                    FishTokens.Add(new FishToken(fishCatch));
                }
            }

            AccountToken = new AccountToken(account);
            DonorToken = new DonorToken
                {
                    Gems = account.Gems,
                    Coins = account.Coins,
                    Donated = account.Donated,
                    SalesPackages = new List<int>(),
                    UnknownSalesPackages = new List<string>(),
                    Transactions = new List<AccountTransactionToken>(),
                    CoinRewards = new List<CoinTransactionToken>(),
                    CustomBuilds = new List<CustomBuildToken>(),
                    Pets = new List<PetToken>(),
                    PetNameTagCount = account.PetNameTagCount
                };

            if (account.Clan != null)
            {
                ClanToken = new ClientClanToken
                                {
                                    Name = account.Clan.Name,
                                    Role = account.ClanRole.Name
                                };
            }

            if (account.IgnoredPlayers == null)
                account.IgnoredPlayers = new List<string>();

            IgnoredPlayers = account.IgnoredPlayers;

            if (account.PvpTransactions == null)
                account.PvpTransactions = new List<GameTransaction>();

            foreach (var transaction in account.PvpTransactions)
            {
                DonorToken.SalesPackages.Add(transaction.GameSalesPackageId);
            }

            if (account.AccountTransactions == null)
                account.AccountTransactions = new List<AccountTransaction>();            

            foreach (var transaction in account.AccountTransactions)
            {
                DonorToken.UnknownSalesPackages.Add(transaction.SalesPackageName);
                DonorToken.Transactions.Add(new AccountTransactionToken(transaction));
            }

            if (account.CustomBuilds == null)
                account.CustomBuilds = new List<CustomBuild>();

            foreach (var customBuild in account.CustomBuilds)
            {
                DonorToken.CustomBuilds.Add(new CustomBuildToken(customBuild));
            }

            if (account.Pets == null)
                account.Pets = new List<OwnedPet>();

            foreach (var pet in account.Pets)
            {
                DonorToken.Pets.Add(new PetToken { PetType = pet.PetType, PetName = pet.PetName });
            }

            if (account.Punishments == null)
                account.Punishments = new List<Punishment>();

            Punishments = account.Punishments;
        }

        public int AccountId { get; set; }

        public bool FilterChat { get; set; }

        public string Name { get; set; }

        public string Uuid { get; set; }

        public string Rank { get; set; }

        public bool RankPerm { get; set; }

        public string RankExpire { get; set; }

        public long Time { get; set; }

        public int EconomyBalance { get; set; }

        public List<Punishment> Punishments { get; set; }

        public List<FishToken> FishTokens;

        public List<string> IgnoredPlayers { get; set; }

        public AccountToken AccountToken;
        public DonorToken DonorToken;
        public ClientClanToken ClanToken;
    }
}
