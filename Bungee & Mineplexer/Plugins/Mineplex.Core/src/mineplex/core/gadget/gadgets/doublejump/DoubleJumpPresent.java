package mineplex.core.gadget.gadgets.doublejump;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DoubleJumpEffectGadget;
import mineplex.core.gadget.util.CostConstants;

public class DoubleJumpPresent extends DoubleJumpEffectGadget
{

	private static final int PRESENTS = 5;
	private static final DustSpellColor[] COLOURS =
			{
				new DustSpellColor(Color.RED),
				new DustSpellColor(Color.GREEN)
			};

	public DoubleJumpPresent(GadgetManager manager)
	{
		super(manager, "Present Leap",
				UtilText.splitLineToArray(C.cGray + "A leap of joy on Christmas Day!", LineFormat.LORE),
				CostConstants.FOUND_IN_GINGERBREAD_CHESTS, Material.GLASS, (byte) 0);

		setDisplayItem(SkinData.PRESENT.getSkull());
	}

	@Override
	public void doEffect(Player player)
	{
		Location location = player.getLocation().add(0, 1, 0);

		for (int i = 0; i < PRESENTS; i++)
		{
			UtilItem.dropItem(getDisplayItem(), location, true, false, 15, false);
		}

		Manager.runSyncTimer(new BukkitRunnable()
		{
			int iterations = 0;

			@Override
			public void run()
			{
				if (++iterations >= 20)
				{
					cancel();
					return;
				}

				Location locationNow = player.getLocation();

				for (DustSpellColor colour : COLOURS)
				{
					new ColoredParticle(ParticleType.RED_DUST, colour, locationNow)
							.display();
				}
			}
		}, 5, 1);
	}
}
