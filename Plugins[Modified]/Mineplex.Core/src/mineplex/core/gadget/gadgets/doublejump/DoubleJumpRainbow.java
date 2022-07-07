package mineplex.core.gadget.gadgets.doublejump;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.achievement.AchievementManager;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.set.SetRainbow;
import mineplex.core.gadget.types.DoubleJumpEffectGadget;
import mineplex.core.gadget.util.CostConstants;

public class DoubleJumpRainbow extends DoubleJumpEffectGadget
{

	private final AchievementManager _achievementManager;
	private final Map<Player, Integer> _rings;

	public DoubleJumpRainbow(GadgetManager manager, AchievementManager achievementManager)
	{
		super(manager, "Rainbow Leap", SetRainbow.GADGET_LORE, CostConstants.NO_LORE, Material.WOOL, (byte) (UtilMath.r(14) + 1));

		_achievementManager = achievementManager;
		_rings = new HashMap<>();
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		super.enableCustom(player, message);

		int size = Math.min(SetRainbow.PER_LEVEL, 1 + _achievementManager.getMineplexLevelNumber(player) / SetRainbow.PER_LEVEL);

		_rings.put(player, size);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		super.disableCustom(player, message);

		_rings.remove(player);
	}

	@Override
	public void doEffect(Player player)
	{
		Location location = player.getLocation().add(0, 0.2, 0);
		int rings = _rings.get(player);

		Manager.runSyncTimer(new BukkitRunnable()
		{

			int iterations = 0;

			@Override
			public void run()
			{
				if (++iterations > 15)
				{
					cancel();
					return;
				}

				double radius = 0.5;

				for (int i = 0; i < rings; i++)
				{
					DustSpellColor colour = SetRainbow.COLOURS[i];

					for (double theta = 0; theta < 2 * Math.PI; theta += Math.PI / (2 * i + 1))
					{
						double x = radius * Math.cos(theta);
						double z = radius * Math.sin(theta);

						location.add(x, 0, z);

						new ColoredParticle(ParticleType.RED_DUST, colour, location)
								.display();

						location.subtract(x, 0, z);
					}

					radius += 0.25;
				}
			}
		}, 1, 1);
	}

}
