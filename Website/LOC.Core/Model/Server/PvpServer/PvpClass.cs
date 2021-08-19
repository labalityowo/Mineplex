namespace LOC.Core.Model.PvpServer
{
    using Sales;

    public class PvpClass
    {
        public int PvpClassId { get; set; }

        public string Name { get; set; }

        public GameSalesPackage SalesPackage { get; set; }
    }
}
