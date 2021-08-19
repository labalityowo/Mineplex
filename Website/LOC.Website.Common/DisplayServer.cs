namespace LOC.Website.Common
{
    using System.Collections.Generic;
    using System.Linq;
    using Core.Model.GameServer;

    public class DisplayServer
    {
        private readonly LinkedList<double[]> _tps;
        private readonly LinkedList<long[]> _memUsage;

        public DisplayServer(Server server, ServerHistory serverHistory, long started)
        {
            _tps = new LinkedList<double[]>();
            _memUsage = new LinkedList<long[]>();

            Name = server.Name;
            PlayerLimit = server.PlayerLimit;
            Address = server.ConnectionAddress;
            TimeStarted = started;
            LastUpdate = serverHistory.Time;
            _tps.AddLast(new [] { 0, serverHistory.TicksPerSecond * 20 });
            _memUsage.AddLast(new[] { 0, serverHistory.MemoryUsage });
        }

        public void Update(ServerHistory serverHistory)
        {
            LastUpdate = serverHistory.Time; 

            if (_tps.Count > 30)
            {
                _tps.RemoveFirst();
            }

            if (_memUsage.Count > 30)
            {
                _memUsage.RemoveFirst();
            }

            _tps.AddLast(new[] { _tps.Count * 15, serverHistory.TicksPerSecond * 20 });
            _memUsage.AddLast(new[] { _tps.Count * 15, serverHistory.MemoryUsage });
        }

        public string Name { get; private set; }

        public int PlayerLimit { get; private set; }

        public string Address { get; private set; }

        public long TimeStarted { get; private set; }

        public long LastUpdate { get; private set; }

        public List<double[]> TicksPerSecond
        {
            get { return _tps.ToList(); }
        }

        public List<long[]> MemoryUsage
        {
            get { return _memUsage.ToList(); }
        }
    }
}
