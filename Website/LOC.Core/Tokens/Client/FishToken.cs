namespace LOC.Core.Tokens.Client
{
    using Model.Server.PvpServer;

    public class FishToken
    {
        public FishToken() { }

        public FishToken(FishCatch fishCatch)
        {
            Size = fishCatch.Size;
            Name = fishCatch.Name;
            Catcher = fishCatch.Catcher.Name;
        }

        public decimal Size { get; set; }

        public string Name { get; set; }

        public string Catcher { get; set; }
    }
}
