package nautilus.game.arcade.game.games.gravity.objects;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import nautilus.game.arcade.game.games.gravity.Gravity;
import nautilus.game.arcade.game.games.gravity.GravityObject;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class GravityDebris extends GravityObject
{
	public GravityDebris(Gravity host, Entity ent, double mass, Vector vel) 
	{
		super(host, ent, mass, 2, vel);
		
		CollideDelay = System.currentTimeMillis() + 500;
		SetMovingBat(true);
	}
	
	@Override
	public void CustomCollide(GravityObject other) 
	{
		Ent.remove();
		UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, Ent.getLocation(), 0, 0, 0, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
	}
	
	public boolean CanCollide(GravityObject other)
	{
		return !(other instanceof GravityDebris);
	}
}
