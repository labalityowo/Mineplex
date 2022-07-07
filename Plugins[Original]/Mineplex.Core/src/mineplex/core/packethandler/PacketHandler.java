package mineplex.core.packethandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import net.minecraft.server.v1_8_R3.Packet;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mineplex.spigot.PacketProcessor;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;

@ReflectivelyCreateMiniPlugin
public class PacketHandler extends MiniPlugin
{
	private Map<Player, PacketVerifier> _playerVerifierMap = new HashMap<>();

	private Map<Class<? extends Packet>, Set<IPacketHandler>> _forceMainThread = new HashMap<>();
	private Map<Class<? extends Packet>, Map<ListenerPriority, List<IPacketHandler>>> _packetHandlers = new HashMap<>();

	private PacketHandler()
	{
		super("PacketHandler");
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		_playerVerifierMap.put(event.getPlayer(), new PacketVerifier(event.getPlayer(), this));

		((CraftPlayer) event.getPlayer()).getHandle().playerConnection.PacketVerifier.setPacketVerifier(_playerVerifierMap
				.get(event.getPlayer()));
	}

	public boolean handlePacket(PacketInfo packetInfo)
	{
		if (!_packetHandlers.containsKey(packetInfo.getPacket().getClass()))
		{
			System.err.print("Received packet " + packetInfo.getPacket().getClass() + " but am not listening for it!");
			return true;
		}

		for (Entry<ListenerPriority, List<IPacketHandler>> entry : _packetHandlers.get(packetInfo.getPacket().getClass()).entrySet())
		{
			for (IPacketHandler handler : entry.getValue())
			{
				try
				{
					handler.handle(packetInfo);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}

		return !packetInfo.isCancelled();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		((CraftPlayer) event.getPlayer()).getHandle().playerConnection.PacketVerifier.setPacketVerifier(null);
		_playerVerifierMap.remove(event.getPlayer());
	}

	public PacketVerifier getPacketVerifier(Player player)
	{
		return _playerVerifierMap.get(player);
	}

	@SafeVarargs
	public final void addPacketHandler(IPacketHandler packetHandler, Class<? extends Packet>... packetsToListen)
	{
		addPacketHandler(packetHandler, ListenerPriority.NORMAL, false, packetsToListen);
	}

	@SafeVarargs
	public final void addPacketHandler(IPacketHandler packetHandler, boolean forceMainThread, Class<? extends Packet>... packetsToListen)
	{
		if (packetsToListen.length == 0)
		{
			throw new IllegalArgumentException("When registering a new packet listener, add the packets its going to listen to");
		}

		addPacketHandler(packetHandler, ListenerPriority.NORMAL, forceMainThread, packetsToListen);
	}

	@SafeVarargs
	public final void addPacketHandler(IPacketHandler packetHandler, ListenerPriority priority, Class<? extends Packet>... packetsToListen)
	{
		if (packetsToListen.length == 0)
		{
			throw new IllegalArgumentException("When registering a new packet listener, add the packets its going to listen to");
		}

		addPacketHandler(packetHandler, priority, false, packetsToListen);
	}

	/**
	 * This should only be used for incoming packets
	 */
	@SafeVarargs
	public final void addPacketHandler(IPacketHandler packetHandler, ListenerPriority priority, boolean forceMainThread,
									   Class<? extends Packet>... packetsToListen)
	{
		if (packetsToListen.length == 0)
		{
			throw new IllegalArgumentException("When registering a new packet listener, add the packets its going to listen to");
		}

		for (Class<? extends Packet> c : packetsToListen)
		{
			if (forceMainThread)
			{
				_forceMainThread
						.computeIfAbsent(c, key -> new HashSet<>())
						.add(packetHandler);
			}

			_packetHandlers
					.computeIfAbsent(c, key -> new TreeMap<>())
					.computeIfAbsent(priority, key -> new ArrayList<>())
					.add(packetHandler);

			PacketProcessor.addPacket(c, forceMainThread || _forceMainThread.containsKey(c));
		}
	}

	public void removePacketHandler(IPacketHandler packetHandler)
	{
		Iterator<Entry<Class<? extends Packet>, Map<ListenerPriority, List<IPacketHandler>>>> itel = _packetHandlers.entrySet().iterator();

		while (itel.hasNext())
		{
			Entry<Class<? extends Packet>, Map<ListenerPriority, List<IPacketHandler>>> entry = itel.next();

			Set<ListenerPriority> removedFrom = new HashSet<>();

			for (Entry<ListenerPriority, List<IPacketHandler>> ent : entry.getValue().entrySet())
			{
				if (ent.getValue().remove(packetHandler))
				{
					removedFrom.add(ent.getKey());
				}
			}

			for (ListenerPriority priority : removedFrom)
			{
				if (entry.getValue().get(priority).isEmpty())
				{
					entry.getValue().remove(priority);
				}
			}

			if (_forceMainThread.containsKey(entry.getKey()) && _forceMainThread.get(entry.getKey()).remove(packetHandler))
			{
				if (_forceMainThread.get(entry.getKey()).isEmpty())
				{
					_forceMainThread.remove(entry.getKey());
					PacketProcessor.addPacket(entry.getKey(), false);
				}
			}

			if (entry.getValue().isEmpty())
			{
				PacketProcessor.removePacket(entry.getKey());
				itel.remove();
			}
		}
	}

	public enum ListenerPriority
	{
		HIGH, NORMAL, LOW
	}
}
