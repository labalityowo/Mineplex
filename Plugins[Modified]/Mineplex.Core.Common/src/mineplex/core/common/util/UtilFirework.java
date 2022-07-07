package mineplex.core.common.util;

import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFirework;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

public class UtilFirework 
{
	public static void playFirework(Location loc, FireworkEffect fe) 
	{
		Firework firework = loc.getWorld().spawn(loc, Firework.class);
		
		FireworkMeta data = firework.getFireworkMeta();
		data.clearEffects();
		data.setPower(1);
		data.addEffect(fe);
		firework.setFireworkMeta(data);

		((CraftFirework) firework).getHandle().expectedLifespan = 1;
//		((CraftWorld)loc.getWorld()).getHandle().broadcastEntityEffect(((CraftEntity)firework).getHandle(), (byte)17);
//		firework.remove();
	}

	public static Firework launchFirework(Location loc, FireworkEffect fe, Vector dir, int power) 
	{
		try
		{
			Firework fw = loc.getWorld().spawn(loc, Firework.class);

			FireworkMeta data = fw.getFireworkMeta();
			data.clearEffects();
			data.setPower(power);
			data.addEffect(fe);
			fw.setFireworkMeta(data);
			
			if (dir != null)
				fw.setVelocity(dir);
			
			return fw;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void detonateFirework(Firework firework)
	{
		((CraftWorld)firework.getWorld()).getHandle().broadcastEntityEffect(((CraftEntity)firework).getHandle(), (byte)17);
		firework.remove();
	}
	
	public static Firework launchFirework(Location loc, Type type, Color color, boolean flicker, boolean trail, Vector dir, int power) 
	{
		return launchFirework(loc, FireworkEffect.builder().flicker(flicker).withColor(color).with(type).trail(trail).build(), dir, power);
	}

	public static void playFirework(Location loc, Type type, Color color, boolean flicker, boolean trail)
	{
		playFirework(loc, FireworkEffect.builder().flicker(flicker).withColor(color).with(type).trail(trail).build());
	}	
	
	public static void packetPlayFirework(Player player, Location loc, Type type, Color color, boolean flicker, boolean trail)
	{
		Firework firework = loc.getWorld().spawn(loc, Firework.class);
		FireworkEffect effect = FireworkEffect.builder().flicker(flicker).withColor(color).with(type).trail(trail).build();
		
		FireworkMeta data = firework.getFireworkMeta();
		data.clearEffects();
		data.setPower(1);
		data.addEffect(effect);
		firework.setFireworkMeta(data);

		((CraftFirework) firework).getHandle().expectedLifespan = 1;

		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[]
			{
				firework.getEntityId()
			});

		for (Player viewing : UtilServer.getPlayers())
		{
			if (player == viewing)
				continue;
			
			UtilPlayer.sendPacket(viewing, packet);
		}
	}

	public static void spawnRandomFirework(Location location)
	{
		playFirework(location,
				Type.values()[UtilMath.r(Type.values().length)],
				Color.fromRGB(UtilMath.r(256), UtilMath.r(256), UtilMath.r(256)),
				UtilMath.random.nextBoolean(),
				UtilMath.random.nextBoolean()
				);
	}

	public static void playFreedomFirework(Location location)
	{
		playFirework(location, FireworkEffect.builder().withColor(Color.RED).withColor(Color.BLUE)
				.withColor(Color.WHITE).withFade(Color.RED).withFade(Color.BLUE).withFade(Color.WHITE).build());
	}

	public static FireworkEffect getRandomFireworkEffect(boolean fade, int maxColors, int maxFade)
	{
		FireworkEffect.Builder builder = FireworkEffect.builder().with(Type.values()[UtilMath.r(Type.values().length)])
				.withColor(Color.fromRGB(UtilMath.r(256), UtilMath.r(256), UtilMath.r(256))).flicker(UtilMath.random.nextBoolean()).trail(UtilMath.random.nextBoolean());
		if (fade)
		{
			for (int i = 0; i < maxFade; i++)
			{
				builder.withFade(Color.fromRGB(UtilMath.r(256), UtilMath.r(256), UtilMath.r(256)));
			}
		}
		for (int i = 0; i < maxColors; i++)
		{
			builder.withColor(Color.fromRGB(UtilMath.r(256), UtilMath.r(256), UtilMath.r(256)));
		}
		return builder.build();
	}
}
