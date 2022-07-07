package nautilus.game.minekart.track.ents;

import java.util.HashSet;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftCreature;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;
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

public class Sheepile extends TrackEntity
{
	private boolean _spawned = false;
	private Kart _owner;
	private HashSet<Kart> _hit = new HashSet<Kart>();

	private long _spawn = 0;

	private int _height = 2;
	
	public Sheepile(Track track, Location loc, Kart owner) 
	{
		super(track, EntityType.SHEEP, "Super Sheep", 5, 1, 30000, loc);

		_owner = owner;

		_spawn = System.currentTimeMillis();
	}

	@Override
	public void CheckCollision(Kart kart) 
	{
		if (kart.equals(_owner))
			return;
		
		if (_hit.contains(kart))
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
		if (!kart.IsInvulnerable(true))
		{
			UtilPlayer.message(kart.GetDriver(), 		F.main("MK", F.elem(_owner.GetDriver().getName()) + " hit you with " + F.item(GetName()) + "."));
			UtilPlayer.message(_owner.GetDriver(), 		F.main("MK", "You hit " + F.elem(kart.GetDriver().getName()) + " with " + F.item(GetName()) + "."));

			//Crash
			new Crash_Explode(kart, 1.2f, true);
		}
		
		_hit.add(kart);

		//Effect
		GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.EXPLODE, 1f, 2f);
	}

	@Override
	public boolean Update()
	{
		if (UtilTime.elapsed(_spawn, 15000))
			return true;

		//Respawn
		if (GetEntity() == null || !GetEntity().isValid())
		{
			if (_spawned)
				return true;

			SetEntity(GetLocation().getWorld().spawnEntity(GetLocation(), GetType()));
			_spawned = true;

			//Color
			Sheep sheep = (Sheep)GetEntity();
			sheep.setBaby();
			sheep.setColor(DyeColor.RED);
			
			return false;
		}


		//Control
		Location target = null;

		//Target Kart
		for (Kart kart : Track.GetGP().GetKarts())
		{
			if (_owner.equals(kart) || _hit.contains(kart))
				continue;
			
			if (UtilMath.offset(kart.GetDriver(), GetEntity()) < 8)
				target = kart.GetDriver().getLocation();
		}

		//Target Lap
		if (target == null)
		{
			int best = GetClosestNode();
			Location node = Track.GetProgress().get((best+1)%Track.GetProgress().size());
			
			target = new Location(node.getWorld(), node.getX(), node.getY() + _height, node.getZ());
		}
		
		//Push
		Vector dir = UtilAlg.getTrajectory(GetEntity().getLocation(), target);
		dir.normalize();
		dir.multiply(1.2);
		

		GetEntity().setVelocity(dir);

		//Direction
		if (GetEntity() instanceof Creature)
		{			
			EntityCreature ec = ((CraftCreature)GetEntity()).getHandle();
			ec.setPositionRotation(GetEntity().getLocation().getX(), GetEntity().getLocation().getY(), GetEntity().getLocation().getZ(), 
					UtilAlg.GetYaw(dir), 0);
		}

		//Color
		Sheep sheep = (Sheep)GetEntity();
		sheep.setBaby();
		double r = Math.random();
		if (r > 0.75)		sheep.setColor(DyeColor.RED);
		else if (r > 0.5)	sheep.setColor(DyeColor.YELLOW);
		else if (r > 0.25)	sheep.setColor(DyeColor.GREEN);
		else 				sheep.setColor(DyeColor.BLUE);
		
		//Sound
		GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.SHEEP_IDLE, 2f, 2f);
		
		return false;
	}
	
	public int GetClosestNode()
	{
		int node = -1;
		double bestDist = 9999;

		for (int i=0 ; i<Track.GetProgress().size() ; i++)
		{
			Location cur = Track.GetProgress().get(i);

			double dist = UtilMath.offset(GetEntity().getLocation().subtract(0, _height, 0), cur);

			if (node == -1)
			{
				node  = i;
				bestDist = dist;
			}

			else if (dist < bestDist)
			{
				node  = i;
				bestDist = dist;
			}
		}

		return node;
	}
}
