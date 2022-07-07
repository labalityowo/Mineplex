package nautilus.game.minekart.track.ents;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftCreature;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.crash.Crash_Explode;
import nautilus.game.minekart.track.Track;
import nautilus.game.minekart.track.TrackEntity;
import net.minecraft.server.v1_7_R1.EntityCreature;
import net.minecraft.server.v1_7_R1.Navigation;

public class Cow extends TrackEntity
{
	private boolean _spawned = false;
	private Kart _owner;
	private Vector _dir;
	
	private long _spawn = 0;

	public Cow(Track track, Location loc, Kart owner, Vector dir) 
	{
		super(track, EntityType.COW, "Stampede", 5, 1.5, 30000, loc);

		_owner = owner;
		_dir = dir;
		
		_dir.setY(0);
		_dir.normalize();
		_dir.multiply(1.2);
		_dir.setY(-0.4);
		
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
		if (!kart.IsInvulnerable(false))
		{
			UtilPlayer.message(kart.GetDriver(), 		F.main("MK", F.elem(_owner.GetDriver().getName()) + " hit you with " + F.item(GetName()) + "."));

			//Crash
			new Crash_Explode(kart, 1f, true);
		}

		//Effect
		GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.COW_HURT, 2f, 1f);
	}

	@Override
	public boolean Update()
	{
		if (UtilTime.elapsed(_spawn, 10000))
		{
			GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.COW_HURT, 2f, 0.5f);
			return true;
		}
			
		if (GetEntity() != null && !UtilBlock.airFoliage(GetEntity().getLocation().add(_dir).add(0, 1, 0).getBlock()))
		{
			GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.COW_HURT, 2f, 0.5f);
			return true;
		}
		
		//Respawn
		if (GetEntity() == null || !GetEntity().isValid())
		{
			if (_spawned)
			{
				GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.COW_HURT, 2f, 0.5f);
				return true;
			}

			SetEntity(GetLocation().getWorld().spawnEntity(GetLocation(), GetType()));
			_spawned = true;
		}
		//Move
		else 
		{
			//Push
			GetEntity().setVelocity(_dir);
			
			//Walk
			if (GetEntity() instanceof Creature)
			{			
				EntityCreature ec = ((CraftCreature)GetEntity()).getHandle();
				Navigation nav = ec.getNavigation();
				nav.a(GetEntity().getLocation().getX() + (_dir.getX() * 5), GetEntity().getLocation().getY(), GetEntity().getLocation().getZ() + (_dir.getZ() * 5), 0.4f);
				
				//Direction
				ec.setPositionRotation(GetEntity().getLocation().getX(), GetEntity().getLocation().getY(), GetEntity().getLocation().getZ(), 
						UtilAlg.GetYaw(_dir), 0);
			}
		}
		
		return false;
	}
}
