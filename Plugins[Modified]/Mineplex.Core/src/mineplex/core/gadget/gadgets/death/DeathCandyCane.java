package mineplex.core.gadget.gadgets.death;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.blood.BloodEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DeathEffectGadget;

public class DeathCandyCane extends DeathEffectGadget
{
	public DeathCandyCane(GadgetManager manager)
	{
		super(manager, "Candy Cane Remains", 
				UtilText.splitLineToArray(C.cGray + "The biggest enemy of the Holidays is January.", LineFormat.LORE),
				-3,
				Material.INK_SACK, (byte)1);
	}

	@Override
	public void onBlood(Player player, BloodEvent event)
	{
		event.setCancelled(true);
		
		event.setItem(Material.INK_SACK, (byte) 15);
		
		Location loc = event.getLocation();
		int a = event.getParticles();
		UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.INK_SACK,  1), loc, 0, 0, 0, 0.1f, a, ViewDist.NORMAL);
		UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.INK_SACK,  2), loc, 0, 0, 0, 0.1f, a, ViewDist.NORMAL);
	}
}
