package nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.data.ReboundData;

public class PerkArrowRebound extends Perk
{
	private HashMap<Entity, ReboundData> _arrows = new HashMap<Entity, ReboundData>();

	private int _max = 0;
	private float _maxPower = 1f;
	private float _maxDistance;
	
	public PerkArrowRebound(int max, float maxPower, float maxDistance) 
	{
		super("Chain Arrows", new String[] 
				{
				C.cGray + "On hit, arrows bounce to nearby enemies.",
				C.cGray + "Arrows bounce up to " + max + " times.",
				});
		
		_max = max;
		_maxPower = maxPower;
		_maxDistance = maxDistance;
	}

	@EventHandler
	public void ShootBow(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		Player player = (Player)event.getEntity();

		if (!Kit.HasKit(player))
			return;

		_arrows.put(event.getProjectile(), new ReboundData(player, _max, null));
	}

	@EventHandler
	public void Rebound(ProjectileHitEvent event)
	{
		ReboundData data = _arrows.remove(event.getEntity());
		if (data == null)	return;
		
		if (data.Bounces <= 0)
			return;
		
		Location arrowLoc = event.getEntity().getLocation().add(event.getEntity().getVelocity());
		
		Player hit = UtilPlayer.getClosest(arrowLoc, data.Ignore);
		if (hit == null)	return;
		
		if (UtilMath.offset(hit.getLocation(), arrowLoc) > _maxDistance &&
			UtilMath.offset(hit.getEyeLocation(), arrowLoc) > _maxDistance)
			return;
		
		data.Ignore.add(hit);
		
		Player target = UtilPlayer.getClosest(event.getEntity().getLocation().add(event.getEntity().getVelocity()), data.Ignore);
		if (target == null)	return;
		
		Vector trajectory = UtilAlg.getTrajectory(hit, target);
		trajectory.add(new Vector(0, UtilMath.offset(hit, target) / 100d, 0));
		
		float power = (float) (0.8 + UtilMath.offset(hit, target) / 30d);
		if (_maxPower > 0 && power > _maxPower)
			power = _maxPower;
		
		Arrow ent = hit.getWorld().spawnArrow(hit.getEyeLocation().add(UtilAlg.getTrajectory(hit, target)), trajectory, power, 0f);
		ent.setShooter(data.Shooter);
		
		_arrows.put(ent, new ReboundData(data.Shooter, data.Bounces-1, data.Ignore));
	}
}
