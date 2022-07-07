package mineplex.core.antihack.actions;

import java.util.concurrent.TimeUnit;

import com.mineplex.anticheat.api.PlayerViolationEvent;

import mineplex.core.Managers;
import mineplex.core.antihack.AntiHack;
import mineplex.core.antihack.banwave.BanWaveManager;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;

public class BanwaveAction extends AntiHackAction
{
	private static final int BAN_DELAY_MINIMUM_MINUTES = 5;
	private static final int BAN_DELAY_MAXIMUM_MINUTES = 10;

	public BanwaveAction(int vl)
	{
		super(vl);
	}

	@Override
	public void handle(PlayerViolationEvent event)
	{
		AntiHack antiHack = Managers.get(AntiHack.class);

		if (event.getViolations() >= (Math.floor(getMinVl() * .9)) && event.getPlayer().getMetadata("GWENEXTREMEPREJUDICE").get(0).asBoolean())
		{
			antiHack.doBan(event.getPlayer(), event.getCheckClass(), true, antiHack.getHoursBanned(event.getPlayer()));
			return;
		}
		if (event.getViolations() >= this.getMinVl())
		{
			// Delay bans by slightly hours for fuzzing
			long banDelayMinutes = UtilMath.r(BAN_DELAY_MAXIMUM_MINUTES - BAN_DELAY_MINIMUM_MINUTES) + BAN_DELAY_MINIMUM_MINUTES;
			long banTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(banDelayMinutes, TimeUnit.MINUTES);
			Managers.get(BanWaveManager.class).insertBanWaveInfo(
					event.getPlayer(),
					banTime,
					event.getCheckClass(),
					event.getViolations(),
					UtilServer.getServerName()
			);
		}
	}
}
