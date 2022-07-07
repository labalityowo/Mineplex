package mineplex.gemhunters.economy;

import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import mineplex.core.MiniClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.donation.DonationManager;
import mineplex.core.donation.Donor;
import mineplex.gemhunters.economy.command.GiveGemsCommand;
import mineplex.gemhunters.economy.event.PlayerCashOutCompleteEvent;
import mineplex.gemhunters.economy.event.PlayerEarnGemsEvent;
import mineplex.gemhunters.spawn.event.PlayerTeleportIntoMapEvent;

@ReflectivelyCreateMiniPlugin
public class EconomyModule extends MiniClientPlugin<Integer>
{
	public enum Perm implements Permission
	{
		GIVE_GEMS_COMMAND,
	}

	public static final float GEM_KILL_FACTOR = 0.5F;
	public static final int GEM_START_COST = 100;

	private final DonationManager _donation;

	private Player _mostValuable;
	private int _mostGems;

	public EconomyModule()
	{
		super("Economy");

		_donation = require(DonationManager.class);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.GIVE_GEMS_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new GiveGemsCommand(this));
	}

	@EventHandler
	public void teleportIn(PlayerTeleportIntoMapEvent event)
	{
		Player player = event.getPlayer();
		addToStore(event.getPlayer(), null, GEM_START_COST);

		Donor donor = _donation.Get(event.getPlayer());

		if (donor.getBalance(GlobalCurrency.GEM) >= GEM_START_COST)
		{
			_donation.purchaseUnknownSalesPackage(player, "Gem Hunters Access", GlobalCurrency.GEM, GEM_START_COST, false, null);
		}
	}

	@EventHandler
	public void death(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		Entity killer = event.getEntity().getKiller();

		int oldGems = getGems(player);

		if (killer != null)
		{
			Player killerPlayer = (Player) killer;
			// Don't award the player negative gems, in the case of an overflow
			int newGems = Math.min((int) (oldGems * GEM_KILL_FACTOR), 0);

			addToStore(killerPlayer, "Killing " + F.name(player.getName()), newGems);
		}

		removeFromStore(player, oldGems);
	}

	@EventHandler
	public void cashOut(PlayerCashOutCompleteEvent event)
	{
		event.incrementGems(getGems(event.getPlayer()));
	}

	public void addToStore(Player player, String reason, int gems)
	{
		PlayerEarnGemsEvent event = new PlayerEarnGemsEvent(player, gems, reason);
		UtilServer.CallEvent(event);
		
		if (event.isCancelled() || event.getGems() == 0)
		{
			return;
		}
		
		gems = event.getGems();
		reason = event.getReason();
		
		Set(player, Get(player) + gems);

		if (reason != null)
		{
			player.sendMessage(F.main(_moduleName, "+" + F.currency(GlobalCurrency.GEM, gems) + " (" + reason + ")."));
		}
	}

	public void removeFromStore(Player player, int gems)
	{
		addToStore(player, null, -gems);
	}

	public void setStore(Player player, int gems)
	{
		Set(player, gems);
	}

	public int getGems(Player player)
	{
		return Get(player);
	}

	@Override
	protected void Set(Player player, Integer data)
	{
		super.Set(player, data);

		if (_mostValuable == null || _mostGems < data)
		{
			_mostValuable = player;
			_mostGems = data;
		}
	}

	public Player getMostValuablePlayer()
	{
		return _mostValuable;
	}

	@Override
	protected Integer addPlayer(UUID uuid)
	{
		return 0;
	}
}