using LOC.Core.Model.GameServer;

namespace LOC.Core.Tokens.Client
{
    public class LoginRequestToken
    {
        public string MacAddress { get; set; }

        public string IpAddress { get; set; }

        public string Name { get; set; }

        public string Uuid { get; set; }

        public Server Server { get; set; }
    }
}
