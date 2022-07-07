package mineplex.core.progression;

import mineplex.core.MiniClientPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.progression.data.PlayerKit;
import mineplex.core.progression.data.PlayerKitDataManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * The main class, process logins and loads data.
 */
public class KitProgressionManager extends MiniClientPlugin<PlayerKit>
{

	private PlayerKitDataManager _dataManager;
	private KitProgressionRepository _kitProgressionRepository;
	private DonationManager _donationManager;
	private CoreClientManager _coreClientManager;

	public KitProgressionManager(JavaPlugin plugin, DonationManager donationManager, CoreClientManager clientManager)
	{
		super("Kit Progression", plugin);
		_dataManager = new PlayerKitDataManager();
		_kitProgressionRepository = new KitProgressionRepository(this);
		_donationManager = donationManager;
		_coreClientManager = clientManager;
	}
	
	public DonationManager getDonationManager()
	{
		return _donationManager;
	}
	
	public CoreClientManager getClientManager()
	{
		return _coreClientManager;
	}

	@Override
	protected PlayerKit addPlayer(UUID uuid)
	{
		return null;
	}

	public PlayerKitDataManager getDataManager()
	{
		return _dataManager;
	}

	public KitProgressionRepository getRepository()
	{
		return _kitProgressionRepository;
	}
}
