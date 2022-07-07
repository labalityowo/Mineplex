package mineplex.staffServer.customerSupport;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.bonuses.BonusRepository;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.donation.DonationManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.pet.repository.PetRepository;
import mineplex.core.powerplayclub.PowerPlayClubRepository;
import mineplex.staffServer.repository.SupportRepository;

public class CustomerSupport extends MiniPlugin
{
	public enum Perm implements Permission
	{
		CHECK_OWNS_PACKAGE_COMMAND,
		CHECK_COMMAND,
		JOIN_SERVER,
	}
	
	private CoreClientManager _clientManager;
	private DonationManager _donationManager;
	private InventoryManager _inventoryManager;
	private BonusRepository _bonusRepository;

	private SupportRepository _repository;
	private PowerPlayClubRepository _powerPlayRepo;
	private PetRepository _petRepository;
	
	private NautHashMap<Integer, List<String>> _accountBonusLog = new NautHashMap<>();
	
	private boolean _allowWeatherChange = false;

	public CustomerSupport(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager, PowerPlayClubRepository powerPlayRepo, InventoryManager inventoryManager, BonusRepository bonusRepository)
	{
		super("Support", plugin);

		_clientManager = clientManager;
		_donationManager = donationManager;
		_inventoryManager = inventoryManager;
		_bonusRepository = bonusRepository;
		_powerPlayRepo = powerPlayRepo;

		_repository = new SupportRepository();
		_petRepository = new PetRepository();
		
		_allowWeatherChange = true;
		Bukkit.getWorlds().get(0).setStorm(false);
		_allowWeatherChange = false;

		addCommand(new CheckCommand(this));
		addCommand(new CheckOwnsPackageCommand(this));

		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.SUPPORT.setPermission(Perm.CHECK_OWNS_PACKAGE_COMMAND, true, true);
		PermissionGroup.SUPPORT.setPermission(Perm.CHECK_COMMAND, true, true);
		PermissionGroup.SUPPORT.setPermission(Perm.JOIN_SERVER, true, true);
	}

	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}

	public SupportRepository getRepository()
	{
		return _repository;
	}

	public DonationManager getDonationManager()
	{
		return _donationManager;
	}

	public InventoryManager getInventoryManager()
	{
		return _inventoryManager;
	}

	public PowerPlayClubRepository getPowerPlayRepo()
	{
		return _powerPlayRepo;
	}

	public PetRepository getPetRepository()
	{
		return _petRepository;
	}

	public BonusRepository getBonusRepository()
	{
		return _bonusRepository;
	}

	@EventHandler
	public void Join(PlayerJoinEvent event)
	{
		if (!_clientManager.Get(event.getPlayer()).hasPermission(Perm.JOIN_SERVER))
		{
			event.getPlayer().kickPlayer("Only for support staff.");
			return;
		}
		
		event.setJoinMessage(F.sys("Join", event.getPlayer().getName()));
	}
	
	@EventHandler
	public void Quit(PlayerQuitEvent event)
	{
		event.setQuitMessage(F.sys("Quit", event.getPlayer().getName()));
	}
	
	@EventHandler
	public void PlayerChat(AsyncPlayerChatEvent event)
	{
		if (event.isCancelled())
			return;

		event.setFormat(C.cGold + "%1$s " + C.cWhite + "%2$s");
	}
	
	@EventHandler
	public void blockBreak(BlockBreakEvent event)
	{
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
			event.setCancelled(true);
	}

	@EventHandler
	public void foodLevelChange(FoodLevelChangeEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void entityDeath(EntityDamageEvent event)
	{
		if (event.getCause() == DamageCause.VOID)
			event.getEntity().teleport(event.getEntity().getWorld().getSpawnLocation());
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void stopRain(WeatherChangeEvent event)
	{
		if (!_allowWeatherChange)
		{
			event.setCancelled(true);
		}
	}

	private String getLockedPackageStr(UUID uuid, String name)
	{
		if (_donationManager.Get(uuid).ownsUnknownSalesPackage(name))
		{
			return C.cGreen + C.Bold + "Unlocked";
		}
		return C.cRed + C.Bold + "Locked";
	}
}
