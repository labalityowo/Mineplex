namespace LOC.Website.Web.Controllers
{
    using System.Collections.Generic;
    using System.Data;
    using System.Data.Entity;
    using System.Linq;
    using System.Web.Mvc;
    using Common.Contexts;
    using Core.Model.Server.PvpServer;
    using Core.Tokens.Client;
    using Newtonsoft.Json;

    public class FishingController : Controller
    {
        private readonly LocContext _context = new LocContext();

        [HttpPost]
        public ContentResult GetFishingAllTimeHigh()
        {
            var tokenList = new List<FishToken>();

            foreach (var fish in _context.FishCatches.Where(x => string.Equals(x.Owner, "AllTimeHigh")).Include(x => x.Catcher))
            {
                tokenList.Add(new FishToken { Catcher = fish.Catcher.Name, Name = fish.Name, Size = fish.Size });
            }

            var json = JsonConvert.SerializeObject(tokenList);
            return Content(json, "application/json");
        }

        [HttpPost]
        public ContentResult GetFishingDayHigh()
        {
            var tokenList = new List<FishToken>();
            
            foreach (var fish in _context.FishCatches.Where(x => string.Equals(x.Owner, "DayHigh")).Include(x => x.Catcher))
            {
                tokenList.Add(new FishToken { Catcher = fish.Catcher.Name, Name = fish.Name, Size = fish.Size });
            }

            var json = JsonConvert.SerializeObject(tokenList);
            return Content(json, "application/json");
        }

        [HttpPost]
        public void ClearDailyFishingScores()
        {
            foreach (var id in _context.FishCatches.Where(x => string.Equals(x.Owner, "DayHigh")).Select(e => e.FishCatchId))
            {
                var entity = new FishCatch { FishCatchId = id };
                _context.FishCatches.Attach(entity);
                _context.FishCatches.Remove(entity);
            }

            _context.SaveChanges();
        }

        [HttpPost]
        public void SaveFishingAllTimeHigh(FishToken fishToken)
        {
            SaveFishingScore("AllTimeHigh", fishToken);
        }

        [HttpPost]
        public void SaveFishingDayHigh(FishToken fishToken)
        {
            SaveFishingScore("DayHigh", fishToken);
        }

        [HttpPost]
        public void SaveFishingScore(FishToken fishToken)
        {
            SaveFishingScore(fishToken.Catcher, fishToken);
        }

        private void SaveFishingScore(string owner, FishToken fishToken)
        {
            var account = _context.Accounts.First(x => string.Equals(x.Name, fishToken.Catcher));

            var fishCatch =
                _context.FishCatches.Where(
                    x => string.Equals(x.Owner, owner) && string.Equals(x.Name, fishToken.Name)).Include(x => x.Catcher).FirstOrDefault();

            if (fishCatch == null)
            {
                fishCatch = new FishCatch { Owner = owner };

                _context.FishCatches.Add(fishCatch);
            }
            else
            {
                if (string.Equals(owner, fishToken.Catcher))
                {
                    account.FishCatches.Remove(fishCatch);
                    account.FishCatches.Add(fishCatch);
                }

                _context.FishCatches.Attach(fishCatch);
            }

            fishCatch.Catcher = account;
            fishCatch.Name = fishToken.Name;
            fishCatch.Size = fishToken.Size;

            

            _context.Entry(account).State = EntityState.Modified;

            _context.SaveChanges();
        }
    }
}
