namespace LOC.Core.Model.Sales
{
    using System;

    public class Transaction
    {
        public int TransactionId { get; set; }

        public Account.Account Account { get; set; }

        public SalesPackage SalesPackage { get; set; }

        public decimal Fee { get; set; }

        public decimal Profit { get; set; }

        public DateTime Time { get; set; }
    }
}