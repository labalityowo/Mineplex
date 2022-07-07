package mineplex.core.gadget.gadgets.particle;

import java.awt.Color;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import mineplex.core.common.shape.ShapeWings;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleWingsLove extends ParticleGadget
{

	private ShapeWings _wings = new ShapeWings(UtilParticle.ParticleType.RED_DUST.particleName, new Vector(1, 1, 1), 1, 0, false, ShapeWings.DEFAULT_ROTATION, ShapeWings.HEART_WING_PATTERN);
	private ShapeWings _wingsWhite = new ShapeWings(UtilParticle.ParticleType.RED_DUST.particleName, new Vector(1, 1, 1), 1, 0, '%', ShapeWings.DEFAULT_ROTATION, ShapeWings.HEART_WING_PATTERN);
	private ShapeWings _wingsEdge = new ShapeWings(UtilParticle.ParticleType.RED_DUST.particleName, new Vector(1, 1, 1), 1, 0, true, ShapeWings.DEFAULT_ROTATION, ShapeWings.HEART_WING_PATTERN);

	public ParticleWingsLove(GadgetManager manager)
	{
		super(manager, "Love Wings",
				UtilText.splitLineToArray(C.cGray + "Sometimes Love just makes you want to fly.", LineFormat.LORE),
				-17, Material.NETHER_STAR, (byte) 0);
		setDisplayItem(ItemStackFactory.Instance.createCustomPotion(PotionType.INSTANT_HEAL));
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		Location loc = player.getLocation().add(0, 1.2, 0).add(player.getLocation().getDirection().multiply(-0.2));
		if (Manager.isMoving(player))
		{
			if (event.getType() == UpdateType.TICK)
			{
				_wings.displayColoredParticle(loc, Color.PINK);
				_wingsWhite.displayColoredParticle(loc, Color.WHITE);
				_wingsEdge.displayColoredParticle(loc, Color.BLACK);
			}
			return;
		}

		if (event.getType() == UpdateType.FAST)
		{
			_wings.displayColored(loc, Color.PINK);
			_wingsWhite.displayColored(loc, Color.WHITE);
			_wingsEdge.displayColored(loc, Color.BLACK);
		}
	}
}
