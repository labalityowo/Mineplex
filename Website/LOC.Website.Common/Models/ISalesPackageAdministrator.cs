namespace LOC.Website.Common.Models
{
    using System.Collections.Generic;
    using Core.Model.Sales;

    public interface ISalesPackageAdministrator
    {
        List<SalesPackage> GetSalesPackages();
        SalesPackage GetSalesPackageById(int id);
        void AddSalesPackage(SalesPackage salesPackage);
        void UpdateSalesPackage(SalesPackage salesPackage);
        void DeleteSalesPackage(SalesPackage salesPackage);
    }
}
