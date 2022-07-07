package mineplex.game.clans.clans.worldevent.raid.wither.creature.magma;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLargeFireball;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossPassive;
import net.minecraft.server.v1_8_R3.EntityLargeFireball;

public class MagmusMeteor extends BossPassive<Magmus, MagmaCube>
{
	private long _lastUse;
	private List<LargeFireball> _shot = new ArrayList<>();
	
	public MagmusMeteor(Magmus creature)
	{
		super(creature);
		_lastUse = System.currentTimeMillis();
	}
	
	private void newBall()
	{
		if (getBoss().HeatingRoom)
		{
			return;
		}
		Player target = UtilMath.randomElement(getBoss().getChallenge().getRaid().getPlayers());
		LargeFireball ball = target.getWorld().spawn(target.getLocation().add(2 * Math.random(), 10 + Math.random() * 16, 2 * Math.random()), LargeFireball.class);

		EntityLargeFireball eFireball = ((CraftLargeFireball) ball).getHandle();
		eFireball.dirX = (Math.random()-0.5)*0.02;
		eFireball.dirY = -0.2 - 0.05 * Math.random();
		eFireball.dirZ = (Math.random()-0.5)*0.02;
		
		ball.setShooter(getEntity());
		ball.setYield(0f);
		ball.setBounce(false);
		ball.setIsIncendiary(false);
		_shot.add(ball);
	}
	
	@Override
	public int getCooldown()
	{
		return 30;
	}
	
	@Override
	public boolean isProgressing()
	{
		return false;
	}

	@Override
	public void tick()
	{
		if (getBoss().HeatingRoom)
		{
			return;
		}
		if (UtilTime.elapsed(_lastUse, getCooldown() * 1000))
		{
			_lastUse = System.currentTimeMillis();
			for (int i = 0; i < 20; i++)
			{
				newBall();
			}
		}
	}
	
	@EventHandler
	public void onExplode(EntityExplodeEvent event)
	{
		if (event.getEntity() instanceof LargeFireball)
		{
			LargeFireball ball = (LargeFireball) event.getEntity();
			if (ball.getShooter() instanceof MagmaCube && ((MagmaCube)ball.getShooter()).getEntityId() == getEntity().getEntityId())
			{
				event.blockList().clear();
				return;
			}
		}
	}
	
	@EventHandler
	public void onBallHit(ProjectileHitEvent event)
	{
		if (event.getEntity() instanceof LargeFireball)
		{
			LargeFireball ball = (LargeFireball) event.getEntity();
			if (_shot.contains(ball))
			{
				_shot.remove(ball);
				UtilParticle.PlayParticle(ParticleType.EXPLODE, ball.getLocation(), null, 0, 2, ViewDist.MAX, UtilServer.getPlayers());
				ball.getWorld().playSound(ball.getLocation(), Sound.EXPLODE, 10, 0);
				Player hit = UtilPlayer.getClosest(ball.getLocation(), 3);
				if (hit != null)
				{
					getBoss().getEvent().getDamageManager().NewDamageEvent(hit, getEntity(), ball, DamageCause.PROJECTILE, 4, true, true, false, getEntity().getName(), "Meteor Shower");
				}
				ball.remove();
			}
		}
	}
}