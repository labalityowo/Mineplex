package mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.game.clans.clans.worldevent.api.BossAbility;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.GolemCreature;

public class GolemDeadlyTremor extends BossAbility<GolemCreature, IronGolem>
{
	private static final long ATTACK_DURATION = 10000;
	private long _start;
	
	public GolemDeadlyTremor(GolemCreature creature)
	{
		super(creature);
		_start = System.currentTimeMillis();
	}

	@Override
	public boolean canMove()
	{
		return false;
	}

	@Override
	public boolean inProgress()
	{
		return true;
	}

	@Override
	public boolean hasFinished()
	{
		return UtilTime.elapsed(_start, ATTACK_DURATION);
	}

	@Override
	public void setFinished()
	{
		_start = System.currentTimeMillis() - ATTACK_DURATION;
	}

	@Override
	public void tick()
	{
		for (Player player : UtilPlayer.getInRadius(getLocation(), 30).keySet())
		{
			player.playSound(player.getLocation(), Sound.MINECART_BASE, 0.2f, 0.2f);
			
			if (UtilEnt.isGrounded(player))
			{
				getBoss().getEvent().getDamageManager().NewDamageEvent(player, getBoss().getEntity(), null, DamageCause.CUSTOM, (1 + 2 * Math.random()) * getBoss().getDifficulty(), false, false, false, getBoss().getEntity().getName(), "Deadly Tremor");	
				
				if (Recharge.Instance.use(player, "Deadly Tremor Hit", 400, false, false))
				{
					UtilAction.velocity(player, new Vector(Math.random() - 0.5, Math.random() * 0.2, Math.random() - 0.5), 
						Math.random() * 1 + 1, false, 0, 0.1 + Math.random() * 0.2, 2, true);
				}
			}
			
			for (Block block : UtilBlock.getInRadius(player.getLocation(), 5).keySet())
			{
				if (Math.random() < 0.98)
					continue;
					
				if (!UtilBlock.solid(block))
					continue;
				
				if (!UtilBlock.airFoliage(block.getRelative(BlockFace.UP)))
					continue;
				
				player.playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
			}
		}
	}
}