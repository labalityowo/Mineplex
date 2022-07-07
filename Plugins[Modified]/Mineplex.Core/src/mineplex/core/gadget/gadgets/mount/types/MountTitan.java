package mineplex.core.gadget.gadgets.mount.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.GadgetManager.Perm;
import mineplex.core.gadget.gadgets.mount.Mount;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MountTitan extends Mount<MountTitanData>
{

	public MountTitan(GadgetManager manager)
	{
		super(manager,
				"Molten Snake",
				UtilText.splitLineToArray(C.cGray + "Deep under the earths surface, there exists a mythical species of Molten Snakes. This one will serve you eternally.", LineFormat.LORE),
				CostConstants.UNLOCKED_WITH_TITAN,
				Material.MAGMA_CREAM,
				(byte) 0
		);

		setMaxActive(3);
	}

	@Override
	public MountTitanData spawnMount(Player player)
	{
		return new MountTitanData(player, getName());
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (MountTitanData data : _active.values())
		{
			data.update();
		}
	}

	@Override
	protected boolean shouldRide(Player player, MountTitanData data, boolean head)
	{
		if (data.ownsMount(player))
		{
			data.getHead().setPassenger(player);
		}

		return false;
	}

	@EventHandler
	public void titanOwner(PlayerJoinEvent event)
	{
		if (Manager.getClientManager().Get(event.getPlayer()).hasPermission(Perm.TITAN_MOUNT))
		{
			Manager.getDonationManager().Get(event.getPlayer()).addOwnedUnknownSalesPackage(getName());
		}
	}
}
