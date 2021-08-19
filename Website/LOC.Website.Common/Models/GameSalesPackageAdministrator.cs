namespace LOC.Website.Common.Models
{
    using System.Collections.Generic;
    using System.Data;
    using System.Linq;
    using Core.Model.Sales;
    using Data;

    public class GameSalesPackageAdministrator : IGameSalesPackageAdministrator
    {
        private readonly INautilusRepositoryFactory _repositoryFactory;

        public GameSalesPackageAdministrator(INautilusRepositoryFactory nautilusRepositoryFactory)
        {
            _repositoryFactory = nautilusRepositoryFactory;
        }

        public List<GameSalesPackage> GetSalesPackages()
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                return repository.GetAll<GameSalesPackage>().ToList();
            }
        }

        public GameSalesPackage GetGameSalesPackageById(int id)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                return repository.Where<GameSalesPackage>(x => x.GameSalesPackageId == id).First();
            }
        }

        public void AddSalesPackage(GameSalesPackage salesPackage)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                repository.Add(salesPackage);
                repository.CommitChanges();
            }
        }

        public void UpdateSalesPackage(GameSalesPackage salesPackage)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                repository.Edit(salesPackage);
                repository.CommitChanges();
            }
        }

        public void DeleteSalesPackage(GameSalesPackage salesPackage)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                repository.Delete(salesPackage);
                repository.CommitChanges();
            }
        }
    }
}
