package mineplex.core.gadget.gadgets.doublejump;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DoubleJumpEffectGadget;

public class DoubleJumpCandyCane extends DoubleJumpEffectGadget
{

	public DoubleJumpCandyCane(GadgetManager manager)
	{
		super(manager, "Candy Cane Blast", 
				UtilText.splitLineToArray(C.cGray + "It is said every time an elf jumps, bits of Candy Cane fall out of their pockets.", LineFormat.LORE),
				-3,
				Material.INK_SACK, (byte)1);
	}

	@Override
	public void doEffect(Player player)
	{
		Location loc = player.getLocation();
		UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.INK_SACK, 15), loc, 0, 0, 0, 0.15f, 100, ViewDist.NORMAL);
		UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.INK_SACK,  1), loc, 0, 0, 0, 0.15f, 100, ViewDist.NORMAL);
		UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.INK_SACK, 15), loc, 0, 0, 0, 0.15f, 100, ViewDist.NORMAL);
	}

}
