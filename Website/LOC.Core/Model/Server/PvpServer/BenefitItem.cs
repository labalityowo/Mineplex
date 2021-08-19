namespace LOC.Core.Model.Server.PvpServer
{
    using Sales;

    public class BenefitItem
    {
        public int BenefitItemId { get; set; }

        public string Name { get; set; }

        public string Material { get; set; }

        public GameSalesPackage SalesPackage { get; set; }
    }
}
