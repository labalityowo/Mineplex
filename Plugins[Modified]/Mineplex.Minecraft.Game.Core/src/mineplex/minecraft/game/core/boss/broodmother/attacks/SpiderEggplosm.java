package mineplex.minecraft.game.core.boss.broodmother.attacks;

import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.minecraft.game.core.boss.broodmother.SpiderCreature;

public class SpiderEggplosm extends SpiderEggAbility
{
	private int _eggsToSpray = 25;
	private float _yaw;
	private float _addYaw;

	public SpiderEggplosm(SpiderCreature creature)
	{
		super(creature);

		_eggsToSpray = 20 + (int) Math.ceil(15 * (1 - creature.getHealthPercent()));

		_addYaw = 360F / _eggsToSpray;
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
		if (_eggsToSpray > 0)
		{
			_eggsToSpray--;

			UtilEnt.CreatureLook(getEntity(), 0, _yaw);

			_yaw += _addYaw;

			Vector vec = getEntity().getLocation().getDirection();

			vec.setY(0).normalize().multiply(0.7).setY(0.4 + (UtilMath.rr(0.4, false)));

			shootEgg(vec);
		}
	}

}
