package mineplex.core.gadget.gadgets.mount.types;

import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseBlock;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.mount.HorseMount;
import mineplex.core.gadget.gadgets.mount.SingleEntityMountData;
import mineplex.core.gadget.util.CostConstants;

public class MountCake extends HorseMount
{

	public MountCake(GadgetManager manager)
	{
		super(manager,
				"Cake Mount",
				UtilText.splitLineToArray(C.cGray + "The Lie.", LineFormat.LORE),
				CostConstants.FOUND_IN_THANKFUL_CHESTS,
				Material.CAKE,
				(byte) 0,
				Horse.Color.BLACK,
				Horse.Style.NONE,
				Horse.Variant.HORSE,
				2,
				null
		);

		BouncyCollisions = true;
	}

	@Override
	public SingleEntityMountData<Horse> spawnMount(Player player)
	{
		SingleEntityMountData<Horse> data = super.spawnMount(player);
		Horse horse = data.getEntity();

		UtilEnt.silence(horse, true);

		DisguiseBlock block = new DisguiseBlock(horse, Material.CAKE_BLOCK, 0);
		Manager.getDisguiseManager().disguise(block);

		return data;
	}
}