package mineplex.core.particleeffects;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.gadget.types.Gadget;

public class LoveDoctorEffect extends Effect
{

	private int _count = 0;
	private Vector _vector;
	private Location _fixedLoc;
	private Gadget _gadget;
	private Player _player;

	public LoveDoctorEffect(Player player, Location target, Gadget gadget)
	{
		super(-1, new EffectLocation(player));
		_gadget = gadget;
		_player = player;
		setTargetLocation(new EffectLocation(target));
	}

	@Override
	public void onStart()
	{
		_player.getWorld().playSound(_player.getLocation(), Sound.PISTON_RETRACT, 1f, 1f);
	}

	@Override
	public void runEffect()
	{
		Location location = _effectLocation.getFixedLocation().clone().add(0, 1, 0);
		if (_vector == null)
		{
			Location targetLoc = getTargetLocation().getFixedLocation().clone();
			Vector link = targetLoc.toVector().subtract(location.toVector());
			float length = (float) link.length();
			link.normalize();
			int particles = 100;
			_vector = link.multiply(length / particles);
			_fixedLoc = location.clone().subtract(_vector);
		}
		for (int i = 0; i < 5; i++){
			_fixedLoc.add(_vector);
			UtilParticle.PlayParticle(UtilParticle.ParticleType.HEART, _fixedLoc, 0, 0, 0, 0, 1, UtilParticle.ViewDist.LONG);
		}
		if (_fixedLoc.getBlock().getType() != Material.AIR )
		{
			stop();
		}
		else if (_count >= 1000)
		{
			stop();
		}
		_count += 5;
	}

	@Override
	public void onStop()
	{
		// Creates the explosion and knockback players
		Location loc = _fixedLoc;
		loc.getWorld().createExplosion(loc, 0f);
		UtilParticle.PlayParticle(UtilParticle.ParticleType.EXPLODE, loc, 3f, 3f, 3f, 0, 32, UtilParticle.ViewDist.MAX, UtilServer.getPlayers());
		HashMap<Player, Double> players = UtilPlayer.getInRadius(loc, 9d);
		for (Player ent : players.keySet())
		{
			if (!_gadget.Manager.selectEntity(_gadget, ent))
			{
				continue;
			}

			double mult = players.get(ent);

			//Knockback
			UtilAction.velocity(ent, UtilAlg.getTrajectory(loc, ent.getLocation()), 2 * mult, false, 0, 1 + 1 * mult, 10, true);
			LoveDoctorHitEffect loveDoctorHitEffect = new LoveDoctorHitEffect(ent);
			loveDoctorHitEffect.start();
		}
	}

}
