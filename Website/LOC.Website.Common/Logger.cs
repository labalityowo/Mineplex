namespace LOC.Website.Common
{
    using System;
    using Contexts;
    using Core;
    using Data;

    public class Logger : ILogger
    {
        private readonly INautilusRepositoryFactory _nautilusRepositoryFactory;

        public Logger(INautilusRepositoryFactory nautilusRepositoryFactory)
        {
            _nautilusRepositoryFactory = nautilusRepositoryFactory;
        }

        public void Log(string category, string message)
        {
            using (var repository = _nautilusRepositoryFactory.CreateRepository())
            {
                repository.Add(new LogEntry
                {
                    Date = DateTime.Now,
                    Category = category,
                    Message = message
                });
                repository.CommitChanges();;
            }
        }
    }
}
