namespace LOC.Core.Tokens.Client
{
    public class RankUpdateToken
    {
        public string Name { get; set; }

        public string Rank { get; set; }

        public bool Perm { get; set; }

        public int Retries { get; set; }
    }
}
