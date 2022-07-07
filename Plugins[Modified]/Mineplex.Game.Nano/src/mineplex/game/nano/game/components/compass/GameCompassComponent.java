package mineplex.game.nano.game.components.compass;

import java.text.DecimalFormat;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.GameComponent;
import mineplex.game.nano.game.components.team.GameTeam;

public class GameCompassComponent extends GameComponent<Game>
{

	private static final ItemStack COMPASS = new ItemBuilder(Material.COMPASS)
			.setTitle(C.cGreenB + "Tracking Compass")
			.build();

	private final GameCompassShop _shop;
	private final DecimalFormat _distanceFormat = new DecimalFormat("0.0");

	private boolean _giveToAlive;

	public GameCompassComponent(Game game)
	{
		super(game, GameState.Live);

		_shop = new GameCompassShop(this, game.getManager());
	}

	public GameCompassComponent setGiveToAlive(boolean giveToAlive)
	{
		_giveToAlive = giveToAlive;
		return this;
	}

	@Override
	public void disable()
	{
		for (Player player : UtilServer.getPlayersCollection())
		{
			player.getInventory().remove(COMPASS);
		}
	}

	@EventHandler
	public void updateCompasses(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		for (Player player : UtilServer.getPlayersCollection())
		{
			boolean alive = getGame().isAlive(player);

			if (!player.getInventory().contains(COMPASS) && (_giveToAlive || !alive))
			{
				player.getInventory().addItem(COMPASS);
			}

			if (COMPASS.equals(player.getItemInHand()))
			{
				Player closest = getClosest(player);

				if (closest == null)
				{
					continue;
				}

				GameTeam closestTeam = getGame().getTeam(closest);

				if (closestTeam == null)
				{
					continue;
				}

				double dist = UtilMath.offset(player, closest);

				player.setCompassTarget(closest.getLocation());
				UtilTextBottom.display(C.cWhiteB + "Target: " + closestTeam.getChatColour() + closest.getName() + C.cWhiteB + "  Distance: " + closestTeam.getChatColour() + _distanceFormat.format(dist), player);
			}
		}
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.PHYSICAL)
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (!COMPASS.equals(itemStack) || getGame().isAlive(player))
		{
			return;
		}

		if (UtilEvent.isAction(event, ActionType.R))
		{
			_shop.attemptShopOpen(player);
		}
		else if (Recharge.Instance.use(player, "Spectate", 1000, true, false))
		{
			Player closest = getClosest(player);

			if (closest != null)
			{
				player.teleport(closest);
			}
		}
	}

	private Player getClosest(Player player)
	{
		if (getGame().isAlive(player) && getGame().getTeams().size() > 1)
		{
			return UtilPlayer.getClosest(player.getLocation(), getGame().getTeam(player).getAlivePlayers());
		}
		else
		{
			return UtilPlayer.getClosest(player.getLocation(), player);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void inventoryClick(InventoryClickEvent event)
	{
		UtilInv.DisallowMovementOf(event, COMPASS.getItemMeta().getDisplayName(), COMPASS.getType(), COMPASS.getData().getData(), true);
	}

	@EventHandler(ignoreCancelled = true)
	public void playerDropItem(PlayerDropItemEvent event)
	{
		if (event.getItemDrop().getItemStack().equals(COMPASS))
		{
			event.setCancelled(true);
		}
	}
}
