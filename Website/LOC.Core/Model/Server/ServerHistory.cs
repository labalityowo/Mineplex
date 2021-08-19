namespace LOC.Core.Model.GameServer
{
    using Core.GameServer;

    public class ServerHistory
    {
        public int ServerHistoryId { get; set; }

        public int ServerId { get; set; }

        public ServerStatus Status { get; set; }

        public long Time { get; set; }

        public double TicksPerSecond { get; set; }

        public long MemoryUsage { get; set; }
    }
}
