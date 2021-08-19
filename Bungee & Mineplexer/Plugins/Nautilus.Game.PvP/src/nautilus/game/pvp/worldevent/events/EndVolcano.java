package nautilus.game.pvp.worldevent.events;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;

import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilWorld;
import nautilus.game.pvp.worldevent.EventBase;
import nautilus.game.pvp.worldevent.EventMob;
import nautilus.game.pvp.worldevent.EventManager;
import nautilus.game.pvp.worldevent.creature.*;

public class EndVolcano extends EventBase
{
	private Location _loc;

	public EndVolcano(EventManager manager) 
	{
		super(manager, "Volcanic Apocalypse", 1); 
	}

	@Override
	public void Start() 
	{

	}

	@Override
	public void Stop() 
	{

	}

	@Override
	public void PrepareCustom() 
	{
		_loc = new Location(UtilWorld.getWorldType(Environment.NORMAL), 0, 0, 0);

		if (_loc != null)							
		{
			AnnounceStart();
			SetState(EventState.LIVE);
		}
	}

	@Override
	public void AnnounceStart() 
	{
		UtilServer.broadcast(F.main("World Event", F.elem(GetEventName()) + " is nearing eruption."));	
	}
	
	@Override
	public void AnnounceDuring() 
	{
		for (EventMob cur : GetCreatures())
			if (cur instanceof SlimeBase)
				UtilServer.broadcast(F.main("World Event", F.elem(GetEventName()) + " is in progress."));
	}
	
	@Override
	public void AnnounceEnd() 
	{
		UtilServer.broadcast(F.main("World Event", F.elem(GetEventName()) + " has ended!"));
		System.out.println(GetEventName() + " End " + UtilWorld.locToStrClean(_loc) + ".");
	}

	@Override
	public void AnnounceExpire() 
	{
		UtilServer.broadcast(F.main("World Event", F.elem(GetEventName()) + " has ended."));		
		System.out.println(GetEventName() + " Expire " + UtilWorld.locToStrClean(_loc) + ".");
	}
	
	@EventHandler
	public void EndCheck(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		if (GetState() != EventState.LIVE)
			return;
		
		if (!GetCreatures().isEmpty())
			return;
		
		TriggerStop();
		AnnounceEnd();		
	}

	@Override
	public boolean CanExpire() 
	{
		for (EventMob cur : GetCreatures())
			if (cur instanceof EventMob)
				if (!((EventMob)cur).CanExpire())
					return false;
		
		return true;
	}	
}
