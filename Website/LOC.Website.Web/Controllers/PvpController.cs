namespace LOC.Website.Web.Controllers
{
    using System.Collections.Generic;
    using System.Web.Mvc;
    using Common.Models;
    using Core.Model.PvpServer;
    using Core.Model.Server.PvpServer;
    using Newtonsoft.Json;

    public abstract class PvpController : Controller
    {
        protected abstract IPvpAdministrator GetAdministrator();

        [HttpPost]
        public ContentResult GetItems(List<Item> items)
        {
            var json = JsonConvert.SerializeObject(GetAdministrator().GetItems(items));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ContentResult GetSkills(List<Skill> skills)
        {
            var json = JsonConvert.SerializeObject(GetAdministrator().GetSkills(skills));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ContentResult GetWeapons(List<Weapon> weapons)
        {
            var json = JsonConvert.SerializeObject(GetAdministrator().GetWeapons(weapons));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ContentResult GetClasses(List<PvpClass> pvpClasses)
        {
            var json = JsonConvert.SerializeObject(GetAdministrator().GetPvpClasses(pvpClasses));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ContentResult GetBenefitItems(List<BenefitItem> benefitItems)
        {
            var json = JsonConvert.SerializeObject(GetAdministrator().GetBenefitItems(benefitItems));
            return Content(json, "application/json");
        }
    }
}
