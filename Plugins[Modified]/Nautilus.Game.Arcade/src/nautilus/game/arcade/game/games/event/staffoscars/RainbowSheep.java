package nautilus.game.arcade.game.games.event.staffoscars;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class RainbowSheep implements Listener
{

	private static final Color[] COLOURS = {
		Color.RED, Color.ORANGE, Color.YELLOW, Color.LIME, Color.AQUA, Color.BLUE, Color.PURPLE
	};

	private final Set<Sheep> _sheep;
	private Player _player;
	private boolean _active;
	private long _start;

	public RainbowSheep()
	{
		_sheep = new HashSet<>();
	}

	@EventHandler
	public void updateSpawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER || !_active)
		{
			return;
		}

		if (UtilTime.elapsed(_start, 5000))
		{
			for (Sheep sheep : _sheep)
			{
				sheep.remove();
			}

			_sheep.clear();
			_active = false;
			return;
		}

		Vector direction = _player.getLocation().getDirection();
		Vector left = UtilAlg.getLeft(direction).add(direction.clone().multiply(1.5));
		Vector right = UtilAlg.getRight(direction).add(direction.clone().multiply(1.5));

		spawnSheep(_player, direction);
		spawnSheep(_player, left);
		spawnSheep(_player, right);
	}

	@EventHandler
	public void updateParticles(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Sheep sheep : _sheep)
		{
			UtilParticle.playColoredParticleToAll(COLOURS[UtilMath.r(COLOURS.length)], UtilParticle.ParticleType.RED_DUST, sheep.getLocation().add(0, 0.75, 0), 2, UtilParticle.ViewDist.LONGER);
		}
	}

	@EventHandler
	public void entityDamage(EntityDamageEvent event)
	{
		if (_sheep.contains(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	private void spawnSheep(Player player, Vector vector)
	{
		Sheep sheep = player.getWorld().spawn(player.getEyeLocation(), Sheep.class);

		sheep.setCustomName("jeb_");
		sheep.setVelocity(vector);

		_sheep.add(sheep);
	}

	public void setActive(Player player)
	{
		_player = player;
		_active = true;
		_start = System.currentTimeMillis();
	}

	public boolean isActive()
	{
		return _active;
	}
}
