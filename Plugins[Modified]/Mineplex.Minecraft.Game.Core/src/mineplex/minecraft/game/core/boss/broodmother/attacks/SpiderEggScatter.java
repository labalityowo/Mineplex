package mineplex.minecraft.game.core.boss.broodmother.attacks;

import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.minecraft.game.core.boss.broodmother.SpiderCreature;

public class SpiderEggScatter extends SpiderEggAbility
{
	private int _eggsToSpray = 25;
	private float _yaw;
	private float _addYaw;
	private int _tick;

	public SpiderEggScatter(SpiderCreature creature)
	{
		super(creature);

		_eggsToSpray = 6 + UtilMath.r(5);

		_addYaw = (360F / _eggsToSpray) / 4;
	}

	public int getCooldown()
	{
		return 120;
	}

	@Override
	public boolean canMove()
	{
		return _eggsToSpray <= 0;
	}

	@Override
	public boolean inProgress()
	{
		return _eggsToSpray > 0 || !hasEggsFinished();
	}

	@Override
	public boolean hasFinished()
	{
		return _eggsToSpray <= 0 && hasEggsFinished();
	}

	@Override
	public void setFinished()
	{
		setEggsFinished();
	}

	@Override
	public void tick()
	{
		if (_eggsToSpray <= 0)
		{
			return;
		}

		UtilEnt.CreatureLook(getEntity(), 0, _yaw);

		_yaw += _addYaw;

		if (_tick % 4 == 0 && _eggsToSpray > 0)
		{
			_eggsToSpray--;

			Vector vec = getEntity().getLocation().getDirection();

			vec.setY(0).normalize().multiply(0.7 + UtilMath.rr(0.16, false)).setY(0.4 + (UtilMath.rr(0.4, false)));

			shootEgg(vec);
		}

		_tick++;
	}

}
