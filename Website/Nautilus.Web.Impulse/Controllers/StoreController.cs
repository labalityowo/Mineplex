namespace LOC.Website.Web.Controllers
{
    using System.Linq;
    using System.Web.Mvc;
    using ViewModels;

    public class StoreController : Controller
    {
        private readonly IStoreViewModel _storeModel;

        public StoreController(IStoreViewModel storeModel)
        {
            _storeModel = storeModel;
        }

        public ActionResult Index()
        {
            return View(_storeModel.SalesPackages.Where(x => !x.Test).ToList());
        }
    }
}
