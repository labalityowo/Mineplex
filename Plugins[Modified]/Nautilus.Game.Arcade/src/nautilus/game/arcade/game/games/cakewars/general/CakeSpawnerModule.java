package nautilus.game.arcade.game.games.cakewars.general;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.cakewars.CakeModule;
import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.games.cakewars.shop.CakeNetherItem;
import nautilus.game.arcade.game.games.cakewars.shop.CakeResource;
import nautilus.game.arcade.game.games.cakewars.team.CakeTeam;

public class CakeSpawnerModule extends CakeModule
{

	private static final int MIN_BLOCK_PLACE_DIST_SQUARED = 4;

	public CakeSpawnerModule(CakeWars game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true)
	public void blockPlace(BlockPlaceEvent event)
	{
		if (isNearSpawner(event.getBlock()))
		{
			event.setCancelled(true);
			event.getPlayer().sendMessage(F.main("Game", "You cannot place blocks that close to a generator."));
		}
	}

	@EventHandler
	public void updateSpawn(UpdateEvent event)
	{
		if (!_game.IsLive())
		{
			return;
		}

		for (CakeResource resource : CakeResource.values())
		{
			if (resource.getSpawnerUpdate() != event.getType())
			{
				continue;
			}

			_game.getCakeTeamModule().getCakeTeams().forEach((team, cakeTeam) -> distributeItem(resource, getItemsToDrop(resource, team, cakeTeam), team));
		}
	}

	@EventHandler
	public void updateGeneratorHologram(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW || !_game.IsLive())
		{
			return;
		}

		CakeResource[] resources = CakeResource.values();
		List<String> text = new ArrayList<>();

		_game.getCakeTeamModule().getCakeTeams().forEach((team, cakeTeam) ->
		{
			text.clear();

			for (CakeResource resource : resources)
			{
				int rate = getItemsToDrop(resource, team, cakeTeam);

				if (rate < 0)
				{
					continue;
				}

				text.add(resource.getChatColor() + resource.getName() + C.cWhite + " : " + resource.getChatColor() + "x" + rate);
			}

			cakeTeam.getGeneratorHologram().setText(text.toArray(new String[0]));
		});
	}

	private int getItemsToDrop(CakeResource resource, GameTeam team, CakeTeam cakeTeam)
	{
		int rate = cakeTeam.getUpgrades().get(CakeNetherItem.RESOURCE);

		switch (resource)
		{
			case BRICK:
				rate++;
				break;
			case EMERALD:
				int emeraldPoints = _game.getCakePointModule().ownedEmeraldPoints(team);

				if (emeraldPoints == 0)
				{
					rate = 0;
				}
				else
				{
					rate += emeraldPoints;
				}
				break;
			case STAR:
				int netherStarPoints = _game.getCakePointModule().ownedNetherStarPoints(team);

				if (netherStarPoints == 0)
				{
					rate = 0;
				}
				else
				{
					rate += netherStarPoints;
				}
				break;
		}

		rate = getGame().getGeneratorRate(resource, rate);
		return rate;
	}

	private void distributeItem(CakeResource resource, int amount, GameTeam team)
	{
		if (amount <= 0)
		{
			return;
		}

		CakeTeam cakeTeam = _game.getCakeTeamModule().getCakeTeam(team);

		ItemStack itemStack = resource.getItemStack().clone();
		itemStack.setAmount(amount);
		Location location = cakeTeam.getGenerator();
		boolean drop = true;
		List<Player> players = new ArrayList<>();
		Item item = null;

		for (Entity entity : location.getWorld().getNearbyEntities(location, 2, 2, 2))
		{
			if (entity instanceof Item)
			{
				Item itemEntity = (Item) entity;

				if (itemEntity.getItemStack().getType() == itemStack.getType())
				{
					item = itemEntity;
				}
			}
			if (entity instanceof Player)
			{
				Player player = (Player) entity;

				if (UtilPlayer.isSpectator(player))
				{
					continue;
				}

				drop = false;
				players.add((Player) entity);
			}
		}

		if (drop)
		{
			if (item != null)
			{
				item.getItemStack().setAmount(Math.min(item.getItemStack().getAmount() + itemStack.getAmount(), resource.getMaxSpawned()));
			}
			else
			{
				item = location.getWorld().dropItem(location, itemStack);
				UtilAction.zeroVelocity(item);
			}
		}
		else
		{
			for (Player player : players)
			{
				player.getInventory().addItem(itemStack);
				player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1, 1);
			}
		}
	}

	public boolean isNearSpawner(Block block)
	{
		return isNearSpawner(block.getLocation().add(0.5, 0, 0.5));
	}

	public boolean isNearSpawner(Location location)
	{
		for (CakeTeam team : _game.getCakeTeamModule().getCakeTeams().values())
		{
			if (UtilMath.offsetSquared(location, team.getGenerator()) < MIN_BLOCK_PLACE_DIST_SQUARED)
			{
				return true;
			}
		}

		return false;
	}
}
