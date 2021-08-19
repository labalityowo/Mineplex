namespace LOC.Core.Model.Server.PvpServer
{
    public class FieldMonster
    {
        public int FieldMonsterId { get; set; }

        public string Server { get; set; }

        public string Name { get; set; }

        public string Type { get; set; }

        public int MobMax { get; set; }

        public double MobRate { get; set; }

        public string Centre { get; set; }

        public int Radius { get; set; }

        public int Height { get; set; }
    }
}