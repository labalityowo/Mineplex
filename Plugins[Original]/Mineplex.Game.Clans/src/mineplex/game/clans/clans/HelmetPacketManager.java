package mineplex.game.clans.clans;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.packethandler.IPacketHandler;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;

/**
 * Handler for custom helmet display packets
 */
public class HelmetPacketManager implements Listener
{
	private static final net.minecraft.server.v1_8_R3.ItemStack MELON = CraftItemStack.asNMSCopy(new ItemStack(Material.MELON_BLOCK));
	private static final HelmetPacketManager Instance = new HelmetPacketManager();
	
	private final Map<Player, IPacketHandler> _handlers = new HashMap<>();
	
	private HelmetPacketManager()
	{
		UtilServer.RegisterEvents(this);
	}
	
	/**
	 * Fetches the registered loaded instance of this class
	 * @return The loaded instance of this class
	 */
	public static HelmetPacketManager getInstance()
	{
		return Instance;
	}
	
	/**
	 * Sends a player helmet update to all other players
	 * @param player The player to update for
	 * @param banner The helmet to display, or null to show the player's actual helmet
	 */
	public void refreshToAll(Player player, ItemStack item)
	{
		ItemStack show = item;
		
		if (show == null)
		{
			show = new ItemStack(Material.AIR);
			if (player.getInventory().getHelmet() != null)
			{
				show = player.getInventory().getHelmet();
			}
		}
		
		for (Player refresh : Bukkit.getOnlinePlayers())
		{
			UtilPlayer.sendPacket(refresh, new PacketPlayOutEntityEquipment(player.getEntityId(), 4, CraftItemStack.asNMSCopy(item)));
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		IPacketHandler helmetHandler = (packetInfo) ->
		{
			if (packetInfo.getPacket() instanceof PacketPlayOutEntityEquipment)
			{
				PacketPlayOutEntityEquipment equip = (PacketPlayOutEntityEquipment) packetInfo.getPacket();
				
				if (equip.a == player.getEntityId() && equip.b == 4)
				{
					ItemStack banner = UtilEnt.GetMetadata(player, "HelmetPacket.Banner");
					boolean melon = UtilEnt.hasFlag(player.getVehicle(), "HelmetPacket.RiderMelon");
					
					if (banner != null)
					{
						equip.c = CraftItemStack.asNMSCopy(banner);
						return;
					}
					if (melon)
					{
						equip.c = MELON;
						return;
					}
				}
			}
		};
		
		_handlers.put(event.getPlayer(), helmetHandler);
		ClansManager.getInstance().getPacketHandler().addPacketHandler(helmetHandler, PacketPlayOutEntityEquipment.class);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		IPacketHandler handler = _handlers.remove(event.getPlayer());
		if (handler != null)
		{
			ClansManager.getInstance().getPacketHandler().removePacketHandler(handler);
		}
	}
}