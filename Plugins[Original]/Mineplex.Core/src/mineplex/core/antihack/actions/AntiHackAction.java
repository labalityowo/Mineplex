package mineplex.core.antihack.actions;

import com.mineplex.anticheat.api.PlayerViolationEvent;

public abstract class AntiHackAction
{
	private final int _vl;

	AntiHackAction(int vl)
	{
		this._vl = vl;
	}

	public final int getMinVl()
	{
		return this._vl;
	}

	public abstract void handle(PlayerViolationEvent event);
}
