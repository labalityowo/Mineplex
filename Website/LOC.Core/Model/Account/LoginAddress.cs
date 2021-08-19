namespace LOC.Core.Model.Account
{
    using Newtonsoft.Json;

    public class LoginAddress
    {
        [JsonIgnore]
        public int LoginAddressId { get; set; }

        public string Address { get; set; }
    }
}
