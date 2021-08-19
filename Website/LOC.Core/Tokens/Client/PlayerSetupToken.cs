namespace LOC.Core.Tokens.Client
{
    using System.Collections.Generic;

    public class PlayerSetupToken
    {
        public string Name { get; set; }

        public int PvpClassId { get; set; }

        public int SwordSkillId { get; set; }

        public int AxeSkillId { get; set; }

        public int BowSkillId { get; set; }

        public int ClassPassiveASkillId { get; set; }
        public int ClassPassiveBSkillId { get; set; }

        public int GlobalPassiveASkillId { get; set; }
        public int GlobalPassiveBSkillId { get; set; }
        public int GlobalPassiveCSkillId { get; set; }

        public List<SlotToken> Slots { get; set; }

        public List<DamageToken> DamageSources { get; set; } 
    }
}
