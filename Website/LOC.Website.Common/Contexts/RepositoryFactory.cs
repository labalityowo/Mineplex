namespace LOC.Core.Data
{
    using System.Data.Entity;

    public class RepositoryFactory : IRepositoryFactory
    {
        public IRepository CreateRepository<TContext>() where TContext : DbContext, new()
        {
            return new Repository<TContext>(new TContext());
        }
    }
}
