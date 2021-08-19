namespace LOC.Website.Common.Models
{
    using System.Collections.Generic;
    using System.Linq;
    using Core.Model.GameServer;
    using Core.Model.Server;
    using Data;

    public class ServerAdministrator : IServerAdministrator
    {
        private readonly INautilusRepositoryFactory _repositoryFactory;
        private readonly IGameServerMonitor _gameServerMonitor;        

        public ServerAdministrator(INautilusRepositoryFactory nautilusRepositoryFactory)
        {
            _repositoryFactory = nautilusRepositoryFactory;
            _gameServerMonitor = GameServerMonitor.Instance;
        }

        public void Started(Server server)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                if (!repository.Any<Server>(x => x.ConnectionAddress == server.ConnectionAddress))
                {
                    server = repository.Add(server);
                }

                repository.CommitChanges();
            }

            _gameServerMonitor.Track(server);
        }

        public void Stopped(Server server)
        {
            _gameServerMonitor.UnTrack(server);
        }

        public void UpdateServerStatus(ServerHistory serverHistory)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                _gameServerMonitor.UpdateServerStatus(repository.Add(serverHistory));
            }
        }

        public ServerUpdate GetServerUpdates(int serverId)
        {
            var serverUpdates = _gameServerMonitor.GetServerUpdatesFor(serverId);
            _gameServerMonitor.ClearServerUpdates(serverId);

            return serverUpdates;
        }

        public List<string> GetFilteredWords()
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                return Enumerable.Select(repository.GetAll<FilteredWord>(), word => (string) word.Name).ToList();
            }
        }
    }
}
