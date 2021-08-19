namespace LOC.Core.Model.Account
{
    public class RemovedPunishment
    {
        public int RemovedPunishmentId { get; set; }

        public int PunishmentId { get; set; }

        public int AdminId { get; set; }

        public string PunishmentType { get; set; }

        public string Reason { get; set; }

        public long DateTime { get; set; }
    }
}
