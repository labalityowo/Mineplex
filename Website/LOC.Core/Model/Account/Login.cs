namespace LOC.Core.Model.Account
{
    public class Login
    {
        public int LoginId { get; set; }

        public long Time { get; set; }

        public LoginAddress IpAddress { get; set; }

        public MacAddress MacAddress { get; set; }

        public Account Account { get; set; }

        public int ServerId { get; set; }
    }
}
