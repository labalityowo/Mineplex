namespace LOC.Website.Web.Controllers
{
    using System.Web.Mvc;
    using Common.Models;

    public class StatsController : Controller
    {
        private readonly IDominateAdministrator _dominateAdministrator;

        public StatsController(IDominateAdministrator dominateAdministrator)
        {
            _dominateAdministrator = dominateAdministrator;
        }

        public ActionResult Index()
        {
            return View(_dominateAdministrator.GetStats());
        }
    }
}
