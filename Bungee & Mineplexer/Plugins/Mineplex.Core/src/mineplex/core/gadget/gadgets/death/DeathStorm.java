package mineplex.core.gadget.gadgets.death;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.blood.BloodEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DeathEffectGadget;

public class DeathStorm extends DeathEffectGadget
{

	public DeathStorm(GadgetManager manager)
	{
		super(manager, "Gloomy Death", 
				UtilText.splitLineToArray(C.cGray + "Drown in a puddle of your own tears.", LineFormat.LORE),
				-2, Material.INK_SACK, (byte) 4);
	}

	@Override
	public void onBlood(Player player, BloodEvent event)
	{
		event.setItem(Material.INK_SACK, (byte) 4);
		UtilParticle.PlayParticleToAll(ParticleType.SPLASH, player.getLocation().add(0, 0.9, 0), 0.6f, 0.9f, 0.6f, 0.6f, 100, ViewDist.NORMAL);
	}

	
}
