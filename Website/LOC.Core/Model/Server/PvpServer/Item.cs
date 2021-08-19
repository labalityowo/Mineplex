namespace LOC.Core.Model.PvpServer
{
    using Sales;

    public class Item
    {
        public int ItemId { get; set; }

        public string Name { get; set; }

        public string Material { get; set; }

        public GameSalesPackage SalesPackage { get; set; }
    }
}
