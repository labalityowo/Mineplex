namespace LOC.Core.Model.Server
{
    using Sales;

    public class Pet
    {
        public int PetId { get; set; }

        public string Name { get; set; }

        public string PetType { get; set; }

        public GameSalesPackage SalesPackage { get; set; }
    }
}
