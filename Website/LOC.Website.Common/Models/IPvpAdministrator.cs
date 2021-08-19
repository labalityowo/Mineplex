namespace LOC.Website.Common.Models
{
    using System.Collections.Generic;
    using Core.Model.PvpServer;
    using Core.Model.Server.PvpServer;

    public interface IPvpAdministrator
    {
        List<Item> GetItems(List<Item> items);
        List<Skill> GetSkills(List<Skill> skills);
        List<Weapon> GetWeapons(List<Weapon> weapons);
        List<PvpClass> GetPvpClasses(List<PvpClass> pvpClasses);
        List<BenefitItem> GetBenefitItems(List<BenefitItem> benefitItems);
        
    }
}
