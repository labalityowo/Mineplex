namespace LOC.Website.Web.Controllers
{
    using System.Web.Mvc;
    using Common;
    using Common.Models;
    using Core;
    using Core.Model.Account;

    public class ProfileController : Controller
    {
        private readonly IAccountAdministrator _accountAdministrator;

        public ProfileController(IAccountAdministrator accountAdministrator)
        {
            _accountAdministrator = accountAdministrator;
        }

        [AcceptVerbs(HttpVerbs.Get)]
        public ActionResult Index(string name)
        {
            Account account = _accountAdministrator.GetAccountByName(name);

            if (account != null)
            {
                return View(account);
            }

            return RedirectToAction("Error");
        }

        public ActionResult Error()
        {
            return View();
        }
    }
}
