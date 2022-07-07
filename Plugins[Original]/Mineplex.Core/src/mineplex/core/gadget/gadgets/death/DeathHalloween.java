package mineplex.core.gadget.gadgets.death;

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
import mineplex.core.gadget.util.CostConstants;

public class DeathHalloween extends DeathEffectGadget
{

	public DeathHalloween(GadgetManager manager)
	{
		super(manager, "Spooky Death",
				UtilText.splitLineToArray(C.cGray + "Explode in a flurry of fright.", LineFormat.LORE),
				CostConstants.FOUND_IN_TRICK_OR_TREAT, Material.PUMPKIN, (byte) 0);
	}

	@Override
	public void onBlood(Player player, BloodEvent event)
	{
		event.setItem(Material.PUMPKIN, (byte) 0);
		UtilParticle.PlayParticleToAll(ParticleType.FLAME, player.getLocation().add(0, 1.1, 0), 0.4F, 0.4F, 0.4F, 0.1F, 30, ViewDist.LONG);
	}

	
}
