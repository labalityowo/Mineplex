namespace LOC.Core
{
    using System;

    public static class TimeUtil
    {
        public static double GetCurrentMilliseconds()
        {
            var staticDate = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);
            var timeSpan = DateTime.UtcNow - staticDate;
            return timeSpan.TotalMilliseconds;
        }
    }
}
