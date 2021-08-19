namespace LOC.Website.Common.Data
{
    using Contexts;
    using Core.Data;
    using StructureMap;

    [PluginFamily(IsSingleton = true, Scope = InstanceScope.Singleton)]
    public class NautilusRepositoryFactory : INautilusRepositoryFactory
    {
        private readonly IRepositoryFactory _repositoryFactory;

        public NautilusRepositoryFactory(IRepositoryFactory repositoryFactory)
        {
            _repositoryFactory = repositoryFactory;
        }

        public IRepository CreateRepository()
        {
            return _repositoryFactory.CreateRepository<LocContext>();
        }
    }
}
