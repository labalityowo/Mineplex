namespace LOC.Website.Web.Controllers
{
    using System.Web.Mvc;

    public class PurchaseController : Controller
    {
        public ActionResult SinglePack(string id)
        {
            return Redirect(@"https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=3426KNUNF6Z3S&on1=Name&os1=" + id);
        }

        public ActionResult DoublePack(string id)
        {
            return Redirect(@"https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=G7BJ3LUJ5HDE6&on1=Name&os1=" + id);
        }

        public ActionResult MegaPack(string id)
        {
            return Redirect(@"https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AM33KJ2DW5RSS&on1=Name&os1=" + id);
        }

        public ActionResult UltraPack(string id)
        {
            return Redirect(@"https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AE478DQAV9EYL&on1=Name&os1=" + id);
        }

        public ActionResult TruckLoad(string id)
        {
            return Redirect(@"https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=YP6RBNHQ5XAWJ&on1=Name&os1=" + id);
        }

        public ActionResult SilverRank(string id)
        {
            return Redirect(@"https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=S4VKBM7BB4CP2&on1=Name&os1=" + id);
        }

        public ActionResult GoldRank(string id)
        {
            return Redirect(@"https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=MBQUM7FMGZ9UN&on1=Name&os1=" + id);
        }
    }
}
