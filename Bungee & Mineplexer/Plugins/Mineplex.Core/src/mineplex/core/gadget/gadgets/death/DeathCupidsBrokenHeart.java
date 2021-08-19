package mineplex.core.gadget.gadgets.death;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.blood.BloodEvent;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DeathEffectGadget;

public class DeathCupidsBrokenHeart extends DeathEffectGadget
{

	public DeathCupidsBrokenHeart(GadgetManager manager)
	{
		super(manager, "Cupid's Broken Heart", 
				UtilText.splitLineToArray("Cue the weeping violins...", LineFormat.LORE),
				-2, Material.APPLE, (byte) 0, "Broken Hearted");
	}
	
	@Override
	public void onBlood(Player player, BloodEvent event)
	{
		event.setCancelled(true);
		Location loc = player.getLocation().add(0, 0.9, 0);
		
		UtilParticle.PlayParticleToAll(ParticleType.HEART, loc, 0.3f, 0.7f, 0.3f, 0, 15, ViewDist.NORMAL);
	}

}
