package nautilus.game.arcade.stats;

import nautilus.game.arcade.game.games.common.CaptureTheFlag;
import nautilus.game.arcade.game.games.common.ctf_data.PlayerCaptureFlagEvent;

import org.bukkit.event.EventHandler;

public class CapturesStatTracker extends StatTracker<CaptureTheFlag>
{
	private final String _stat;

	public CapturesStatTracker(CaptureTheFlag game, String stat)
	{
		super(game);
		_stat = stat;
	}
	
	@EventHandler
	public void onCapture(PlayerCaptureFlagEvent event)
	{
		addStat(event.GetPlayer(), getStat(), 1, false, false);
	}
	
	public String getStat()
	{
		return _stat;
	}

}
