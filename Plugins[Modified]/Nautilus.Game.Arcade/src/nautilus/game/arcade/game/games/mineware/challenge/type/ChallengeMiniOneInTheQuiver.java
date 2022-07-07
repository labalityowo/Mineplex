package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;
import nautilus.game.arcade.game.games.mineware.challenge.NumberTracker;

/**
 * A challenge based in one in the quiver.
 */
public class ChallengeMiniOneInTheQuiver extends Challenge implements NumberTracker
{
	private static final int LOCKED_INVENTORY_SLOT = 0;
	private static final int MAP_SPAWN_SHIFT = 1;
	private static final int SPAWN_COORDINATE_MULTIPLE = 2;
	private static final int MAP_HEIGHT = 1;

	private static final int MAP_BARRIER_SHIFT_1 = 2;
	private static final int MAP_BARRIER_SHIFT_2 = 4;
	private static final int MAP_BARRIER_SPAWN_CHANCE_RANGE = 8;

	private static final Material BARRIER_MATERIAL = Material.STAINED_CLAY;
	private static final byte BARRIER_DATA = 14;

	private Map<Player, Integer> _killTracker = new HashMap<Player, Integer>();

	public ChallengeMiniOneInTheQuiver(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.LastStanding,
			"Mini OITQ",
			"Shoot arrows to instant kill others.",
			"Avoid getting hit by them.",
			"Arrow supply every 4 seconds.");

		Settings.setUseMapHeight();
		Settings.setCanCruble();
		Settings.setLockInventory(LOCKED_INVENTORY_SLOT);
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (int x = -(size); x <= size; x++)
		{
			for (int z = -(size); z <= size; z++)
			{
				if (x % SPAWN_COORDINATE_MULTIPLE == 0 && z % SPAWN_COORDINATE_MULTIPLE == 0)
				{
					spawns.add(getCenter().add(x, MAP_HEIGHT, z));
				}
			}
		}
		return spawns;
	}

	@Override
	public void createMap()
	{
		for (int x = -getArenaSize(); x <= getArenaSize(); x++)
		{
			for (int z = -getArenaSize(); z <= getArenaSize(); z++)
			{
				for (int y = 0; y <= MAP_HEIGHT; y++)
				{
					Block block = getCenter().getBlock().getRelative(x, y, z);

					int absX = Math.abs(x);
					int absZ = Math.abs(z);

					if (y == 0)
					{
						setBlock(block, Material.GRASS);
					}
					else
					{
						if (absX == getArenaSize() || absZ == getArenaSize())
						{
							setBlock(block, Material.FENCE);
						}
						else if (absX <= getArenaSize() - 1 || absZ <= getArenaSize() - 1)
						{
							if (((absX == getArenaSize() - MAP_BARRIER_SHIFT_1 || absZ == getArenaSize() - MAP_BARRIER_SHIFT_1) || (absX == getArenaSize() - MAP_BARRIER_SHIFT_2 || absZ == getArenaSize() - MAP_BARRIER_SHIFT_2)) && UtilMath.r(MAP_BARRIER_SPAWN_CHANCE_RANGE) == 0
								&& canPlaceBarrier(block.getRelative(BlockFace.UP)) && !Data.isSpawnLocation(block.getLocation()))
							{
								generateBarrier(block);
							}
							else
							{
								generateGrass(block);
							}
						}
					}

					addBlock(block.getRelative(BlockFace.UP));
					addBlock(block);
				}
			}
		}
	}

	@Override
	public void onStart()
	{
		Host.DamagePvP = true;
		Host.DamagePvE = true;

		ItemStack bow = new ItemBuilder(Material.BOW)
			.setUnbreakable(true)
			.setItemFlags(ItemFlag.HIDE_UNBREAKABLE)
			.build();

		addItem(bow, new ItemStack(Material.ARROW));

		for (Player player : getPlayersAlive())
		{
			_killTracker.put(player, 0);
		}
	}

	@Override
	public void onEnd()
	{
		Host.DamagePvP = false;
		Host.DamagePvE = false;

		_killTracker.clear();

		remove(EntityType.ARROW);
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event)
	{
		if (!isChallengeValid())
			return;

		Projectile entity = event.getEntity();

		if (entity instanceof Arrow)
		{
			entity.remove();
		}
	}

	@EventHandler
	public void onArrowEquipUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;

		if (!isChallengeValid())
			return;

		for (Player player : getPlayersIn(true))
		{
			if (UtilInv.contains(player, Material.ARROW, (byte) 0, 1))
				continue;

			player.getInventory().addItem(new ItemStack(Material.ARROW));
			player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1.0F, 1.0F);
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
	{
		if (!isChallengeValid())
			return;

		if (!(event.getDamager() instanceof Arrow))
			return;

		if (!(event.getEntity() instanceof Player))
			return;

		Arrow arrow = (Arrow) event.getDamager();
		Player shooter = (Player) arrow.getShooter();
		Player damaged = (Player) event.getEntity();

		if (Data.isLost(shooter) || !Host.IsPlaying(shooter) || shooter.equals(damaged))
		{
			event.setCancelled(true);
			return;
		}

		damaged.setHealth(1);
		event.setDamage(event.getDamage());
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		if (!isChallengeValid())
			return;

		Player victim = event.getEntity();
		Player killer = victim.getKiller();

		if (killer != null && isPlayerValid(killer) && _killTracker.containsKey(killer))
		{
			_killTracker.put(killer, _killTracker.get(killer) + 1);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (_killTracker.containsKey(player))
		{
			_killTracker.remove(player);
		}
	}

	private boolean canPlaceBarrier(Block block)
	{
		Block[] relatives = {
			block.getRelative(BlockFace.NORTH_EAST),
			block.getRelative(BlockFace.NORTH_WEST),
			block.getRelative(BlockFace.SOUTH_EAST),
			block.getRelative(BlockFace.SOUTH_WEST)
		};

		boolean foundOtherBarrier = false;

		for (Block relative : relatives)
		{
			if (relative.getType() == Material.STAINED_CLAY)
			{
				foundOtherBarrier = true;
				break;
			}
		}

		return !foundOtherBarrier;
	}

	private void generateBarrier(Block block)
	{
		setBlock(block, BARRIER_MATERIAL, BARRIER_DATA);
		setBlock(block.getRelative(BlockFace.UP), BARRIER_MATERIAL, BARRIER_DATA);
	}

	@Override
	public Number getData(Player player)
	{
		return _killTracker.get(player);
	}

	@Override
	public boolean hasData(Player player)
	{
		return _killTracker.containsKey(player);
	}
}
