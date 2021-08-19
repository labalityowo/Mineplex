namespace LOC.Core.Tokens.Client
{
    public class RemovePunishmentToken
    {
        public string Target { get; set; }

        public int PunishmentId { get; set; }

        public string Reason { get; set; }

        public string Admin { get; set; }
    }
}
