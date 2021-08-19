namespace LOC.Core.Model.Account
{
    using System.Collections.Generic;
    using Newtonsoft.Json;

    public class MacAddress
    {
        [JsonIgnore]
        public int MacAddressId { get; set; }

        public string Address { get; set; }

        public virtual List<Account> Accounts { get; set; }
    }
}
