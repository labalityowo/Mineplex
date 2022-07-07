package nautilus.game.arcade.game.games.christmas;

import java.util.HashSet;
import java.util.UUID;

import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTrackerEntry;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.Vector3f;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;

public class SleighHorse
{
	private Location _lastFacing;
	private Vector _lastMovement = new Vector();
	private int[] _previousDir = new int[4];
	public Horse Ent;
	public int[] hornsAndNose = new int[]
			{
					UtilEnt.getNewEntityId(), UtilEnt.getNewEntityId()
			};
	public int horseId;
	public double OffsetX;
	public double OffsetZ;
	public String Name;

	SleighHorse(Location loc, String name, double x, double z)
	{
		Name = name;
		OffsetX = x;
		OffsetZ = z;
		_lastFacing = loc.add(x, 0.5, z);
	}

	private int[] getAngles(float yaw)
	{
		double angle = ((2 * Math.PI) / 360D) * (yaw + 90);
		double dist = 0.7;
		double angleMod = 0.19;
		int pX1 = (int) Math.floor(dist * Math.cos(angle + angleMod) * 32);
		int pZ1 = (int) Math.floor(dist * Math.sin(angle + angleMod) * 32);
		int pX2 = (int) Math.floor(dist * Math.cos(angle - angleMod) * 32);
		int pZ2 = (int) Math.floor(dist * Math.sin(angle - angleMod) * 32);
		return new int[]
				{
						pX1, pZ1, pX2, pZ2
				};
	}

	public boolean HasEntity(LivingEntity ent)
	{
		return Ent.equals(ent);
	}

	public void onTick()
	{
		EntityTrackerEntry entityTrackerEntry = ((CraftWorld) Ent.getWorld()).getHandle().tracker.trackedEntities
				.get(Ent.getEntityId());
		if (entityTrackerEntry != null)
		{
			Location newLocation = Ent.getLocation().add(0, 0.5, 0);
			newLocation.setPitch(_lastFacing.getPitch());

			if (!newLocation.equals(_lastFacing))
			{
				Packet[] packets1_8 = new Packet[2];
				_lastMovement.add(new Vector(newLocation.getX() - _lastFacing.getX(), newLocation.getY() - _lastFacing.getY(),
						newLocation.getZ() - _lastFacing.getZ()));
				final int xP = (int) Math.floor(32 * _lastMovement.getX());
				final int yP = (int) Math.floor(32 * _lastMovement.getY());
				final int zP = (int) Math.floor(32 * _lastMovement.getZ());
				_lastMovement.subtract(new Vector(xP / 32D, yP / 32D, zP / 32D));
				_lastFacing = newLocation;
				int[] angles = getAngles(_lastFacing.getYaw());
				boolean tp = false;
				for (int i = 0; i < 2; i++)
				{
					int pX = angles[i * 2] - _previousDir[i * 2];
					int pZ = angles[(i * 2) + 1] - _previousDir[(i * 2) + 1];
					int x = xP + pX;
					int y = yP;
					int z = zP + pZ;
//                    if (x >= -128 && x <= 127 && y >= -128 && y <= 127 && z >= -128 && z <= 127)
//                    {
//                        PacketPlayOutEntity relMove = pX != 0 || pZ != 0 ? new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook()
//                                : new PacketPlayOutEntity.PacketPlayOutRelEntityMove();
//                        relMove.a = hornsAndNose[i];
//                        relMove.b = (byte) x;
//                        relMove.c = (byte) y;
//                        relMove.d = (byte) z;
//                        relMove.e = ((byte) (int) (newLocation.getYaw() * 256.0F / 360.0F));
//                        packets1_8[i] = relMove;
//                    }
//                    else
					{
						x = (int) Math.floor(32 * newLocation.getX());
						y = (int) Math.floor(32 * newLocation.getY());
						z = (int) Math.floor(32 * newLocation.getZ());
						tp = true;
						PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport();
						teleportPacket.a = hornsAndNose[i];
						teleportPacket.b = x + pX;
						teleportPacket.c = y;
						teleportPacket.d = z + pZ;
						teleportPacket.e = ((byte) (int) (newLocation.getYaw() * 256.0F / 360.0F));
						packets1_8[i] = teleportPacket;
					}
				}
				if (tp)
				{
					int x = (int) Math.floor(32 * newLocation.getX());
					int y = (int) Math.floor(32 * newLocation.getY());
					int z = (int) Math.floor(32 * newLocation.getZ());
					_lastMovement = new Vector(newLocation.getX() - (x / 32D), newLocation.getY() - (y / 32D), newLocation.getZ()
							- (z / 32D));
				}
				_previousDir = angles;
				HashSet trackedPlayers = (HashSet) entityTrackerEntry.trackedPlayers;
				HashSet<EntityPlayer> cloned = (HashSet) trackedPlayers.clone();
				for (EntityPlayer p : cloned)
				{
					for (Packet packet : packets1_8)
					{
						p.playerConnection.sendPacket(packet);
					}
				}
			}
		}
	}

	public void spawnHorns(Player player)
	{
		Location loc = Ent == null ? _lastFacing : Ent.getLocation().add(0, 0.5, 0);
		for (int i = 0; i < 2; i++)
		{
			int id = hornsAndNose[i];
			PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving();
			packet.a = id;
			packet.b = 30;
			packet.c = (int) (loc.getX() * 32) + this._previousDir[i * 2];
			packet.d = (int) (loc.getY() * 32);
			packet.e = (int) (loc.getZ() * 32) + this._previousDir[(i * 2) + 1];
			packet.f = ((byte) (int) (loc.getYaw() * 256.0F / 360.0F));
			packet.uuid = UUID.randomUUID();

			// Setup datawatcher for armor stand
			DataWatcher watcher = new DataWatcher(null);
			watcher.a(0, (byte) 32, Entity.META_ENTITYDATA, (byte) 32);
			watcher.a(10, (byte) 4, EntityArmorStand.META_ARMOR_OPTION, (byte) 4);
			watcher.a(11, new Vector3f(0, i * 180, (i == 0 ? -1 : 1) * 60f), EntityArmorStand.META_HEAD_POSE, new Vector3f(0, i * 180, (i == 0 ? -1 : 1) * 60f));
			packet.l = watcher;
			PacketPlayOutEntityEquipment enquipPacket = new PacketPlayOutEntityEquipment();
			enquipPacket.a = id;
			enquipPacket.b = 4;
			enquipPacket.c = CraftItemStack.asNMSCopy(new ItemStack(Material.DEAD_BUSH));
			UtilPlayer.sendPacket(player, packet);
			UtilPlayer.sendPacket(player, enquipPacket);
		}
	}

	public void spawnHorse()
	{
		horseId = UtilEnt.getNewEntityId(false);
		_previousDir = getAngles(_lastFacing.getYaw());
		Ent = _lastFacing.getWorld().spawn(_lastFacing.subtract(0, 0.5, 0), Horse.class);
		UtilEnt.vegetate(Ent);
		UtilEnt.ghost(Ent, true, false);
		UtilEnt.setTickWhenFarAway(Ent, true);
		Ent.setRemoveWhenFarAway(false);

		Ent.setCustomName(C.cWhiteB + Name);
		Ent.setCustomNameVisible(true);

		Ent.setStyle(Style.BLACK_DOTS);
		Ent.setColor(Color.BROWN);
	}
}
