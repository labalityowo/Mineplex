using LOC.Core.Tokens.Clan;

namespace LOC.Website.Web.Controllers
{
    using System.Web.Mvc;
    using Common.Models;
    using Newtonsoft.Json;

    public class ClanController : Controller
    {
        private readonly IClanAdministrator _clanAdministrator;        

        public ClanController(IClanAdministrator clanAdministrator)
        {
            _clanAdministrator = clanAdministrator;
        }

        [HttpPost]
        public void AddClan(ClanToken clan)
        {
            _clanAdministrator.AddClan(clan);
        }

        [HttpPost]
        public void EditClan(ClanToken clan)
        {
            _clanAdministrator.EditClan(clan);
        }

        [HttpPost]
        public void DeleteClan(ClanToken clan)
        {
            _clanAdministrator.DeleteClan(clan);
        }

        [HttpPost]
        public ActionResult GetClans()
        {
            var clans = _clanAdministrator.GetClans();
            var json = JsonConvert.SerializeObject(clans);
            return Content(json, "application/json");
        }
    }
}
