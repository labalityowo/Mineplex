namespace LOC.Core.Model.Sales
{
    public class GameTransaction
    {
        public int GameTransactionId { get; set; }

        public Account.Account Account { get; set; }

        public int GameSalesPackageId { get; set; }

        public int Gems { get; set; }

        public int Economy { get; set; }
    }
}