namespace LOC.Website.Web.Controllers
{
    using System.Web.Mvc;
    using Common.Models;
    using Core.Model.GameServer;
    using Newtonsoft.Json;

    public class ServerController : Controller
    {
        private readonly IServerAdministrator _serverAdministrator;

        public ServerController(IServerAdministrator serverAdministrator)
        {
            _serverAdministrator = serverAdministrator;
        }

        [HttpPost]
        public void Start(Server server)
        {
            _serverAdministrator.Started(server);
        }

        [HttpPost]
        public ActionResult CheckForUpdates(ServerHistory server)
        {
            _serverAdministrator.UpdateServerStatus(server);
            var serverUpdates = _serverAdministrator.GetServerUpdates(server.ServerId);
            var json = JsonConvert.SerializeObject(serverUpdates);
            return Content(json, "application/json");
        }
    }
}
