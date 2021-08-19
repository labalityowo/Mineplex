package mineplex.core.particleeffects;

import java.awt.*;

import org.bukkit.FireworkEffect;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;

public class FreedomTrailEffect extends Effect
{

	private Color _color = Color.RED;
	private long _count, _jumpingTimer = 0;
	private boolean _isJumping = false;
	private Entity _entity;

	public FreedomTrailEffect(Entity entity)
	{
		super(-1, new EffectLocation(entity));
		_entity = entity;
	}

	@Override
	public void runEffect()
	{
		if (!_entity.isValid() || _entity.isDead())
		{
			stop();
			return;
		}
		ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST,
				new DustSpellColor(_color), _effectLocation.getLocation().clone().add(0, .5, 0));
		for (int i = 0; i < 7; i++)
		{
			coloredParticle.setLocation(_effectLocation.getLocation().clone().add(0, .5, 0));
			coloredParticle.display();
			if (_isJumping)
			{
				coloredParticle.setLocation(_effectLocation.getLocation().clone().add(.25, .5, 0));
				coloredParticle.display();
				coloredParticle.setLocation(_effectLocation.getLocation().clone().add(0, .5, .25));
				coloredParticle.display();
			}
		}
		if (_isJumping)
		{
			_jumpingTimer++;
			if (_jumpingTimer >= 30)
			{
				setJumping(false);
				_jumpingTimer = 0;
			}
		}
		_count++;
		if (_count % 5 == 0)
		{
			if (_color == Color.RED)
				_color = Color.WHITE;
			else if (_color == Color.WHITE)
				_color = Color.BLUE;
			else
				_color = Color.RED;
		}
		if (_count == Long.MAX_VALUE - 1)
			_count = 0;
	}

	public void setJumping(boolean jumping)
	{
		_isJumping = jumping;
		if (_isJumping)
		{
			int r = (int) (Math.random() * 3);
			UtilFirework.launchFirework(_effectLocation.getLocation().clone(), FireworkEffect.Type.BALL, (r == 0) ?
							org.bukkit.Color.RED : (r == 1) ? org.bukkit.Color.WHITE : org.bukkit.Color.BLUE, false, false,
					new Vector(0, 0.01, 0), 1);
		}
	}

}
