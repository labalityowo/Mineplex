package mineplex.gemhunters.beta;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

@ReflectivelyCreateMiniPlugin
public class BetaModule extends MiniPlugin
{

	private static final String[] ANNOUCEMENTS = {
		"Please remember this game is an early access BETA and all bugs should be reported at mineplex.com/forums/viewforum/2369449/m/11929946",
		"Thank you for playing Gem Hunters!",
		"Safezones are marked as green areas on your map!",
		"Players that have super valuable items show up on your map!",
		"Tell us what you want added next by voting on our features Trello! https://trello.com/b/ia1kjwcx",
		"The highest value player is shown on the map as a red pointer."
	};
	
	private int _lastIndex;
	
	private BetaModule()
	{
		super("Beta");
	}

	@EventHandler
	public void announce(UpdateEvent event)
	{
		if (event.getType() != UpdateType.MIN_01)
		{
			return;
		}
		
		Bukkit.broadcastMessage(F.main(C.cRedB + "BETA", C.cYellow + ANNOUCEMENTS[_lastIndex]));
	
		if (++_lastIndex == ANNOUCEMENTS.length)
		{
			_lastIndex = 0;
		}
	}
	
}
