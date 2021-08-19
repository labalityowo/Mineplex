namespace LOC.Website.Web.Controllers
{
    using System;
    using System.Web.Mvc;
    using Common;
    using Common.Models;
    using Core.Model.Server;
    using Core.Tokens;
    using Core.Tokens.Client;
    using Newtonsoft.Json;
    using System.Collections.Generic;
    using LOC.Core;

    public class PlayerAccountController : Controller
    {
        private readonly IAccountAdministrator _accountAdministrator;
        private readonly ILogger _logger;

        public PlayerAccountController(IAccountAdministrator accountAdministrator, ILogger logger)
        {
            _accountAdministrator = accountAdministrator;
            _logger = logger;
        }

        [HttpPost]
        public ActionResult GetAccountNames()
        {
            var accountNames = _accountAdministrator.GetAccountNames();

            var json = JsonConvert.SerializeObject(accountNames);
            return Content(json, "application/json");
        }

        [HttpPost]
        public ActionResult GetAccount(string name)
        {
            var account = _accountAdministrator.GetAccountByName(name);

            var json = JsonConvert.SerializeObject(account);
            return Content(json, "application/json");
        }

        /*
        [HttpPost]
        public ActionResult GetTasksByCount(SearchConf searchConf)
        {
            var tasks = _accountAdministrator.GetTasksByCount(searchConf);

            var json = JsonConvert.SerializeObject(tasks);
            return Content(json, "application/json");
        }
        */

        [HttpPost]
        public ActionResult GetAccountByUUID(string uuid)
        {
            var json = JsonConvert.SerializeObject(_accountAdministrator.GetAccountByUUID(uuid));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ActionResult GetDonor(string name)
        {
            var account = _accountAdministrator.GetAccountByName(name);

            var json = JsonConvert.SerializeObject(account);
            return Content(json, "application/json");
        }

        [HttpPost]
        public ActionResult Login(LoginRequestToken loginRequest)
        {
            var json = JsonConvert.SerializeObject(new ClientToken(_accountAdministrator.Login(loginRequest)));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ActionResult PurchaseKnownSalesPackage(PurchaseToken token)
        {
            var json = JsonConvert.SerializeObject(_accountAdministrator.PurchaseGameSalesPackage(token));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ActionResult PurchaseUnknownSalesPackage(UnknownPurchaseToken token)
        {
            var json = JsonConvert.SerializeObject(_accountAdministrator.PurchaseUnknownSalesPackage(token));
            return Content(json, "application/json");
        }

        [HttpPost]
        public void ApplyKits(string name)
        {
            _accountAdministrator.ApplyKits(name);
        }

        [HttpPost]
        public void Logout(string name)
        {
            _accountAdministrator.Logout(name);
        }

        [HttpPost]
        public ActionResult AccountExists(string name)
        {
            var json = JsonConvert.SerializeObject(_accountAdministrator.AccountExists(name));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ActionResult GetMatches(string name)
        {
            var json = JsonConvert.SerializeObject(_accountAdministrator.GetAllAccountNamesMatching(name));
            return Content(json, "application/json");
        }
        
        [HttpPost]
        public ActionResult Punish(PunishToken token)
        {
            var json = JsonConvert.SerializeObject(_accountAdministrator.Punish(token).ToString());
            return Content(json, "application/json");
        }

        [HttpPost]
        public ActionResult RemovePunishment(RemovePunishmentToken token)
        {
            var json = JsonConvert.SerializeObject(_accountAdministrator.RemovePunishment(token).ToString());
            return Content(json, "application/json");
        }

        [HttpPost]
        public void RemoveBan(RemovePunishmentToken token)
        {
            _accountAdministrator.RemoveBan(token);
        }

        [HttpPost]
        public void Ignore(ClientIgnoreToken token)
        {
            _accountAdministrator.Ignore(token.Name, token.IgnoredPlayer);
        }

        [HttpPost]
        public void RemoveIgnore(ClientIgnoreToken token)
        {
            _accountAdministrator.RemoveIgnore(token.Name, token.IgnoredPlayer);
        }

        [HttpPost]
        public void SaveCustomBuild(CustomBuildToken token)
        {
            _accountAdministrator.SaveCustomBuild(token);
        }

        [HttpPost]
        public void UpdateAccountUUIDs(List<AccountNameToken> tokens)
        {
            _accountAdministrator.UpdateAccountUUIDs(tokens);
        }
        
        [HttpPost]
        public ActionResult GetAccounts(AccountBatchToken token)
        {
            var json = JsonConvert.SerializeObject(_accountAdministrator.GetAccounts(token));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ActionResult GemReward(GemRewardToken token)
        {
            var json = JsonConvert.SerializeObject(_accountAdministrator.GemReward(token));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ActionResult CoinReward(GemRewardToken token)
        {
            var json = JsonConvert.SerializeObject(_accountAdministrator.CoinReward(token));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ActionResult RankUpdate(RankUpdateToken token)
        {
            var json = JsonConvert.SerializeObject(_accountAdministrator.UpdateRank(token));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ActionResult GetPunishClient(string name)
        {
            var json = JsonConvert.SerializeObject(new ClientToken(_accountAdministrator.GetAccountByName(name)));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ActionResult GetAdminPunishments(string name)
        {
            var json = JsonConvert.SerializeObject(_accountAdministrator.GetAdminPunishments(name));
            return Content(json, "application/json");
        }
    }
}