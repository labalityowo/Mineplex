namespace LOC.Website.Web.Controllers
{
    using System.Web.Mvc;
    using Common.Models;
    using Core.Model.Server.GameServer.CaptureThePig.Stats;
    using Newtonsoft.Json;

    public class CaptureThePigController  : PvpController
    {
        private readonly ICaptureThePigAdministrator _captureThePigAdministrator;

        public CaptureThePigController(ICaptureThePigAdministrator captureThePigAdministrator)
        {
            _captureThePigAdministrator = captureThePigAdministrator;
        }

        protected override IPvpAdministrator GetAdministrator()
        {
            return _captureThePigAdministrator;
        }

        [HttpPost]
        public ContentResult UploadStats(CaptureThePigGameStatsToken token)
        {
            var json = JsonConvert.SerializeObject(_captureThePigAdministrator.UploadStats(token));
            return Content(json, "application/json");
        }
    }
}
