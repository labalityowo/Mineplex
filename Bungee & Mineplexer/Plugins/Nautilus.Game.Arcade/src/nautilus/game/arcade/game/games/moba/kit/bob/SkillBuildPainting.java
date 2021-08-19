package nautilus.game.arcade.game.games.moba.kit.bob;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class SkillBuildPainting extends HeroSkill implements IThrown
{

	private static final String[] DESCRIPTION = {
			"Bob Ross"
	};
	private static final BlockFace[] AXIS = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
	private static final byte[][] PAINTING = {
			{
				3, 3, 3, 3, 0, 3, 3, 3, 3, 0, 3, 3, 3, 3, 3
			},
			{
				3, 3, 0, 0, 0, 0, 3, 3, 0, 0, 0, 3, 3, 3, 3
			},
			{
				3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0, 3, 3, 3
			},
			{
				3, 3, 3, 0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 13, 3
			},
			{
				3, 3, 0, 0, 0, 3, 3, 3, 5, 3, 3, 13, 3, 13, 3
			},
			{
				3, 3, 8, 8, 8, 8, 3, 5, 5, 5, 3, 13, 13, 13, 13
			},
			{
				3, 3, 8, 8, 8, 8, 3, 3, 12, 3, 13, 13, 13, 13, 13
			},
			{
				3, 3, 7, 7, 7, 7, 7, 3, 12, 3, 3, 12, 3, 12, 3
			},
			{
				5, 7, 7, 7, 7, 7, 7, 5, 12, 5, 5, 12, 5, 12, 5
			}
	};

	private static final ItemStack SKILL_ITEM = new ItemStack(Material.NETHER_STAR);

	public SkillBuildPainting(int slot)
	{
		super("The Joy Of Painting", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(60000);
		setDropItemActivate(true);
	}

	@Override
	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!Recharge.Instance.use(player, GetName() + " Trigger", 5000, false, false))
		{
			return;
		}

		useActiveSkill(player, 4000);
		broadcast(player);

		Set<Block> blocks = new HashSet<>();

		Vector direction = player.getLocation().getDirection().normalize().setY(0);
		Location start = player.getLocation().add(direction);
		BlockFace facing = UtilBlock.getFace(start.getYaw() + 180F);
		Block center = start.getBlock().getRelative(facing).getRelative(BlockFace.UP);

		float leftYaw = start.getYaw() - 90;
		BlockFace leftSide = UtilBlock.getFace(leftYaw);

		for (int i = 0; i < 7; i++)
		{
			center = center.getRelative(leftSide);
		}

		BlockFace rightSide = leftSide.getOppositeFace();

		// Rows
		for (int y = 0; y < PAINTING.length; y++)
		{
			byte[] row = PAINTING[y];

			// Column in row
			for (int x = 0; x < row.length; x++)
			{
				Block result = center;

				for (int i = 0; i < x; i++)
				{
					result = result.getRelative(rightSide);
				}

				result = result.getRelative(0, 8, 0);

				for (int i = 0; i < y; i++)
				{
					result = result.getRelative(BlockFace.DOWN);
				}

				Block fResult = result;
				byte blockData = row[x];

				Manager.runSyncLater(() ->
				{
					blocks.add(fResult);
					Manager.GetBlockRestore().add(fResult, Material.WOOL.getId(), blockData, Long.MAX_VALUE);

				}, UtilMath.r(40));
			}
		}

		Manager.runSyncLater(() ->
		{
			for (Block block : blocks)
			{
				if (Math.random() < 0.2)
				{
					FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation().add(0.5, 0.5, 0.5), block.getType(), block.getData());

					fallingBlock.setVelocity(direction.clone().multiply(1 + (Math.random() * 0.4)));
					Manager.GetProjectile().AddThrow(fallingBlock, player, this, 2000, true, true, true, false, 0.5F);
				}

				Manager.GetBlockRestore().restore(block);
			}
		}, 80);
	}

	@EventHandler
	public void itemSpawn(ItemSpawnEvent event)
	{
		if (event.getEntity().getItemStack().getType() == Material.WOOL)
		{
			event.setCancelled(true);
		}
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		damage(data);
	}

	@Override
	public void Idle(ProjectileUser data)
	{

	}

	@Override
	public void Expire(ProjectileUser data)
	{
		damage(data);
	}

	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	private void damage(ProjectileUser data)
	{
		Entity entity = data.getThrown();
		data.getThrown().getWorld().playEffect(entity.getLocation(), Effect.STEP_SOUND, Material.WOOL, (byte) 0);

		for (Entry<LivingEntity, Double> entry : UtilEnt.getInRadius(entity.getLocation(), 3).entrySet())
		{
			Manager.GetDamage().NewDamageEvent(entry.getKey(), data.getThrower(), null, DamageCause.BLOCK_EXPLOSION, 5, true, true, false, UtilEnt.getName(data.getThrower()), GetName());
		}

		data.getThrown().remove();
	}

}