namespace LOC.Website.Web.Areas.Manage.Controllers
{
    using System.Web.Mvc;

    [Authorize(Roles = "Admins")]
    public abstract class ManageControllerBase : Controller
    {
    }
}
