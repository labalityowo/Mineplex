using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using LOC.Core.Model.PvpServer;
using LOC.Website.Common.Contexts;

namespace LOC.Website.Web.Areas.Manage.Controllers
{
    using Core.Model.Sales;

    public class SkillsController : ManageControllerBase
    {
        private LocContext db = new LocContext();

        public ViewResult Index(string searchString)
        {
            var skills = new List<Skill>();

            if (!String.IsNullOrEmpty(searchString))
            {
                skills = db.Skills.Include(x => x.SalesPackage).Where(c => c.Name.ToUpper().Contains(searchString.ToUpper())).ToList();
            }

            return View(skills);
        }

        public ViewResult Details(int id)
        {
            Skill skill = db.Skills.Include(x => x.SalesPackage).FirstOrDefault(x => x.SkillId == id);
            return View(skill);
        }
        
        public ActionResult Edit(int id)
        {
            Skill skill = db.Skills.Include(x => x.SalesPackage).FirstOrDefault(x => x.SkillId == id);
            return View(skill);
        }

        public ActionResult RefundAccounts(int skillId)
        {
            var skill = db.Skills.Include(x => x.SalesPackage).FirstOrDefault(x => x.SkillId == skillId);
            var accounts = db.Accounts.Where(x => x.PvpTransactions.Any(y => y.GameSalesPackageId == skill.SalesPackage.GameSalesPackageId)).ToList();

            foreach (var account in accounts)
            {
                account.Gems += skill.SalesPackage.Gems;
                account.PvpTransactions.Remove(account.PvpTransactions.First(x => x.GameSalesPackageId == skill.SalesPackage.GameSalesPackageId));
            }

            db.SaveChanges();

            return RedirectToAction("Index");
        }

        //
        // POST: /Manage/Skills/Edit/5

        [HttpPost]
        public ActionResult Edit(Skill skill)
        {
            if (ModelState.IsValid)
            {
                var editedSkill = db.Skills.Include(x => x.SalesPackage).First(x => x.SkillId == skill.SkillId);
                db.Set<Skill>().Attach(editedSkill);

                var editedPackage = db.PvpSalesPackages.First(x => x.GameSalesPackageId == editedSkill.SalesPackage.GameSalesPackageId);
                db.Set<GameSalesPackage>().Attach(editedPackage);

                editedPackage.Gems = skill.SalesPackage.Gems;
                editedPackage.Economy = skill.SalesPackage.Economy;
                editedPackage.Free = skill.SalesPackage.Free;

                editedSkill.Name = skill.Name;
                editedSkill.Level = skill.Level;
                editedSkill.SalesPackage = editedPackage;
                
                db.SaveChanges();

                return RedirectToAction("Index", "Skills", new { searchString = skill.Name});
            }
            return View(skill);
        }

        //
        // GET: /Manage/Skills/Delete/5
 
        public ActionResult Delete(int id)
        {
            Skill skill = db.Skills.Find(id);
            return View(skill);
        }

        //
        // POST: /Manage/Skills/Delete/5

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(int id)
        {            
            Skill skill = db.Skills.Find(id);
            db.Skills.Remove(skill);
            db.SaveChanges();
            return RedirectToAction("Index");
        }

        protected override void Dispose(bool disposing)
        {
            db.Dispose();
            base.Dispose(disposing);
        }
    }
}