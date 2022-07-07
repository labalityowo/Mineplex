package mineplex.core.imagemap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.EntityItemFrame;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;

import org.bukkit.craftbukkit.libs.com.google.common.base.Optional;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftItemFrame;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.packethandler.PacketInfo;

@ReflectivelyCreateMiniPlugin
public class CustomItemFrames extends MiniPlugin implements IPacketHandler
{

	private final List<Packet> _ourPackets;
	private final Map<Integer, Map<UUID, ItemStack>> _frameData;

	private CustomItemFrames()
	{
		super("CustomItemFrames");

		_ourPackets = new ArrayList<>();
		_frameData = new HashMap<>();
	}

	@Override
	public void enable()
	{
		require(PacketHandler.class).addPacketHandler(this, PacketPlayOutEntityMetadata.class, PacketPlayInUseEntity.class);
	}

	@Override
	public void disable()
	{
		require(PacketHandler.class).removePacketHandler(this);
	}

	@Override
	public void handle(PacketInfo packetInfo)
	{
		if (packetInfo.getPacket() instanceof PacketPlayOutEntityMetadata)
		{
			if (_ourPackets.remove(packetInfo.getPacket()))
			{
				return;
			}

			PacketPlayOutEntityMetadata packet = (PacketPlayOutEntityMetadata) packetInfo.getPacket();
			Map<UUID, ItemStack> map = _frameData.get(packet.a);

			if (map != null)
			{
				UUID uuid = packetInfo.getPlayer().getUniqueId();
				ItemStack item = map.get(uuid);

				if (item != null)
				{
					for (DataWatcher.WatchableObject meta : packet.b)
					{
						if (meta.getIndex().a() == 8)
						{
							meta.a(item, Optional.fromNullable(item));
							break;
						}
					}
				}
			}
		}
		else if (packetInfo.getPacket() instanceof PacketPlayInUseEntity)
		{
			PacketPlayInUseEntity packet = (PacketPlayInUseEntity) packetInfo.getPacket();

			if (_frameData.containsKey(packet.a))
			{
				packetInfo.setCancelled(true);
			}
		}
	}

	public void setItem(Player player, ItemFrame frame, org.bukkit.inventory.ItemStack item)
	{
		ItemStack nmsItem = CraftItemStack.asNMSCopy(item.clone());

		DataWatcher.WatchableObject<Optional<ItemStack>> frameMetaItem = new DataWatcher.WatchableObject<>(5, 8, nmsItem, EntityItemFrame.META_ITEM, Optional.fromNullable(nmsItem));

		PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata();
		packet.a = frame.getEntityId();
		packet.b = Collections.singletonList(frameMetaItem);

		_ourPackets.add(packet);

		UtilPlayer.sendPacket(player, packet);

		_frameData.computeIfAbsent(frame.getEntityId(), HashMap::new).put(player.getUniqueId(), nmsItem);
	}

	public void removeItems(ItemFrame frame)
	{
		_frameData.remove(frame.getEntityId());

		ItemStack nmsItem = null;
		EntityItemFrame nmsEntity = ((CraftItemFrame) frame).getHandle();

		DataWatcher watcher = new DataWatcher(nmsEntity);
		watcher.add(8, 5, EntityItemFrame.META_ITEM, Optional.fromNullable(nmsItem));

		PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(frame.getEntityId(), watcher, true);

		MinecraftServer.getServer().getPlayerList().players.forEach(player -> player.playerConnection.sendPacket(packet));
	}
}