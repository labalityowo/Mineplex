package mineplex.core.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.DummyEntity;
import mineplex.core.common.MinecraftVersion;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityWither;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.PacketPlayOutBossBar;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;

public class UtilTextTop
{
	// Base Commands
	public static void display(String text, Player... players)
	{
		displayProgress(text, 1, players);
	}

	public static void displayProgress(String text, double progress, Player... players)
	{
		for (Player player : players)
			displayTextBar(player, progress, text);
	}

	// Logic
	public static final int EntityDragonId = 777777;
	public static final int EntityWitherId = 777778;
	public static final UUID BossUUID = UUID.fromString("178f5cde-2fb0-3e73-8296-967ec7e46748");
	private static Map<String, BukkitRunnable> _lastUpdated = new HashMap<>();

	// Display
	public static void displayTextBar(final Player player, double healthPercent, String text)
	{
		if (_lastUpdated.containsKey(player.getName()))
		{
			_lastUpdated.get(player.getName()).cancel();
		}

		healthPercent = Math.max(0, Math.min(1, healthPercent));

		// Remove
		final BukkitRunnable runnable = new BukkitRunnable()
		{
			public void run()
			{
				if (_lastUpdated.containsKey(player.getName()) && _lastUpdated.get(player.getName()) != this)
					return;

				deleteOld(player);

				_lastUpdated.remove(player.getName());
			}
		};

		runnable.runTaskLater(UtilServer.getPlugin(), 20);

		if (UtilPlayer.getVersion(player).atOrAbove(MinecraftVersion.Version1_9))
		{
			sendBossBar(player, healthPercent, text);

			_lastUpdated.put(player.getName(), runnable);
			return;
		}

		_lastUpdated.put(player.getName(), runnable);

		deleteOld(player);

		// Display Dragon
		{
			Location loc = player.getLocation().subtract(0, 200, 0);

			UtilPlayer.sendPacket(player, getDragonPacket(text, healthPercent, loc));
		}

		// Display Wither (as well as Dragon)
		Location loc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(24));

		UtilPlayer.sendPacket(player, getWitherPacket(text, healthPercent, loc));

	}

	private static void sendBossBar(Player player, double health, String text)
	{
		if (_lastUpdated.containsKey(player.getName()))
		{
			PacketPlayOutBossBar bossBar1 = new PacketPlayOutBossBar();

			bossBar1.uuid = BossUUID;
			bossBar1.action = 2;
			bossBar1.health = (float) health;

			PacketPlayOutBossBar bossBar2 = new PacketPlayOutBossBar();

			bossBar2.uuid = BossUUID;
			bossBar2.action = 3;
			bossBar2.title = text;

			UtilPlayer.sendPacket(player, bossBar1, bossBar2);
		}
		else
		{
			PacketPlayOutBossBar bossBar = new PacketPlayOutBossBar();

			bossBar.uuid = BossUUID;
			bossBar.title = text;
			bossBar.health = (float) health;
			bossBar.color = 2;

			UtilPlayer.sendPacket(player, bossBar);
		}
	}

	private static void deleteOld(Player player)
	{
		if (UtilPlayer.getVersion(player).atOrAbove(MinecraftVersion.Version1_9))
		{
			PacketPlayOutBossBar bossBar = new PacketPlayOutBossBar();

			bossBar.uuid = BossUUID;
			bossBar.action = 1;

			UtilPlayer.sendPacket(player, bossBar);
			return;
		}
		// Delete Dragon (All Clients)
		PacketPlayOutEntityDestroy destroyDragonPacket = new PacketPlayOutEntityDestroy(new int[]
			{
					EntityDragonId
			});
		UtilPlayer.sendPacket(player, destroyDragonPacket);

		// Delete Wither (1.8+ Only)
		PacketPlayOutEntityDestroy destroyWitherPacket = new PacketPlayOutEntityDestroy(new int[]
			{
					EntityWitherId
			});
		UtilPlayer.sendPacket(player, destroyWitherPacket);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		if (UtilPlayer.getVersion(event.getPlayer()).atOrAbove(MinecraftVersion.Version1_9))
		{
			deleteOld(event.getPlayer());
		}
	}

	public static PacketPlayOutSpawnEntityLiving getDragonPacket(String text, double healthPercent, Location loc)
	{
		PacketPlayOutSpawnEntityLiving mobPacket = new PacketPlayOutSpawnEntityLiving();

		mobPacket.a = (int) EntityDragonId; // Entity ID
		mobPacket.b = (byte) EntityType.ENDER_DRAGON.getTypeId(); // Mob type
		mobPacket.c = (int) Math.floor(loc.getBlockX() * 32.0D); // X position
		mobPacket.d = (int) MathHelper.floor(loc.getBlockY() * 32.0D); // Y position
		mobPacket.e = (int) Math.floor(loc.getBlockZ() * 32.0D); // Z position
		mobPacket.f = (byte) 0; // Pitch
		mobPacket.g = (byte) 0; // Head Pitch
		mobPacket.h = (byte) 0; // Yaw
		mobPacket.i = (short) 0; // X velocity
		mobPacket.j = (short) 0; // Y velocity
		mobPacket.k = (short) 0; // Z velocity
		mobPacket.uuid = UUID.randomUUID();

		// Health
		double health = healthPercent * 199.9 + 0.1;
		// if (halfHealth)
		// health = healthPercent * 99 + 101;

		// Watcher
		DataWatcher watcher = getWatcher(text, health, loc.getWorld());
		mobPacket.l = watcher;

		return mobPacket;
	}

	public static PacketPlayOutSpawnEntityLiving getWitherPacket(String text, double healthPercent, Location loc)
	{
		PacketPlayOutSpawnEntityLiving mobPacket = new PacketPlayOutSpawnEntityLiving();

		mobPacket.a = (int) EntityWitherId; // Entity ID
		mobPacket.b = (byte) EntityType.WITHER.getTypeId(); // Mob type
		mobPacket.c = (int) Math.floor(loc.getBlockX() * 32.0D); // X position
		mobPacket.d = (int) MathHelper.floor(loc.getBlockY() * 32.0D); // Y position
		mobPacket.e = (int) Math.floor(loc.getBlockZ() * 32.0D); // Z position
		mobPacket.f = (byte) 0; // Pitch
		mobPacket.g = (byte) 0; // Head Pitch
		mobPacket.h = (byte) 0; // Yaw
		mobPacket.i = (short) 0; // X velocity
		mobPacket.j = (short) 0; // Y velocity
		mobPacket.k = (short) 0; // Z velocity
		mobPacket.uuid = UUID.randomUUID();

		// Health
		double health = healthPercent * 299.9 + 0.1;
		// if (halfHealth)
		// health = healthPercent * 149 + 151;

		// Watcher
		DataWatcher watcher = getWatcher(text, health, loc.getWorld());
		mobPacket.l = watcher;

		return mobPacket;
	}

	public static DataWatcher getWatcher(String text, double health, World world)
	{
		DataWatcher watcher = new DataWatcher(new DummyEntity(((CraftWorld) world).getHandle()));

		watcher.a(0, (Byte) (byte) (0 | 1 << 5), Entity.META_ENTITYDATA, (byte) (0 | 1 << 5)); // Flags, 0x20 = invisible
		watcher.a(6, (Float) (float) health, EntityLiving.META_HEALTH, (float) health);
		watcher.a(2, (String) text, Entity.META_CUSTOMNAME, text); // Entity name
		watcher.a(3, (Byte) (byte) 0, Entity.META_CUSTOMNAME_VISIBLE, false); // Show name, 1 = show, 0 = don't show
		// watcher.a(16, (Integer) (int) health, EntityWither.META); //Health
		watcher.a(20, (Integer) (int) 881, EntityWither.META_INVUL_TIME, 881); // Inv

		return watcher;
	}
}