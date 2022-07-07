package mineplex.core.gadget.gadgets.particle.christmas;

import java.awt.*;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.shape.ShapeWings;
import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleWingsChristmas extends ParticleGadget
{

	private static final Color GREEN = new Color(0, 158, 21);

	private final ShapeWings _wings = new ShapeWings(ParticleType.RED_DUST.particleName, new Vector(1,1,1), 1, 0, false, ShapeWings.DEFAULT_ROTATION, ShapeWings.ANGEL_WING_PATTERN);
	private final ShapeWings _wingsEdge = new ShapeWings(ParticleType.RED_DUST.particleName, new Vector(1,1,0.5), 1, 0, true, ShapeWings.DEFAULT_ROTATION, ShapeWings.ANGEL_WING_PATTERN);

	public ParticleWingsChristmas(GadgetManager manager)
	{
		super(manager, "Christmas Wings",
				UtilText.splitLineToArray(C.cGray + "The wings that keep on giving.", LineFormat.LORE),
				CostConstants.FOUND_IN_GINGERBREAD_CHESTS, Material.GLASS, (byte) 0);

		setDisplayItem(SkinData.PRESENT.getSkull());
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST && event.getType() != UpdateType.FAST)
		{
			return;
		}

		Location location = player.getLocation();
		location.add(0, 1.2, 0).add(location.getDirection().multiply(-0.2));

		if (Manager.isMoving(player))
		{
			if (event.getType() == UpdateType.FASTEST)
			{
				UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, location, 0.2F, 0.2F, 0.2F, 0, 3, ViewDist.NORMAL);
			}
		}
		else if (event.getType() == UpdateType.FAST)
		{
			_wings.displayColored(location, GREEN);
			_wingsEdge.displayColored(location, Color.RED);
		}
	}

}
