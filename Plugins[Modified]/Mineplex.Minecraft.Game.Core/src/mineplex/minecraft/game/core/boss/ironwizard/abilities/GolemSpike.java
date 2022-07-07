package mineplex.minecraft.game.core.boss.ironwizard.abilities;

import java.util.List;
import java.util.Random;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.minecraft.game.core.boss.BossAbility;
import mineplex.minecraft.game.core.boss.ironwizard.GolemCreature;

import org.bukkit.Sound;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

public class GolemSpike extends BossAbility<GolemCreature, IronGolem>
{
	private static final int SPIKE_HEIGHT = 2;
	private static final long ATTACK_DURATION = 1000 + (500 * SPIKE_HEIGHT * 2);
	private static final long SPIKE_REMAIN = 1000;
	private long _start;
	private List<GroundSpike> _spikes;
	
	public GolemSpike(GolemCreature creature)
	{
		super(creature);
		_start = System.currentTimeMillis();
		_spikes = Lists.newArrayList();
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
		return true;
	}

	@Override
	public boolean hasFinished()
	{
		return UtilTime.elapsed(_start, ATTACK_DURATION) && _spikes.isEmpty();
	}

	@Override
	public void setFinished()
	{
		_start = System.currentTimeMillis() - ATTACK_DURATION;
		for (GroundSpike spike : _spikes)
		{
			spike.finish();
		}
		_spikes.clear();
	}

	@Override
	public void tick()
	{
		if (_spikes.isEmpty())
		{
			for (Player player : UtilPlayer.getInRadius(getLocation(), 20).keySet())
			{
				if (UtilEnt.isGrounded(player))
				{
					player.playSound(player.getLocation(), Sound.STEP_STONE, 0.2f, 0.2f);
					getBoss().getEvent().getDamageManager().NewDamageEvent(player, getBoss().getEntity(), null, DamageCause.CUSTOM, 5 * getBoss().getDifficulty(), false, false, false, getBoss().getEntity().getName(), "Stone Spike");	
					player.teleport(player.getLocation().add(0, SPIKE_HEIGHT, 0));
					UtilAction.velocity(player, player.getLocation().toVector().normalize().add(new Vector(0, 0.02, 0)).normalize());
					_spikes.add(new GroundSpike(player.getLocation().getBlock(), player.getLocation().getBlock().getRelative(0, -1, 0).getType(), new Random().nextInt(SPIKE_HEIGHT) + 2, SPIKE_REMAIN));
				}
			}
		}
		else
		{
			List<GroundSpike> toRemove = Lists.newArrayList();
			for (GroundSpike spike : _spikes)
			{
				spike.tick();
				if (spike.isFinished())
				{
					toRemove.add(spike);
				}
			}
			for (GroundSpike remove : toRemove)
			{
				_spikes.remove(remove);
			}
		}
	}
}