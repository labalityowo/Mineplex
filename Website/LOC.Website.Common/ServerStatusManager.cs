namespace LOC.Website.Common
{
    using System.Collections.Generic;
    using System.Linq;
    using System.Timers;
    using LobbyProxy;

    public class ServerStatusManager
    {
        public static ServerStatusManager Instance = new ServerStatusManager();

        private readonly Timer _statusTimer;

        private static object _statusUpdateLock = new object();

        private readonly HashSet<string> _serverAddresses;
        private readonly List<ServerStatus> _serverStatuses;

        protected ServerStatusManager()
        {
            _statusTimer = new Timer(5000);
            _statusTimer.Elapsed += StatusTick;

            _serverAddresses = new HashSet<string>();
            _serverStatuses = new List<ServerStatus>();
        }

        public List<string> GetServers()
        {
            lock (_statusUpdateLock)
            {
                return _serverAddresses.ToList();
            }
        }

        public void RegisterServer(string address)
        {
            lock (_statusUpdateLock)
            {
                _serverAddresses.Add(address);
            }
        }

        public void RemoveServer(string address)
        {
            lock (_statusUpdateLock)
            {
                _serverAddresses.Remove(address);
            }
        }

        private void StatusTick(object sender, ElapsedEventArgs e)
        {
            lock (_statusUpdateLock)
            {
                _serverStatuses.Clear();

                /*
                _serverStatuses.Add(GetStatus("Dominate", "/Content/Images/Dominate.png", "dom.bettermc.com", 25565));
                _serverStatuses.Add(GetStatus("Dominate2", "/Content/Images/Dominate.png", "dom2.bettermc.com", 25565));
                _serverStatuses.Add(GetStatus("Tutorial", "/Content/Images/Dominate.png", "tut.bettermc.com", 25565));
                //_serverStatuses.Add(GetStatus("CaptureThePig", "/Content/Images/CaptureThePig.png", "ctp.bettermc.com", 25565));
                */
                _serverStatuses.Add(GetStatus("BetterMC Hub", "/Content/Images/Pvp.png", "bettermc.com", 25565));
            }
        }

        private ServerStatus GetStatus(string name, string image, string ipAddress, int port)
        {
            var pingResult = PingResult.Ping(ipAddress, port);
            return new ServerStatus
                {
                    Name = name,
                    Image = image,
                    IpAddress = ipAddress,
                    MaxPlayers = pingResult.MaxPlayers,
                    Players = pingResult.Players,
                    Success = pingResult.Success
                };
        }
    }
}
