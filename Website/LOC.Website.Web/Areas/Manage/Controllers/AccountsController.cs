namespace LOC.Website.Web.Areas.Manage.Controllers
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Web.Mvc;
    using Common;
    using Common.Contexts;
    using Common.Models;
    using System.Data.Entity;
    using Core.Model.Account;
    using Core.Model.GameServer;
    using Core.Tokens.Client;

    public class AccountsController : ManageControllerBase
    {
        private readonly IAccountAdministrator _accountAdministrator;
        private readonly ILogger _logger;
        private LocContext context = new LocContext();

        public AccountsController(IAccountAdministrator accountAdministrator, ILogger logger)
        {
            _accountAdministrator = accountAdministrator;
            _logger = logger;
        }

        public ViewResult Index(string searchString)
        {
            var accounts = new List<Account>();

            if (!String.IsNullOrEmpty(searchString))
            {
                accounts = _accountAdministrator.GetAllAccountsMatching(searchString);
            }

            return View(accounts);
        }

        public ViewResult Details(int id)
        {
            var account = context.Accounts.Where(x => x.AccountId == id).Include(x => x.Rank).Include(x => x.Transactions.Select(y => y.SalesPackage)).Include(x => x.PvpTransactions).First();
            return View(account);
        }

        public ViewResult AccountLogins(int id)
        {
            var logins = context.Logins.Where(x => x.Account.AccountId == id).Include(x => x.Account).Include(x => x.IpAddress).Include(x => x.MacAddress);
            return View(logins);
        }

        public ActionResult Create()
        {
            return View();
        }

        public void TestLogin()
        {
            _accountAdministrator.Login(new LoginRequestToken
                {
                    IpAddress = "127.0.0.1",
                    MacAddress = null,
                    Name = "defek7",
                    Server = new Server { ConnectionAddress = "127.0.0.1", Name = "Test" }
                });
        }

        [HttpPost]
        public ActionResult Create(Account account)
        {
            if (ModelState.IsValid)
            {
                context.Accounts.Add(account);
                context.SaveChanges();
                return RedirectToAction("Index");  
            }

            return View(account);
        }
 
        public ActionResult Edit(int id)
        {
            Account account = context.Accounts.Single(x => x.AccountId == id);
            return View(account);
        }

        public ActionResult Delete(int id)
        {
            Account account = context.Accounts.Single(x => x.AccountId == id);
            return View(account);
        }

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(int id)
        {
            Account account = context.Accounts.Single(x => x.AccountId == id);
            context.Accounts.Remove(account);
            context.SaveChanges();
            return RedirectToAction("Index");
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                context.Dispose();
            }
            base.Dispose(disposing);
        }
    }
}