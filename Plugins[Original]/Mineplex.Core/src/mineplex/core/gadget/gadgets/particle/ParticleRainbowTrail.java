package mineplex.core.gadget.gadgets.particle;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.set.SetRainbow;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleRainbowTrail extends ParticleGadget
{

	private static final double Y_MOD = 0.18;

	public ParticleRainbowTrail(GadgetManager manager)
	{
		super(manager, "Trail of the Rainbow", UtilText.splitLineToArray(C.cGray + "Did you know Unicorn farts seem like flowers? The More You Know.", LineFormat.LORE), CostConstants.FOUND_IN_TREASURE_CHESTS, Material.WOOL, (byte) UtilMath.r(15));
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Location location = player.getLocation();
		location.setPitch(0);
		location.subtract(location.getDirection().multiply(0.5));
		location.add(0, Y_MOD + Y_MOD * SetRainbow.COLOURS.length, 0);

		for (DustSpellColor colour : SetRainbow.COLOURS)
		{
			new ColoredParticle(ParticleType.MOB_SPELL_AMBIENT, colour, location.subtract(0, Y_MOD, 0))
					.display();
		}
	}
}
