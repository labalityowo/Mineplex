package mineplex.core.antihack.actions;

import com.mineplex.anticheat.api.PlayerViolationEvent;

import mineplex.core.Managers;
import mineplex.core.antihack.AntiHack;

public class GEPBanAction extends AntiHackAction
{
	public GEPBanAction(int vl)
	{
		super(vl);
	}
	
	@Override
	public void handle(PlayerViolationEvent event)
	{
		AntiHack _antiHack = Managers.get(AntiHack.class);

		if (event.getViolations() >= this.getMinVl() && event.getPlayer().getMetadata("GWENEXTREMEPREJUDICE").get(0).asBoolean())
		{
			_antiHack.doBan(event.getPlayer(), event.getCheckClass(), true, _antiHack.getHoursBanned(event.getPlayer()));
		}
	}
}