package mineplex.core.gadget.gadgets.particle;

import org.bukkit.Material;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.util.CostConstants;

public class ParticleJetPack extends ParticleImageGadget
{

	private static final String[] PARTICLE =
		{
				"-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/139,139,139/139,139,139/255,0,0/255,102,0/-1,-1,-1/",
				"54,54,54/54,54,54/54,54,54/46,46,46/46,46,46/46,46,46/46,46,46/38,38,38/38,38,38/99,99,99/119,119,119/255,102,0/255,168,0/255,252,0/",
				"-1,-1,-1/-1,-1,-1/38,38,38/139,139,139/68,81,104/119,119,119/99,99,99/46,46,46/38,38,38/99,99,99/99,99,99/255,0,0/255,102,0/-1,-1,-1/",
				"-1,-1,-1/-1,-1,-1/38,38,38/162,162,162/139,139,139/68,81,104/119,119,119/99,99,99/38,38,38/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/",
				"-1,-1,-1/-1,-1,-1/38,38,38/162,162,162/139,139,139/68,81,104/119,119,119/99,99,99/38,38,38/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/",
				"-1,-1,-1/-1,-1,-1/38,38,38/139,139,139/68,81,104/119,119,119/99,99,99/46,46,46/38,38,38/99,99,99/99,99,99/255,0,0/255,102,0/-1,-1,-1/",
				"54,54,54/54,54,54/54,54,54/46,46,46/46,46,46/46,46,46/46,46,46/38,38,38/38,38,38/99,99,99/119,119,119/255,102,0/255,168,0/255,252,0/",
				"-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/-1,-1,-1/139,139,139/139,139,139/255,0,0/255,102,0/-1,-1,-1/",
		};

	public ParticleJetPack(GadgetManager manager)
	{
		super(manager, "Jet Pack", UtilText.splitLineToArray(C.cGray + "Shame it doesn't actually allow you to fly ¯\\_(ツ)_/¯", LineFormat.LORE), PARTICLE, CostConstants.FOUND_IN_TREASURE_CHESTS, Material.IRON_INGOT, (byte) 0);

		_yOffset = 0.8;
	}
}
