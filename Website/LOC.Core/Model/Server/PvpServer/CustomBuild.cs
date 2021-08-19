namespace LOC.Core.Model.Server.PvpServer
{
    public class CustomBuild
    {
        public int CustomBuildId { get; set; }

        public Account.Account Account { get; set; }

        public string Name { get; set; }

        public bool Active { get; set; }

        public int CustomBuildNumber { get; set; }

        public string PvpClass { get; set; }

        public string SwordSkill { get; set; }
        public int SwordSkillLevel { get; set; }

        public string AxeSkill { get; set; }
        public int AxeSkillLevel { get; set; }

        public string BowSkill { get; set; }
        public int BowSkillLevel { get; set; }

        public string ClassPassiveASkill { get; set; }
        public int ClassPassiveASkillLevel { get; set; }

        public string ClassPassiveBSkill { get; set; }
        public int ClassPassiveBSkillLevel { get; set; }

        public string GlobalPassiveSkill { get; set; }
        public int GlobalPassiveSkillLevel { get; set; }

        public int SkillTokens { get; set; }

        public int ItemTokens { get; set; }

        public string Slot1Name { get; set; }
        public string Slot1Material { get; set; }
        public int Slot1Amount { get; set; }

        public string Slot2Name { get; set; }
        public string Slot2Material { get; set; }
        public int Slot2Amount { get; set; }

        public string Slot3Name { get; set; }
        public string Slot3Material { get; set; }
        public int Slot3Amount { get; set; }

        public string Slot4Name { get; set; }
        public string Slot4Material { get; set; }
        public int Slot4Amount { get; set; }

        public string Slot5Name { get; set; }
        public string Slot5Material { get; set; }
        public int Slot5Amount { get; set; }

        public string Slot6Name { get; set; }
        public string Slot6Material { get; set; }
        public int Slot6Amount { get; set; }

        public string Slot7Name { get; set; }
        public string Slot7Material { get; set; }
        public int Slot7Amount { get; set; }

        public string Slot8Name { get; set; }
        public string Slot8Material { get; set; }
        public int Slot8Amount { get; set; }

        public string Slot9Name { get; set; }
        public string Slot9Material { get; set; }
        public int Slot9Amount { get; set; }
    }
}
