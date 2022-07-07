package mineplex.core.gadget.gadgets.doublejump;

import mineplex.core.common.util.C;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DoubleJumpEffectGadget;

public class DoubleJumpShadow extends DoubleJumpEffectGadget
{

	public DoubleJumpShadow(GadgetManager manager)
	{
		super(manager, "Shadow Leap", 
				UtilText.splitLineToArray(C.cGray + "A shadowy leap preferred by master assassins.", LineFormat.LORE),
				-2, Material.COAL, (byte) 0);
	}

	@Override
	public void doEffect(Player player)
	{
		UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, player.getLocation(), 0.5f, 0.0f, 0.5f, 0, 20, ViewDist.LONG);
	}

}
