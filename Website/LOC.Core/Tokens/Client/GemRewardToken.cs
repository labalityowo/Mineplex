namespace LOC.Core.Tokens.Client
{
    public class GemRewardToken
    {
        public int OriginalBalance;

        public string Name { get; set; }

        public string Source { get; set; }

        public int Amount { get; set; }

        public int Retries { get; set; }
    }
}