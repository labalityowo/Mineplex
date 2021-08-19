using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace LOC.Core.Tokens.Client
{
    public class AccountTransactionToken
    {
        public long Date { get; set; }

        public string SalesPackageName { get; set; }

        public int Gems { get; set; }

        public int Coins { get; set; }

        public AccountTransactionToken(Model.Sales.AccountTransaction transaction)
        {
            SalesPackageName = transaction.SalesPackageName;
            Date = transaction.Date;
            Gems = transaction.Gems;
            Coins = transaction.Coins;
        }
    }
}
