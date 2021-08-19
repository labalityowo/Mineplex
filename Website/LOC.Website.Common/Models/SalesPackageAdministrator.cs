namespace LOC.Website.Common.Models
{
    using System.Collections.Generic;
    using System.Data.Entity;
    using System.Linq;
    using Core.Model.Sales;
    using Data;

    public class SalesPackageAdministrator : ISalesPackageAdministrator
    {
        private readonly INautilusRepositoryFactory _repositoryFactory;

        public SalesPackageAdministrator(INautilusRepositoryFactory nautilusRepositoryFactory)
        {
            _repositoryFactory = nautilusRepositoryFactory;
        }

        public List<SalesPackage> GetSalesPackages()
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                return repository.GetAll<SalesPackage>().Include(x => x.Rank).ToList();
            }
        }

        public SalesPackage GetSalesPackageById(int id)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                return repository.Where<SalesPackage>(x => x.SalesPackageId == id).Include(x => x.Rank).First();
            }
        }

        public void AddSalesPackage(SalesPackage salesPackage)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                repository.Add(salesPackage);
                repository.CommitChanges();
            }
        }

        public void UpdateSalesPackage(SalesPackage salesPackage)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                repository.Attach(salesPackage);
                repository.CommitChanges();
            }
        }

        public void DeleteSalesPackage(SalesPackage salesPackage)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                repository.Delete(salesPackage);
                repository.CommitChanges();
            }
        }
    }
}
