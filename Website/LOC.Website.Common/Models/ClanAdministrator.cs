using LOC.Core.Data;
using LOC.Core.Model.Account;
using LOC.Core.Tokens.Clan;

namespace LOC.Website.Common.Models
{
    using System.Collections.Generic;
    using System.Data.Entity;
    using System.Linq;
    using Core.Model.Server.PvpServer.Clan;
    using Data;

    public class ClanAdministrator : IClanAdministrator
    {
        private readonly INautilusRepositoryFactory _repositoryFactory;

        private static readonly object _clanLock = new object();

        public ClanAdministrator(INautilusRepositoryFactory repositoryFactory)
        {
            _repositoryFactory = repositoryFactory;
        }

        public List<ClanToken> GetClans(string serverName)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                var clanTokens = new List<ClanToken>();

                foreach (var clan in repository.GetAll<Clan>()
                                               .Include(x => x.Alliances.Select(y => y.Clan))
                                               .Include(x => x.Wars.Select(y => y.Clan))
                                               .Include(x => x.Territories)
                                               .Include(x => x.Members.Select(y => y.ClanRole))
                                               .ToList())
                {
                    foreach (var territory in clan.Territories.Where(x => x.ServerName != serverName).ToList())
                    {
                        clan.Territories.Remove(territory);
                    }

                    clanTokens.Add(new ClanToken(clan));
                }

                return clanTokens;
            }
        }

        public void AddClan(ClanToken token)
        {
            lock (_clanLock)
            {
                using (var repository = _repositoryFactory.CreateRepository())
                {
                    var clan = new Clan
                        {
                            Members = new List<Account>(),
                            Alliances = new List<Alliance>(),
                            Wars = new List<War>(),
                            Territories = new List<Territory>()
                        };

                    UpdateClanInformation(repository, clan, token);

                    repository.Add(clan);
                    repository.CommitChanges();
                }
            }
        }

        public void EditClan(ClanToken token)
        {
            lock (_clanLock)
            {
                using (var repository = _repositoryFactory.CreateRepository())
                {
                    var clan =
                        repository.Where<Clan>(x => x.Name == token.Name)
                                  .Include(x => x.Members)
                                  .Include(x => x.Alliances.Select(y => y.Clan))
                                  .Include(x => x.Wars.Select(y => y.Clan))
                                  .Include(x => x.Territories)
                                  .First();
                    UpdateClanInformation(repository, clan, token);

                    repository.Edit(clan);
                    repository.CommitChanges();
                }
            }
        }

        public void DeleteClan(ClanToken token)
        {
            lock (_clanLock)
            {
                using (var repository = _repositoryFactory.CreateRepository())
                {
                    var clan =
                        repository.Where<Clan>(x => x.Name == token.Name)
                                  .Include(x => x.Members)
                                  .Include(x => x.Alliances.Select(y => y.Clan))
                                  .Include(x => x.Wars.Select(y => y.Clan))
                                  .Include(x => x.Territories)
                                  .First();
                    
                    foreach (var clanMember in clan.Members.ToList())
                    {
                        clanMember.Clan = null;
                        clanMember.ClanRole = null;
                        repository.Edit(clanMember);
                    }

                    if (clan.Alliances != null)
                    {
                        foreach (var alliance in clan.Alliances.ToList())
                        {
                            repository.Delete(alliance);
                        }
                    }

                    if (clan.Wars != null)
                    {
                        foreach (var war in clan.Wars.ToList())
                        {
                            repository.Delete(war);
                        }
                    }

                    var alliancesToThisClan = repository.Where<Alliance>(x => x.Clan.Name == clan.Name);

                    foreach (var alliance in alliancesToThisClan.ToList())
                    {
                        repository.Delete(alliance);
                    }

                    var warsToThisClan = repository.Where<War>(x => x.Clan.Name == clan.Name);

                    foreach (var war in warsToThisClan.ToList())
                    {
                        repository.Delete(war);
                    }

                    repository.Delete(clan);
                    repository.CommitChanges();
                }
            }
        }

        public void UpdateClanTNTGenerators(List<ClanGeneratorToken> tokens)
        {
            lock (_clanLock)
            {
                using (var repository = _repositoryFactory.CreateRepository())
                {
                    foreach (var token in tokens)
                    {
                        UpdateClanTNTGenerator(repository, token);
                    }

                    repository.CommitChanges();
                }
            }
        }

        public void UpdateClanTNTGenerator(ClanGeneratorToken token)
        {
            lock (_clanLock)
            {
                using (var repository = _repositoryFactory.CreateRepository())
                {
                    UpdateClanTNTGenerator(repository, token);

                    repository.CommitChanges();
                }
            }
        }

        public void ResetClanData()
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                foreach (var clan in repository.GetAll<Clan>())
                {
                    if (clan.Territories != null)
                        clan.Territories.Clear();

                    if (clan.Wars != null)
                        clan.Wars.Clear();

                    if (clan.Alliances != null)
                        clan.Alliances.Clear();

                    clan.Generator = null;
                    clan.GeneratorStock = 0;
                    clan.GeneratorTime = 0;

                    repository.Edit(clan);
                }

                foreach (var war in repository.GetAll<War>().ToList())
                {
                    repository.Delete(war);    
                }

                foreach (var alliance in repository.GetAll<Alliance>().ToList())
                {
                    repository.Delete(alliance);
                }

                foreach (var territory in repository.GetAll<Territory>().Where(x => x.ServerName == "PVP").ToList())
                {
                    repository.Delete(territory);
                }

                repository.CommitChanges();
            }
        }

        private void UpdateClanTNTGenerator(IRepository repository, ClanGeneratorToken token)
        {
            var clan = repository.Where<Clan>(x => string.Equals(x.Name, token.Name)).First();

            clan.Generator = token.Location;
            clan.GeneratorStock = token.Stock;
            clan.GeneratorTime = token.Time;

            repository.Edit(clan);
        }

        private void UpdateClanInformation(IRepository repository, Clan clan, ClanToken token)
        {
            clan.Name = token.Name;
            clan.Description = token.Description;
            clan.Power = token.Power;
            clan.Home = token.Home;
            clan.Admin = token.Admin;
            clan.DateCreated = token.DateCreated;
            clan.LastTimeOnline = token.LastTimeOnline;

            UpdateClanMembers(repository, clan, token);
            UpdateClanAlliances(repository, clan, token);
            UpdateClanWars(repository, clan, token);
            UpdateClanTerritories(repository, clan, token);
        }

        private void UpdateClanMembers(IRepository repository, Clan clan, ClanToken token)
        {
            if (clan.Members != null)
            {
                // Remove existing that aren't in token list
                var enumerator = clan.Members.Where(member => token.Members.All(x => x.Name != member.Name)).ToList().GetEnumerator();

                while(enumerator.MoveNext())
                {
                    enumerator.Current.Clan = null;
                    enumerator.Current.ClanRole = null;
                    repository.Edit(enumerator.Current);
                }
            }
            else
            {
                clan.Members = new List<Account>();
            }

            if (token.Members == null)
                return;

            // Add or update
            foreach (var member in token.Members)
            {
                var account = repository.GetAll<Account>().First(x => x.Name == member.Name);
                account.Clan = clan;
                account.ClanRole = repository.Where<ClanRole>(x => x.Name == member.ClanRole.Name).First();

                repository.Edit(account);
            }
        }

        private void UpdateClanAlliances(IRepository repository, Clan clan, ClanToken token)
        {
            if (clan.Alliances != null)
            {
                foreach (var relationship in clan.Alliances.ToList())
                {
                    repository.Delete(relationship);
                }
            }
            else
            {
                clan.Alliances = new List<Alliance>();
            }

            if (token.Alliances == null)
                return;

            // Add or update
            foreach (var relationshipToken in token.Alliances)
            {
                var relationship = new Alliance
                                       {
                                           Clan =
                                               repository.Where<Clan>(x => x.Name == relationshipToken.ClanName).First(),
                                           Trusted = relationshipToken.Trusted,
                                       };

                clan.Alliances.Add(relationship);
            }
        }

        private void UpdateClanWars(IRepository repository, Clan clan, ClanToken token)
        {
            if (clan.Wars != null)
            {
                foreach (var relationship in clan.Wars.ToList())
                {
                    repository.Delete(relationship);
                }
            }
            else
            {
                clan.Wars = new List<War>();
            }

            if (token.Wars == null)
                return;

            // Add or update
            foreach (var relationshipToken in token.Wars)
            {
                var relationship = new War
                {
                    Clan =
                        repository.Where<Clan>(x => x.Name == relationshipToken.ClanName).First(),
                    Cooldown = relationshipToken.Cooldown,
                    Ended = relationshipToken.Ended,
                    Dominance = relationshipToken.Dominance,
                };

                clan.Wars.Add(relationship);
            }
        }

        private void UpdateClanTerritories(IRepository repository, Clan clan, ClanToken token)
        {
            if (clan.Territories != null)
            {
                foreach (var relationship in clan.Territories.ToList())
                {
                    repository.Delete(relationship);
                }
            }
            else
            {
                clan.Territories = new List<Territory>();
            }

            if (token.Territories == null)
                return;

            // Add or update
            foreach (var territoryToken in token.Territories)
            {
                var territory = new Territory
                                    {
                                        Chunk = territoryToken.Chunk,
                                        Clan = clan,
                                        Safe = territoryToken.Safe,
                                        ServerName = territoryToken.ServerName
                                    };

                clan.Territories.Add(territory);
            }
        }
    }
}
