namespace LOC.Core.Model.Account
{
    using System;

    public class Punishment
    {
        public int PunishmentId { get; set; }

        public int UserId { get; set; }

        public String Admin { get; set; }

        public bool Active { get; set; }

        public string Category { get; set; }

        public string Reason { get; set; }

        public long Time { get; set; }

        public int Severity { get; set; }

        public double Duration { get; set; }

        public string Sentence { get; set; }

        public bool Removed { get; set; }

        public string RemoveAdmin { get; set; }

        public string RemoveReason { get; set; }

        public long RemoveTime { get; set; }
    }
}
