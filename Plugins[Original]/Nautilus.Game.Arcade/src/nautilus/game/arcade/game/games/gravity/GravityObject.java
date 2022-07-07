package nautilus.game.arcade.game.games.gravity;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.disguise.disguises.DisguiseBat;
import nautilus.game.arcade.game.games.gravity.objects.*;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.util.Vector;

public abstract class GravityObject 
{
	public Gravity Host;
	
	public Entity Ent;
	public double Mass;
	public double Size;
	public Vector Vel;
	
	public Zombie Base;
	public DisguiseBat Bat;
	
	public long GrabDelay = 0;
	
	public long CollideDelay;
	
	public GravityObject(Gravity host, Entity ent, double mass, double size, Vector vel)
	{
		Host = host; 
		
		Ent = ent;
		Mass = mass;
		Size = size;
		
		CollideDelay = System.currentTimeMillis() + 100;
		
		if (vel != null)
			Vel = vel;
		else
			Vel = new Vector(0,0,0);
		
		Host.CreatureAllowOverride = true;
		Base = ent.getWorld().spawn(ent.getLocation().subtract(0, 0, 0), Zombie.class);
		Host.CreatureAllowOverride = false;
	
		Base.setMaxHealth(60);
		Base.setHealth(60);
		
		Bat = new DisguiseBat(Base);
		Bat.setSitting(true);
		Host.Manager.GetDisguise().disguise(Bat);
		
		UtilEnt.vegetate(Base, true);
		//UtilEnt.ghost(Base, true, true);
		Host.Manager.GetCondition().Factory().Invisible(null, Base, null, 9999, 1, false, false, false);
	}
	
	public boolean IsPlayer()
	{
		return Ent instanceof Player;
	}

	public boolean Update() 
	{
		if (!Ent.isValid())
			return false;
		
		if (!Base.isValid())
			return false;
		
		if (IsPlayer())
			if (!Host.IsAlive((Player)Ent))
				return false;
		
		if (Ent.getVehicle() == null)
			Base.setPassenger(Ent);

		Base.setVelocity(Vel);
		
		//Effect
		if (Vel.length() > 0)
		{
			if (this instanceof GravityPlayer)
			{
				UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, Ent.getLocation().subtract(0, 0.5, 0), 0, 0, 0, 0, 1, 
						ViewDist.MAX, UtilServer.getPlayers());
			}
			else if (this instanceof GravityBomb)
			{
				UtilParticle.PlayParticle(ParticleType.FLAME, Ent.getLocation().add(0, -0.1, 0), 0, 0, 0, 0, 1,
						ViewDist.MAX, UtilServer.getPlayers());
			}
			else if (this instanceof GravityHook)
			{
				UtilParticle.PlayParticle(ParticleType.SNOW_SHOVEL, Ent.getLocation().add(0, 0.1, 0), 0, 0, 0, 0, 1,
						ViewDist.MAX, UtilServer.getPlayers());
			}
		}		
		
		return true;
	}
	
	public void Collide(GravityObject other) 
	{
		if (this.equals(other))
			return;
		
		if (System.currentTimeMillis() < this.CollideDelay)
			return;
		
		if (System.currentTimeMillis() < other.CollideDelay)
			return;
		
		if (this.Vel.length() == 0 && other.Vel.length() == 0)
			return;
		
		if (!this.CanCollide(other) || !other.CanCollide(this))
			return;
		
		double size = this.Size;
		if (other.Size > size)
			size = other.Size;
	
		if (UtilMath.offset(this.Base, other.Base) > size)
			return;
		
		Vector v1 = Vel;
		Vector v2 = other.Vel;
		
		//Physics
		double totalMass = this.Mass + other.Mass;
		
		this.Vel = v1.clone().multiply((this.Mass - other.Mass)/(totalMass)).add(v2.clone().multiply((2 * other.Mass)/(totalMass)));
		
		other.Vel = v1.clone().multiply((2 * this.Mass)/(totalMass)).subtract(v2.clone().multiply((this.Mass - other.Mass)/(totalMass)));
		
		//Sound
		double power = v1.clone().multiply(this.Mass).subtract(v2.clone().multiply(other.Mass)).length();
		//Host.Announce("Collision Power: " + power);
		
		this.PlayCollideSound(power);
		other.PlayCollideSound(power);
				
		//Delay 
		this.CollideDelay = System.currentTimeMillis() + 1000;
		other.CollideDelay = System.currentTimeMillis() + 1000;
		
		this.GrabDelay = System.currentTimeMillis();
		other.GrabDelay = System.currentTimeMillis();
		
		//Collide
		this.CustomCollide(other);
		other.CustomCollide(this);
		
		//Animation
		this.SetMovingBat(true);
		other.SetMovingBat(true);
	}

	public boolean CanCollide(GravityObject other)
	{
		return true;
	}

	public void CustomCollide(GravityObject other) 
	{
		
	}

	public void PlayCollideSound(double power) 
	{
		Ent.getWorld().playSound(Ent.getLocation(), Sound.ZOMBIE_WOOD, 1f, 1f);
	}

	public void AddVelocity(Vector vel)
	{
		AddVelocity(vel, 50);
	}
	
	public void AddVelocity(Vector vel, double limit)
	{
		double preLength = Vel.length();
		
		Vel.add(vel);
		
		//Soft Limit
		if (Vel.length() > limit && Vel.length() > preLength)
		{
			Vel.normalize().multiply(preLength);
		}
		
		//Hard Limit
		if (Vel.length() > 3)
		{
			Vel.normalize().multiply(3);
		}
		
		SetMovingBat(true);
	}

	public void Clean() 
	{
		Ent.leaveVehicle();
		
		if (!(Ent instanceof Player))
			Ent.remove();
		
		Base.remove();
	}
	
	public void SetMovingBat(boolean moving)
	{
		Bat.setSitting(!moving);
		//Host.Manager.GetDisguise().updateDisguise(Bat);
	}
	
	public void remove()
	{
		if (Base.getPassenger() != null)
			Base.getPassenger().remove();
		
		Base.remove();
	}
}
