package mineplex.core.gadget.gadgets.arrowtrail;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

import mineplex.core.achievement.AchievementManager;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.set.SetRainbow;
import mineplex.core.gadget.types.ArrowEffectGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.particleeffects.BabyFireworkEffect;

public class ArrowTrailRainbow extends ArrowEffectGadget
{

	private final AchievementManager _achievementManager;
	private final Map<Player, Integer> _rings;

	public ArrowTrailRainbow(GadgetManager manager, AchievementManager achievementManager)
	{
		super(manager, "Rainbow Arrows", SetRainbow.GADGET_LORE, CostConstants.NO_LORE, Material.WOOL, (byte) (UtilMath.r(14) + 1));

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
	public void doTrail(Arrow arrow)
	{
		Player shooter = (Player) arrow.getShooter();
		int rings = _rings.get(shooter);

		for (int i = 0; i < rings; i++)
		{
			DustSpellColor colour = SetRainbow.COLOURS[i];

			new ColoredParticle(ParticleType.RED_DUST, colour, UtilAlg.getRandomLocation(arrow.getLocation(), 0.2))
					.display();
		}
	}

	@Override
	public void doHitEffect(Arrow arrow)
	{
		Player shooter = (Player) arrow.getShooter();
		Location location = arrow.getLocation();
		int rings = _rings.get(shooter);

		for (int i = 0; i < rings; i++)
		{
			DustSpellColor colour = SetRainbow.COLOURS[i];

			new BabyFireworkEffect(UtilAlg.getRandomLocation(location, 1, 0.5, 1), colour.toAwtColor())
					.start();
		}
	}
}
