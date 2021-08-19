namespace LOC.Core.Tokens.Client
{
    using System.Collections.Generic;

    public class DeathStatToken
    {
        public PlayerSetupToken Killer { get; set; }
        public PlayerSetupToken Victim { get; set; }
        public List<PlayerSetupToken> Assistants { get; set; }
    }
}
