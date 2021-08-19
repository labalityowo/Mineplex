using LOC.Core.Model.PvpServer;

namespace LOC.Website.Web.Controllers
{
    using System.Collections.Generic;
    using System.Web.Mvc;
    using Common.Models;
    using Core.Model.GameServer.Stats;
    using Newtonsoft.Json;

    public class DominateController : Controller
    {
        private readonly IDominateAdministrator _dominateAdministrator;

        public DominateController(IDominateAdministrator dominateAdministrator)
        {
            _dominateAdministrator = dominateAdministrator;
        }

        [HttpPost]
        public void UploadStats(DominateGameStatsToken token)
        {
            _dominateAdministrator.UploadStats(token);
        }

        [HttpPost]
        public ContentResult GetStats()
        {
            var playerStats = _dominateAdministrator.GetStats();

            var json = JsonConvert.SerializeObject(playerStats);
            return Content(json, "application/json");
        }

        [HttpPost]
        public ContentResult GetItems(List<Item> items)
        {
            var json = JsonConvert.SerializeObject(_dominateAdministrator.GetItems(items));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ContentResult GetSkills(List<Skill> skills)
        {
            var json = JsonConvert.SerializeObject( _dominateAdministrator.GetSkills(skills));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ContentResult GetWeapons(List<Weapon> weapons)
        {
            var json = JsonConvert.SerializeObject(_dominateAdministrator.GetWeapons(weapons));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ContentResult GetClasses(List<PvpClass> pvpClasses)
        {
            var json = JsonConvert.SerializeObject(_dominateAdministrator.GetPvpClasses(pvpClasses));
            return Content(json, "application/json");
        }

        /*
        [HttpPost]
        public ContentResult GetMostPointsStats()
        {
            var playerStats = _dominateAdministrator.GetMostPointsStats();

            var json = JsonConvert.SerializeObject(playerStats);
            return Content(json, "application/json");
        }

        [HttpPost]
        public ContentResult GetMostAssistsStats()
        {
            var playerStats = _dominateAdministrator.GetMostAssistsStats();

            var json = JsonConvert.SerializeObject(playerStats);
            return Content(json, "application/json");
        }

        [HttpPost]
        public ContentResult GetMostDeathsStats()
        {
            var playerStats = _dominateAdministrator.GetMostDeathsStats();

            var json = JsonConvert.SerializeObject(playerStats);
            return Content(json, "application/json");
        }

        [HttpPost]
        public ContentResult GetBestKillsAverageStats()
        {
            var playerStats = _dominateAdministrator.GetBestKillsAverageStats();

            var json = JsonConvert.SerializeObject(playerStats);
            return Content(json, "application/json");
        }

        [HttpPost]
        public ContentResult GetBestPointsAverageStats()
        {
            var playerStats = _dominateAdministrator.GetBestPointsAverageStats();

            var json = JsonConvert.SerializeObject(playerStats);
            return Content(json, "application/json");
        }

        [HttpPost]
        public ContentResult GetBestAssistsAverageStats()
        {
            var playerStats = _dominateAdministrator.GetBestAssistsAverageStats();

            var json = JsonConvert.SerializeObject(playerStats);
            return Content(json, "application/json");
        }

        [HttpPost]
        public ContentResult GetBestDeathsAverageStats()
        {
            var playerStats = _dominateAdministrator.GetBestDeathsAverageStats();

            var json = JsonConvert.SerializeObject(playerStats);
            return Content(json, "application/json");
        }
         * */
    }
}
