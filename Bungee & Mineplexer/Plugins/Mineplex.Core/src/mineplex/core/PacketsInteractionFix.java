package mineplex.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.MovingObjectPosition;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity.EnumEntityUseAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.Vec3D;
import net.minecraft.server.v1_8_R3.WorldSettings.EnumGamemode;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class PacketsInteractionFix extends MiniPlugin implements IPacketHandler
{
	private Map<UUID, HashSet<Integer>> _armorStands = new HashMap<>();

	public PacketsInteractionFix(JavaPlugin plugin, PacketHandler packetHandler)
	{
		super("Packets Interaction Fix", plugin);

		packetHandler.addPacketHandler(this, true, PacketPlayOutSpawnEntityLiving.class, PacketPlayOutEntityDestroy.class,
				PacketPlayInUseEntity.class);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		_armorStands.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void removeDeadNames(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		Iterator<UUID> itel = _armorStands.keySet().iterator();

		while (itel.hasNext())
		{
			UUID id = itel.next();

			Player player = Bukkit.getPlayer(id);

			if (player != null)
			{
				continue;
			}

			itel.remove();
		}
	}

	@Override
	public void handle(PacketInfo packetInfo)
	{
		Player player = packetInfo.getPlayer();

		if (!player.isOnline())
		{
			return;
		}

		if (packetInfo.isCancelled())
		{
			return;
		}

		if (!_armorStands.containsKey(player.getUniqueId()))
		{
			_armorStands.put(player.getUniqueId(), new HashSet<Integer>());
		}

		HashSet<Integer> list = _armorStands.get(player.getUniqueId());

		if (packetInfo.getPacket() instanceof PacketPlayOutSpawnEntityLiving)
		{
			PacketPlayOutSpawnEntityLiving packet = (PacketPlayOutSpawnEntityLiving) packetInfo.getPacket();

			if (packet.b != EntityType.ARMOR_STAND.getTypeId())
			{
				return;
			}

			list.add(packet.a);
		}
		else if (packetInfo.getPacket() instanceof PacketPlayOutEntityDestroy)
		{
			PacketPlayOutEntityDestroy packet = (PacketPlayOutEntityDestroy) packetInfo.getPacket();

			list.removeAll(Arrays.asList(packet.a));
		}
		else if (packetInfo.getPacket() instanceof PacketPlayInUseEntity)
		{
			PacketPlayInUseEntity packet = (PacketPlayInUseEntity) packetInfo.getPacket();

			if (packet.action == EnumEntityUseAction.ATTACK)
			{
				EntityPlayer nmsPlayer = ((CraftPlayer) packetInfo.getPlayer()).getHandle();

				float f1 = nmsPlayer.pitch;
				float f2 = nmsPlayer.yaw;
				double d0 = nmsPlayer.locX;
				double d1 = nmsPlayer.locY + nmsPlayer.getHeadHeight();
				double d2 = nmsPlayer.locZ;
				Vec3D vec3d = new Vec3D(d0, d1, d2);

				float f3 = MathHelper.cos(-f2 * 0.01745329F - 3.141593F);
				float f4 = MathHelper.sin(-f2 * 0.01745329F - 3.141593F);
				float f5 = -MathHelper.cos(-f1 * 0.01745329F);
				float f6 = MathHelper.sin(-f1 * 0.01745329F);
				float f7 = f4 * f5;
				float f8 = f3 * f5;
				nmsPlayer.playerInteractManager.getGameMode();
				double d3 = nmsPlayer.playerInteractManager.getGameMode() == EnumGamemode.CREATIVE ? 5 : 4.5;
				Vec3D vec3d1 = vec3d.add(f7 * d3, f6 * d3, f8 * d3);
				MovingObjectPosition movingobjectposition = nmsPlayer.world.rayTrace(vec3d, vec3d1, false);

				if (movingobjectposition != null && movingobjectposition.type == MovingObjectPosition.EnumMovingObjectType.BLOCK)
				{
					CraftEventFactory.callPlayerInteractEvent(nmsPlayer, Action.LEFT_CLICK_AIR,
							nmsPlayer.inventory.getItemInHand());
				}

				return;
			}

			if (!list.contains(packet.a))
			{
				return;
			}

			CraftEventFactory.callPlayerInteractEvent(((CraftPlayer) player).getHandle(), Action.RIGHT_CLICK_AIR,
					((CraftPlayer) player).getHandle().inventory.getItemInHand());
		}
	}
}
