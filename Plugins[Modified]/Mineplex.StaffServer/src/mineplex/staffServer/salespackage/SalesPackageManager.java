package mineplex.staffServer.salespackage;

import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.donation.DonationManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.pet.repository.PetRepository;
import mineplex.core.powerplayclub.PowerPlayClubRepository;
import mineplex.core.stats.StatsManager;
import mineplex.staffServer.salespackage.command.Sales;

public class SalesPackageManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		SALES_COMMAND,
		DISPLAY_PACKAGE_COMMAND,
	}

	private CoreClientManager _clientManager;
	private DonationManager _donationManager;
	private InventoryManager _inventoryManager;
	private StatsManager _statsManager;
	private PowerPlayClubRepository _powerPlayRepo;
	private PetRepository _petRepo;

	public SalesPackageManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager, InventoryManager inventoryManager, StatsManager statsManager, PowerPlayClubRepository powerPlayRepo)
	{
		super("SalesPackageManager", plugin);
		
		_clientManager = clientManager;
		_donationManager = donationManager;
		_inventoryManager = inventoryManager;
		_statsManager = statsManager;
		_powerPlayRepo = powerPlayRepo;
		
		_petRepo = new PetRepository();

		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.SUPPORT.setPermission(Perm.SALES_COMMAND, true, true);
		PermissionGroup.SUPPORT.setPermission(Perm.DISPLAY_PACKAGE_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new Sales(this));
	}

	public DonationManager getDonationManager()
	{
		return _donationManager;
	}

	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}

	public InventoryManager getInventoryManager()
	{
		return _inventoryManager;
	}

	public PowerPlayClubRepository getPowerPlay()
	{
		return _powerPlayRepo;
	}

	public PetRepository getPetRepo()
	{
		return _petRepo;
	}

	public StatsManager getStatsManager()
	{
		return _statsManager;
	}
}