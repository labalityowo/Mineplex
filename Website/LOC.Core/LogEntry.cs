namespace LOC.Core
{
    using System;

    public class LogEntry
    {
        public int LogEntryId { get; set; }

        public DateTime Date { get; set; }

        public string Category { get; set; }

        public string Message { get; set; }
    }
}
