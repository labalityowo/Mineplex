namespace LOC.Website.Web.ViewModels
{
    using System.Collections.Generic;
    using Common.Models;
    using Core.Model.Sales;

    public class StoreViewModel : IStoreViewModel
    {
        private readonly ISalesPackageAdministrator _salesPackageAdministrator;

        public StoreViewModel(ISalesPackageAdministrator salesPackageAdministrator)
        {
            _salesPackageAdministrator = salesPackageAdministrator;
        }

        public List<SalesPackage> SalesPackages
        {
            get { return _salesPackageAdministrator.GetSalesPackages(); }
        }
    }
}