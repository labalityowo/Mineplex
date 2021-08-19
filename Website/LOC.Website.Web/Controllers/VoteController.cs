namespace LOC.Website.Web.Controllers
{
    using System.Web.Mvc;

    public class VoteController : Controller
    {
        public ActionResult Index()
        {
            return Redirect(@"http://minecraftservers.org/server/31063");
        }
    }
}
