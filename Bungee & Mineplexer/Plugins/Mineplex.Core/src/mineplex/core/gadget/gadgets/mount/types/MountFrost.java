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

public class MountFrost extends HorseMount
{
	public MountFrost(GadgetManager manager)
	{
		super(manager, "Glacial Steed",
				UtilText.splitLineToArray(C.cGray + "Born in the North Pole, it leaves a trail of frost as it moves!", LineFormat.LORE),
				15000,
				Material.SNOW_BALL,
				(byte) 0,
				Color.WHITE,
				Style.WHITE,
				Variant.HORSE,
				1,
				null
		);
	}


	@EventHandler
	public void trail(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (SingleEntityMountData<Horse> horseData : getActiveMounts().values())
		{
			Horse horse = horseData.getEntity();
			UtilParticle.PlayParticleToAll(ParticleType.SNOW_SHOVEL, horse.getLocation().add(0, 1, 0), 0.25f, 0.25f, 0.25f, 0.1f, 4, ViewDist.NORMAL);
		}
	}
}
