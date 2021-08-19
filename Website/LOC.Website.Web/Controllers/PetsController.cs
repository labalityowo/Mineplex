namespace LOC.Website.Web.Controllers
{
    using System.Collections.Generic;
    using System.Web.Mvc;
    using Common.Models;
    using Core.Model.Server;
    using Core.Tokens.Client;
    using Newtonsoft.Json;
    using LOC.Website.Common;

    public class PetsController : Controller
    {
        private readonly IPetAdministrator _petAdministrator;
        private readonly ILogger _logger;

        public PetsController(IPetAdministrator petAdministrator, ILogger logger)
        {
            _petAdministrator = petAdministrator;
            _logger = logger;
        }

        [HttpPost]
        public ContentResult GetPets(List<Pet> pets)
        {
            var json = JsonConvert.SerializeObject(_petAdministrator.GetPets(pets));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ContentResult GetBulkPets(BulkConfig bulkConfig)
        {
            var json = JsonConvert.SerializeObject(_petAdministrator.GetBulkPets(bulkConfig.Start, bulkConfig.Count));
            return Content(json, "application/json");
        }

        [HttpPost]
        public ContentResult GetPetExtras(List<PetExtra> petExtras)
        {
            var json = JsonConvert.SerializeObject(_petAdministrator.GetPetExtras(petExtras));
            return Content(json, "application/json");
        }

        [HttpPost]
        public void AddPet(PetChangeToken pet)
        {
            _petAdministrator.AddPet(pet);
        }

        [HttpPost]
        public void UpdatePet(PetChangeToken pet)
        {
            _petAdministrator.UpdatePet(pet);
        }

        [HttpPost]
        public void RemovePet(PetChangeToken pet)
        {
            _petAdministrator.RemovePet(pet);
        }

        [HttpPost]
        public void AddPetNameTag(string name)
        {
            _petAdministrator.AddPetNameTag(name);
        }

        [HttpPost]
        public void RemovePetNameTag(string name)
        {
            _petAdministrator.RemovePetNameTag(name);
        }
    }
}
