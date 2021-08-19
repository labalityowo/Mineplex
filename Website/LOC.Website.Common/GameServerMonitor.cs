namespace LOC.Website.Common
{
    using System;
    using System.Collections.Generic;
    using System.Data.Entity;
    using System.Linq;
    using System.Timers;
    using Contexts;
    using Core.Model.Account;
    using Core.Model.GameServer;
    using Core.Tokens.Client;

    public class GameServerMonitor : IGameServerMonitor
    {
        public static GameServerMonitor Instance = new GameServerMonitor();

        private readonly Dictionary<int, DisplayServer> _displayServers;
        private readonly Dictionary<int, ServerHistory> _activeServerStatuses;
        private readonly Dictionary<int, ServerUpdate> _activeServerUpdates;
        private readonly Dictionary<int, int> _accountServerMapping;

        private readonly Timer _cleanUpTimer;

        protected GameServerMonitor()
        {
            _displayServers = new Dictionary<int, DisplayServer>();
            _activeServerStatuses = new Dictionary<int, ServerHistory>();
            _activeServerUpdates = new Dictionary<int, ServerUpdate>();
            _accountServerMapping = new Dictionary<int, int>();

            _cleanUpTimer = new Timer();
            _cleanUpTimer.Interval = 60000;
            _cleanUpTimer.Elapsed += CleanUpTick;
            _cleanUpTimer.AutoReset = true;
        }

        public List<DisplayServer> ServerStatuses { get { return _displayServers.Values.ToList(); } }

        public void Track(Server server)
        {
            ClearServerUpdates(server.ServerId);

            _displayServers.Remove(server.ServerId);
            _displayServers.Add(server.ServerId, new DisplayServer(server, new ServerHistory(), (long)new TimeSpan(DateTime.Now.ToUniversalTime().Ticks - new DateTime(1970, 1, 1).Ticks).TotalMilliseconds));

            if (!_cleanUpTimer.Enabled)
            {
                _cleanUpTimer.Enabled = true;
                _cleanUpTimer.Start();
            }
        }

        public void UnTrack(Server server)
        {
            _activeServerUpdates.Remove(server.ServerId);
            _displayServers.Remove(server.ServerId);
        }

        public ServerUpdate GetServerUpdate(int serverId)
        {
            ServerUpdate serverUpdates;
            _activeServerUpdates.TryGetValue(serverId, out serverUpdates);
            
            return serverUpdates;
        }

        public ServerUpdate GetServerUpdatesFor(int serverId)
        {
            return GetServerUpdate(serverId);
        }

        public void ClearServerUpdates(int serverId)
        {
            _activeServerUpdates.Remove(serverId);
            _activeServerUpdates.Add(serverId, new ServerUpdate());
        }
        
        public void AccountChanged(int accountId)
        {
            if (_accountServerMapping.ContainsKey(accountId))
            {
                int serverId;
                if (_accountServerMapping.TryGetValue(accountId, out serverId))
                {
                    var serverUpdates = GetServerUpdate(serverId);

                    if (serverUpdates != null)
                    {
                        using (var context = new LocContext())
                        {
                            serverUpdates.ClientTokens.Add(new ClientToken(context.Accounts.Include(x => x.Rank).Include(x => x.PvpTransactions).Single(x => x.AccountId == accountId)));
                        }
                    }
                }
            }
        }

        public void PlayerLoggedIn(Account account, int serverId)
        {
            _accountServerMapping.Remove(account.AccountId);
            _accountServerMapping.Add(account.AccountId, serverId);
        }

        public void PlayerLoggedOut(Account account)
        {
            int serverId;

            if (_accountServerMapping.TryGetValue(account.AccountId, out serverId))
            {
                _accountServerMapping.Remove(account.AccountId);

                ServerUpdate serverUpdates;
                _activeServerUpdates.TryGetValue(serverId, out serverUpdates);

                if (serverUpdates != null && serverUpdates.ClientTokens.Any(x => x.AccountId == account.AccountId))
                {
                    serverUpdates.ClientTokens.Remove(serverUpdates.ClientTokens.First(x  => x.AccountId == account.AccountId));

                    _activeServerUpdates.Remove(serverId);
                    _activeServerUpdates.Add(serverId, serverUpdates);
                }
            }
        }

        public void WikiItemChanged(int itemWikiId)
        {
            //throw new NotImplementedException();
        }

        public void WikiPlayerChanged(int playerWikiId)
        {
            //throw new NotImplementedException();
        }

        public void UpdateServerStatus(ServerHistory serverHistory)
        {
            _activeServerStatuses.Remove(serverHistory.ServerId);
            _activeServerStatuses.Add(serverHistory.ServerId, serverHistory);

            if (_displayServers.Any(x => x.Key == serverHistory.ServerId))
            {
                DisplayServer displayServer;
                _displayServers.TryGetValue(serverHistory.ServerId, out displayServer);

                displayServer.Update(serverHistory);
            }
        }

        protected void CleanUpTick(object sender, EventArgs eventArgs)
        {
            var currentTimeSpan = new TimeSpan(DateTime.Now.ToUniversalTime().Ticks - new DateTime(1970, 1, 1).Ticks);
            var deadServers = _activeServerStatuses.Where(x => currentTimeSpan.Subtract(TimeSpan.FromMilliseconds(x.Value.Time)).TotalMilliseconds > TimeSpan.FromMinutes(1).TotalMilliseconds).ToList();
            
            foreach (var deadServer in deadServers)
            {
                _activeServerStatuses.Remove(deadServer.Key);
                _displayServers.Remove(deadServer.Key);
            }
        }
    }
}
