namespace LOC.Core.Model.Server.GameServer.MineKart
{
    using Sales;

    public class MineKart
    {
        public int MineKartId { get; set; }

        public string Name { get; set; }

        public string Material { get; set; }

        public string Data { get; set; }

        public GameSalesPackage SalesPackage { get; set; }
    }
}