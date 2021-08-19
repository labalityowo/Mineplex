package nautilus.game.arcade.game.games.moba.kit.bob;

import mineplex.core.common.util.*;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.util.MobaParticles;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.Map.Entry;

public class SkillHappyTrees extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Bob Ross"
	};

	private static final ItemStack SKILL_ITEM = new ItemStack(Material.SAPLING);

	private final Set<HappyTreeData> _data = new HashSet<>();

	public SkillHappyTrees(int slot)
	{
		super("Happy Little Trees", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(15000);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();

		useSkill(player);
		_data.add(new HappyTreeData(player));
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		Iterator<HappyTreeData> iterator = _data.iterator();

		while (iterator.hasNext())
		{
			HappyTreeData data = iterator.next();

			if (UtilTime.elapsed(data.Start, 9000))
			{
				iterator.remove();
			}
			else if (data.Tree1 == null)
			{
				data.Tree1 = buildTree(data.Center);
			}
			else if (data.Tree2 == null)
			{
				data.Tree2 = buildTree(data.Center);
			}

			if (UtilTime.elapsed(data.Start, 2000))
			{
				healPlayers(data.Owner, data.Tree1);
				healPlayers(data.Owner, data.Tree2);
			}
		}
	}

	private Block buildTree(Location center)
	{
		Location start = UtilAlg.getRandomLocation(center, 5, 0, 5);
		Map<Block, Material> blocks = getTree(start);

		for (Entry<Block, Material> entry : blocks.entrySet())
		{
			Manager.runSyncLater(() -> Manager.GetBlockRestore().add(entry.getKey(), entry.getValue().getId(), (byte) 0, (long) (6000 + (Math.random() * 1000))), UtilMath.r(60));
		}

		return start.getBlock();
	}

	private Map<Block, Material> getTree(Location start)
	{
		Block last = start.getBlock().getRelative(BlockFace.DOWN);
		Map<Block, Material> blocks = new HashMap<>();

		// Trunk
		for (int i = 0; i < 5; i++)
		{
			Block next = last.getRelative(BlockFace.UP);
			last = next;
			blocks.put(next, Material.LOG);
		}

		last = last.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);

		// Bottom Leaves
		for (Block block : UtilBlock.getInBoundingBox(last.getLocation().add(2, 1, 2), last.getLocation().subtract(2, 0, 2), false))
		{
			blocks.put(block, Material.LEAVES);
		}

		last = last.getRelative(BlockFace.UP).getRelative(BlockFace.UP);

		// Middle Leaves
		for (Block block : UtilBlock.getInBoundingBox(last.getLocation().add(1, 0, 1), last.getLocation().subtract(1, 0, 1), false))
		{
			blocks.put(block, Material.LEAVES);
		}

		last = last.getRelative(BlockFace.UP);

		// Top Leaves
		blocks.put(last.getRelative(BlockFace.NORTH), Material.LEAVES);
		blocks.put(last.getRelative(BlockFace.WEST), Material.LEAVES);
		blocks.put(last.getRelative(BlockFace.EAST), Material.LEAVES);
		blocks.put(last.getRelative(BlockFace.SOUTH), Material.LEAVES);
		blocks.put(last.getRelative(BlockFace.UP), Material.LEAVES);

		return blocks;
	}

	private void healPlayers(Player owner, Block block)
	{
		for (LivingEntity entity : UtilEnt.getInRadius(block.getLocation(), 5).keySet())
		{
			// Don't heal enemies
			if (!isTeamDamage(entity, owner))
			{
				continue;
			}

			MobaUtil.heal(entity, owner, 2);
			MobaParticles.healing(entity, 1);
		}
	}

	private class HappyTreeData
	{
		public Player Owner;
		public long Start;
		public Location Center;
		public Block Tree1;
		public Block Tree2;

		public HappyTreeData(Player owner)
		{
			Owner = owner;
			Start = System.currentTimeMillis();
			Center = owner.getLocation();
		}
	}
}
