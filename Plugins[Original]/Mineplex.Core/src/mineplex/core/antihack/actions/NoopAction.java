package mineplex.core.antihack.actions;

import com.mineplex.anticheat.api.PlayerViolationEvent;

public class NoopAction extends AntiHackAction
{
	public NoopAction()
	{
		super(Integer.MAX_VALUE);
	}

	@Override
	public void handle(PlayerViolationEvent event)
	{

	}
}
