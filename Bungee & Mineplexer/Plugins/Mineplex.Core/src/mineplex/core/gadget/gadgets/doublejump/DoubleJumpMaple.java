package mineplex.core.gadget.gadgets.doublejump;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.banner.CountryFlag;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DoubleJumpEffectGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DoubleJumpMaple extends DoubleJumpEffectGadget
{
	/**
	 * Amount of particles played per tick as a player flies through the air.
	 */
	private static final int PARTICLES = 50;

	/**
	 * Vertical offset of the animation's center from the player's location.
	 */
	private static final float Y_OFFSET = 0.95f;

	/**
	 * Divisor of the gaussian distribution of particles as the player flies through the air.
	 */
	private static final int DISTRIBUTION = 2;

	/**
	 * Particle ring count when a player launches from the ground.
	 */
	private static final int LAUNCH_RINGS = 6;

	/**
	 * The distance between launch rings.
	 */
	private static final float RING_SPACING = 0.4f;

	/**
	 * Particles played per 1 unit radius.
	 */
	private static final int RING_DENSITY = 8;

	private HashMap<Player, Long> _playerMap = new HashMap<>();

	public DoubleJumpMaple(GadgetManager manager)
	{
		super(manager, "Maple Leap",
				UtilText.splitLineToArray(C.cGray + "Jump higher than the maple trees!", LineFormat.LORE),
				-8, Material.WOOL, (byte)0);
		setDisplayItem(CountryFlag.CANADA.getBanner());
	}

	@Override
	public void doEffect(Player player)
	{
		_playerMap.put(player, System.currentTimeMillis() + 1000);

		float limit = (LAUNCH_RINGS * RING_SPACING) + RING_SPACING;

		for (float r = RING_SPACING; r < limit; r++)
		{
			double[][] points = UtilMath.normalCircle(player.getLocation(), player.getVelocity(), r, Math.round(RING_DENSITY * r));

			for (int i = 0; i < points.length; i++)
			{
				UtilParticle.PlayParticleToAll(UtilParticle.ParticleType.EXPLODE, new Location(player.getWorld(), points[i][0], points[i][1], points[i][2]),
						null, 0, 1, UtilParticle.ViewDist.NORMAL);
			}
		}
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if(event.getType() != UpdateType.FASTEST)
		{
			return;
		}

		for(Iterator<Map.Entry<Player, Long>> it = _playerMap.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry<Player, Long> e = it.next();

			if(e.getValue() <= System.currentTimeMillis())
			{
				it.remove();
				continue;
			}

			Location loc = e.getKey().getLocation().add(0, Y_OFFSET, 0);

			for (int i = 0; i < PARTICLES; ++i)
			{
				UtilParticle.playColoredParticleToAll(java.awt.Color.RED, UtilParticle.ParticleType.RED_DUST,
						UtilMath.gauss(loc, DISTRIBUTION, DISTRIBUTION, DISTRIBUTION), 0, UtilParticle.ViewDist.NORMAL);
			}
		}
	}
}
