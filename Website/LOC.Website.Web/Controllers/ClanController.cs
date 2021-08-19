using LOC.Core.Tokens.Clan;

namespace LOC.Website.Web.Controllers
{
    using System.Collections.Generic;
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
        public ActionResult GetClans(string serverName)
        {
            var clans = _clanAdministrator.GetClans(serverName);
            var json = JsonConvert.SerializeObject(clans);
            return Content(json, "application/json");
        }

        [HttpPost]
        public void UpdateClanTNTGenerators(List<ClanGeneratorToken> tokens)
        {
            _clanAdministrator.UpdateClanTNTGenerators(tokens);
        }

        [HttpPost]
        public void UpdateClanTNTGenerator(ClanGeneratorToken token)
        {
            _clanAdministrator.UpdateClanTNTGenerator(token);
        }

        [HttpPost]
        public void ResetClanData()
        {
            _clanAdministrator.ResetClanData();
        }
    }
}
