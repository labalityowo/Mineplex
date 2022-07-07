package mineplex.minecraft.game.core.boss.skeletonking.abilities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.minecraft.game.core.boss.BossAbility;
import mineplex.minecraft.game.core.boss.skeletonking.SkeletonCreature;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.google.common.collect.Lists;

public class SkeletonStrike extends BossAbility<SkeletonCreature, Skeleton>
{
	private static final double MAX_RANGE = 25;
	private static final Integer MAX_TARGETS = 1;
	private boolean _shot;
	
	public SkeletonStrike(SkeletonCreature creature)
	{
		super(creature);
		_shot = false;
	}
	
	private int getPosition(Player toAdd, LinkedList<Player> ordered)
	{
		int position = ordered.size();
		int index = 0;
		for (Player player : ordered)
		{
			if (player.getHealth() < toAdd.getHealth())
			{
				position = index;
				return position;
			}
			index++;
		}
		
		return position;
	}
	
	private List<Player> getTargets()
	{
		Skeleton necromancer = getBoss().getEntity();
		LinkedList<Player> selections = new LinkedList<>();
		List<Player> targeted = Lists.newArrayList();
		
		HashMap<Player, Double> near = UtilPlayer.getInRadius(necromancer.getLocation(), MAX_RANGE); 
		
		for (Player nearby : near.keySet())
		{
			if (nearby.getGameMode() == GameMode.CREATIVE || nearby.getGameMode() == GameMode.SPECTATOR)
			{
				continue;
			}
			
			if (selections.isEmpty())
			{
				selections.addFirst(nearby);
			}
			else
			{
				selections.add(getPosition(nearby, selections), nearby);
			}
		}
		
		for (int i = 0; i < MAX_TARGETS; i++)
		{
			if (i < selections.size())
			{
				targeted.add(selections.get(i));
			}
		}
		
		return targeted;
	}
	
	private void shootAt(Player target)
	{
		double curRange = 0;
		boolean canHit = false;
		
		while (curRange <= MAX_RANGE)
		{
			Location newTarget = getEntity().getEyeLocation().add(UtilAlg.getTrajectory(getEntity(), target).multiply(curRange));

			if (!UtilBlock.airFoliage(newTarget.getBlock()))
			{
				canHit = false;
				break;
			}
			if (UtilMath.offset(newTarget, target.getLocation()) <= 0.9)
			{
				canHit = true;
				break;
			}

			curRange += 0.2;

			UtilParticle.PlayParticle(ParticleType.WITCH_MAGIC, newTarget, 0, 0, 0, 0, 1,
					ViewDist.MAX, UtilServer.getPlayers());
			
			canHit = true;
		}
		
		if (canHit)
		{
			getBoss().getEvent().getDamageManager().NewDamageEvent(target, getEntity(), null, DamageCause.CUSTOM, 12 * getBoss().getDifficulty(), true, true, false, getEntity().getName(), "Mystical Energy");
		}
	}
	
	@Override
	public int getCooldown()
	{
		return 15;
	}

	@Override
	public boolean canMove()
	{
		return true;
	}

	@Override
	public boolean inProgress()
	{
		return false;
	}

	@Override
	public boolean hasFinished()
	{
		return _shot;
	}

	@Override
	public void setFinished()
	{
		_shot = true;
	}

	@Override
	public void tick()
	{
		if (_shot)
			return;
		
		_shot = true;
		
		for (Player target : getTargets())
		{
			shootAt(target);
		}
	}
}