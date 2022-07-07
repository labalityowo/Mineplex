package mineplex.game.clans.clans.worldevent.raid.wither.creature.wither;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossAbility;

public class MiniCharlesSkulls extends BossAbility<MiniCharles, Wither>
{
	private long _lastUnguided;
	private List<WitherSkull> _shot = new ArrayList<>();
	
	public MiniCharlesSkulls(MiniCharles creature)
	{
		super(creature);
		
		_lastUnguided = System.currentTimeMillis();
	}

	@Override
	public void tick()
	{
		if (UtilTime.elapsed(_lastUnguided, 10000))
		{
			Player target = UtilPlayer.getClosest(getLocation());
			if (target != null)
			{
				_lastUnguided = System.currentTimeMillis();
				UtilEnt.LookAt(getEntity(), target.getEyeLocation());
				_shot.add(getEntity().launchProjectile(WitherSkull.class));
				return;
			}
		}
	}
	
	@EventHandler
	public void onExplode(EntityExplodeEvent event)
	{
		if (event.getEntity().getEntityId() == getEntity().getEntityId())
		{
			event.blockList().clear();
			return;
		}
		if (event.getEntity() instanceof WitherSkull)
		{
			WitherSkull skull = (WitherSkull) event.getEntity();
			if (skull.getShooter() instanceof Wither && ((Wither)skull.getShooter()).getEntityId() == getEntity().getEntityId())
			{
				event.blockList().clear();
				return;
			}
		}
	}
	
	@EventHandler
	public void onSkullHit(ProjectileHitEvent event)
	{
		if (event.getEntity() instanceof WitherSkull)
		{
			WitherSkull skull = (WitherSkull) event.getEntity();
			if (skull.getShooter() instanceof Wither && ((Wither)skull.getShooter()).getEntityId() == getEntity().getEntityId())
			{
				_shot.remove(skull);
				UtilParticle.PlayParticle(ParticleType.EXPLODE, skull.getLocation(), null, 0, 2, ViewDist.MAX, UtilServer.getPlayers());
				skull.getWorld().playSound(skull.getLocation(), Sound.EXPLODE, 10, 0);
				Player hit = UtilPlayer.getClosest(skull.getLocation(), 0.5);
				if (hit != null)
				{
					getBoss().getEvent().getDamageManager().NewDamageEvent(hit, getEntity(), skull, DamageCause.PROJECTILE, 4, true, true, false, getEntity().getName(), "Wither Skull");
					getBoss().getEvent().getCondition().Factory().Wither("Wither Skull", hit, getEntity(), 3, 0, false, true, false);
				}
				skull.remove();
			}
		}
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
		return false;
	}

	@Override
	public void setFinished()
	{
		_shot.forEach(WitherSkull::remove);
		_shot.clear();
	}
}