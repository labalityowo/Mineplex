using System.Linq;
using System.Web.Mvc;
using LOC.Core;
using LOC.Website.Common.Contexts;

namespace LOC.Website.Web.Areas.Manage.Controllers
{   
    public class LogController : ManageControllerBase
    {
        private LocContext context = new LocContext();

        //
        // GET: /Log/

        public ViewResult Index()
        {
            return View(context.LogEntries.ToList());
        }

        //
        // GET: /Log/Details/5

        public ViewResult Details(int id)
        {
            LogEntry logentry = context.LogEntries.Single(x => x.LogEntryId == id);
            return View(logentry);
        }
  
        //
        // GET: /Log/Delete/5
 
        public ActionResult Delete(int id)
        {
            LogEntry logentry = context.LogEntries.Single(x => x.LogEntryId == id);
            return View(logentry);
        }

        //
        // POST: /Log/Delete/5

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(int id)
        {
            LogEntry logentry = context.LogEntries.Single(x => x.LogEntryId == id);
            context.LogEntries.Remove(logentry);
            context.SaveChanges();
            return RedirectToAction("Index");
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                context.Dispose();
            }
            base.Dispose(disposing);
        }
    }
}