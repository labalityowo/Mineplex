namespace LOC.Core.Model.PvpServer
{
    using System.Collections.Generic;
    using Sales;
    using Server.PvpServer;

    public class Skill
    {
        public int SkillId { get; set; }

        public string Name { get; set; }

        public int Level { get; set; }

        public GameSalesPackage SalesPackage { get; set; }
    }
}
