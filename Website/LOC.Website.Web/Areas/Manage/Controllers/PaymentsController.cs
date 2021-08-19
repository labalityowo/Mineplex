namespace LOC.Website.Web.Areas.Manage.Controllers
{
    using System.Collections.Generic;
    using System.Data.Entity;
    using System.Linq;
    using System.Web.Mvc;
    using Common.Contexts;
    using Core.Model.Sales;

    public class PaymentsController : ManageControllerBase
    {
        private LocContext context = new LocContext();

        public ActionResult Index()
        {
            return View(context.Set<Transaction>().Include(x => x.SalesPackage).Include(x => x.Account).ToList());
        }

        public ViewResult Details(int dayOfYear)
        {
            return View(context.Transactions.Where(x => x.Time.DayOfYear == dayOfYear).ToList());
        }
    }
}
