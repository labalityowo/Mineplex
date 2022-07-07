package mineplex.game.nano.game.games.spleef;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.SoloGame;

public class Spleef extends SoloGame
{

	private static final long BLOCK_DECAY = 1800;

	private final Map<Block, Long> _blocks = new HashMap<>();

	public Spleef(NanoManager manager)
	{
		super(manager, GameType.SPLEEF, new String[]
				{
						C.cGreen + "Punch Blocks" + C.Reset + " to break them!",
						C.cRed + "Blocks Fall" + C.Reset + " from underneath you.",
						C.cYellow + "Last Player" + C.Reset + " alive wins!"
				});

		_prepareComponent.setPrepareFreeze(false);

		_damageComponent.setPvp(false);
		_damageComponent.setFall(false);

		_worldComponent.setBlockBreak(true);

		_waterComponent.override();
	}

	@Override
	protected void parseData()
	{

	}

	@Override
	public void disable()
	{

	}

	@EventHandler
	public void snowballHit(ProjectileHitEvent event)
	{
		Projectile projectile = event.getEntity();

		if (!isLive() || !(projectile instanceof Snowball) || !(projectile.getShooter() instanceof Player))
		{
			return;
		}

		Location location = projectile.getLocation().add(projectile.getVelocity().multiply(0.8));
		Block block = location.getBlock();

		if (block.getType() == Material.AIR)
		{
			Block closest = null;
			double closestDist = Double.MAX_VALUE;

			for (Block other : UtilBlock.getSurrounding(block, true))
			{
				if (other.getType() == Material.AIR)
				{
					continue;
				}

				double dist = UtilMath.offsetSquared(location, other.getLocation().add(0.5, 0.5, 0.5));

				if (closest == null || dist < closestDist)
				{
					closest = other;
					closestDist = dist;
				}
			}

			if (closest != null)
			{
				block = closest;
			}
		}

		blockFade(block, (Player) projectile.getShooter());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void blockHit(BlockDamageEvent event)
	{
		Player player = event.getPlayer();

		if (!isLive() || UtilPlayer.isSpectator(player))
		{
			return;
		}

		Block block = event.getBlock();
		event.setCancelled(true);

		if (block.getType() == Material.BEDROCK)
		{
			return;
		}

		blockFade(block, player);

		if (!player.getInventory().contains(Material.SNOW_BALL, 16))
		{
			player.getInventory().addItem(new ItemStack(Material.SNOW_BALL));
		}
	}

	private void blockFade(Block block, Player player)
	{
		if (block.getType() == Material.BEDROCK || block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA)
		{
			return;
		}

		UtilPlayer.hunger(player, 1);
		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
		MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR);
	}

	@EventHandler
	public void updateRunner(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !isLive())
		{
			return;
		}

		for (Player player : getAlivePlayers())
		{
			Pair<Location, Location> box = UtilEnt.getSideStandingBox(player);
			Location min = box.getLeft(), max = box.getRight();

			for (int x = min.getBlockX(); x <= max.getBlockX(); x++)
			{
				for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++)
				{
					addBlock(player.getLocation().add(x, -0.5, z).getBlock());
				}
			}
		}

		_blocks.entrySet().removeIf(entry ->
		{
			Block block = entry.getKey();
			long time = entry.getValue();

			if (!UtilTime.elapsed(time, BLOCK_DECAY))
			{
				return false;
			}

			MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR);
			return true;
		});
	}

	private void addBlock(Block block)
	{
		if (block == null || block.getType() == Material.AIR || block.getType() == Material.BEDROCK || block.isLiquid() || block.getRelative(BlockFace.UP).getType() != Material.AIR || _blocks.containsKey(block))
		{
			return;
		}

		_blocks.put(block, System.currentTimeMillis());
		MapUtil.QuickChangeBlockAt(block.getLocation(), Material.STAINED_CLAY, (byte) 14);
	}

	@EventHandler
	public void blockForm(EntityChangeBlockEvent event)
	{
		event.setCancelled(true);
	}
}
