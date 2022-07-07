package nautilus.game.arcade.game.games.moba.ai;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import mineplex.core.common.geom.Point2D;
import mineplex.core.common.geom.Polygon2D;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.ai.goal.MobaAIMethod;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;

public class MobaAI
{

	private final GameTeam _owner;
	private final float _speedTarget;
	private final float _speedHome;
	private final Polygon2D _boundaries;

	protected LivingEntity _entity;
	private LivingEntity _target;
	private Location _home;

	protected MobaAIMethod _aiMethod;

	public MobaAI(Moba host, GameTeam owner, LivingEntity entity, Location home, float speedTarget, float speedHome, MobaAIMethod aiMethod)
	{
		_owner = owner;
		_speedTarget = speedTarget;
		_speedHome = speedHome;
		_entity = entity;
		_home = home;
		_aiMethod = aiMethod;

		List<Point2D> boundaryPoints = host.WorldData.GetDataLocs(getBoundaryKey()).stream()
				.map(Point2D::of)
				.collect(Collectors.toList());

		_boundaries = Polygon2D.fromUnorderedPoints(boundaryPoints);
	}

	public void updateTarget()
	{
		// Entity not spawned
		if (_entity == null || _entity.isDead() || !_entity.isValid())
		{
			return;
		}

		if (_target == null || _target.isDead() || !_target.isValid())
		{
			_target = MobaUtil.getBestEntityTarget(_owner, _entity, _home, _boundaries);

			if (_target == null)
			{
				returnToHome();
				return;
			}
		}
		else if (!MobaUtil.isInBoundary(_owner, _entity, _home, _boundaries, _target))
		{
			_target = null;
			returnToHome();
		}

		if (_target != null)
		{
			_aiMethod.updateMovement(_entity, _target.getLocation(), _speedTarget);
		}
	}

	private void returnToHome()
	{
		_aiMethod.updateMovement(_entity, _home, _speedHome);
	}

	public void setEntity(LivingEntity entity)
	{
		_entity = entity;
	}

	public LivingEntity getTarget()
	{
		return _target;
	}

	public Polygon2D getBoundaries()
	{
		return _boundaries;
	}

	public String getBoundaryKey()
	{
		return _owner.GetColor() == ChatColor.RED ? "ORANGE" : "LIGHT_BLUE";
	}
}
