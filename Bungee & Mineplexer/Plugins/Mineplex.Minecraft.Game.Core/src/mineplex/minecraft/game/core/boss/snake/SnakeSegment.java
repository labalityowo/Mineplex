package mineplex.minecraft.game.core.boss.snake;

import java.util.UUID;

import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.Vector3f;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;

public class SnakeSegment
{
	private int _entityId = UtilEnt.getNewEntityId();
	private Vector _entityLocation;
	private Vector _entityLastLocation;
	private ItemStack _item;
	private Vector _prevDir = new Vector();

	public SnakeSegment(Vector location, ItemStack item)
	{
		_entityLocation = location.clone();
		_item = item;
	}

	public int getId()
	{
		return _entityId;
	}

	public Packet[] moveEntity(Vector newLocation)
	{
		_entityLastLocation = _entityLocation.clone();
		
		Vector toMove = newLocation.clone().subtract(_entityLocation);
		Packet packet1;

		int x = (int) Math.floor(32 * toMove.getX());
		int y = (int) Math.floor(32 * toMove.getY());
		int z = (int) Math.floor(32 * toMove.getZ());

		if (x >= -128 && x <= 127 && y >= -128 && y <= 127 && z >= -128 && z <= 127)
		{
			_entityLocation.add(new Vector(x / 32D, y / 32D, z / 32D));

			PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook relMove = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook();

			relMove.a = getId();
			relMove.b = (byte) x;
			relMove.c = (byte) y;
			relMove.d = (byte) z;
			// relMove.e = (byte) (int) (UtilAlg.GetYaw(toMove) * 256.0F / 360.0F);
			// relMove.f = (byte) (int) (UtilAlg.GetPitch(toMove) * 256.0F / 360.0F);

			packet1 = relMove;
		}
		else
		{
			_entityLocation = newLocation.clone();

			x = (int) Math.floor(_entityLocation.getX() * 32);
			y = (int) Math.floor(_entityLocation.getY() * 32);
			z = (int) Math.floor(_entityLocation.getZ() * 32);

			PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport();
			teleportPacket.a = getId();
			teleportPacket.b = x;
			teleportPacket.c = y;
			teleportPacket.d = z;
			// teleportPacket.e = (byte) (int) (UtilAlg.GetYaw(toMove) * 256.0F / 360.0F);
			// teleportPacket.f = (byte) (int) (UtilAlg.GetPitch(toMove) * 256.0F / 360.0F);

			packet1 = teleportPacket;
		}

		Vector vec = new Vector(UtilAlg.GetPitch(toMove), UtilAlg.GetYaw(toMove), 0);

		if (vec.equals(_prevDir))
		{
			return new Packet[]
				{
						packet1
				};
		}

		_prevDir = vec;

		DataWatcher watcher = new DataWatcher(null);
		watcher.a(0, (byte) 32, net.minecraft.server.v1_8_R3.Entity.META_ENTITYDATA, (byte) 0);
		watcher.a(1, 0, net.minecraft.server.v1_8_R3.Entity.META_AIR, 0);
		watcher.a(10, (byte) 0, EntityArmorStand.META_ARMOR_OPTION, (byte) 0);
		watcher.a(11, new Vector3f(0, 0, 0), EntityArmorStand.META_HEAD_POSE,
				new Vector3f((float) vec.getX(), (float) vec.getY(), (float) vec.getZ()));
		watcher.a(12, new Vector3f(0, 0, 0), EntityArmorStand.META_BODY_POSE, new Vector3f(0, 0, 0));
		watcher.a(13, new Vector3f(0, 0, 0), EntityArmorStand.META_LEFT_ARM_POSE, new Vector3f(0, 0, 0));
		watcher.a(14, new Vector3f(0, 0, 0), EntityArmorStand.META_RIGHT_ARM_POSE, new Vector3f(0, 0, 0));
		watcher.a(15, new Vector3f(0, 0, 0), EntityArmorStand.META_LEFT_LEG_POSE, new Vector3f(0, 0, 0));
		watcher.a(16, new Vector3f(0, 0, 0), EntityArmorStand.META_RIGHT_LEG_POSE, new Vector3f(0, 0, 0));

		PacketPlayOutEntityMetadata meta = new PacketPlayOutEntityMetadata(getId(), watcher, true);

		return new Packet[]
			{
					packet1, meta
			};
	}

	public Vector getLocation()
	{
		return _entityLocation.clone();
	}
	
	public Vector getLastLocation()
	{
		return _entityLastLocation.clone();
	}

	public Packet[] getSpawn()
	{
		PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving();

		DataWatcher watcher = new DataWatcher(null);
		watcher.a(1, 0, net.minecraft.server.v1_8_R3.Entity.META_AIR, 0);

		packet.a = getId();
		packet.c = (int) Math.floor(_entityLocation.getX() * 32);
		packet.d = (int) Math.floor(_entityLocation.getY() * 32);
		packet.e = (int) Math.floor(_entityLocation.getZ() * 32);
		packet.l = watcher;
		packet.uuid = UUID.randomUUID();

		if (_item != null)
		{
			watcher.a(0, (byte) 32, net.minecraft.server.v1_8_R3.Entity.META_ENTITYDATA, (byte) 32);
			watcher.a(10, (byte) 0, EntityArmorStand.META_ARMOR_OPTION, (byte) 0);
			watcher.a(11, new Vector3f(0, 0, 0), EntityArmorStand.META_HEAD_POSE, new Vector3f(0, 0, 0));
			watcher.a(12, new Vector3f(0, 0, 0), EntityArmorStand.META_BODY_POSE, new Vector3f(0, 0, 0));
			watcher.a(13, new Vector3f(0, 0, 0), EntityArmorStand.META_LEFT_ARM_POSE, new Vector3f(0, 0, 0));
			watcher.a(14, new Vector3f(0, 0, 0), EntityArmorStand.META_RIGHT_ARM_POSE, new Vector3f(0, 0, 0));
			watcher.a(15, new Vector3f(0, 0, 0), EntityArmorStand.META_LEFT_LEG_POSE, new Vector3f(0, 0, 0));
			watcher.a(16, new Vector3f(0, 0, 0), EntityArmorStand.META_RIGHT_LEG_POSE, new Vector3f(0, 0, 0));

			packet.b = 30;

			PacketPlayOutEntityEquipment packet2 = new PacketPlayOutEntityEquipment();

			packet2.a = getId();
			packet2.b = 4;
			packet2.c = CraftItemStack.asNMSCopy(_item);

			return new Packet[]
				{
						packet, packet2
				};
		}
		else
		{
			watcher.a(0, (byte) 0, net.minecraft.server.v1_8_R3.Entity.META_ENTITYDATA, (byte) 0);
			packet.b = EntityType.MAGMA_CUBE.getTypeId();

			return new Packet[]
				{
						packet
				};
		}
	}
}
