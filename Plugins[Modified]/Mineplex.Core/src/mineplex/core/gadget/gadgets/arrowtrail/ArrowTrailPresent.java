package mineplex.core.gadget.gadgets.arrowtrail;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ArrowEffectGadget;
import mineplex.core.gadget.util.CostConstants;

public class ArrowTrailPresent extends ArrowEffectGadget
{

	private static final int PRESENTS_HIT_EFFECT = 5;

	public ArrowTrailPresent(GadgetManager manager)
	{
		super(manager, "Present Arrows",
				UtilText.splitLineToArray(C.cGray + "Incoming gifts!!", LineFormat.LORE),
				CostConstants.FOUND_IN_GINGERBREAD_CHESTS, Material.GLASS, (byte) 0);

		setDisplayItem(SkinData.PRESENT.getSkull());
	}

	@Override
	public void doTrail(Arrow arrow)
	{
		Location location = arrow.getLocation();

		UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.WOOL, 5), location, null, 0.1F, 1, ViewDist.NORMAL);
		UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.WOOL, 14), location, null, 0.1F, 1, ViewDist.NORMAL);
	}

	@Override
	public void doHitEffect(Arrow arrow)
	{
		Location location = arrow.getLocation();

		for (int i = 0; i < PRESENTS_HIT_EFFECT; i++)
		{
			UtilItem.dropItem(getDisplayItem(), location, true, false, 15, false);
		}
	}

}
