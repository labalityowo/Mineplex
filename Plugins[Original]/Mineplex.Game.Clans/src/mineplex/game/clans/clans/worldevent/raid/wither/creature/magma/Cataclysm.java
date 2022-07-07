package mineplex.game.clans.clans.worldevent.raid.wither.creature.magma;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.worldevent.raid.wither.challenge.six.ChallengeSix;

public abstract class Cataclysm implements Listener
{
	protected ChallengeSix Challenge;
	protected Magmus Magmus;
	
	public Cataclysm(ChallengeSix challenge, Magmus magmus)
	{
		Challenge = challenge;
		Magmus = magmus;
		
		challenge.getRaid().getPlayers().forEach(player ->
		{
			UtilPlayer.message(player, F.main(challenge.getRaid().getName() + " Raid", getAnnouncement()));
			UtilTextMiddle.display("", getAnnouncement(), player);
		});
		onStart();
		UtilServer.RegisterEvents(this);
	}
	
	protected abstract String getAnnouncement();
	
	protected abstract void onStart();
	
	protected abstract void onEnd();
	
	protected abstract void tick();
	
	protected void end()
	{
		onEnd();
		HandlerList.unregisterAll(this);
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
		{
			if (Challenge.isComplete())
			{
				end();
			}
			else
			{
				tick();
			}
		}
	}
}