using System.Collections.Generic;
using LOC.Core.Model.Account;

namespace LOC.Core.Tokens.Client
{
    using System.Collections.ObjectModel;

    public class AccountToken
    {
        public AccountToken()
        {

        }

        public AccountToken(Account account)
        {
            LastLogin = account.LastLogin;

            IpAddresses = new Collection<string>();
            MacAddresses = new Collection<string>();
            IpAliases = new Collection<string>();
            MacAliases = new Collection<string>();

            account.IpAddresses = new List<LoginAddress>();
            account.MacAddresses = new List<MacAddress>();
        }

        public long TotalPlayingTime { get; set; }
        public long LastLogin { get; set; }
        public int LoginCount { get; set; }

        public Collection<string> IpAddresses { get; set; }
        public Collection<string> MacAddresses { get; set; }
        public Collection<string> IpAliases { get; set; }
        public Collection<string> MacAliases { get; set; }
    }
}