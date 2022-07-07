package mineplex.minecraft.game.core.boss.broodmother.attacks;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.minecraft.game.core.boss.BossAbility;
import mineplex.minecraft.game.core.boss.broodmother.SpiderCreature;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;

/**
 * A attack where webs appear all around the spider in a kinda maze formation, making it hard to approach it.
 */
public class SpiderWebStomp extends BossAbility<SpiderCreature, Spider>
{
	private int _tick;

	public SpiderWebStomp(SpiderCreature creature)
	{
		super(creature);
	}

	@Override
	public int getCooldown()
	{
		return 120;
	}

	@Override
	public boolean canMove()
	{
		return true;
	}

	@Override
	public boolean inProgress()
	{
		return true;
	}

	@Override
	public boolean hasFinished()
	{
		return _tick > 40;
	}

	@Override
	public void setFinished()
	{
	}

	public Player getTarget()
	{
		if (!UtilEnt.isGrounded(getEntity()))
		{
			return null;
		}

		return super.getTarget();
	}

	@Override
	public void tick()
	{
		if (_tick++ == 0)
		{
			int amount = UtilMath.r(60) + 60;

			for (int i = 0; i < amount; i++)
			{
				Block block = getLocation().getBlock().getRelative((int) UtilMath.rr(UtilMath.r(16) + 4, true), 0,
						(int) UtilMath.rr(UtilMath.r(16) + 4, true));

				if (block.getType() != Material.AIR)
				{
					continue;
				}
				
				Bukkit.broadcastMessage("Setting Web");
				getBoss().getEvent().setBlock(block, Material.WEB);
				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Material.WEB);
			}
		}
	}

}
