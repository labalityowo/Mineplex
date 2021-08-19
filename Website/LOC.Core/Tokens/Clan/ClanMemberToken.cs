namespace LOC.Core.Tokens.Clan
{
    using Model.Account;
    using Model.Server.PvpServer.Clan;

    public class ClanMemberToken
    {
        public ClanMemberToken()
        {
            
        }

        public ClanMemberToken(Account account)
        {
            AccountId = account.AccountId;
            Name = account.Name;
            ClanRole = account.ClanRole;
        }

        public int AccountId { get; set; }

        public string Name { get; set; }

        public ClanRole ClanRole { get; set; }
    }
}
