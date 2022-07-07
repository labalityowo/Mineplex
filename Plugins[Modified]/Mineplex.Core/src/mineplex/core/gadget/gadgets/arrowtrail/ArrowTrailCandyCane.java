package mineplex.core.gadget.gadgets.arrowtrail;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ArrowEffectGadget;

import net.minecraft.server.v1_8_R3.MinecraftServer;

public class ArrowTrailCandyCane extends ArrowEffectGadget
{

	public ArrowTrailCandyCane(GadgetManager manager)
	{
		super(manager, "Candy Cane Arrows", 
				UtilText.splitLineToArray(C.cGray + "The real reason no one visits the North Pole? Santa's Elves are deadly shots.", LineFormat.LORE),
				-3,
				Material.INK_SACK, (byte)1);
	}

	@Override
	public void doTrail(Arrow arrow)
	{
		int data = 15;
		int tick = Math.abs(MinecraftServer.currentTick%3);
		if(tick == 1) data = 1;
		if(tick == 2) data = 2;
		Location loc = arrow.getLocation();
		UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.INK_SACK, data), loc, 0, 0, 0, 0.0f, 1, ViewDist.NORMAL);
	}

	@Override
	public void doHitEffect(Arrow arrow)
	{
		Location loc = arrow.getLocation();
		UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.INK_SACK, 15), loc, 0, 0, 0, 0.1f, 20, ViewDist.NORMAL);
		UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.INK_SACK,  1), loc, 0, 0, 0, 0.1f, 20, ViewDist.NORMAL);
		UtilParticle.PlayParticleToAll(ParticleType.ICON_CRACK.getParticle(Material.INK_SACK,  2), loc, 0, 0, 0, 0.1f, 20, ViewDist.NORMAL);
	}

}
