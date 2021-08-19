namespace LOC.Core.Model.PvpServer
{
    using Sales;

    public class Weapon
    {
        public int WeaponId { get; set; }

        public string Name { get; set; }

        public GameSalesPackage SalesPackage { get; set; }
    }
}
