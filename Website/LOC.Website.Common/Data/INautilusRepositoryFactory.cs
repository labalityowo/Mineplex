namespace LOC.Website.Common.Data
{
    using Core.Data;

    public interface INautilusRepositoryFactory
    {
        IRepository CreateRepository();
    }
}
