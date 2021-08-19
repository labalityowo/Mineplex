namespace LOC.Website.Web.Controllers
{
    using System.Linq;
    using System.Web.Mvc;
    using Newtonsoft.Json;
    using ViewModels;

    public class StoreController : Controller
    {
        private readonly IStoreViewModel _storeModel;

        public StoreController(IStoreViewModel storeModel)
        {
            _storeModel = storeModel;
        }

        [HttpPost]
        public ActionResult GetSalesPackages()
        {
            var json = JsonConvert.SerializeObject(_storeModel.SalesPackages.Where(x => !x.Test).ToList());
            return Content(json, "application/json");
        }

        public ActionResult Index()
        {
            return View(_storeModel.SalesPackages.Where(x => !x.Test).ToList());
        }
    }
}
