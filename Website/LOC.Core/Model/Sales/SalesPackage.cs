namespace LOC.Core.Model.Sales
{
    using Account;

    public class SalesPackage
    {
        public int SalesPackageId { get; set; }

        public string Name { get; set; }

        public string Description { get; set; }

        public bool Test { get; set; }

        public double Price { get; set; }

        public int Gems { get; set; }

        public Rank Rank { get; set; }

        public bool RankPerm { get; set; }

        public long Length { get; set; }

        public string Image { get; set; }

        public string PaypalButtonId { get; set; }
    }
}
