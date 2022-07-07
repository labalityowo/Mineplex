package mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;

import mineplex.core.common.util.UtilEnt;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;

public class BlockHailBlock
{
	private int _block = UtilEnt.getNewEntityId();
	private int _silverfish = UtilEnt.getNewEntityId();
	private Location _location;
	private Material _mat;

	public BlockHailBlock(Location loc, Material mat)
	{
		_location = loc;
		_mat = mat;
	}

	public PacketPlayOutEntityDestroy getDestroyPacket()
	{
		return new PacketPlayOutEntityDestroy(new int[]
			{
					_silverfish, _block
			});
	}

	public Location getLocation()
	{
		return _location;
	}

	public Material getMaterial()
	{
		return _mat;
	}

	@SuppressWarnings("deprecation")
	public Packet<?>[] getSpawnPackets(IronGolem entity)
	{
		Packet<?>[] packets = new Packet[3];

		PacketPlayOutSpawnEntityLiving packet1 = new PacketPlayOutSpawnEntityLiving();

		DataWatcher watcher = new DataWatcher(null);
		watcher.a(0, (byte) 32, Entity.META_ENTITYDATA, (byte) 0);
		watcher.a(1, 0, Entity.META_AIR, 0);

		packet1.a = _silverfish;
		packet1.b = EntityType.SILVERFISH.getTypeId();
		packet1.c = (int) Math.floor(_location.getX() * 32);
		packet1.d = (int) Math.floor(_location.getY() * 32);
		packet1.e = (int) Math.floor(_location.getZ() * 32);
		packet1.l = watcher;

		packets[0] = packet1;

		PacketPlayOutSpawnEntity packet2 = new PacketPlayOutSpawnEntity(((CraftEntity) entity).getHandle(), 70, _mat.getId());

		packet2.a = _block;

		packet2.b = (int) Math.floor(_location.getX() * 32);
		packet2.c = (int) Math.floor(entity.getLocation().getY() * 32);
		packet2.d = (int) Math.floor(_location.getZ() * 32);

		packets[1] = packet2;

		PacketPlayOutAttachEntity packet3 = new PacketPlayOutAttachEntity();

		packet3.b = _block;
		packet3.c = _silverfish;

		packets[2] = packet3;

		return packets;
	}
}