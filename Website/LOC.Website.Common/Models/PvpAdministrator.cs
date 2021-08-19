namespace LOC.Website.Common.Models
{
    using System.Collections.Generic;
    using System.Data.Entity;
    using System.Linq;
    using Core.Model.PvpServer;
    using Core.Model.Server.PvpServer;
    using Data;

    public class PvpAdministrator : IPvpAdministrator
    {
        protected readonly INautilusRepositoryFactory RepositoryFactory;

        public PvpAdministrator(INautilusRepositoryFactory repositoryFactory)
        {
            RepositoryFactory = repositoryFactory;
        }

        public List<Item> GetItems(List<Item> items)
        {
            using (var repository = RepositoryFactory.CreateRepository())
            {
                foreach (var item in items.Where(item => !repository.Any<Item>(x => x.Name == item.Name)))
                {
                    repository.Add(item);
                }

                repository.CommitChanges();

                return repository.GetAll<Item>().Include(x => x.SalesPackage).ToList();
            }
        }

        public List<Skill> GetSkills(List<Skill> skills)
        {
            using (var repository = RepositoryFactory.CreateRepository())
            {
                bool added = false;
                foreach (var skill in skills.Where(skill => !repository.Any<Skill>(x => x.Name == skill.Name && x.Level == skill.Level)))
                {
                    repository.Add(skill);
                    added = true;
                }

                if (added)
                    repository.CommitChanges();

                return repository.GetAll<Skill>().Include(x => x.SalesPackage).ToList();
            }
        }

        public List<Weapon> GetWeapons(List<Weapon> weapons)
        {
            using (var repository = RepositoryFactory.CreateRepository())
            {
                foreach (var weapon in weapons.Where(weapon => !repository.Any<Weapon>(x => x.Name == weapon.Name)))
                {
                    repository.Add(weapon);
                }

                repository.CommitChanges();

                return repository.GetAll<Weapon>().Include(x => x.SalesPackage).ToList();
            }
        }

        public List<PvpClass> GetPvpClasses(List<PvpClass> pvpClasses)
        {
            using (var repository = RepositoryFactory.CreateRepository())
            {
                foreach (var pvpClass in pvpClasses.Where(pvpClass => !repository.Any<PvpClass>(x => x.Name == pvpClass.Name)))
                {
                    repository.Add(pvpClass);
                }

                repository.CommitChanges();

                return repository.GetAll<PvpClass>().Include(x => x.SalesPackage).ToList();
            }
        }

        public List<BenefitItem> GetBenefitItems(List<BenefitItem> benefitItems)
        {
            using (var repository = RepositoryFactory.CreateRepository())
            {
                foreach (var benefitItem in benefitItems.Where(x => !repository.Any<BenefitItem>(y => y.Name == x.Name)))
                {
                    repository.Add(benefitItem);
                }

                repository.CommitChanges();

                return repository.GetAll<BenefitItem>().Include(x => x.SalesPackage).ToList();
            }
        }
    }
}
