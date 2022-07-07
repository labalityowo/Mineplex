package mineplex.game.nano.game.components.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.arcadeevents.CoreGameStartEvent;
import mineplex.core.arcadeevents.CoreGameStopEvent;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.gadget.event.GadgetEnableEvent;
import mineplex.core.gadget.persistence.UserGadgetPersistence;
import mineplex.core.pet.event.PetSpawnEvent;
import mineplex.core.teleport.event.MineplexTeleportEvent;
import mineplex.game.nano.GameManager;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.components.ComponentHook;
import mineplex.game.nano.game.event.GameStateChangeEvent;

@ReflectivelyCreateMiniPlugin
public class GamePlayerManager extends GameManager implements ComponentHook<GamePlayerComponent>
{

	public enum Perm implements Permission
	{
		ALLOW_TELEPORTS_IN_GAME
	}

	private GamePlayerComponent _hook;

	private GamePlayerManager()
	{
		super("Player Hook");

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.ALLOW_TELEPORTS_IN_GAME, true, true);

		if (UtilServer.isTestServer())
		{
			PermissionGroup.QAT.setPermission(Perm.ALLOW_TELEPORTS_IN_GAME, true, true);
		}
	}

	@Override
	public void setHook(GamePlayerComponent hook)
	{
		_hook = hook;
	}

	@Override
	public GamePlayerComponent getHook()
	{
		return _hook;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void gameState(GameStateChangeEvent event)
	{
		switch (event.getState())
		{
			case Prepare:
				_manager.getAntiHack().enableAnticheat();
				UtilServer.CallEvent(new CoreGameStartEvent(NanoManager.getGameDisplay()));
				break;
			case End:
				_manager.getAntiHack().disableAnticheat();
				UtilServer.CallEvent(new CoreGameStopEvent(NanoManager.getGameDisplay()));
				break;
			case Dead:
				setHook(null);
				break;
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void foodLevelChange(FoodLevelChangeEvent event)
	{
		if (_hook == null || !_hook.isHunger() || UtilPlayer.isSpectator(event.getEntity()))
		{
			event.setFoodLevel(20);
		}

		if (event.getEntity() instanceof Player)
		{
			((Player) event.getEntity()).setSaturation(3.8F);
		}
	}

	@EventHandler
	public void regainHealth(EntityRegainHealthEvent event)
	{
		if (_hook == null || _hook.isRegainHealth() || event.getRegainReason() != RegainReason.SATIATED)
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void inventoryClick(InventoryClickEvent event)
	{
		if (_hook == null || !_hook.isItemMovement())
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void itemDrop(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();

		if (_hook == null || !_hook.isItemDropPickup() || UtilPlayer.isSpectator(player))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void itemPickup(PlayerPickupItemEvent event)
	{
		Player player = event.getPlayer();

		if (_hook == null || !_hook.isItemDropPickup() || UtilPlayer.isSpectator(player))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void entityCombust(EntityCombustEvent event)
	{
		if (_hook == null || UtilPlayer.isSpectator(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void hideParticles(GameStateChangeEvent event)
	{
		if (_hook == null || event.getState() != GameState.Prepare)
		{
			return;
		}

		_manager.getCosmeticManager().setHideParticles(_hook.isHideParticles());
		_manager.getCosmeticManager().disableItemsForGame();
	}

	@EventHandler
	public void enableCosmetics(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Loading)
		{
			return;
		}

		UserGadgetPersistence gadgetPersistence = _manager.getCosmeticManager().getGadgetManager().getUserGadgetPersistence();
		gadgetPersistence.setEnabled(true);

		for (Player player : UtilServer.getPlayersCollection())
		{
			gadgetPersistence.load(player);
		}
	}

	// Big hack to prevent people opening it in-game, remove this at some point
	@EventHandler
	public void cosmeticChest(InventoryOpenEvent event)
	{
		if (_hook == null || UtilPlayer.isSpectator(event.getPlayer()) || !event.getInventory().getName().equals("Cosmetics"))
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void gadgetEnable(GadgetEnableEvent event)
	{
		if (_hook == null)
		{
			return;
		}

		if (event.getGadget().getGadgetType().disableForGame())
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void petSpawn(PetSpawnEvent event)
	{
		if (_hook == null)
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void vehicleEnter(VehicleEnterEvent event)
	{
		if (UtilPlayer.isSpectator(event.getEntered()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerTeleport(MineplexTeleportEvent event)
	{
		Player player = event.getPlayer();

		if (_hook == null || !_hook.getGame().isAlive(player) || _manager.getClientManager().Get(player).hasPermission(Perm.ALLOW_TELEPORTS_IN_GAME))
		{
			return;
		}

		event.setCancelled(true);
		_hook.getGame().addSpectator(player, false, true);
	}
}
