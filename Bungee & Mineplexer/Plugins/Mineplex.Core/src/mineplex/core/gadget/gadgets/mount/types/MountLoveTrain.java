package mineplex.core.gadget.gadgets.mount.types;

import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseBlock;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.mount.HorseMount;
import mineplex.core.gadget.gadgets.mount.SingleEntityMountData;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MountLoveTrain extends HorseMount
{

	public MountLoveTrain(GadgetManager manager)
	{
		super(manager,
				"Love Train",
				UtilText.splitLineToArray(C.cGray + "Woo Woo! All aboard!", LineFormat.LORE),
				CostConstants.FOUND_IN_LOVE_CHESTS,
				Material.WOOL,
				(byte) 6,
				Color.BLACK,
				Style.NONE,
				Variant.HORSE,
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

		DisguiseBlock block = new DisguiseBlock(horse, Material.BARRIER, 0);
		Manager.getDisguiseManager().disguise(block);

		return data;
	}

	@Override
	@EventHandler
	public void updateBounce(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (SingleEntityMountData<Horse> data : getActiveMounts().values())
		{
			Horse horse = data.getEntity();
			UtilParticle.PlayParticleToAll(UtilParticle.ParticleType.HEART, horse.getLocation(), 0.25f, 0.25f, 0.25f, 0.5f, 3, ViewDist.NORMAL);
		}

		super.updateBounce(event);
	}
}
