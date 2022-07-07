package mineplex.core.gadget.gadgets.doublejump;

import org.bukkit.Material;
import org.bukkit.entity.Player;
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
import mineplex.core.gadget.types.DoubleJumpEffectGadget;

public class DoubleJumpTitan extends DoubleJumpEffectGadget
{
	public DoubleJumpTitan(GadgetManager manager)
	{
		super(manager, "Leap of the Titans", 
				UtilText.splitLineToArray(C.cGray + "Out of the frying pan and into the fire.", LineFormat.LORE),
				-13,
				Material.FIREBALL, (byte)0);
	}
	
	@Override
	public void doEffect(Player player)
	{
		UtilParticle.PlayParticle(ParticleType.LAVA, player.getLocation(), 0f, 0f, 0f, 0.2f, 40,
				ViewDist.NORMAL, UtilServer.getPlayers());
		UtilParticle.PlayParticleToAll(ParticleType.FLAME, player.getLocation(), 0, 0, 0, 0.2f, 40, ViewDist.NORMAL);
	}
		
	@EventHandler
	public void titanOwner(PlayerJoinEvent event)
	{
		if (Manager.getClientManager().Get(event.getPlayer()).hasPermission(GadgetManager.Perm.TITAN_DOUBLE_JUMP))
		{
			Manager.getDonationManager().Get(event.getPlayer()).addOwnedUnknownSalesPackage(getName());
		}
	}
}