package nautilus.game.pvp.worldevent.events;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.pvp.worldevent.EventBase;
import nautilus.game.pvp.worldevent.EventMob;
import nautilus.game.pvp.worldevent.EventManager;
import nautilus.game.pvp.worldevent.creature.*;

public class BossWither extends EventBase
{
	private Location _loc;

	public BossWither(EventManager manager) 
	{
		super(manager, "Charles Witherton", 1); 
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
		if (_loc == null)								
			FindLocation();

		if (_loc != null)							
		{
			AnnounceStart();
			SetState(EventState.LIVE);
		}
	}

	private void FindLocation() 
	{
		_loc = Manager.TerrainFinder().FindArea(UtilWorld.getWorldType(Environment.NORMAL), 8, 1);

		if (_loc != null)
		{
			Location loc = Manager.TerrainFinder().LocateSpace(_loc, 8, 0, 10, 0, false, false, GetBlocks().keySet());

			if (loc == null)
			{
				_loc = null;
				return;
			}
			
			CreatureRegister(new Wither(this, loc.add(0.5, 5, 0.5)));
		}
	}
	
	@Override
	public void AnnounceStart() 
	{
		UtilServer.broadcast(F.main("World Event", F.elem(GetEventName()) + " has entered the world."));	
		System.out.println(GetEventName() + " Start " + UtilWorld.locToStrClean(_loc) + ".");
	}
	
	@Override
	public void AnnounceDuring() 
	{
		for (EventMob cur : GetCreatures())
			if (cur instanceof Wither)
				UtilServer.broadcast(F.main("World Event", F.elem(cur.GetName()) + " is near " + 
						F.elem(UtilWorld.locToStrClean(cur.GetEntity().getLocation())) + "."));
	}
	
	@Override
	public void AnnounceEnd() 
	{
		UtilServer.broadcast(F.main("World Event", F.elem(GetEventName()) + " has been defeated!"));
		System.out.println(GetEventName() + " End " + UtilWorld.locToStrClean(_loc) + ".");
	}

	@Override
	public void AnnounceExpire() 
	{
		UtilServer.broadcast(F.main("World Event", F.elem(GetEventName()) + " has left the world."));		
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
