package mineplex.core.gadget.set;

import java.awt.*;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.donation.Donor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.GadgetManager.Perm;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailLegend;
import mineplex.core.gadget.gadgets.death.DeathLegend;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpLegend;
import mineplex.core.gadget.gadgets.particle.ParticleLegend;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetSet;

public class SetLegend extends GadgetSet
{

	public static final DustSpellColor[] SELECTABLE_COLORS =
			{
					new DustSpellColor(new Color(60, 170, 25)),
					new DustSpellColor(new Color(33, 92, 13)),
					new DustSpellColor(Color.BLACK)
			};

	public SetLegend(GadgetManager manager)
	{
		super(manager, "Legend", "Improved Legendary Aura",
				manager.getGadget(ArrowTrailLegend.class),
				manager.getGadget(DeathLegend.class),
				manager.getGadget(DoubleJumpLegend.class),
				manager.getGadget(ParticleLegend.class)
		);
	}

	@EventHandler
	public void legendOwner(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		if (Manager.getClientManager().Get(player).hasPermission(Perm.LEGEND_PARTICLE_EFFECT))
		{
			Donor donor = Manager.getDonationManager().Get(player);

			for (Gadget gadget : getGadgets())
			{
				donor.addOwnedUnknownSalesPackage(gadget.getName());
			}
		}
	}
}
