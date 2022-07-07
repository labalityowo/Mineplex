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

public class DoubleJumpBlood extends DoubleJumpEffectGadget
{

	public DoubleJumpBlood(GadgetManager manager)
	{
		super(manager, "Bloody Leap", 
				UtilText.splitLineToArray(C.cGray + "Their blood gives you power, the power to soar!", LineFormat.LORE),
				-2, Material.REDSTONE, (byte) 0);
	}

	@Override
	public void doEffect(Player player)
	{
		UtilParticle.PlayParticleToAll(ParticleType.RED_DUST, player.getLocation(), 0.5f, 0.5f, 0.5f, 0f, 40, ViewDist.LONG);
		UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.REDSTONE_BLOCK, 0), player.getLocation(), 0.1f, 0.1f, 0.1f, 0.25f, 100, ViewDist.LONG);
	}

}
