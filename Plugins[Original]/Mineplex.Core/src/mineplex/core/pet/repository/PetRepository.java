package mineplex.core.pet.repository;

import mineplex.core.database.MinecraftRepository;
import mineplex.core.pet.repository.token.PetChangeToken;
import mineplex.serverdata.database.DBPool;

public class PetRepository extends MinecraftRepository
{
	public PetRepository()
	{
		super(DBPool.getAccount());
	}

	public void AddPet(final PetChangeToken token)
	{
		handleAsyncMSSQLCall("Pets/AddPet", token);
	}

	public void UpdatePet(final PetChangeToken token)
	{
		handleAsyncMSSQLCall("Pets/UpdatePet", token);
	}
}
