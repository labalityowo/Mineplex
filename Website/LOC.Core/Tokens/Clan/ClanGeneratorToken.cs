namespace LOC.Core.Tokens.Clan
{
    public class ClanGeneratorToken
    {
        public ClanGeneratorToken() { }
 
        public ClanGeneratorToken(string name, string generator, int generatorStock, long generatorTime)
        {
            Name = name;
            Location = generator;
            Stock = generatorStock;
            Time = generatorTime;
        }

        public string Name { get; set; }
        public string Location { get; set; }
        public int Stock { get; set; }
        public long Time { get; set; }
    }
}
