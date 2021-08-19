namespace LOC.Core.Model.GameServer
{
    using System.Collections.Generic;
    using Tokens.Client;

    public class ServerUpdate
    {
        public ServerUpdate()
        {
            ClientTokens = new List<ClientToken>();
        }

        public List<ClientToken> ClientTokens { get; set; }
    }
}
