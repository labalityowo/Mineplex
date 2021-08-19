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

public class DeathBlood extends DeathEffectGadget
{

	public DeathBlood(GadgetManager manager)
	{
		super(manager, "Gory Blood Death", 
				UtilText.splitLineToArray(C.cGray + "A gruesome display fit for Kill Bill.", LineFormat.LORE),
				-2, Material.REDSTONE, (byte) 0);
	}

	@Override
	public void onBlood(Player player, BloodEvent event)
	{
		event.setItem(Material.REDSTONE_BLOCK, (byte) 0);
		UtilParticle.PlayParticleToAll(ParticleType.RED_DUST, player.getLocation(), 0.4f, 0.4f, 0.4f, 0f, 20, ViewDist.LONG);
		UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.REDSTONE_BLOCK, 0), player.getLocation(), 0.1f, 0.1f, 0.1f, 0.25f, 60, ViewDist.LONG);
	}

	
}
