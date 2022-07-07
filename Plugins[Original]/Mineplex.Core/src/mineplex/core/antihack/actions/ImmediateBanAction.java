package mineplex.core.antihack.actions;

import com.mineplex.anticheat.api.PlayerViolationEvent;

import mineplex.core.Managers;
import mineplex.core.antihack.AntiHack;

public class ImmediateBanAction extends AntiHackAction
{
	public ImmediateBanAction(int vl)
	{
		super(vl);
	}

	@Override
	public void handle(PlayerViolationEvent event)
	{
		AntiHack _antiHack = Managers.get(AntiHack.class);

		if (event.getViolations() >= (Math.floor(getMinVl() * .9)) && event.getPlayer().getMetadata("GWENEXTREMEPREJUDICE").get(0).asBoolean())
		{
			_antiHack.doBan(event.getPlayer(), event.getCheckClass(), true, _antiHack.getHoursBanned(event.getPlayer()));
			return;
		}
		if (event.getViolations() >= this.getMinVl())
		{
			_antiHack.doBan(event.getPlayer(), event.getCheckClass(), false, _antiHack.getHoursBanned(event.getPlayer()));
		}
	}
}
