namespace LOC.Core.Model.Sales
{
    public class AccountTransaction
    {
        public int AccountTransactionId { get; set; }

        public Account.Account Account { get; set; }

        public long Date { get; set; }

        public string SalesPackageName { get; set; }

        public int Gems { get; set; }

        public int Coins { get; set; }
    }
}
