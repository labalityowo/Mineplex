using LOC.Core.Model.GameServer;

namespace LOC.Core.Tokens.Client
{
    public class LoginToken
    {
        public string MacAddress { get; set; }

        public string IpAddress { get; set; }

        public string Name { get; set; }

        public Server Server { get; set; }
    }
}
