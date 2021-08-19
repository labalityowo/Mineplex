namespace LOC.Core.DependencyResolution
{
    using System;
    using System.Collections.Generic;

    public interface IResolver
    {
        object GetService(Type serviceType);
        IEnumerable<object> GetServices(Type serviceType);
    	T GetService<T>();
    }
}
