namespace LOC.Core.Tokens.Client
{
    using System.Collections.Generic;
    using Model.Server.PvpServer;

    public class CustomBuildToken
    {
        public CustomBuildToken() { }

        public CustomBuildToken(CustomBuild customBuild)
        {
            CustomBuildId = customBuild.CustomBuildId;
            Name = customBuild.Name;
            Active = customBuild.Active;
            CustomBuildNumber = customBuild.CustomBuildNumber;
            PvpClass = customBuild.PvpClass;

            SwordSkill = customBuild.SwordSkill;
            SwordSkillLevel = customBuild.SwordSkillLevel;

            AxeSkill = customBuild.AxeSkill;
            AxeSkillLevel = customBuild.AxeSkillLevel;

            BowSkill = customBuild.BowSkill;
            BowSkillLevel = customBuild.BowSkillLevel;

            ClassPassiveASkill = customBuild.ClassPassiveASkill;
            ClassPassiveASkillLevel = customBuild.ClassPassiveASkillLevel;

            ClassPassiveBSkill = customBuild.ClassPassiveBSkill;
            ClassPassiveBSkillLevel = customBuild.ClassPassiveBSkillLevel;

            GlobalPassiveSkill = customBuild.GlobalPassiveSkill;
            GlobalPassiveSkillLevel = customBuild.GlobalPassiveSkillLevel;

            SkillTokens = customBuild.SkillTokens;
            ItemTokens = customBuild.ItemTokens;

            Slots = new List<SlotToken>();

            Slots.Add(new SlotToken { Material = customBuild.Slot1Material, Name = customBuild.Slot1Name, Amount = customBuild.Slot1Amount });
            Slots.Add(new SlotToken { Material = customBuild.Slot2Material, Name = customBuild.Slot2Name, Amount = customBuild.Slot2Amount });
            Slots.Add(new SlotToken { Material = customBuild.Slot3Material, Name = customBuild.Slot3Name, Amount = customBuild.Slot3Amount });
            Slots.Add(new SlotToken { Material = customBuild.Slot4Material, Name = customBuild.Slot4Name, Amount = customBuild.Slot4Amount });
            Slots.Add(new SlotToken { Material = customBuild.Slot5Material, Name = customBuild.Slot5Name, Amount = customBuild.Slot5Amount });
            Slots.Add(new SlotToken { Material = customBuild.Slot6Material, Name = customBuild.Slot6Name, Amount = customBuild.Slot6Amount });
            Slots.Add(new SlotToken { Material = customBuild.Slot7Material, Name = customBuild.Slot7Name, Amount = customBuild.Slot7Amount });
            Slots.Add(new SlotToken { Material = customBuild.Slot8Material, Name = customBuild.Slot8Name, Amount = customBuild.Slot8Amount });
            Slots.Add(new SlotToken { Material = customBuild.Slot9Material, Name = customBuild.Slot9Name, Amount = customBuild.Slot9Amount });
        }

        public int CustomBuildId { get; set; }

        public string PlayerName { get; set; }

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

        public List<SlotToken> Slots { get; set; }

        public int SkillTokens { get; set; }

        public int ItemTokens { get; set; }

        public CustomBuild GetCustomBuild()
        {
            var customBuild = new CustomBuild();

            UpdateCustomBuild(customBuild);

            return customBuild;
        }

        public void UpdateCustomBuild(CustomBuild customBuild)
        {
            customBuild.Name = Name;
            customBuild.Active = Active;
            customBuild.CustomBuildNumber = CustomBuildNumber;
            customBuild.PvpClass = PvpClass;

            customBuild.SwordSkill = SwordSkill;
            customBuild.SwordSkillLevel = SwordSkillLevel;
            customBuild.AxeSkill = AxeSkill;
            customBuild.AxeSkillLevel = AxeSkillLevel;
            customBuild.BowSkill = BowSkill;
            customBuild.BowSkillLevel = BowSkillLevel;

            customBuild.ClassPassiveASkill = ClassPassiveASkill;
            customBuild.ClassPassiveASkillLevel = ClassPassiveASkillLevel;

            customBuild.ClassPassiveBSkill = ClassPassiveBSkill;
            customBuild.ClassPassiveBSkillLevel = ClassPassiveBSkillLevel;

            customBuild.GlobalPassiveSkill = GlobalPassiveSkill;
            customBuild.GlobalPassiveSkillLevel = GlobalPassiveSkillLevel;

            customBuild.ItemTokens = ItemTokens;
            customBuild.SkillTokens = SkillTokens;

            if (Slots != null && Slots.Count > 0)
            {
                var slots = Slots.ToArray();

                customBuild.Slot1Name = slots[0].Name;
                customBuild.Slot1Material = slots[0].Material;                
                customBuild.Slot1Amount = slots[0].Amount;

                customBuild.Slot2Name = slots[1].Name;
                customBuild.Slot2Material = slots[1].Material;
                customBuild.Slot2Amount = slots[1].Amount;

                customBuild.Slot3Name = slots[2].Name;
                customBuild.Slot3Material = slots[2].Material;
                customBuild.Slot3Amount = slots[2].Amount;

                customBuild.Slot4Name = slots[3].Name;
                customBuild.Slot4Material = slots[3].Material;
                customBuild.Slot4Amount = slots[3].Amount;

                customBuild.Slot5Name = slots[4].Name;
                customBuild.Slot5Material = slots[4].Material;
                customBuild.Slot5Amount = slots[4].Amount;

                customBuild.Slot6Name = slots[5].Name;
                customBuild.Slot6Material = slots[5].Material;
                customBuild.Slot6Amount = slots[5].Amount;

                customBuild.Slot7Name = slots[6].Name;
                customBuild.Slot7Material = slots[6].Material;
                customBuild.Slot7Amount = slots[6].Amount;

                customBuild.Slot8Name = slots[7].Name;
                customBuild.Slot8Material = slots[7].Material;
                customBuild.Slot8Amount = slots[7].Amount;

                customBuild.Slot9Name = slots[8].Name;
                customBuild.Slot9Material = slots[8].Material;
                customBuild.Slot9Amount = slots[8].Amount;
            }
        }
    }
}
