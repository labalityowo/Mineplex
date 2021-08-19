using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace LOC.Core.Tokens.Client
{
    public class CoinTransactionToken
    {
        public long Date { get; set; }

        public int Amount { get; set; }

        public string Source { get; set; }

        public CoinTransactionToken(Model.Sales.CoinTransaction transaction)
        {
            Amount = transaction.Amount;
            Date = transaction.Date;
            Source = transaction.Source;
        }
    }
}
