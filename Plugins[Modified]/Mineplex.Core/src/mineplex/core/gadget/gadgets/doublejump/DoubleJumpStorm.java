package mineplex.core.gadget.gadgets.doublejump;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DoubleJumpEffectGadget;

public class DoubleJumpStorm extends DoubleJumpEffectGadget
{

	public DoubleJumpStorm(GadgetManager manager)
	{
		super(manager, "Wet Leap", 
				UtilText.splitLineToArray(C.cGray + "Time to get your feet wet.", LineFormat.LORE),
				-2, Material.INK_SACK, (byte) 4);
	}

	@Override
	public void doEffect(Player player)
	{
		UtilParticle.PlayParticleToAll(ParticleType.SPLASH, player.getLocation(), 0.4f, 0.3f, 0.4f, 1f, 160, ViewDist.NORMAL);
		UtilParticle.PlayParticleToAll(ParticleType.CLOUD, player.getLocation(), 0.3f, 0.1f, 0.3f, 0.05f, 50, ViewDist.NORMAL);
	}

}
