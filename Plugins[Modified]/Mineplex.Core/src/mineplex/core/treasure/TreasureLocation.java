package mineplex.core.treasure;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.Managers;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.Hologram.HologramTarget;
import mineplex.core.hologram.HologramManager;
import mineplex.core.recharge.Recharge;
import mineplex.core.treasure.event.TreasureFinishEvent;
import mineplex.core.treasure.event.TreasureStartEvent;
import mineplex.core.treasure.types.Treasure;
import mineplex.core.treasure.types.TreasureType;
import mineplex.core.treasure.ui.TreasureShop;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class TreasureLocation implements Listener
{

	private static final HologramManager HOLOGRAM_MANAGER = Managers.require(HologramManager.class);
	private static final String HOLOGRAM_TEXT = C.cGreenB + "Open Treasure";

	private final TreasureManager _manager;
	private final Location _chest;
	private final List<Location> _chests;
	private final TreasureShop _shop;
	private final Map<Player, Hologram> _hologramMap;

	private TreasureSession _session;
	private boolean _colourTick;

	public TreasureLocation(TreasureManager manager, Location chest)
	{
		_manager = manager;
		_chest = chest;
		_chests = Arrays.asList(
				_chest.clone().add(3, 0, 1),
				_chest.clone().add(3, 0, -1),
				_chest.clone().add(-3, 0, 1),
				_chest.clone().add(-3, 0, -1),
				_chest.clone().add(1, 0, 3),
				_chest.clone().add(-1, 0, 3),
				_chest.clone().add(1, 0, -3),
				_chest.clone().add(-1, 0, -3)
		);
		_shop = new TreasureShop(manager, manager.getClientManager(), manager.getDonationManager(), this);
		_hologramMap = new HashMap<>();

		setHologramVisible(true);
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK) || inUse())
		{
			return;
		}

		Location location = event.getClickedBlock().getLocation();

		if (UtilMath.offsetSquared(location, _chest) > 2)
		{
			return;
		}

		event.setCancelled(true);
		_shop.attemptShopOpen(event.getPlayer());
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || inUse())
		{
			return;
		}

		for (Player player : UtilServer.getPlayersCollection())
		{
			Hologram hologram = _hologramMap.get(player);

			if (hologram == null)
			{
				hologram = new Hologram(HOLOGRAM_MANAGER, _chest.clone().add(0, 1, 0), true, "")
						.setHologramTarget(HologramTarget.WHITELIST)
						.addPlayer(player)
						.setInteraction((interactor, clicktype) ->
						{
							if (clicktype == ClickType.LEFT)
							{
								return;
							}

							_shop.attemptShopOpen(player);
						})
						.start();
				_hologramMap.put(player, hologram);
			}

			String amountLine = _manager.getChestsToOpen(player) > 0 ? ((_colourTick ? C.cAqua : C.cDAqua) + _manager.getChestsToOpen(player) + " Chests to Open") : null;

			if (amountLine == null)
			{
				hologram.setText(HOLOGRAM_TEXT);
			}
			else
			{
				hologram.setText(amountLine, HOLOGRAM_TEXT);
			}
		}

		_colourTick = !_colourTick;
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		Hologram hologram = _hologramMap.remove(event.getPlayer());

		if (hologram != null)
		{
			hologram.stop();
		}
	}

	public boolean openChest(Player player, Treasure treasure)
	{
		if (!Recharge.Instance.use(player, "Open Chest", 10000, false, false))
		{
			return false;
		}

		if (inUse())
		{
			player.closeInventory();
			player.sendMessage(F.main(getManager().getName(), "This station is already in use."));
			return false;
		}

		if (getManager().getChestsToOpen(player, treasure) <= 0)
		{
			player.sendMessage(F.main(getManager().getName(), "You do not have any " + F.name(treasure.getTreasureType().getName()) + " to open."));
			return false;
		}

		if (!treasure.isDuplicates() && getManager().getRewardManager().hasAllItems(player, treasure))
		{
			player.sendMessage(F.main(getManager().getName(), "You already have all the rewards possible for this chest."));
			return false;
		}

		if (!treasure.hasAnimations())
		{
			player.sendMessage(F.main(getManager().getName(), "It looks like there is no animation associated with that chest type. Please file a bug report."));
			return false;
		}

		TreasureSession session = new TreasureSession(player, this, treasure);

		if (session.hasFailed())
		{
			player.sendMessage(F.main(getManager().getName(), "Sorry, it looks like the rewards for the chest could not be generated."));
			return false;
		}

		getManager().getInventoryManager().addItemToInventory(success ->
		{
			// Need to check if it's inUse again since this is called in the future
			if (!success || inUse())
			{
				player.sendMessage(F.main(getManager().getName(), "It looks like something went wrong when processing your request. Please try again."));
				// Using the variation of addItemToInventory with a callback so our request is processed ASAP
				getManager().getInventoryManager().addItemToInventory(null, player, treasure.getTreasureType().getItemName(), 1);
			}
			else
			{
				_session = session;

				UtilServer.CallEvent(new TreasureStartEvent(player, _session));

				session.register();
				player.eject();
				player.leaveVehicle();
				player.teleport(_chest);
				prepareChestArea();

				String treasureName = treasure.getTreasureType().getName();

				if (treasure.getTreasureType() != TreasureType.OLD)
				{
					boolean an = UtilText.startsWithVowel(ChatColor.stripColor(treasureName));
					Bukkit.broadcastMessage(F.main(getManager().getName(), F.name(player.getName()) + " is opening " + (an ? "an" : "a") + " " + F.name(treasureName) + "."));
				}

				UtilTextMiddle.display(treasureName, "Choose " + C.cYellow + treasure.getRewardsPerChest() + C.cWhite + " to open", 10, 30, 10, player);
			}
		}, player, treasure.getTreasureType().getItemName(), -1);

		return true;
	}

	public void cleanup()
	{
		if (_session == null)
		{
			return;
		}

		for (Location location : _chests)
		{
			location.getWorld().playEffect(location, Effect.STEP_SOUND, location.getBlock().getType());
			MapUtil.QuickChangeBlockAt(location, Material.AIR);
		}

		TreasureFinishEvent finishEvent = new TreasureFinishEvent(_session.getPlayer(), _session);
		UtilServer.CallEvent(finishEvent);

		_session = null;
		setHologramVisible(true);
	}

	private void prepareChestArea()
	{
		_session.pushEntitiesAway();
		setHologramVisible(false);
	}

	private void setHologramVisible(boolean visible)
	{
		if (visible)
		{
			MapUtil.QuickChangeBlockAt(_chest, Material.CHEST);
			_hologramMap.values().forEach(Hologram::start);
		}
		else
		{
			MapUtil.QuickChangeBlockAt(_chest, Material.AIR);
			_hologramMap.values().forEach(Hologram::stop);
		}
	}

	public boolean inUse()
	{
		return _session != null;
	}

	public Location getChest()
	{
		return _chest;
	}

	public List<Location> getChestLocations()
	{
		return Collections.unmodifiableList(_chests);
	}

	public TreasureManager getManager()
	{
		return _manager;
	}

	public TreasureSession getSession()
	{
		return _session;
	}

	public TreasureShop getShop()
	{
		return _shop;
	}
}
