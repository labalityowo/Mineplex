package mineplex.core.gadget.gadgets.outfit.reindeer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import mineplex.core.common.MinecraftVersion;
import mineplex.core.common.Pair;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class OutfitReindeerAntlers extends OutfitReindeer implements IPacketHandler
{

	private static final ItemStack HELMET = new ItemStack(Material.DEAD_BUSH);
	private static final EulerAngle LEFT_POS = new EulerAngle(0, 0, -Math.PI / 4D);
	private static final EulerAngle RIGHT_POS = new EulerAngle(0, 0, -LEFT_POS.getZ());

	private final Map<Player, Pair<ArmorStand, ArmorStand>> _antlers;

	public OutfitReindeerAntlers(GadgetManager manager)
	{
		super(manager, "Antlers", ArmorSlot.HELMET, Material.DEAD_BUSH, (byte) 0);

		_antlers = new HashMap<>();

		manager.getPacketManager().addPacketHandler(this, PacketPlayOutEntityTeleport.class);
	}

	@Override
	public void applyArmor(Player player, boolean message)
	{
		super.applyArmor(player, message);

		player.getInventory().setHelmet(null);

		ArmorStand left = spawnStand(player);
		ArmorStand right = spawnStand(player);

		left.setHeadPose(LEFT_POS);
		right.setHeadPose(RIGHT_POS);

		_antlers.put(player, Pair.create(left, right));
		updateRotation(player, left, right);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		super.disableCustom(player, message);

		Pair<ArmorStand, ArmorStand> pair = _antlers.remove(player);

		if (pair != null)
		{
			pair.getLeft().remove();
			pair.getRight().remove();
		}
	}

	private ArmorStand spawnStand(Player player)
	{
		ArmorStand stand = player.getWorld().spawn(player.getLocation(), ArmorStand.class);

		stand.setGravity(false);
		stand.setVisible(false);
		stand.getEquipment().setHelmet(HELMET);

		return stand;
	}

	private void updateRotation(Player player, ArmorStand left, ArmorStand right)
	{
		Location location = player.getLocation().add(0, player.isSneaking() ? -0.3 : 0.2, 0);
		location.setPitch(0);
		location.add(location.getDirection().multiply(0.2));

		left.teleport(location);
		right.teleport(location);
	}

	@EventHandler
	public void updateRotation(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_antlers.forEach((player, pair) -> updateRotation(player, pair.getLeft(), pair.getRight()));
	}

	@Override
	public void handle(PacketInfo packetInfo)
	{
		Player player = packetInfo.getPlayer();

		if (UtilPlayer.getVersion(player).atOrAbove(MinecraftVersion.Version1_9))
		{
			return;
		}

		PacketPlayOutEntityTeleport packet = (PacketPlayOutEntityTeleport) packetInfo.getPacket();
		int id = packet.a;

		for (Entry<Player, Pair<ArmorStand, ArmorStand>> entry : _antlers.entrySet())
		{
			Player other = entry.getKey();
			Pair<ArmorStand, ArmorStand> pair = entry.getValue();
			ArmorStand left = pair.getLeft();
			ArmorStand right = pair.getRight();
			boolean isLeft = left.getEntityId() == id;

			if (isLeft || right.getEntityId() == id)
			{
				Location location = isLeft ? left.getLocation() : right.getLocation();
				Location otherLocation = other.getLocation();
				otherLocation.setPitch(0);
				Vector direction = otherLocation.getDirection();
				Vector offset;

				if (isLeft)
				{
					offset = UtilAlg.getRight(direction);
				}
				else
				{
					offset = UtilAlg.getLeft(direction);
				}

				location.add(offset.multiply(0.4));

				packet.b = MathHelper.floor(location.getX() * 32);
				packet.d = MathHelper.floor(location.getZ() * 32);
				return;
			}
		}
	}
}
