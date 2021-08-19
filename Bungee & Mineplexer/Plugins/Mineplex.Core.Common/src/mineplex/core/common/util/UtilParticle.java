package mineplex.core.common.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class UtilParticle
{
	public enum ViewDist
	{
		/**
		 * 8 blocks
		 */
		SHORT(8),
		/**
		 * 24 blocks
		 */
		NORMAL(24),
		/**
		 * 48 blocks
		 */
		LONG(48),
		/**
		 * 96 blocks
		 */
		LONGER(96),
		/**
		 * 256 blocks
		 */
		MAX(256);

		private int _dist;

		ViewDist(int dist)
		{
			_dist = dist;
		}

		public int getDist()
		{
			return _dist;
		}
	}

	public enum ParticleType
	{
		ANGRY_VILLAGER(EnumParticle.VILLAGER_ANGRY, "angryVillager", "Lightning Cloud", Material.INK_SACK, (byte) 11),

		BLOCK_CRACK(EnumParticle.BLOCK_CRACK, "blockcrack")
				{
					@Override
					public String getParticle(Material type, int data)
					{
						return "blockcrack_" + type.getId() + "_" + data;
					}
				},

		BLOCK_DUST(EnumParticle.BLOCK_DUST, "blockdust")
				{
					@Override
					public String getParticle(Material type, int data)
					{
						return "blockdust_" + type.getId() + "_" + data;
					}
				},

		BUBBLE(EnumParticle.WATER_BUBBLE, "bubble"),

		CLOUD(EnumParticle.CLOUD, "cloud", "White Smoke", Material.INK_SACK, (byte) 7),

		CRIT(EnumParticle.CRIT, "crit", "Brown Magic", Material.INK_SACK, (byte) 14),

		DEPTH_SUSPEND(EnumParticle.SUSPENDED_DEPTH, "depthSuspend"),

		DRIP_LAVA(EnumParticle.DRIP_LAVA, "dripLava", "Lava Drip", Material.LAVA_BUCKET, (byte) 0),

		DRIP_WATER(EnumParticle.DRIP_WATER, "dripWater", "Water Drop", Material.WATER_BUCKET, (byte) 0),

		DROPLET(EnumParticle.WATER_DROP, "droplet", "Water Splash", Material.INK_SACK, (byte) 4),

		ENCHANTMENT_TABLE(EnumParticle.ENCHANTMENT_TABLE, "enchantmenttable", "Enchantment Words", Material.BOOK, (byte) 0),

		EXPLODE(EnumParticle.EXPLOSION_NORMAL, "explode", "Big White Smoke", Material.INK_SACK, (byte) 15),

		FIREWORKS_SPARK(EnumParticle.FIREWORKS_SPARK, "fireworksSpark", "White Sparkle", Material.GHAST_TEAR, (byte) 0),

		FLAME(EnumParticle.FLAME, "flame", "Flame", Material.BLAZE_POWDER, (byte) 0),

		FOOTSTEP(EnumParticle.FOOTSTEP, "footstep", "Foot Step", Material.LEATHER_BOOTS, (byte) 0),

		HAPPY_VILLAGER(EnumParticle.VILLAGER_HAPPY, "happyVillager", "Emerald Sparkle", Material.EMERALD, (byte) 0),

		HEART(EnumParticle.HEART, "heart", "Love Heart", Material.APPLE, (byte) 0),

		HUGE_EXPLOSION(EnumParticle.EXPLOSION_HUGE, "hugeexplosion", "Huge Explosion", Material.TNT, (byte) 0),

		ICON_CRACK(EnumParticle.ITEM_CRACK, "iconcrack")
				{
					@Override
					public String getParticle(Material type, int data)
					{
						return "iconcrack_" + type.getId() + "_" + data;
					}
				},

		INSTANT_SPELL(EnumParticle.SPELL_INSTANT, "instantSpell"),

		LARGE_EXPLODE(EnumParticle.EXPLOSION_LARGE, "largeexplode", "Explosion", Material.FIREBALL, (byte) 0),

		LARGE_SMOKE(EnumParticle.SMOKE_LARGE, "largesmoke", "Black Smoke", Material.INK_SACK, (byte) 0),

		SMOKE(EnumParticle.SMOKE_NORMAL, "smoke", "Smoke", Material.INK_SACK, (byte) 0),

		LAVA(EnumParticle.LAVA, "lava", "Lava Debris", Material.LAVA, (byte) 0),

		MAGIC_CRIT(EnumParticle.CRIT_MAGIC, "magicCrit", "Teal Magic", Material.INK_SACK, (byte) 6),

		/**
		 * Can be colored if count is 0, color is RGB and depends on the offset of xyz
		 */
		MOB_SPELL(EnumParticle.SPELL_MOB, "mobSpell", "Black Swirls", Material.getMaterial(2263), (byte) 0),

		/**
		 * Can be colored if count is 0, color is RGB and depends on the offset of xyz
		 */
		MOB_SPELL_AMBIENT(EnumParticle.SPELL_MOB_AMBIENT, "mobSpellAmbient", "Transparent Black Swirls", Material
				.getMaterial(2266), (byte) 0),

		/**
		 * To do certain colors, use "no / 24F" for the random X value, 1 for speed. 0 for count.
		 */
		NOTE(EnumParticle.NOTE, "note", "Musical Note", Material.JUKEBOX, (byte) 0),

		PORTAL(EnumParticle.PORTAL, "portal", "Portal Effect", Material.INK_SACK, (byte) 5),

		/**
		 * Can be colored if count is 0, color is RGB and depends on the offset of xyz. Offset y if 0 will default to 1, counter
		 * by making it 0.0001
		 */
		RED_DUST(EnumParticle.REDSTONE, "reddust", "Red Smoke", Material.INK_SACK, (byte) 1),

		SLIME(EnumParticle.SLIME, "slime", "Slime Particles", Material.SLIME_BALL, (byte) 0),

		SNOW_SHOVEL(EnumParticle.SNOW_SHOVEL, "snowshovel", "Snow Puffs", Material.SNOW_BALL, (byte) 0),

		SNOWBALL_POOF(EnumParticle.SNOWBALL, "snowballpoof"),

		SPELL(EnumParticle.SPELL, "spell", "White Swirls", Material.getMaterial(2264), (byte) 0),

		SPLASH(EnumParticle.WATER_SPLASH, "splash"),

		SUSPEND(EnumParticle.SUSPENDED, "suspended"),

		TOWN_AURA(EnumParticle.TOWN_AURA, "townaura", "Black Specks", Material.COAL, (byte) 0),

		WITCH_MAGIC(EnumParticle.SPELL_WITCH, "witchMagic", "Purple Magic", Material.INK_SACK, (byte) 13),

		MOB_APPEARANCE(EnumParticle.MOB_APPEARANCE, "mobappearance"),

		BARRIER(EnumParticle.BARRIER, "barrier"),

		ITEM_TAKE(EnumParticle.ITEM_TAKE, "take"),

		WATER_WAKE(EnumParticle.WATER_WAKE, "wake");

		public EnumParticle particle;
		public String particleName;
		private boolean _friendlyData;
		private String _friendlyName;
		private Material _material;
		private byte _data;

		ParticleType(EnumParticle particle, String particleName)
		{
			this.particleName = particleName;
			_friendlyData = false;
			this.particle = particle;
		}

		ParticleType(EnumParticle particle, String particleName, String friendlyName, Material material, byte data)
		{
			this.particleName = particleName;
			this.particle = particle;
			_friendlyData = true;
			_friendlyName = friendlyName;
			_material = material;
			_data = data;
		}

		public String getParticle(Material type, int data)
		{
			return particleName;
		}

		public boolean hasFriendlyData()
		{
			return _friendlyData;
		}

		public String getFriendlyName()
		{
			if (_friendlyName == null)
			{
				return toString();
			}

			return _friendlyName;
		}

		public Material getMaterial()
		{
			return _material;
		}

		public byte getData()
		{
			return _data;
		}

		public static ParticleType getFromFriendlyName(String name)
		{
			for (ParticleType type : values())
			{
				if (type.hasFriendlyData() && type.getFriendlyName().equals(name))
					return type;
			}
			return null;
		}
	}

	private static PacketPlayOutWorldParticles getPacket(String particleName, Location location, float offsetX, float offsetY,
														 float offsetZ, float speed, int count, boolean displayFar)
	{
		String[] parts = particleName.split("_");
		int[] details = new int[parts.length - 1];

		for (int i = 0; i < details.length; i++)
		{
			details[i] = Integer.parseInt(parts[i + 1]);
		}

		ParticleType particleType = ParticleType.CRIT;

		for (ParticleType type : ParticleType.values())
		{
			if (type.particleName.equalsIgnoreCase(parts[0]))
			{
				particleType = type;
			}
		}

		return new PacketPlayOutWorldParticles(particleType.particle, displayFar,
				(float) location.getX(), (float) location.getY(), (float) location.getZ(), offsetX, offsetY, offsetZ, speed,
				count, details);
	}

	public static void PlayParticle(ParticleType type, Location location, float offsetX, float offsetY,
									float offsetZ, float speed, int count, ViewDist dist)
	{
		PlayParticle(type, location, offsetX, offsetY, offsetZ, speed, count, dist, UtilServer.getPlayers());
	}

	public static void playParticleFor(Player player, ParticleType type, Location location, Vector offset, float speed, int count, ViewDist dist)
	{
		if (player.getGameMode() == GameMode.SPECTATOR)
			return;
		float x = 0;
		float y = 0;
		float z = 0;
		if (offset != null)
		{
			x = (float) offset.getX();
			y = (float) offset.getY();
			z = (float) offset.getZ();
		}
		List<Player> players = new ArrayList<>(UtilServer.getPlayersCollection());
		players.removeIf(other -> !other.canSee(player));
		PlayParticle(type, location, x, y, z, speed, count, dist, players.toArray(new Player[0]));
	}

	public static void playParticleFor(Player player, ParticleType type, Location location, float offsetX, float offsetY, float offsetZ,
									   float speed, int count, ViewDist dist)
	{
		if (player.getGameMode() == GameMode.SPECTATOR)
			return;
		List<Player> players = new ArrayList<>(UtilServer.getPlayersCollection());
		players.removeIf(other -> !other.canSee(player));
		PlayParticle(type.particleName, location, offsetX, offsetY, offsetZ, speed, count, dist, players.toArray(new Player[0]));
	}

	public static void playParticleFor(Player player, String particle, Location location, Vector offset, float speed, int count, ViewDist dist)
	{
		if (player.getGameMode() == GameMode.SPECTATOR)
			return;
		float x = 0;
		float y = 0;
		float z = 0;
		if (offset != null)
		{
			x = (float) offset.getX();
			y = (float) offset.getY();
			z = (float) offset.getZ();
		}
		List<Player> players = new ArrayList<>(UtilServer.getPlayersCollection());
		players.removeIf(other -> !other.canSee(player));
		PlayParticle(particle, location, x, y, z, speed, count, dist, players.toArray(new Player[0]));
	}

	public static void playParticleFor(Player player, String particle, Location location, float offsetX, float offsetY, float offsetZ,
										 float speed, int count, ViewDist dist)
	{
		if (player.getGameMode() == GameMode.SPECTATOR)
			return;
		List<Player> players = new ArrayList<>(UtilServer.getPlayersCollection());
		players.removeIf(other -> !other.canSee(player));
		PlayParticle(particle, location, offsetX, offsetY, offsetZ, speed, count, dist, players.toArray(new Player[0]));
	}

	public static void PlayParticleToAll(ParticleType type, Location location, Vector offset, float speed, int count, ViewDist dist)
	{
		float x = 0;
		float y = 0;
		float z = 0;
		if (offset != null)
		{
			x = (float) offset.getX();
			y = (float) offset.getY();
			z = (float) offset.getZ();
		}
		PlayParticle(type, location, x, y, z, speed, count, dist, UtilServer.getPlayers());
	}


	public static void PlayParticle(ParticleType type, Location location, Vector offset, float speed, int count, ViewDist dist, Player... players)
	{
		float x = 0;
		float y = 0;
		float z = 0;
		if (offset != null)
		{
			x = (float) offset.getX();
			y = (float) offset.getY();
			z = (float) offset.getZ();
		}
		PlayParticle(type, location, x, y, z, speed, count, dist, players);
	}

	public static void PlayParticleToAll(ParticleType type, Location location, float offsetX, float offsetY, float offsetZ,
										 float speed, int count, ViewDist dist)
	{
		PlayParticle(type.particleName, location, offsetX, offsetY, offsetZ, speed, count, dist, UtilServer.getPlayers());
	}

	public static void PlayParticle(ParticleType type, Location location, float offsetX, float offsetY, float offsetZ,
									float speed, int count, ViewDist dist, Player... players)
	{
		PlayParticle(type.particleName, location, offsetX, offsetY, offsetZ, speed, count, dist, players);
	}

	public static void PlayParticle(String particle, Location location, float offsetX, float offsetY, float offsetZ, float speed,
									int count, ViewDist dist, Player... players)
	{
		PacketPlayOutWorldParticles packet = getPacket(particle, location, offsetX, offsetY, offsetZ, speed, count, true);
		int distValue = dist.getDist() * dist.getDist();
		
		for (Player player : players)
		{
			// Out of range for player
			if (UtilMath.offsetSquared(player.getLocation(), location) > distValue)
				continue;

			UtilPlayer.sendPacket(player, packet);
		}
	}

	public static void PlayParticleToAll(String particle, Location location, Vector offset, float speed, int count, ViewDist dist)
	{
		float x = 0;
		float y = 0;
		float z = 0;
		if (offset != null)
		{
			x = (float) offset.getX();
			y = (float) offset.getY();
			z = (float) offset.getZ();
		}
		PlayParticle(particle, location, x, y, z, speed, count, dist, UtilServer.getPlayers());
	}

	public static void PlayParticleToAll(String particle, Location location, float offsetX, float offsetY, float offsetZ,
										 float speed, int count, ViewDist dist)
	{
		PlayParticle(particle, location, offsetX, offsetY, offsetZ, speed, count, dist, UtilServer.getPlayers());
	}

	public static void playColoredParticle(Color color, ParticleType particleType, Location location, int count, ViewDist dist, Player... players)
	{
		if (particleType != ParticleType.RED_DUST
				&& particleType != ParticleType.MOB_SPELL_AMBIENT)
			return;
		PlayParticle(particleType, location, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1f, count, dist, players);
	}

	public static void playColoredParticleToAll(Color color, ParticleType particleType, Location location, int count, ViewDist dist)
	{
		if (particleType != ParticleType.RED_DUST
				&& particleType != ParticleType.MOB_SPELL_AMBIENT)
			return;
		PlayParticleToAll(particleType, location, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1f, count, dist);
	}

	public static void playColoredParticle(java.awt.Color color, ParticleType particleType, Location location, int count, ViewDist dist, Player... players)
	{
		if (particleType != ParticleType.RED_DUST
				&& particleType != ParticleType.MOB_SPELL_AMBIENT)
			return;
		PlayParticle(particleType, location, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1f, count, dist, players);
	}

	public static void playColoredParticleToAll(java.awt.Color color, ParticleType particleType, Location location, int count, ViewDist dist)
	{
		if (particleType != ParticleType.RED_DUST && particleType != ParticleType.MOB_SPELL_AMBIENT)
		{
			return;
		}

		PlayParticleToAll(particleType, location, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1f, count, dist);
	}

}