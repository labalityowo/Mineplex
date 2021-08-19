namespace LOC.Core.Data
{
    using System.Data.Entity;
    using StructureMap;

    [PluginFamily(IsSingleton = true, Scope = InstanceScope.Singleton)]
    public interface IRepositoryFactory
    {
        IRepository CreateRepository<TContext>() where TContext : DbContext, new();
    }
}
