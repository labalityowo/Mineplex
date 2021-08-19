namespace LOC.Website.Web.ViewModels
{
    using System.Collections.Generic;
    using Core.Model.Sales;

    public interface IStoreViewModel
    {
        List<SalesPackage> SalesPackages { get; }
    }
}
