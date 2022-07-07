package mineplex.core.gadget.gadgets.death;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.achievement.AchievementManager;
import mineplex.core.blood.BloodEvent;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.set.SetRainbow;
import mineplex.core.gadget.types.DeathEffectGadget;
import mineplex.core.gadget.util.CostConstants;

public class DeathRainbow extends DeathEffectGadget
{

	private static final double RADIUS = 1.3;

	private final AchievementManager _achievementManager;
	private final Map<Player, Integer> _rings;

	public DeathRainbow(GadgetManager manager, AchievementManager achievementManager)
	{
		super(manager, "Rainbow Death", SetRainbow.GADGET_LORE, CostConstants.NO_LORE, Material.WOOL, (byte) (UtilMath.r(14) + 1));

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
	public void onBlood(Player player, BloodEvent event)
	{
		event.setItem(Material.WOOL, (byte) (UtilMath.r(14) + 1));

		Location location = player.getLocation().add(0, 0.2, 0);
		int rings = _rings.get(player);

		Manager.runSyncTimer(new BukkitRunnable()
		{

			int iterations = 0;

			@Override
			public void run()
			{
				if (++iterations > 9)
				{
					cancel();
					return;
				}

				Location pLocation = location.clone();

				for (int i = 0; i < rings; i++)
				{
					DustSpellColor colour = SetRainbow.COLOURS[i];

					for (double theta = 0; theta < 2 * Math.PI; theta += Math.PI / 10)
					{
						double x = RADIUS * Math.cos(theta);
						double z = RADIUS * Math.sin(theta);

						pLocation.add(x, 0, z);

						new ColoredParticle(ParticleType.RED_DUST, colour, pLocation)
								.display();

						pLocation.subtract(x, 0, z);
					}

					pLocation.add(0, 0.2, 0);
				}
			}
		}, 1, 1);
	}
}
