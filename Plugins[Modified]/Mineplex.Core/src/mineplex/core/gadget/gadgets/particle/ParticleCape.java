package mineplex.core.gadget.gadgets.particle;

import org.bukkit.Material;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.banner.CountryFlag;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.util.CostConstants;

public class ParticleCape extends ParticleImageGadget
{

	private static final String[] PARTICLE =
		{
				"175,175,175/155,155,155/255,156,0/255,156,0/255,156,0/255,156,0/53,53,53/91,91,91/79,79,79/69,69,69/",
				"175,175,175/155,155,155/255,156,0/255,138,0/255,138,0/255,138,0/53,53,53/91,91,91/79,79,79/69,69,69/",
				"175,175,175/155,155,155/255,156,0/255,138,0/53,53,53/121,121,121/105,105,105/91,91,91/79,79,79/69,69,69/",
				"175,175,175/155,155,155/137,137,137/255,138,0/255,138,0/53,53,53/105,105,105/91,91,91/79,79,79/69,69,69/",
				"255,156,0/53,53,53/255,156,0/255,138,0/53,53,53/121,121,121/105,105,105/91,91,91/79,79,79/69,69,69/",
				"255,138,0/53,53,53/255,138,0/255,138,0/255,138,0/255,138,0/53,53,53/91,91,91/79,79,79/69,69,69/",
		};

	public ParticleCape(GadgetManager manager)
	{
		super(manager, "Mineplex Cape", UtilText.splitLineToArray(C.cGray + "This is a real super hero cape!", LineFormat.LORE), PARTICLE, CostConstants.NO_LORE, Material.GLASS, (byte) 0);

		setDisplayItem(CountryFlag.MINEPLEX.getBanner());
	}
}
