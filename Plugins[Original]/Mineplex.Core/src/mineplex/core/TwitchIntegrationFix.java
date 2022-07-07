package mineplex.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayInArmAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;
import net.minecraft.server.v1_8_R3.PacketPlayInEntityAction;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PacketPlayInHeldItemSlot;
import net.minecraft.server.v1_8_R3.PacketPlayInRightClick;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutCloseWindow;
import net.minecraft.server.v1_8_R3.PacketPlayOutOpenWindow;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.packethandler.PacketInfo;

/**
 * Why do we need this you ask?
 * <p>
 * In 1.8.x, Mojang added Twitch integration, and in standard Mojang fashion completely broke inventory handling.
 * <p>
 * Specifically, you are able to close an inventory and not actually trigger an InventoryCloseEvent. This kinda breaks
 * literally anything relying on that event.
 * <p>
 * So we just add lots of strict checks to make sure they can't do much without closing the inventory
 */
@ReflectivelyCreateMiniPlugin
public class TwitchIntegrationFix extends MiniPlugin implements IPacketHandler
{
	private final Map<UUID, Location> _inventoryOpenedAt = new HashMap<>();
	private final Map<UUID, Long> _inventoryOpenedAtTime = new HashMap<>();

	private TwitchIntegrationFix()
	{
		super("Twitch Integration Fix");

		require(PacketHandler.class).addPacketHandler(this, true,
				PacketPlayOutOpenWindow.class,
				PacketPlayOutCloseWindow.class,
				PacketPlayInRightClick.class,
				PacketPlayInBlockPlace.class,
				PacketPlayInArmAnimation.class,
				PacketPlayInBlockDig.class,
				PacketPlayInHeldItemSlot.class,
				PacketPlayInUseEntity.class,
				PacketPlayInFlying.PacketPlayInPosition.class,
				PacketPlayInFlying.PacketPlayInPositionLook.class
		);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		_inventoryOpenedAt.remove(event.getPlayer().getUniqueId());
		_inventoryOpenedAtTime.remove(event.getPlayer().getUniqueId());
	}

	@Override
	public void handle(PacketInfo packetInfo)
	{
		EntityPlayer entityPlayer = ((CraftPlayer) packetInfo.getPlayer()).getHandle();
		if (packetInfo.getPacket() instanceof PacketPlayOutOpenWindow)
		{
			_inventoryOpenedAt.put(packetInfo.getPlayer().getUniqueId(), packetInfo.getPlayer().getLocation());
			_inventoryOpenedAtTime.put(packetInfo.getPlayer().getUniqueId(), entityPlayer.playerConnection.networkManager.packetCount);
		}
		else if (packetInfo.getPacket() instanceof PacketPlayOutCloseWindow)
		{
			_inventoryOpenedAt.remove(packetInfo.getPlayer().getUniqueId());
			_inventoryOpenedAtTime.remove(packetInfo.getPlayer().getUniqueId());
		}
		else if (packetInfo.getPacket() instanceof PacketPlayInRightClick ||
				packetInfo.getPacket() instanceof PacketPlayInBlockPlace ||
				packetInfo.getPacket() instanceof PacketPlayInArmAnimation ||
				packetInfo.getPacket() instanceof PacketPlayInBlockDig ||
				packetInfo.getPacket() instanceof PacketPlayInHeldItemSlot ||
				packetInfo.getPacket() instanceof PacketPlayInUseEntity
				)
		{
			// Impossible to do while inventory is open
			if (entityPlayer.activeContainer != entityPlayer.defaultContainer && _inventoryOpenedAtTime.containsKey(packetInfo.getPlayer().getUniqueId()))
			{
				long openedTime = _inventoryOpenedAtTime.get(packetInfo.getPlayer().getUniqueId());
				if (entityPlayer.playerConnection.networkManager.packetCount - openedTime > 5)
				{
					System.out.println("Impossible packet: " + packetInfo.getPacket().getClass());
					packetInfo.getPlayer().closeInventory();
				}
			}
		}
		else if (packetInfo.getPacket() instanceof PacketPlayInFlying)
		{
			if (entityPlayer.activeContainer != entityPlayer.defaultContainer)
			{
				if (_inventoryOpenedAt.containsKey(packetInfo.getPlayer().getUniqueId()))
				{
					Location openedAt = _inventoryOpenedAt.get(packetInfo.getPlayer().getUniqueId());
					if (!packetInfo.getPlayer().getWorld().equals(openedAt.getWorld()))
					{
						packetInfo.getPlayer().closeInventory();
					}
					else
					{
						double distance = packetInfo.getPlayer().getLocation().distanceSquared(openedAt);
						// You get a 9 block radius before you're considered too far away
						if (distance > 9 * 9)
						{
							packetInfo.getPlayer().closeInventory();
						}
					}
				}
			}
		}
	}
}
