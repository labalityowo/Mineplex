namespace LOC.Core.Model.GameServer
{
    using System.Collections.Generic;

    public class Server
    {
        public int ServerId { get; set; }

        public string Name { get; set; }

        public string ConnectionAddress { get; set; }

        public int PlayerLimit { get; set; }

        public List<ServerHistory> History { get; set; }
    }
}