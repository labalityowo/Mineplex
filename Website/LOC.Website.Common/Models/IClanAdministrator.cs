using LOC.Core.Tokens.Clan;

namespace LOC.Website.Common.Models
{
    using System.Collections.Generic;

    public interface IClanAdministrator
    {
        List<ClanToken> GetClans(string serverName);
        void AddClan(ClanToken clan);
        void EditClan(ClanToken clan);
        void DeleteClan(ClanToken clan);
        void UpdateClanTNTGenerators(List<ClanGeneratorToken> tokens);
        void UpdateClanTNTGenerator(ClanGeneratorToken token);

        void ResetClanData();
    }
}
