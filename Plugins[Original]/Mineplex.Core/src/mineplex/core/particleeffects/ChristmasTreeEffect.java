package mineplex.core.particleeffects;

import java.awt.*;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;

public class ChristmasTreeEffect extends Effect
{

	private Player _player;
	private JavaPlugin _plugin;
	private double _currentRadius = 0;
	private double _currentY = 0;
	private GadgetManager _manager;

	private static final double MAX_RADIUS = 1.;
	private static final double MAX_Y = 2.5;

	public ChristmasTreeEffect(Player player, GadgetManager manager)
	{
		super(-1, new EffectLocation(player), 5);
		_player = player;
		_manager = manager;
	}

	@Override
	public void runEffect()
	{
		if (_manager.isMoving(_player))
		{
			Location loc = _player.getLocation().add(0, 1.2, 0).add(_player.getLocation().getDirection().multiply(-0.2));
			ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST, new DustSpellColor(new Color(8388608)), loc);
			coloredParticle.display();
			coloredParticle.setColor(new DustSpellColor(Color.GREEN));
			coloredParticle.display();
			return;
		}
		if (_currentY < MAX_Y)
		{
			double radius = MAX_RADIUS - _currentRadius;
			_currentRadius += 0.1;
			CircleEffect circleEffect = new CircleEffect(_player.getLocation().clone().add(0, _currentY, 0), radius, Color.GREEN);
			circleEffect.addRandomColor(Color.RED);
			circleEffect.addRandomColor(Color.YELLOW);
			circleEffect.start();
			_currentY += 0.25;
		}
		else
		{
			BabyFireworkEffect babyFireworkEffect = new BabyFireworkEffect(_player.getLocation().clone().add(0, MAX_Y + .5, 0), Color.YELLOW, 1);
			babyFireworkEffect.setTrail(false);
			babyFireworkEffect.start();
			babyFireworkEffect.setCallback(data ->
			{
				_currentY = 0;
				_currentRadius = 0;
			});
		}
	}

}
