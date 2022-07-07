package mineplex.core.particleeffects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;

public class BlowAKissEffect extends Effect
{

	private int _count = 0;
	private Vector _vector;
	private Location _fixedLoc;
	private Player _player;

	public BlowAKissEffect(Player player, Location target)
	{
		super(-1, new EffectLocation(player));
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
			UtilParticle.PlayParticle(UtilParticle.ParticleType.HEART, _fixedLoc, 0, 0, 0, 0, 2, UtilParticle.ViewDist.LONG);
		}
		if (checkPlayer())
		{
			stop();
			return;
		}
		if (_fixedLoc.getBlock().getType() != Material.AIR || _count >= 1000)
		{
			UtilServer.broadcast(F.main("Blow A Kiss", F.name(_player.getName()) + " wanted to kiss someone but no one was around!"));
			stop();
		}
		_count += 5;
	}

	private boolean checkPlayer()
	{
		for (Player player : UtilPlayer.getNearby(_fixedLoc, 1.25))
		{
			if (player.equals(_player) || UtilPlayer.isSpectator(player))
			{
				continue;
			}

			UtilParticle.PlayParticle(UtilParticle.ParticleType.HEART, player.getLocation(), 0.25f, 0.25f, 0.25f, 0.5f, 7, UtilParticle.ViewDist.NORMAL);
			UtilServer.broadcast(F.main("Blow A Kiss", F.name(_player.getName()) + " blows a kiss at " + F.name(player.getName()) + "!"));
			return true;
		}

		return false;
	}

}
