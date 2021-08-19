package mineplex.core.gadget.gadgets.mount.types;

import org.bukkit.DyeColor;
import org.bukkit.Location;
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
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseSheep;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.mount.HorseMount;
import mineplex.core.gadget.gadgets.mount.SingleEntityMountData;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MountValentinesSheep extends HorseMount
{

	public MountValentinesSheep(GadgetManager manager)
	{
		super(manager,
				"Loving Sheeples",
				UtilText.splitLineToArray(C.cGray + "This symbol of love will live on with you forever! Mainly because we couldn't attach the cupid wings to it. I guess duct tape can't fix everything!", LineFormat.LORE),
				CostConstants.NO_LORE,
				Material.WOOL,
				(byte) 6,
				Color.BLACK,
				Style.NONE,
				Variant.HORSE,
				1,
				null
		);
	}

	@Override
	public SingleEntityMountData<Horse> spawnMount(Player player)
	{
		SingleEntityMountData<Horse> data =  super.spawnMount(player);
		Horse horse = data.getEntity();

		UtilEnt.silence(horse, true);

		DisguiseSheep disguise = new DisguiseSheep(horse);
		disguise.setColor(DyeColor.PINK);

		Manager.getDisguiseManager().disguise(disguise);

		return data;
	}

	@EventHandler
	public void doAnimations(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST || event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (SingleEntityMountData<Horse> ent : getActiveMounts().values())
		{
			if (!ent.getEntity().isValid())
			{
				continue;
			}

			if (event.getType() == UpdateType.FASTEST)
			{
				Location loc = ent.getEntity().getLocation().clone().add(0, .5, 0);
				
				UtilParticle.PlayParticleToAll(ParticleType.HEART, loc, .5F, .4F, .5F, 0F, 1, ViewDist.NORMAL);
			
				UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.WOOL, 14), loc, .5F, .4F, .5F, 0.0f, 1, ViewDist.NORMAL);
				UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.WOOL, 6), loc, .5F, .4F, .5F, 0.0f, 1, ViewDist.NORMAL);
			}
			else if (event.getType() == UpdateType.FAST)
			{
				DisguiseBase disguise = Manager.getDisguiseManager().getActiveDisguise(ent.getEntity());
				
				if (disguise instanceof DisguiseSheep)
				{
					DisguiseSheep sheep = (DisguiseSheep) disguise;
										
					if (sheep.getColor() == 6) // Pink
					{
						sheep.setColor(DyeColor.RED);
					}
					else if (sheep.getColor() == 14) // Red
					{
						sheep.setColor(DyeColor.MAGENTA);
					}
					else if (sheep.getColor() == 2) // Magenta
					{
						sheep.setColor(DyeColor.PINK);
					}
					
					Manager.getDisguiseManager().updateDisguise(sheep);
				}
			}
		}
	}
}
