package mineplex.core;

import java.util.Iterator;
import java.util.Map.Entry;

import mineplex.core.common.util.NautHashMap;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Replay extends MiniPlugin implements IPacketHandler
{
	private NautHashMap<PacketInfo, Long> _packetList = new NautHashMap<PacketInfo, Long>();
	private long _startTime = 0;
	private long _replayTime = 0;
	private boolean _replay = false;
	private long _speed = 20;
	
	public Replay(JavaPlugin plugin, PacketHandler packetHandler)
	{
		super("Replay", plugin);
	}
	
	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (event.getItem().getType() == Material.WEB)
		{
			event.getPlayer().setItemInHand(new ItemStack(Material.STICK, 1));
			_replay = true;
			_replayTime = System.currentTimeMillis();
		}
	}
	
	@EventHandler
	public void replay(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !_replay)
			return;
		
		for (Iterator<Entry<PacketInfo, Long>> entryIterator = _packetList.entrySet().iterator(); entryIterator.hasNext();)
		{
			Entry<PacketInfo, Long> entry = entryIterator.next();
			
			if ((System.currentTimeMillis() + _speed) - _replayTime > entry.getValue())
			{
				entry.getKey().getVerifier().bypassProcess(entry.getKey().getPacket());
				entryIterator.remove();
			}
		}
	}
	
	public void handle(PacketInfo packetInfo)
	{
		if (_replay)
		{
			packetInfo.setCancelled(true);
			return;
		}
		
		if (_startTime == 0)
			_startTime = System.currentTimeMillis();
		
		_packetList.put(packetInfo, System.currentTimeMillis() - _startTime);
		
		// write out packets?
		if (packetInfo.isCancelled())
			return;
	}
}
