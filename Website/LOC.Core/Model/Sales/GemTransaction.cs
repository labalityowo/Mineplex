namespace LOC.Core.Model.Sales
{
    public class GemTransaction
    {
        public int GemTransactionId { get; set; }

        public Account.Account Account { get; set; }

        public long? Date { get; set; }

        public string Source { get; set; }

        public int Amount { get; set; }
    }
}
