namespace LOC.Core.DependencyResolution
{
    using StructureMap;

    public static class IoC
    {
        public static IContainer Initialize()
        {
            ObjectFactory.Initialize(
                                     x => x.Scan(
                                                 scan =>
                                                 {
                                                     scan.TheCallingAssembly();
                                                     scan.AssembliesFromApplicationBaseDirectory();
                                                     scan.WithDefaultConventions();
                                                 }));
            return ObjectFactory.Container;
        }
    }
}
