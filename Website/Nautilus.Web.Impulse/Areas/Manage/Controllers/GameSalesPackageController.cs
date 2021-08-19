namespace LOC.Website.Web.Areas.Manage.Controllers
{
    using System.Web.Mvc;
    using Common.Models;
    using Core.Model.Sales;

    public class GameSalesPackageController : ManageControllerBase
    {
        private readonly IGameSalesPackageAdministrator _gameSalesPackageAdministrator;

        public GameSalesPackageController(IGameSalesPackageAdministrator gameSalesPackageAdministrator)
        {
            _gameSalesPackageAdministrator = gameSalesPackageAdministrator;
        }

        public ViewResult Index()
        {
            return View(_gameSalesPackageAdministrator.GetSalesPackages());
        }

        public ViewResult Details(int id)
        {
            GameSalesPackage gamesalespackage = _gameSalesPackageAdministrator.GetGameSalesPackageById(id);
            return View(gamesalespackage);
        }

        public ActionResult Create()
        {
            return View();
        }

        [HttpPost]
        public ActionResult Create(GameSalesPackage gamesalespackage)
        {
            if (ModelState.IsValid)
            {
                _gameSalesPackageAdministrator.AddSalesPackage(gamesalespackage);
                return RedirectToAction("Index");
            }

            return View(gamesalespackage);
        }

        public ActionResult Edit(int id)
        {
            GameSalesPackage gamesalespackage = _gameSalesPackageAdministrator.GetGameSalesPackageById(id);
            return View(gamesalespackage);
        }

        [HttpPost]
        public ActionResult Edit(GameSalesPackage gamesalespackage)
        {
            if (ModelState.IsValid)
            {
                _gameSalesPackageAdministrator.UpdateSalesPackage(gamesalespackage);
                return RedirectToAction("Index");
            }
            return View(gamesalespackage);
        }

        public ActionResult Delete(int id)
        {
            GameSalesPackage gamesalespackage = _gameSalesPackageAdministrator.GetGameSalesPackageById(id);
            return View(gamesalespackage);
        }

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(int id)
        {
            GameSalesPackage gamesalespackage = _gameSalesPackageAdministrator.GetGameSalesPackageById(id);
            _gameSalesPackageAdministrator.DeleteSalesPackage(gamesalespackage);

            return RedirectToAction("Index");
        }
    }
}