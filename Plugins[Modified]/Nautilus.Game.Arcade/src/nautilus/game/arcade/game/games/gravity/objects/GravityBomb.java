package nautilus.game.arcade.game.games.gravity.objects;

import java.util.HashSet;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import nautilus.game.arcade.game.games.gravity.Gravity;
import nautilus.game.arcade.game.games.gravity.GravityObject;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class GravityBomb extends GravityObject
{
	public Player Owner;
	private long _blockHitDelay;
	
	public GravityBomb(Gravity host, Entity ent, double mass, Vector vel, Player owner) 
	{
		super(host, ent, mass, 1.5, vel);
		
		Owner = owner;
		
		_blockHitDelay = System.currentTimeMillis();
	}

	@Override
	public void PlayCollideSound(double power) 
	{
		Ent.getWorld().playSound(Ent.getLocation(), Sound.EXPLODE, 1f, 1f);
	}
	
	public boolean CollideCheck(GravityObject other) 
	{
		if (this.equals(other))
			return false;
		
		if (System.currentTimeMillis() < this.CollideDelay)
			return false;
		
		if (System.currentTimeMillis() < other.CollideDelay)
			return false;
		
		if (this.Vel.length() == 0 && other.Vel.length() == 0)
			return false;
		
		double size = this.Size;
		if (other.Size > size)
			size = other.Size;
	
		if (UtilMath.offset(this.Base, other.Base) > size)
			return false;
		
		return true;
	}
	
	public HashSet<GravityDebris> BombDetonate()
	{
		boolean collided = false;
		
		//Collide with Objects
		for (GravityObject obj : Host.GetObjects())
		{
			if (!CollideCheck(obj))
				continue;
			
			collided = true;
		}
		
		//Collide with Blocks
		if (!collided && UtilTime.elapsed(_blockHitDelay, 100))
			for (Block block : UtilBlock.getInRadius(Base.getLocation().add(0, 0.5, 0), 2d).keySet())
			{
				if (UtilBlock.airFoliage(block))
					continue;	
				
				//X
				if (block.getLocation().getX() + 0.5 < Base.getLocation().getX())
					if (Vel.getX() > 0)
						continue;
				
				if (block.getLocation().getX() + 0.5 > Base.getLocation().getX())
					if (Vel.getX() < 0)
						continue;

				//Y
				if (block.getLocation().getY() + 0.5 < Base.getLocation().getY())
					if (Vel.getY() > 0)
						continue;
				
				if (block.getLocation().getY() + 0.5 > Base.getLocation().getY())
					if (Vel.getY() < 0)
						continue;

				//Z
				if (block.getLocation().getZ() + 0.5 < Base.getLocation().getZ())
					if (Vel.getZ() > 0)
						continue;
				
				if (block.getLocation().getZ() + 0.5 > Base.getLocation().getZ())
					if (Vel.getZ() < 0)
						continue;

				collided = true;
				break;
			}
		
		if (!collided)
			return null;
		
		//Blast Objs
		for (GravityObject obj : Host.GetObjects())
		{
			if (UtilMath.offset(this.Base, obj.Base) > 3)
				continue;
			
			if (this.equals(obj))
				continue;
			
			if (System.currentTimeMillis() < obj.CollideDelay)
				continue;
			
			if (this.Vel.length() == 0 && obj.Vel.length() == 0)
				continue;
			
			obj.AddVelocity(UtilAlg.getTrajectory(this.Base, obj.Base).multiply(0.4), 10);
		}
		
		//Blast Debris
		HashSet<GravityDebris> debris = new HashSet<GravityDebris>();
		
		for (Block block : UtilBlock.getInRadius(Base.getLocation().add(0, 0.5, 0), 3d).keySet())
		{
			if (UtilBlock.airFoliage(block))
				continue;
			
			if (block.getType() == Material.EMERALD_BLOCK)
				continue;

			//Projectile
			Vector velocity = UtilAlg.getTrajectory(Ent.getLocation(), block.getLocation().add(0.5, 0.5, 0.5));
			velocity.add(Vel.clone().normalize());
			velocity.add((new Vector(Math.random()-0.5,Math.random()-0.5,Math.random()-0.5)).multiply(0.5));
			velocity.multiply(0.3);
			
			//Block
			Material type = block.getType();
			byte data = block.getData();
			block.setType(Material.AIR);

			//Projectile
			FallingBlock projectile = block.getWorld().spawnFallingBlock(block.getLocation().add(0.5, 0.6, 0.5), type,data);
			GravityDebris newDebris = new GravityDebris(Host, projectile, 12, velocity);
			
			//Add
			debris.add(newDebris);
		}
		
		return debris;
	}
	
	@Override
	public void CustomCollide(GravityObject other) 
	{
		
		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, Ent.getLocation(), 0, 0, 0, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
		Ent.getWorld().playSound(Ent.getLocation(), Sound.EXPLODE, 0.3f, 1f);
		Ent.remove();
	}
	
	@Override
	public boolean CanCollide(GravityObject other)
	{
		return false;
	}
}
