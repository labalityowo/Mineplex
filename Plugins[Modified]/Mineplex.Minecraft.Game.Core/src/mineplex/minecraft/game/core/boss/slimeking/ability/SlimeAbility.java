package mineplex.minecraft.game.core.boss.slimeking.ability;

import mineplex.minecraft.game.core.boss.slimeking.creature.SlimeCreature;

public abstract class SlimeAbility
{
	private SlimeCreature _slime;
	private boolean _idle;
	private int _ticks;
	private int _idleTicks;

	public SlimeAbility(SlimeCreature slime)
	{
		_slime = slime;
	}

	public final void tick()
	{
		if (isIdle())
		{
			_idleTicks++;
		}
		else
		{
			_ticks++;
			tickCustom();
		}
	}

	public int getTicks()
	{
		return _ticks;
	}

	public int getIdleTicks()
	{
		return _idleTicks;
	}

	public boolean isIdle()
	{
		return _idle;
	}

	public SlimeCreature getSlime()
	{
		return _slime;
	}

	public abstract void tickCustom();

	protected void setIdle(boolean idle)
	{
		_idle = idle;
	}
}
