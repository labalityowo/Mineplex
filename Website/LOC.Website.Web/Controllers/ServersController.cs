namespace LOC.Website.Web.Controllers
{
    using System.Web.Mvc;
    using Common;
    using Newtonsoft.Json;

    public class ServersController : Controller
    {
        [HttpPost]
        public void RegisterServer(string address)
        {
            ServerStatusManager.Instance.RegisterServer(address);
        }

        [HttpPost]
        public void RemoveServer(string address)
        {
            ServerStatusManager.Instance.RemoveServer(address);
        }

        [HttpPost]
        public ActionResult GetServers()
        {
            var json = JsonConvert.SerializeObject(ServerStatusManager.Instance.GetServers());
            return Content(json, "application/json");
        }
    }
}
