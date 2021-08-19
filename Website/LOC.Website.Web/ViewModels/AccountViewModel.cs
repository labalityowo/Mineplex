namespace LOC.Website.Web.ViewModels
{
    using System.Collections.Generic;
    using Common.Models;
    using Core.Model.Account;

    public class AccountViewModel
    {
        private readonly IAccountAdministrator _accountAdministrator;

        public AccountViewModel(IAccountAdministrator accountAdministrator)
        {
            _accountAdministrator = accountAdministrator;
        }

        public List<Account> Accounts()
        {
            return _accountAdministrator.GetAllAccountsMatching();
        }

        public Account GetAccountByName(string name)
        {
            return _accountAdministrator.GetAccountByName(name);
        }
    }
}