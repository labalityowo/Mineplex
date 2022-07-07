package nautilus.game.pvp.worldevent.command;

import org.bukkit.entity.Player;

import nautilus.game.pvp.worldevent.EventBase;
import nautilus.game.pvp.worldevent.EventManager;
import nautilus.game.pvp.worldevent.events.BaseUndead;
import nautilus.game.pvp.worldevent.events.BossSkeleton;
import nautilus.game.pvp.worldevent.events.BossSlime;
import nautilus.game.pvp.worldevent.events.BossSpider;
import nautilus.game.pvp.worldevent.events.BossSwarmer;
import nautilus.game.pvp.worldevent.events.BossWither;
import nautilus.game.pvp.worldevent.events.EndFlood;
import mineplex.core.command.CommandBase;
import mineplex.core.common.Rank;
import mineplex.core.common.util.UtilServer;

public class EventCommand extends CommandBase<EventManager>
{
	public EventCommand(EventManager plugin)
	{
		super(plugin, Rank.ADMIN, "ev", "event");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			caller.sendMessage("Missing Event Parameter.");
			return;
		}
		
		EventBase event = null;
		
		if (args[0].equals("dead"))		event = new BaseUndead(Plugin);
		if (args[0].equals("dead4"))	event = new BaseUndead(Plugin, 4);
		if (args[0].equals("slime"))	event = new BossSlime(Plugin);
		if (args[0].equals("skel"))		event = new BossSkeleton(Plugin);	
		if (args[0].equals("swarm"))	event = new BossSwarmer(Plugin);
		if (args[0].equals("wither"))	event = new BossWither(Plugin);
		if (args[0].equals("brood"))	event = new BossSpider(Plugin);
		
		if (args[0].equals("flood"))	event = new EndFlood(Plugin, caller.getLocation());

		if (event != null)
		{
			event.TriggerStart();
			Plugin.addEvent(event);
			
			UtilServer.getServer().getPluginManager().registerEvents(event, Plugin.GetPlugin());
		}
	}
}
