package mineplex.core.gadget.gadgets.particle;

import mineplex.core.common.util.UtilMath;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleCoalFumes extends ParticleGadget
{

	public ParticleCoalFumes(GadgetManager manager)
	{
		super(manager, "Coal Fumes", 
				UtilText.splitLineToArray(C.cGray + "Being on the Naughty List does have some perks... if you love coal, that is...", LineFormat.LORE),
				-1, Material.COAL, (byte) 0);
	}
	
	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if(event.getType() != UpdateType.FASTER) return;
		
		float xz = 1;
		int amount = 5;
		ParticleType type = ParticleType.LARGE_SMOKE;
		
		if(Manager.isMoving(player)) 
		{
			xz = 0.4f;
			amount = 2;
		}

		UtilParticle.playParticleFor(player, type, UtilMath.gauss(player.getLocation(), 2, 6, 2), xz, 0, xz, 0, amount, ViewDist.NORMAL);
	}
}
