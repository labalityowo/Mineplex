namespace LOC.Website.Web
{
    using System.Linq;
    using System.Web.Mvc;
    using System.Web.Routing;
    using LOC.Core;
    using LOC.Core.DependencyResolution;
    using LOC.Website.Common.Contexts;

    // Note: For instructions on enabling IIS6 or IIS7 classic mode, 
    // visit http://go.microsoft.com/?LinkId=9394801

    public class MvcApplication : System.Web.HttpApplication
    {
        public static void RegisterGlobalFilters(GlobalFilterCollection filters)
        {
            filters.Add(new HandleErrorAttribute());
        }

        public static void RegisterRoutes(RouteCollection routes)
        {
            routes.IgnoreRoute("{resource}.axd/{*pathInfo}");

            routes.MapRoute(
                "Profile", // Route name
                "Profile/{name}", // URL with parameters
                new { controller = "Profile", action = "Index", name = UrlParameter.Optional } // Parameter defaults
                );

            routes.MapRoute(
                "Default", // Route name
                "{controller}/{action}/{id}", // URL with parameters
                new { controller = "Home", action = "Index", id = UrlParameter.Optional } // Parameter defaults
                );
        }

        protected void Application_Start()
        {
            AreaRegistration.RegisterAllAreas();

            RegisterGlobalFilters(GlobalFilters.Filters);
            RegisterRoutes(RouteTable.Routes);
            ModelBinders.Binders.DefaultBinder = new JsonModelBinder();

            UpdateJsonProvider();

            var resolver = Resolver.Current;
            var locDatabaseUpdater = new LOCDatabaseUpdater();
            locDatabaseUpdater.Update();
        }

        private void UpdateJsonProvider()
        {
            // find the default JsonVAlueProviderFactory
            JsonValueProviderFactory jsonValueProviderFactory = ValueProviderFactories.Factories.OfType<JsonValueProviderFactory>().FirstOrDefault();

            // remove the default JsonVAlueProviderFactory)
            if (jsonValueProviderFactory != null)
            {
                ValueProviderFactories.Factories.Remove(jsonValueProviderFactory);
            }

            // add the custom one
            ValueProviderFactories.Factories.Add(new CustomJsonValueProviderFactory());
        }
    }
}