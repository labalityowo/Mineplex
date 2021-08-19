namespace LOC.Website.Common.Models
{
    using System.Collections.Generic;
    using Core.Model.GameServer;

    public interface IServerAdministrator
    {
        void Started(Server server);
        void Stopped(Server server);
        void UpdateServerStatus(ServerHistory serverHistory);
        ServerUpdate GetServerUpdates(int serverId);
        List<string> GetFilteredWords();
    }
}
