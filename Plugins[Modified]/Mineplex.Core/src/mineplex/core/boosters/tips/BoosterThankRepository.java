package mineplex.core.boosters.tips;

import mineplex.core.database.MinecraftRepository;
import mineplex.database.routines.CheckAmplifierThank;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Shaun Bennett
 */
public class BoosterThankRepository extends RepositoryBase
{
	public BoosterThankRepository(JavaPlugin plugin)
	{
		super(DBPool.getAccount());
	}

	/**
	 * Checks the database if an accountId is allowed to thank a specific Amplifier.
	 * This will return true and update the database if the thank is okay, or false
	 * if that account ID has already thanked that Amplifier ID.
	 *
	 * @param accountId Account ID of the player trying to thank the Amplifier
	 * @param amplifierId The ID of the Amplifier the player is trying to thank
	 * @return True if the account id can thank the amplifier id, false otherwise
	 */
	public boolean checkAmplifierThank(int accountId, int amplifierId)
	{
		CheckAmplifierThank checkAmplifierThank = new CheckAmplifierThank();
		checkAmplifierThank.setInAccountId(accountId);
		checkAmplifierThank.setInAmplifierId(amplifierId);
		checkAmplifierThank.execute(jooq().configuration());
		return checkAmplifierThank.getCanThank() == 1;
	}
}
