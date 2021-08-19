namespace LOC.Website.Web.Controllers
{
    using System.Collections.Generic;
    using System.Data.Entity;
    using System.Linq;
    using System.Web.Mvc;
    using Common.Data;
    using Core.Model.Server.GameServer.MineKart;
    using Newtonsoft.Json;

    public class MineKartController : Controller
    {
        private readonly INautilusRepositoryFactory _repositoryFactory;

        public MineKartController(INautilusRepositoryFactory nautilusRepositoryFactory)
        {
            _repositoryFactory = nautilusRepositoryFactory;
        }

        [HttpPost]
        public ContentResult GetKartItems(List<MineKart> minekarts)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                foreach (var item in minekarts.Where(item => !repository.Any<MineKart>(x => x.Name == item.Name)))
                {
                    repository.Add(item);
                }

                repository.CommitChanges();

                var json = JsonConvert.SerializeObject(repository.GetAll<MineKart>().Include(x => x.SalesPackage).ToList());
                return Content(json, "application/json");
            }
        }
    }
}
