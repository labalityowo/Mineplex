package mineplex.core.particleeffects;

import java.awt.Color;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public class FreedomFireworkEffect extends Effect
{

	private Color _fireworkColor = Color.RED;
	private int _count = 0;
	private boolean _increaseY;
	private Entity _playerEntity;

	public FreedomFireworkEffect(Entity entity, boolean increaseY)
	{
		super(-1, new EffectLocation(entity), 15);
		_increaseY = increaseY;
		_playerEntity = entity;
	}

	@Override
	public void runEffect()
	{
		if (_playerEntity.isDead() || !_playerEntity.isValid())
		{
			this.stop();
			return;
		}
		if (_count == -1)
		{
			this.stop();
			return;
		}
		Location location = _effectLocation.getLocation().clone().add(0, (_increaseY) ? 1 : 0, 0);
		BabyFireworkEffect babyFireworkEffect = new BabyFireworkEffect(location, _fireworkColor);
		babyFireworkEffect.start();
		_count++;
		if (_fireworkColor == null)
			_fireworkColor = Color.RED;
		if (_fireworkColor == Color.RED)
			_fireworkColor = Color.WHITE;
		else if (_fireworkColor == Color.WHITE)
			_fireworkColor = Color.BLUE;
		else if (_fireworkColor == Color.BLUE)
			_fireworkColor = null;
	}

}
