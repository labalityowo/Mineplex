package mineplex.game.clans.gameplay;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockAction;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.UtilServer;
import mineplex.game.clans.clans.ClansManager;

public class HiddenChestManager implements Listener
{

	private final ClansManager _clansManager;

	private final Map<Player, BlockPosition> _openChest;

	private PacketPlayOutBlockAction _lastPacket;

	public HiddenChestManager(ClansManager clansManager)
	{
		_clansManager = clansManager;
		_openChest = new HashMap<>();

		clansManager.getPacketHandler().addPacketHandler(packetInfo ->
		{
			PacketPlayOutBlockAction packet = (PacketPlayOutBlockAction) packetInfo.getPacket();

			if (packet.equals(_lastPacket))
			{
				return;
			}

			// b - Action Id
			// c - Action Param - How many people have the chest open
			// a - Block Position
			if (packet.b == 1 && packet.c > 0 && _openChest.containsValue(packet.a))
			{
				packet.c--;
				_lastPacket = packet;
			}
		}, PacketPlayOutBlockAction.class);
		UtilServer.RegisterEvents(this);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !_clansManager.getIncognitoManager().Get(player).Status)
		{
			return;
		}

		Block block = event.getClickedBlock();
		Material type = block.getType();

		if (type != Material.CHEST && type != Material.TRAPPED_CHEST)
		{
			return;
		}

		Location location = event.getClickedBlock().getLocation();
		_openChest.put(player, new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
	}

	@EventHandler
	public void handleClose(InventoryCloseEvent event)
	{
		_openChest.remove(event.getPlayer());
	}

	@EventHandler
	public void handleClose(PlayerQuitEvent event)
	{
		_openChest.remove(event.getPlayer());
	}
}
