package mineplex.core.gadget.gadgets.arrowtrail;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ArrowEffectGadget;

public class ArrowTrailShadow extends ArrowEffectGadget
{

	public ArrowTrailShadow(GadgetManager manager)
	{
		super(manager, "Shadow Arrows", 
				UtilText.splitLineToArray(C.cGray + C.Italics + "\"What's blacker than black? None more black.\"", LineFormat.LORE),
				-2, Material.COAL, (byte) 0);
	}

	@Override
	public void doTrail(Arrow arrow)
	{
//		if(!(arrow.getShooter() instanceof Player)) return;
//		Player player = (Player) arrow.getShooter();
//		if(getSet() == null || !getSet().isActive(player)) return;
		UtilParticle.PlayParticleToAll(ParticleType.SMOKE, arrow.getLocation(), null, 0.1f, 3, ViewDist.LONG);
	}

	@Override
	public void doHitEffect(Arrow arrow)
	{
		UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, arrow.getLocation(), 0.3f, 0.3f, 0.3f , 0, 5, ViewDist.LONG);
	}

}
