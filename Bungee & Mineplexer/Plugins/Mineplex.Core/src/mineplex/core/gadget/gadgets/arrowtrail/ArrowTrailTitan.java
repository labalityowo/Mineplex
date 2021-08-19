package mineplex.core.gadget.gadgets.arrowtrail;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ArrowEffectGadget;

public class ArrowTrailTitan extends ArrowEffectGadget
{
	public ArrowTrailTitan(GadgetManager manager)
	{
		super(manager, "Arrows of the Titans", 
				UtilText.splitLineToArray(C.cGray + "Arrows forged in the Fires of Creation, they leave fiery trails in their wake.", LineFormat.LORE),
				-13,
				Material.FIREBALL, (byte)0);
	}

	@Override
	public void doTrail(Arrow arrow)
	{
		UtilParticle.PlayParticle(ParticleType.FLAME, arrow.getLocation(), 0f, 0f, 0f, 0f, 1,
				ViewDist.LONGER, UtilServer.getPlayers());
	}
	
	@Override
	public void doHitEffect(Arrow arrow)
	{
		UtilParticle.PlayParticle(ParticleType.LAVA, arrow.getLocation(), 0f, 0f, 0f, 0f, 24,
				ViewDist.LONGER, UtilServer.getPlayers());
	}

	@EventHandler
	public void titanOwner(PlayerJoinEvent event)
	{
		if (Manager.getClientManager().Get(event.getPlayer()).hasPermission(GadgetManager.Perm.TITAN_ARROW_TRAIL))
		{
			Manager.getDonationManager().Get(event.getPlayer()).addOwnedUnknownSalesPackage(getName());
		}
	}
}