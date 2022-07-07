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

public class DeathShadow extends DeathEffectGadget
{

	public DeathShadow(GadgetManager manager)
	{
		super(manager, "Shadow Death", 
				UtilText.splitLineToArray(C.cGray + "A poof of smoke, and you’re gone!", LineFormat.LORE),
				-2, Material.COAL, (byte) 0);
	}

	@Override
	public void onBlood(Player player, BloodEvent event)
	{
		event.setCancelled(true);
		
		UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, player.getLocation().add(0, 0.5, 0), 0.1f, 0.5f, 0.1f, 0, 50, ViewDist.NORMAL);
		UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, player.getLocation(), 0.5f, 0.0f, 0.5f, 0, 50, ViewDist.NORMAL);
	}

}
