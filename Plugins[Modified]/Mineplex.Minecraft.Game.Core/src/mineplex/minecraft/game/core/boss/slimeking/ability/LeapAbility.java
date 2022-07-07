package mineplex.minecraft.game.core.boss.slimeking.ability;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.boss.slimeking.creature.SlimeCreature;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class LeapAbility extends SlimeAbility
{
	private Player _target;
	private int _jumpTick;

	public LeapAbility(SlimeCreature slime)
	{
		super(slime);
		_target = UtilPlayer.getRandomTarget(slime.getEntity().getLocation(), 30);
		_jumpTick = 20;

		// Only attempt to find a target once for this ability!
		if (_target == null)
		{
			setIdle(true);
		}
	}

	@Override
	public void tickCustom()
	{
		if (_target != null)
		{
			if (getTicks() == _jumpTick)
			{
				// Jump
				Vector dir = UtilAlg.getTrajectory2d(getSlime().getEntity(), _target);
				UtilAction.velocity(getSlime().getEntity(), dir, 2, false, 0, 0.5, 0.5, true);
			}
			else if (getTicks() > _jumpTick)
			{
				if (getSlime().getEntity().isOnGround())
				{
					setIdle(true);
				}
				else if (getSlime().isEnraged())
				{
					World world = getSlime().getEntity().getWorld();
					Block block = world.getHighestBlockAt(getSlime().getEntity().getLocation()).getRelative(BlockFace.UP);

					if (block.getType() == Material.AIR)
					{
						block.setType(Material.FIRE);
					}
				}
			}
		}
	}
}
