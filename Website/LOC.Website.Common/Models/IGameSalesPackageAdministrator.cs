namespace LOC.Website.Common.Models
{
    using System.Collections.Generic;
    using Core.Model.Sales;

    public interface IGameSalesPackageAdministrator
    {
        List<GameSalesPackage> GetSalesPackages();
        GameSalesPackage GetGameSalesPackageById(int id);
        void AddSalesPackage(GameSalesPackage salesPackage);
        void UpdateSalesPackage(GameSalesPackage salesPackage);
        void DeleteSalesPackage(GameSalesPackage salesPackage);
    }
}
