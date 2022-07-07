package nautilus.game.minekart.track.ents;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftCreature;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.crash.Crash_Explode;
import nautilus.game.minekart.track.Track;
import nautilus.game.minekart.track.TrackEntity;
import net.minecraft.server.v1_7_R1.EntityCreature;
import net.minecraft.server.v1_7_R1.Navigation;

public class Spiderling extends TrackEntity
{
	private boolean _spawned = false;
	private Kart _target;
	private Kart _owner;

	private long _spawn = 0;
	
	private boolean _dead = false;

	public Spiderling(Track track, Location loc, Kart owner, Kart target) 
	{
		super(track, EntityType.CAVE_SPIDER, "Spiderling", 5, 1, 30000, loc);

		_owner = owner;
		_target = target;
		
		_spawn = System.currentTimeMillis();
	}
	
	@Override
	public void CheckCollision(Kart kart) 
	{
		if (kart.equals(_owner))
			return;
		
		if (GetEntity() == null || !GetEntity().isValid())
			return;
		
		if (UtilMath.offset(kart.GetDriver().getLocation(), GetEntity().getLocation()) > GetCollideRange())
			return;
		
		Collide(kart);
	}

	@Override
	public void Collide(Kart kart) 
	{
		this.SetSpawnTimer(System.currentTimeMillis());
		this.GetEntity().remove();

		if (!kart.IsInvulnerable(true))
		{
			UtilPlayer.message(kart.GetDriver(), 		F.main("MK", F.elem(_owner.GetDriver().getName()) + " hit you with " + F.item(GetName()) + "."));
			UtilPlayer.message(_owner.GetDriver(), 		F.main("MK", "You hit " + F.elem(kart.GetDriver().getName()) + " with " + F.item(GetName()) + "."));

			//Crash
			new Crash_Explode(kart, 1.2f, true);
		}

		//Effect
		GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.SPIDER_DEATH, 2f, 1f);
		GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.SPIDER_DEATH, 2f, 1f);
		GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.SPIDER_DEATH, 2f, 1f);
		GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.SPIDER_DEATH, 2f, 1f);
		
		_dead = true;
	}

	@Override
	public boolean Update()
	{
		if (_dead)
			return true;
		
		if (UtilTime.elapsed(_spawn, 15000))
			return true;
		
		if (_target == null)
			return true;

		if (!_target.GetDriver().isOnline())
			return true;

		//Respawn
		if (GetEntity() == null || !GetEntity().isValid())
		{
			if (_spawned)
				return true;

			SetEntity(GetLocation().getWorld().spawnEntity(GetLocation(), GetType()));
			_spawned = true;
		}
		//Return
		else 
		{
			//Push
			Vector dir = UtilAlg.getTrajectory2d(GetEntity(), _target.GetDriver());
			dir.multiply(0.75);
			dir.setY(-0.4);
			GetEntity().setVelocity(dir);
			
			//Walk
			if (GetEntity() instanceof Creature)
			{			
				EntityCreature ec = ((CraftCreature)GetEntity()).getHandle();
				Navigation nav = ec.getNavigation();
				nav.a(_target.GetDriver().getLocation().getX(), _target.GetDriver().getLocation().getY(), _target.GetDriver().getLocation().getZ(), 0.4f);
				
				//Direction
				ec.setPositionRotation(GetEntity().getLocation().getX(), GetEntity().getLocation().getY(), GetEntity().getLocation().getZ(), 
						UtilAlg.GetYaw(dir), 0);
			}
		}
			
		return false;
	}
}
