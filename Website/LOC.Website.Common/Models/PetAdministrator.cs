namespace LOC.Website.Common.Models
{
    using System.Collections.Generic;
    using System.Data.Entity;
    using System.Linq;
    using Core.Model.Account;
    using Core.Model.Server;
    using Core.Tokens.Client;
    using Data;
using System;
    using System.Data.SqlClient;

    public class PetAdministrator : IPetAdministrator
    {
        private readonly INautilusRepositoryFactory _repositoryFactory;
        private readonly ILogger _logger;

        public PetAdministrator(INautilusRepositoryFactory repositoryFactory, ILogger logger)
        {
            _repositoryFactory = repositoryFactory;
            _logger = logger;
        }

        public List<Pet> GetPets(List<Pet> petTokens)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                bool added = false;
                foreach (var item in petTokens.Where(item => !repository.Any<Pet>(x => x.PetType == item.PetType)))
                {
                    repository.Add(item);
                    added = true;
                }

                if (added)
                    repository.CommitChanges();

                return repository.GetAll<Pet>().Include(x => x.SalesPackage).ToList();
            }
        }

        public List<PetExtra> GetPetExtras(List<PetExtra> petExtraTokens)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                bool added = false;
                foreach (var item in petExtraTokens.Where(item => !repository.Any<PetExtra>(x => x.Name == item.Name && x.Material == item.Material)))
                {
                    repository.Add(item);
                    added = true;
                }

                if (added)
                    repository.CommitChanges();

                return repository.GetAll<PetExtra>().Include(x => x.SalesPackage).ToList();
            }
        }

        public void AddPet(PetChangeToken token)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                var account = repository.Where<Account>(x => x.Name == token.Name).Include(x => x.Pets).FirstOrDefault();

                if (account == null)
                    return;

                account.Pets.Add(new OwnedPet { PetType = token.PetType, PetName = token.PetName });

                repository.CommitChanges();
            }
        }

        public void UpdatePet(PetChangeToken token)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                var account = repository.Where<Account>(x => x.Name == token.Name).Include(x => x.Pets).FirstOrDefault();

                if (account == null)
                    return;

                var pet = account.Pets.FirstOrDefault(x => x.PetType.Equals(token.PetType));
                
                if (pet == null)
                    return;

                account.Pets.RemoveAll(x => x.PetType.Equals(token.PetType) && x.OwnedPetId != pet.OwnedPetId);

                pet.PetName = token.PetName;

                repository.Edit(pet);
                repository.Edit(account);

                repository.CommitChanges();
            }
        }

        public void RemovePet(PetChangeToken token)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                var account = repository.Where<Account>(x => x.Name == token.Name).Include(x => x.Pets).FirstOrDefault();

                if (account == null)
                    return;

                var ownedPet = account.Pets.FirstOrDefault(x => x.PetType == token.PetType);

                if (ownedPet == null)
                    return;

                account.Pets.Remove(ownedPet);

                repository.CommitChanges();
            }
        }

        public void AddPetNameTag(string name)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                var account = repository.Where<Account>(x => x.Name == name).FirstOrDefault();

                if (account == null)
                    return;

                account.PetNameTagCount++;

                repository.CommitChanges();
            }
        }

        public void RemovePetNameTag(string name)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                var account = repository.Where<Account>(x => x.Name == name).FirstOrDefault();

                if (account == null)
                    return;

                account.PetNameTagCount--;

                if (account.PetNameTagCount < 0)
                    account.PetNameTagCount = 0;

                repository.CommitChanges();
            }
        }

        #region IPetAdministrator Members


        public List<AccountPetToken> GetBulkPets(int start, int count)
        {
            using (var repository = _repositoryFactory.CreateRepository())
            {
                try
                {
                    return repository.Context.Database.SqlQuery<AccountPetToken>(@"SELECT OwnedPetId AS PetId, Uuid, PetType, PetName FROM (SELECT OwnedPetId, A.Uuid, PetType, PetName, ROW_NUMBER() OVER (ORDER BY OwnedPetId) as row FROM Nautilus.dbo.OwnedPets INNER JOIN Nautilus.dbo.Accounts AS A ON A.AccountId = Account_AccountId) a WHERE row > " + start + @" AND row <= " + count + @";", string.Empty).ToList();
                }
                catch (Exception ex)
                {
                    _logger.Log("ERROR", ex.Message + " : " + ex.InnerException);
                }

                return new List<AccountPetToken>();
            }
        }

        #endregion
    }
}
