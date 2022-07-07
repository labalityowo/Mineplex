package mineplex.core.common.util;

import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkUtil
{
	public static Firework LaunchRandomFirework(Location location)
	{
		Builder builder = FireworkEffect.builder();
		
		if (RandomUtils.nextInt(3) == 0)
		{
			builder.withTrail();
		}
		else if (RandomUtils.nextInt(2) == 0)
		{
			builder.withFlicker();
		}
		
		builder.with(FireworkEffect.Type.values()[RandomUtils.nextInt(FireworkEffect.Type.values().length)]);
	
		int colorCount = 17;
		
		builder.withColor(Color.fromRGB(RandomUtils.nextInt(255), RandomUtils.nextInt(255), RandomUtils.nextInt(255)));
		
		while (RandomUtils.nextInt(colorCount) != 0)
		{
			builder.withColor(Color.fromRGB(RandomUtils.nextInt(255), RandomUtils.nextInt(255), RandomUtils.nextInt(255)));
			colorCount--;
		}
		
		Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta data = (FireworkMeta) firework.getFireworkMeta();
        data.addEffects(builder.build());
        data.setPower(RandomUtils.nextInt(3));
        firework.setFireworkMeta(data);

		return firework;
	}
}
