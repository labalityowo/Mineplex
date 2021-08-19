package nautilus.game.arcade.game.modules.compass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.modules.Module;
import nautilus.game.arcade.game.modules.compass.menu.CompassMenu;

public class CompassModule extends Module
{

	private static final ItemStack COMPASS_ITEM =
			new ItemBuilder(Material.COMPASS)
					.setAmount(1)
					.setTitle(C.cGreenB + "Tracking Compass")
					.build();

	public static ItemStack getCompassItem()
	{
		return COMPASS_ITEM;
	}

	private List<Supplier<Collection<CompassEntry>>> _suppliers = new ArrayList<>();

	private CompassMenu _compassMenu;
	private boolean _giveCompassToAlive = false;
	private boolean _giveCompassItem = true;
	private boolean _giveCompassItemToSpectators = true;

	public CompassModule addSupplier(Supplier<Collection<CompassEntry>> supplier)
	{
		_suppliers.add(supplier);
		return this;
	}

	public CompassModule setGiveCompassToAlive(boolean b)
	{
		_giveCompassToAlive = b;
		return this;
	}

	public CompassModule setGiveCompass(boolean b)
	{
		_giveCompassItem = b;
		return this;
	}

	public CompassModule setGiveCompassToSpecs(boolean b)
	{
		_giveCompassItemToSpectators = b;
		return this;
	}

	@Override
	public void cleanup()
	{
		HandlerList.unregisterAll(_compassMenu);
		_compassMenu = null;
		_suppliers = null;
		UtilServer.getPlayersCollection().forEach(player -> ((CraftPlayer) player).getHandle().compassTarget = null);
	}

	@Override
	protected void setup()
	{
		_compassMenu = new CompassMenu(this);
		_suppliers.add(() ->
				getGame()
						.GetPlayers(true)
						.stream()
						.map(player -> new CompassEntry(player, player.getName(), player.getName(), getGame().GetTeam(player), getGame().GetKit(player)))
						.collect(Collectors.toList()));
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		if (!getGame().IsLive())
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (!_giveCompassToAlive && getGame().IsAlive(player))
				continue;

			GameTeam team = getGame().GetTeam(player);

			stream()
					.filter(entry -> entry.getEntity() != player)
					.filter(entry -> getGame().GetTeamList().size() <= 1 || team == null || !team.equals(entry.getTeam()))
					.filter(entry -> !UtilServer.CallEvent(new CompassAttemptTargetEvent(player, entry.getEntity())).isCancelled())
					.min(Comparator.comparingDouble(a -> UtilMath.offset(player, a.getEntity())))
					.ifPresent(target ->
					{
						Entity targetEntity = target.getEntity();
						GameTeam targetTeam = target.getTeam();

						if (_giveCompassItem || (_giveCompassItemToSpectators && getGame().getArcadeManager().isSpectator(player)))
						{
							long count = UtilInv.getItems(player, true, true, true)
									.stream()
									.filter(this::isCompassItem)
									.count();

							if (count == 0)
							{
								player.getInventory().addItem(COMPASS_ITEM);
							}
						}


						player.setCompassTarget(targetEntity.getLocation());

						double heightDiff = targetEntity.getLocation().getY() - player.getLocation().getY();

						//Action Bar
						if (isCompassItem(player.getItemInHand()))
						{
							UtilTextBottom.display(
									"    " + C.cWhite + C.Bold + "Nearest Target: " + targetTeam.GetColor() + target.getDisplayName() +
											"    " + C.cWhite + C.Bold + "Distance: " + targetTeam.GetColor() + UtilMath.trim(1, UtilMath.offset(player, targetEntity)) +
											"    " + C.cWhite + C.Bold + "Height: " + targetTeam.GetColor() + UtilMath.trim(1, heightDiff), player
							);
						}
					});
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event)
	{
		if (!isCompassItem(event.getItemDrop().getItemStack()))
			return;

		event.setCancelled(true);

		UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot drop " + F.item("Target Compass") + "."));
	}

	@EventHandler
	public void DeathRemove(PlayerDeathEvent event)
	{
		event.getDrops().removeIf(this::isCompassItem);
	}

	@EventHandler
	public void SpectatorTeleport(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.PHYSICAL)
			return;

		Player player = event.getPlayer();

		if (getGame().IsAlive(player))
			return;

		if (!isCompassItem(player.getItemInHand()))
			return;

		if (player.getGameMode() == GameMode.SPECTATOR)
			return;

		event.setCancelled(true);

		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			if (!Recharge.Instance.use(player, "Spectate", 3000, true, false))
			{
				return;
			}

			spectateNearestPlayer(player);
		}
		else
		{
			_compassMenu.attemptShopOpen(player);
		}
	}

	private void spectateNearestPlayer(Player spectator)
	{
		stream()
				.min((a, b) -> Double.compare(UtilMath.offset(spectator, a.getEntity()), UtilMath.offset(spectator, b.getEntity())))
				.map(CompassEntry::getEntity)
				.ifPresent(target -> spectator.teleport(target.getLocation().add(0, 1, 0)));
	}

	@EventHandler
	public void closeShop(GameStateChangeEvent event)
	{
		if (event.GetState().equals(Game.GameState.End))
		{
			UtilServer.getPlayersCollection().stream().filter(_compassMenu::isPlayerInShop).forEach(Player::closeInventory);
		}
	}

	@EventHandler
	public void updateShop(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		_compassMenu.update();
	}

	public Stream<CompassEntry> stream()
	{
		return _suppliers.stream().map(Supplier::get).flatMap(Collection::stream);
	}

	// Defined here to make modifying definitions easier
	public boolean isCompassItem(ItemStack item)
	{
		return UtilItem.isSimilar(COMPASS_ITEM, item, UtilItem.ItemAttribute.NAME, UtilItem.ItemAttribute.MATERIAL);
	}
}
