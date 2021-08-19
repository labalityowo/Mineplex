namespace LOC.Core.Model.Server.PvpServer
{
    public class FishCatch
    {
        public int FishCatchId { get; set; }

        public string Owner { get; set; }

        public decimal Size { get; set; }

        public string Name { get; set; }

        public virtual Account.Account Catcher { get; set; }
    }
}