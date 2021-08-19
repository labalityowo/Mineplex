namespace LOC.Core.DependencyResolution
{
    using System;
    using System.Collections.Generic;
    using System.IO;
    using System.Linq;
    using StructureMap;

    public class Resolver : IResolver
    {
        private readonly IContainer _container;

        static Resolver()
        {
            try
            {
                ObjectFactory.Initialize(
                                         x =>
                                         {
                                             x.Scan(
                                                    scan =>
                                                    {
                                                        scan.TheCallingAssembly();
                                                        scan.WithDefaultConventions();
                                                        scan.AssembliesFromApplicationBaseDirectory(
                                                                                                    a =>
                                                                                                    a.FullName.StartsWith("LOC"));
                                                    });
                                             x.For<IResolver>().Use(r => Current);
                                         });
                Current = new Resolver(ObjectFactory.Container);
            }
            catch (Exception e)
            {
                var path = Path.Combine(Path.GetFullPath(AppDomain.CurrentDomain.BaseDirectory), "Log.txt");
                var contents = new List<string> { DateTime.Now.ToString(), e.Message, e.StackTrace };
                File.AppendAllLines(path, contents);

                throw;
            }
        }

        private Resolver(IContainer container)
        {
            _container = container;
        }

        public static IResolver Current { get; private set; }

        public T GetService<T>()
        {
            return (T)GetService(typeof(T));
        }

        public object GetService(Type serviceType)
        {
            if (serviceType == null)
            {
                return null;
            }

            return serviceType.IsAbstract || serviceType.IsInterface
                       ? _container.TryGetInstance(serviceType)
                       : _container.GetInstance(serviceType);
        }

        public IEnumerable<object> GetServices(Type serviceType)
        {
            return _container.GetAllInstances(serviceType).Cast<object>();
        }
    }
}
