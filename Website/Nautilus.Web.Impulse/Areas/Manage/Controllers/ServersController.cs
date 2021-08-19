namespace LOC.Website.Web.Areas.Manage.Controllers
{
    using System.Web.Mvc;
    using Common;
    using Newtonsoft.Json;

    public class ServersController : ManageControllerBase
    {
        private readonly IGameServerMonitor _gameServerMonitor;

        public ServersController()
        {
            _gameServerMonitor = GameServerMonitor.Instance;
        }

        [HttpPost]
        public ActionResult UpdateServerStatuses()
        {
            var json = JsonConvert.SerializeObject(_gameServerMonitor.ServerStatuses);
            return Content(json, "application/json");
        }

        public ActionResult Index()
        {
            return View();
        }
    }
}
