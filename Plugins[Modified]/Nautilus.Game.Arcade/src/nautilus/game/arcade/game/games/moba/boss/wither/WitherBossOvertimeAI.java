package nautilus.game.arcade.game.games.moba.boss.wither;

import mineplex.core.common.geom.Polygon2D;
import mineplex.core.common.util.UtilMath;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.ai.MobaAI;
import nautilus.game.arcade.game.games.moba.ai.goal.MobaAIMethod;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public class WitherBossOvertimeAI extends MobaAI
{

	private List<Location> _path;
	private Location _target;
	private int _targetIndex;

	public WitherBossOvertimeAI(Moba host, GameTeam owner, LivingEntity entity, Location home, float speedTarget, float speedHome, MobaAIMethod aiMethod)
	{
		super(host, owner, entity, home, speedTarget, speedHome, aiMethod);

		_path = host.getMinionManager().getPath(owner.GetColor() == ChatColor.RED);
		_path = _path.subList(0, (int) (_path.size() / 2D));
	}

	@Override
	public void updateTarget()
	{
		if (_target == null)
		{
			_target = _path.get(0);
			_targetIndex = 0;
		}

		double dist = UtilMath.offsetSquared(_target, _entity.getLocation());

		if (dist < 16 && _targetIndex < _path.size() - 1)
		{
			_targetIndex++;
			_target = _path.get(_targetIndex);
		}

		_aiMethod.updateMovement(_entity, _target, 2);
	}

	@Override
	public Polygon2D getBoundaries()
	{
		return null;
	}
}
