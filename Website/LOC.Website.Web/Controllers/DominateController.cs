namespace LOC.Website.Web.Controllers
{
    using System.Web.Mvc;
    using Common.Models;
    using Core.Model.GameServer.Stats;
    using Newtonsoft.Json;

    public class DominateController : PvpController
    {
        private readonly IDominateAdministrator _dominateAdministrator;

        public DominateController(IDominateAdministrator dominateAdministrator)
        {
            _dominateAdministrator = dominateAdministrator;
        }

        protected override IPvpAdministrator GetAdministrator()
        {
            return _dominateAdministrator;
        }

        [HttpPost]
        public ContentResult UploadStats(DominateGameStatsToken token)
        {
            var json = JsonConvert.SerializeObject(_dominateAdministrator.UploadStats(token));
            return Content(json, "application/json");
        }
    }
}
