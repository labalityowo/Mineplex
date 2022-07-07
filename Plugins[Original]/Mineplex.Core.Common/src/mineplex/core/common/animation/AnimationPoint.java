package mineplex.core.common.animation;

import org.bukkit.util.Vector;

import java.util.Objects;

public class AnimationPoint
{
	
	private final int _tick;
	private final Vector _move;
	private final Vector _dir;

	public AnimationPoint(int tick, Vector move, Vector dir)
	{
		_tick = tick;
		_move = move.clone();
		_dir = dir.clone();
	}
	
	public Vector getMove()
	{
		return _move.clone();
	}
	
	public Vector getDirection()
	{
		return _dir.clone();
	}
	
	public int getTick()
	{
		return _tick;
	}
	
	@Override
	public String toString()
	{
		return "AnimationPoint[tick" + _tick + ", motion:[" + _move + "], dir:[" + _dir + "]]";
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof AnimationPoint) {
			AnimationPoint point = (AnimationPoint) obj;
			return point._tick == _tick && point._move.equals(_move);
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(_tick, _move);
	}
}