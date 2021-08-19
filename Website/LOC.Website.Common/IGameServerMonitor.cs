namespace LOC.Website.Common
{
    using System.Collections.Generic;
    using Core.Model.Account;
    using Core.Model.GameServer;

    public interface IGameServerMonitor
    {
        List<DisplayServer> ServerStatuses { get; }
        void Track(Server server);
        void UnTrack(Server server);
        ServerUpdate GetServerUpdatesFor(int serverId);
        void UpdateServerStatus(ServerHistory serverHistory);
        void ClearServerUpdates(int serverId);
        void AccountChanged(int accountId);
        void PlayerLoggedIn(Account account, int serverId);
        void PlayerLoggedOut(Account account);

        void WikiItemChanged(int itemWikiId);

        void WikiPlayerChanged(int playerWikiId);
    }
}
