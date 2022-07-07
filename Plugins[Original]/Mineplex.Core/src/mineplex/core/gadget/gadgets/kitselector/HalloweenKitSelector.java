package mineplex.core.gadget.gadgets.kitselector;

import java.awt.*;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.KitSelectorGadget;

public class HalloweenKitSelector extends KitSelectorGadget
{

	private static final DustSpellColor COLOR_A = new DustSpellColor(Color.ORANGE);
	private static final DustSpellColor COLOR_B = new DustSpellColor(Color.BLACK);

	public HalloweenKitSelector(GadgetManager manager)
	{
		super(manager, "Haunted Kit", UtilText.splitLinesToArray(new String[]{C.cGray + "Are you scared?"}, LineFormat.LORE),
				0, Material.PUMPKIN, (byte) 0);
	}


	@Override
	public void playParticle(Entity entity, Player playTo)
	{
		Location location = entity.getLocation().add(0, 1.1, 0);

		new ColoredParticle(ParticleType.RED_DUST, COLOR_A, UtilAlg.getRandomLocation(location, 1, 1, 1))
				.display(ViewDist.NORMAL, playTo);
		new ColoredParticle(ParticleType.RED_DUST, COLOR_B, UtilAlg.getRandomLocation(location, 1, 1, 1))
				.display(ViewDist.NORMAL, playTo);
	}

}
