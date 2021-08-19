namespace LOC.Website.Web.Controllers
{
    using System.Web.Mvc;
    using Common.Models;
    using Core.Model.Account;
    using Core.Tokens;
    using Core.Tokens.Client;
    using Newtonsoft.Json;

    public class PlayerAccountController : Controller
    {
        private readonly IAccountAdministrator _accountAdministrator;

        public PlayerAccountController(IAccountAdministrator accountAdministrator)
        {
            _accountAdministrator = accountAdministrator;
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

        [HttpPost]
        public ActionResult GetBan(string name)
        {
            var account = _accountAdministrator.GetAccountByName(name);

            var json = JsonConvert.SerializeObject(account);
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
        public ActionResult Login(LoginToken loginToken)
        {
            _accountAdministrator.Login(loginToken);
            var account = _accountAdministrator.GetAccountByName(loginToken.Name);

            var json = JsonConvert.SerializeObject(new ClientToken(account));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ActionResult PurchaseSalesPackage(PurchaseToken token)
        {
            var json = JsonConvert.SerializeObject(_accountAdministrator.PurchaseGameSalesPackage(token));
            return Content(json, "application/json");
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
        public void Ban(PunishToken punish)
        {
            _accountAdministrator.BanAccount(punish);
        }
    }
}