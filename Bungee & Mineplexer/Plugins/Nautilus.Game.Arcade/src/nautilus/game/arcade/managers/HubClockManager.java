package nautilus.game.arcade.managers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;
import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerStateChangeEvent;
import nautilus.game.arcade.game.GameTeam.PlayerState;

public class HubClockManager implements Listener
{

	private static final int HUB_CLOCK_SLOT = 8;
	private static final ItemStack HUB_CLOCK_ITEM = new ItemBuilder(Material.WATCH)
			.setTitle(C.cGreen + "Return to Hub")
			.addLore("", C.Reset + "Click while holding this", C.Reset + "to return to the Hub.")
			.build();

	private final ArcadeManager _manager;

	public HubClockManager(ArcadeManager manager)
	{
		_manager = manager;

		manager.registerEvents(this);
	}

	public void giveClock(Player player)
	{
		player.getInventory().setItem(HUB_CLOCK_SLOT, HUB_CLOCK_ITEM);
	}

	public void giveClockToAll()
	{
		UtilServer.getPlayersCollection().forEach(this::giveClock);
	}

	public void removeClock(Player player)
	{
		player.getInventory().remove(HUB_CLOCK_ITEM);
	}

	@EventHandler
	public void giveOnJoin(PlayerJoinEvent event)
	{
		giveClock(event.getPlayer());
	}

	@EventHandler
	public void giveOnNextLobby(GameStateChangeEvent event)
	{
		if (!canGiveClock())
		{
			return;
		}

		giveClockToAll();
	}

	@EventHandler
	public void giveOnDeath(PlayerStateChangeEvent event)
	{
		if (!canGiveClock())
		{
			return;
		}

		// Only handle when the player is now out
		if (event.GetState() == PlayerState.OUT)
		{
			_manager.runSyncLater(() -> giveClock(event.getPlayer()), GameSpectatorManager.ITEM_GIVE_DELAY);
		}
		// Fix for MineStrike
		else
		{
			removeClock(event.getPlayer());
		}
	}

	@EventHandler
	public void preventDrop(PlayerDropItemEvent event)
	{
		if (event.getItemDrop().getItemStack().equals(HUB_CLOCK_ITEM))
		{
			event.setCancelled(true);
			event.getPlayer().sendMessage(F.main("Game", "You can't drop the Hub Clock."));
		}
	}

	@EventHandler
	public void handleClockInteract(PlayerInteractEvent event)
	{
		// Don't trigger the clock when players walk on pressure plates
		if (event.getAction() == Action.PHYSICAL)
		{
			return;
		}

		Player player = event.getPlayer();

		// Only allow this exact clock to be used.
		if (!HUB_CLOCK_ITEM.equals(player.getItemInHand()))
		{
			return;
		}

		// Don't allow spamming so we don't make too many send requests
		if (!Recharge.Instance.usable(player, "Return to Hub"))
		{
			return;
		}

		// Send to server
		_manager.GetPortal().sendPlayerToGenericServer(player, GenericServer.HUB, Intent.PLAYER_REQUEST);
	}

	private boolean canGiveClock()
	{
		return _manager.GetGame() == null || _manager.GetGame().inLobby() && _manager.GetGame().GiveClock;
	}
}
