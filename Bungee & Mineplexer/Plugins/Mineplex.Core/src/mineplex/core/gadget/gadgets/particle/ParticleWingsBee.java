package mineplex.core.gadget.gadgets.particle;

import java.awt.Color;
import java.time.Month;
import java.time.YearMonth;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.shape.ShapeWings;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleWingsBee extends ParticleGadget
{

	private ShapeWings _wingsBlack = new ShapeWings(UtilParticle.ParticleType.RED_DUST.particleName, new Vector(1, 1, 1), 1, 0, '$', ShapeWings.DEFAULT_ROTATION, ShapeWings.BEE_WING_PATTERN);
	private ShapeWings _wingsYellow = new ShapeWings(UtilParticle.ParticleType.RED_DUST.particleName, new Vector(1, 1, 1), 1, 0, '#', ShapeWings.DEFAULT_ROTATION, ShapeWings.BEE_WING_PATTERN);

	public ParticleWingsBee(GadgetManager manager)
	{
		super(manager, "Bumblebee's Wings",
				UtilText.splitLineToArray(C.cGray + "Float like a butterfly and sting like a bee with these new spring wings!", LineFormat.LORE),
				-14, Material.WOOL, (byte) 4);

		setPPCYearMonth(YearMonth.of(2017, Month.APRIL));
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		Location loc = player.getLocation().add(0, 1, 0).add(player.getLocation().getDirection().multiply(-0.4));
		if (Manager.isMoving(player))
		{
			if (event.getType() == UpdateType.TICK)
			{
				_wingsBlack.displayColoredParticle(loc, Color.BLACK);
				_wingsYellow.displayColoredParticle(loc, Color.YELLOW);
			}
			return;
		}

		if (event.getType() == UpdateType.FAST)
		{
			_wingsBlack.displayColored(loc, Color.BLACK);
			_wingsYellow.displayColored(loc, Color.YELLOW);
		}
	}
}
