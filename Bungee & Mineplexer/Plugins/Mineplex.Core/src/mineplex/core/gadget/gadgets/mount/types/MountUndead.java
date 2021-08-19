package mineplex.core.gadget.gadgets.mount.types;

import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.mount.HorseMount;
import mineplex.core.gadget.gadgets.mount.SingleEntityMountData;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MountUndead extends HorseMount
{

	public MountUndead(GadgetManager manager)
	{
		super(manager,
				"Infernal Horror",
				UtilText.splitLineToArray(C.cGray + "The most ghastly horse in existance, from the pits of the Nether.", LineFormat.LORE),
				20000,
				Material.BONE,
				(byte) 0,
				Color.BLACK,
				Style.BLACK_DOTS,
				Variant.SKELETON_HORSE,
				0.8,
				null
		);
	}

	@EventHandler
	public void trail(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
		{
			for (SingleEntityMountData<Horse> horseData : getActiveMounts().values())
			{
				Horse horse = horseData.getEntity();
				UtilParticle.PlayParticleToAll(ParticleType.FLAME, horse.getLocation().add(0, 1, 0), 0.25f, 0.25f, 0.25f, 0, 2, ViewDist.NORMAL);
			}
		}

		else if (event.getType() == UpdateType.FAST)
		{
			for (SingleEntityMountData<Horse> horseData : getActiveMounts().values())
			{
				Horse horse = horseData.getEntity();
				UtilParticle.PlayParticleToAll(ParticleType.LAVA, horse.getLocation().add(0, 1, 0), 0.25f, 0.25f, 0.25f, 0, 1, ViewDist.NORMAL);
			}
		}
	}
}
