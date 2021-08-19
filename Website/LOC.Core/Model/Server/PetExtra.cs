namespace LOC.Core.Model.Server
{
    using Sales;

    public class PetExtra
    {
        public int PetExtraId { get; set; }

        public string Name { get; set; }

        public string Material { get; set; }

        public GameSalesPackage SalesPackage { get; set; }
    }
}
