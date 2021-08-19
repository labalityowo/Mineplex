namespace LOC.Website.Web.Controllers
{
    using System.Web.Mvc;

    public class ManageController : Controller
    {
        [Authorize(Roles = "Admins")]
        public ActionResult Index()
        {
            return View();
        }
    }
}
