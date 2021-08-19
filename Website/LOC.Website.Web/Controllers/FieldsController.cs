namespace LOC.Website.Web.Controllers
{
    using System.Linq;
    using System.Web.Mvc;
    using Common.Contexts;
    using Core.Model.Server.PvpServer;
    using Newtonsoft.Json;

    public class FieldsController : Controller
    {
        private readonly LocContext context = new LocContext();

        [HttpPost]
        public ActionResult GetFieldBlocks(string key)
        {
            var json = JsonConvert.SerializeObject(context.Fields.Where(x => x.Server == key).ToList());
            return Content(json, "application/json");
        }

        [HttpPost]
        public void AddField(FieldBlock newFieldBlock)
        {
            var field = context.Fields.FirstOrDefault(x => x.Location == newFieldBlock.Location);

            if (field != null)
                context.Fields.Remove(field);

            context.Fields.Add(newFieldBlock);
            context.SaveChanges();
        }

        [HttpPost]
        public void DeleteField(FieldBlock block)
        {
            var field = context.Fields.FirstOrDefault(x => x.Server == block.Server && x.Location == block.Location);

            if (field != null)
            {
                context.Fields.Remove(field);
                context.SaveChanges();
            }
        }

        [HttpPost]
        public ActionResult GetFieldOres(string key)
        {
            var json = JsonConvert.SerializeObject(context.FieldOres.Where(x => x.Server == key).ToList());
            return Content(json, "application/json");
        }

        [HttpPost]
        public void AddFieldOre(FieldOre newField)
        {
            var field = context.FieldOres.FirstOrDefault(x => x.Location == newField.Location);

            if (field != null)
                context.FieldOres.Remove(field);

            context.FieldOres.Add(newField);
            context.SaveChanges();
        }

        [HttpPost]
        public void DeleteFieldOre(FieldOre ore)
        {
            var field = context.FieldOres.FirstOrDefault(x => x.Server == ore.Server && x.Location == ore.Location);

            if (field != null)
            {
                context.FieldOres.Remove(field);
                context.SaveChanges();
            }
        }

        [HttpPost]
        public ActionResult GetFieldMonsters(string key)
        {
            var json = JsonConvert.SerializeObject(context.FieldMonsters.Where(x => x.Server == key).ToList());
            return Content(json, "application/json");
        }

        [HttpPost]
        public void AddFieldMonster(FieldMonster newField)
        {
            var field = context.FieldMonsters.FirstOrDefault(x => x.Name == newField.Name);

            if (field != null)
                context.FieldMonsters.Remove(field);

            context.FieldMonsters.Add(newField);
            context.SaveChanges();
        }

        [HttpPost]
        public void DeleteFieldMonster(FieldMonster monster)
        {
            var field = context.FieldMonsters.FirstOrDefault(x => x.Server == monster.Server && x.Name == monster.Name);

            while (field != null)
            {
                context.FieldMonsters.Remove(field);
                context.SaveChanges();
            }
        }
    }
}
