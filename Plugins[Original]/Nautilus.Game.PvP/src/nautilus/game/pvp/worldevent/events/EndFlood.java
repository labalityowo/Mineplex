package nautilus.game.pvp.worldevent.events;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;

import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilWorld;
import nautilus.game.pvp.worldevent.EventBase;
import nautilus.game.pvp.worldevent.EventMob;
import nautilus.game.pvp.worldevent.EventManager;
import nautilus.game.pvp.worldevent.creature.*;

public class EndFlood extends EventBase
{
	private ArrayList<ArrayList<Block>> blocks = new ArrayList<ArrayList<Block>>();
	boolean canEnd = false;
	private int lowest = 256;
	private int borders = 600;
	private Location _startLocation;
	
	public EndFlood(EventManager manager, Location location) 
	{
		super(manager, "Flooding Rains", 1); 
		
		_startLocation = location;
		
		while (blocks.size() < 256)
			blocks.add(new ArrayList<Block>());
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
		Flow(_startLocation.getBlock());
		
		AnnounceStart();
		SetState(EventState.LIVE);
	}

	@Override
	public void AnnounceStart() 
	{
		UtilServer.broadcast(F.main("World Event", F.elem(GetEventName()) + " is beginning..."));	
		System.out.println(GetEventName() + " Start.");
		Manager.ServerM().stopWeather = true;
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
		UtilServer.broadcast(F.main("World Event", F.elem(GetEventName()) + " have ended!"));
		System.out.println(GetEventName() + " End.");
		StormOff();
	}

	@Override
	public void AnnounceExpire() 
	{
		UtilServer.broadcast(F.main("World Event", F.elem(GetEventName()) + " have ended."));		
		System.out.println(GetEventName() + " Expire.");
		StormOff();
	}
	
	public void StormOn()
	{
		Manager.ServerM().stopWeather = false;
		UtilWorld.getWorldType(Environment.NORMAL).setStorm(true);
		UtilWorld.getWorldType(Environment.NORMAL).setThundering(true);
		UtilWorld.getWorldType(Environment.NORMAL).setWeatherDuration(60000);
		UtilWorld.getWorldType(Environment.NORMAL).setThunderDuration(60000);
	}
	
	public void StormOff()
	{
		Manager.ServerM().stopWeather = true;
	}

	@EventHandler
	public void EndCheck(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		if (GetState() != EventState.LIVE)
			return;

		if (GetSize() > 0)
			return;

		if (!canEnd)
			return;

		TriggerStop();
		AnnounceEnd();		
	}

	@Override
	public boolean CanExpire() 
	{
		return blocks.isEmpty() && canEnd;
	}	

	@EventHandler
	public void Flood(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (int i=0 ; i < 100 ; i++)
		{			
			Block front = GetBlock();		
			if (front == null)
			{
				System.out.println("Flood Null");
				return;
			}
			
			canEnd = true;

			Flow(front.getRelative(BlockFace.NORTH));
			Flow(front.getRelative(BlockFace.SOUTH));
			Flow(front.getRelative(BlockFace.EAST));
			Flow(front.getRelative(BlockFace.WEST));	
			Flow(front.getRelative(BlockFace.DOWN));	
		}
	}

	@EventHandler
	public void Weather(UpdateEvent event)
	{
		if (event.getType() != UpdateType.MIN_01)
			return;

		StormOn();
	}

	public Block GetBlock()
	{
		while (lowest < 256)
		{			
			if (blocks.get(lowest).isEmpty())
			{
				lowest++;	
				//System.out.println("Blocks: " + GetSize() + "      Lowest: " + lowest);
				continue;
			}
			
			Block block = blocks.get(lowest).remove(UtilMath.r(blocks.get(lowest).size()));
			
			return block;
		}

		return null;
	}

	public boolean Flow(Block block)
	{
		if (Math.abs(block.getX()) > borders || Math.abs(block.getZ()) > borders)
			return false;

		if (block.getTypeId() != 0 && !(block.getTypeId() == 9 && block.getData() != 0))
			return false;

		block.setTypeIdAndData(8, (byte)0, false);

		blocks.get(block.getY()).add(block);

		if (block.getY() < lowest)
			lowest = block.getY();
		
		return true;
	}

	public int GetSize()
	{
		int count = 0;
		for (int i = lowest ; i<256 ; i++)
			count += blocks.get(i).size();
		
		return count;
	}
}
