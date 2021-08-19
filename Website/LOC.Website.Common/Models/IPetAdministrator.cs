namespace LOC.Website.Common.Models
{
    using System.Collections.Generic;
    using Core.Model.Server;
    using Core.Tokens.Client;

    public interface IPetAdministrator
    {
        List<Pet> GetPets(List<Pet> petTokens);
        List<AccountPetToken> GetBulkPets(int start, int count);
        List<PetExtra> GetPetExtras(List<PetExtra> petExtraTokens);
        void AddPet(PetChangeToken pet);
        void UpdatePet(PetChangeToken pet);
        void RemovePet(PetChangeToken pet);
        void AddPetNameTag(string name);
        void RemovePetNameTag(string name);
    }
}
