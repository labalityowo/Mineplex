namespace LOC.Core.Tokens.Client
{
    using Model.Account;

    public class PunishToken
    {
        public PunishToken() { }

        public PunishToken(Punishment punishment)
        {
            Admin = punishment.Admin;
            Category = punishment.Category;
            Sentence = punishment.Sentence;
            Reason = punishment.Reason;
            Severity = punishment.Severity;
            Duration = punishment.Duration;
            Time = punishment.Time;
        }

        public string Target { get; set; }

        public string Admin { get; set; }

        public long Time { get; set; }

        public string Sentence { get; set; }

        public string Category { get; set; }

        public string Reason { get; set; }

        public double Duration { get; set; }

        public int Severity { get; set; }
    }
}
