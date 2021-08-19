namespace LOC.Website.Web.Controllers
{
    using System;
    using System.Collections.Generic;
    using System.IO;
    using System.Linq;
    using System.Net;
    using System.Text;
    using System.Web.Mvc;
    using Common;
    using Common.Models;
    using ViewModels;

    public class PaypalTestController : Controller
    {
        private readonly IAccountAdministrator _accountAdministrator;
        private readonly ISalesPackageAdministrator _salesPackageAdministrator;
        private readonly IStoreViewModel _storeModel;
        private readonly ILogger _logger;

        public PaypalTestController(IAccountAdministrator accountAdministrator, ISalesPackageAdministrator salesPackageAdministrator, IStoreViewModel storeModel, ILogger logger)
        {
            _accountAdministrator = accountAdministrator;
            _salesPackageAdministrator = salesPackageAdministrator;
            _storeModel = storeModel;
            _logger = logger;
        }

        public ActionResult Ipn() 
        {
            var formVals = new Dictionary<string, string>();
            formVals.Add("cmd", "_notify-validate");

            string response = GetPayPalResponse(formVals, true);

            if (response == "VERIFIED") 
            {
                var transactionType = Request["txn_type"];

                if (String.Equals(transactionType, "web_accept"))
                {
                    var itemNumber = Request["item_number"];
                    var playerName = Request["option_selection1"];

                    try
                    {
                        var account = _accountAdministrator.GetAccountByName(playerName);

                        if (account == null)
                        {
                            if(!String.IsNullOrEmpty(playerName))
                            {
                                account = _accountAdministrator.CreateAccount(playerName);
                            }
                            else
                            {
                                account = _accountAdministrator.CreateAccount("NullPlayerNameInTransactionRequest");
                            }
                        }

                        var salesPackage = _salesPackageAdministrator.GetSalesPackageById(Convert.ToInt32(itemNumber));

                        _accountAdministrator.ApplySalesPackage(salesPackage, account.AccountId);
                    }
                    catch (Exception ex)
                    {
                        StringBuilder sb = new StringBuilder();

                        foreach (var variable in Request.Params.AllKeys)
                        {
                            sb.AppendLine(variable + "=" + Request.Params[variable]);
                        }

                        _logger.Log("Error", sb + ex.Message + "\n" + ex.StackTrace);
                        throw;
                    }
                }
            }

            return View();
        }

        public ActionResult Receipt(int transactionId)
        {
            return View(transactionId);
        }

        public ActionResult Index()
        {
            return View(_storeModel.SalesPackages.Where(x => x.Test).ToList());
        }

        /// <summary>
        /// Utility method for handling PayPal Responses
        /// </summary>
        string GetPayPalResponse(Dictionary<string, string> formVals, bool useSandbox) {

            string paypalUrl = useSandbox ? "https://www.sandbox.paypal.com/cgi-bin/webscr"
                : "https://www.paypal.com/cgi-bin/webscr";


            HttpWebRequest req = (HttpWebRequest)WebRequest.Create(paypalUrl);

            // Set values for the request back
            req.Method = "POST";
            req.ContentType = "application/x-www-form-urlencoded";

            byte[] param = Request.BinaryRead(Request.ContentLength);
            string strRequest = Encoding.ASCII.GetString(param);

            StringBuilder sb = new StringBuilder();
            sb.Append(strRequest);

            foreach (string key in formVals.Keys) 
            {
                sb.AppendFormat("&{0}={1}", key, formVals[key]);
            }

            strRequest += sb.ToString();
            req.ContentLength = strRequest.Length;

            //Send the request to PayPal and get the response
            string response = "";
            using (var streamOut = new StreamWriter(req.GetRequestStream(), System.Text.Encoding.ASCII)) 
            {
                streamOut.Write(strRequest);
                streamOut.Close();
                using (var streamIn = new StreamReader(req.GetResponse().GetResponseStream())) 
                {
                    response = streamIn.ReadToEnd();
                }
            }

            return response;
        }
    }
}
