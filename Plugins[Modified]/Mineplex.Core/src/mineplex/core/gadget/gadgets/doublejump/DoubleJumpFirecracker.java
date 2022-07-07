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

public class DoubleJumpFirecracker extends DoubleJumpEffectGadget
{
	
	private byte[] _data = new byte[]{1,2,4,5,6,9,10,11,12,13,14,15};

	public DoubleJumpFirecracker(GadgetManager manager)
	{
		super(manager, "Firecracker Leap", 
				UtilText.splitLineToArray(C.cGray + "Propel yourself in the air with the power of " + C.cPurple + "FREEDOM!", LineFormat.LORE),
				-2, Material.FIREWORK, (byte)0);
	}

	@Override
	public void doEffect(Player player)
	{
		for(byte data : _data) {
			String particle = ParticleType.ICON_CRACK.getParticle(Material.INK_SACK, data);
			UtilParticle.PlayParticleToAll(particle, player.getLocation(), null, 0.3f, 15, ViewDist.LONGER);
		}
	}

}
