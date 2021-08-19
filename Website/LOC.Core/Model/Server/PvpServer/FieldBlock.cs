namespace LOC.Core.Model.Server.PvpServer
{
    public class FieldBlock
    {
        public int FieldBlockId { get; set; }

        public string Server { get; set; }

        public string Location { get; set; }

        public int BlockId { get; set; }

        public byte BlockData { get; set; }

        public int EmptyId { get; set; }

        public byte EmptyData { get; set; }

        public int StockMax { get; set; }

        public double StockRegenTime { get; set; }

        public string Loot { get; set; }
    }
}
