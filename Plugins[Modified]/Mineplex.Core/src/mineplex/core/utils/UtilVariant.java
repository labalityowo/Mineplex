package mineplex.core.utils;

import net.minecraft.server.v1_8_R3.EntityGuardian;
import net.minecraft.server.v1_8_R3.EntityHorse;
import net.minecraft.server.v1_8_R3.EntitySkeleton;
import net.minecraft.server.v1_8_R3.EntityZombie;
import net.minecraft.server.v1_8_R3.World;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class UtilVariant
{

	public static Horse spawnHorse(Location location, Horse.Variant variant)
	{
		World world = ((CraftWorld) location.getWorld()).getHandle();

		EntityHorse horse = new EntityHorse(world);
		horse.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		horse.setType(variant.ordinal());
		world.addEntity(horse, CreatureSpawnEvent.SpawnReason.CUSTOM);

		return (Horse) horse.getBukkitEntity();
	}

	public static Zombie spawnZombieVillager(Location location)
	{
		World world = ((CraftWorld) location.getWorld()).getHandle();

		EntityZombie zombie = new EntityZombie(world);
		zombie.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		zombie.setVillager(true);
		world.addEntity(zombie, CreatureSpawnEvent.SpawnReason.CUSTOM);

		return (Zombie) zombie.getBukkitEntity();
	}

	public static Skeleton spawnWitherSkeleton(Location location)
	{
		World world = ((CraftWorld) location.getWorld()).getHandle();

		EntitySkeleton skeleton = new EntitySkeleton(world);
		skeleton.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		skeleton.setSkeletonType(1);
		world.addEntity(skeleton, CreatureSpawnEvent.SpawnReason.CUSTOM);

		return (Skeleton) skeleton.getBukkitEntity();
	}

	public static Guardian spawnElderGuardian(Location location)
	{
		World world = ((CraftWorld) location.getWorld()).getHandle();

		EntityGuardian guardian = new EntityGuardian(world);
		guardian.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		guardian.setElder(true);
		world.addEntity(guardian, CreatureSpawnEvent.SpawnReason.CUSTOM);

		return (Guardian) guardian.getBukkitEntity();

	}
}
