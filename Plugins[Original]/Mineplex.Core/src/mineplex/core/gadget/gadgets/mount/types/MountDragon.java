package mineplex.core.gadget.gadgets.mount.types;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.GadgetManager.Perm;
import mineplex.core.gadget.gadgets.mount.DragonData;
import mineplex.core.gadget.gadgets.mount.DragonMount;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MountDragon extends DragonMount
{

	public MountDragon(GadgetManager manager)
	{
		super(manager,
				"Ethereal Dragon",
				UtilText.splitLineToArray(C.cGray + "From the distant Ether Realm, this prized dragon is said to only obey true Heroes!", LineFormat.LORE),
				CostConstants.UNLOCKED_WITH_HERO,
				Material.DRAGON_EGG,
				(byte) 0
		);

		setMaxActive(4);
	}

	@EventHandler
	public void Trail(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (DragonData data : getActiveMounts().values())
		{
			UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, data.Dragon.getLocation().add(0, 1, 0), 1f, 1f, 1f, 0f, 20, ViewDist.NORMAL);
		}
	}

	@EventHandler
	public void DragonLocation(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (DragonData data : getActiveMounts().values())
		{
			data.Move();
		}

		Set<Player> toRemove = new HashSet<>();

		for (Player player : getActiveMounts().keySet())
		{
			DragonData data = getActiveMounts().get(player);

			if (data == null)
			{
				toRemove.add(player);
				continue;
			}

			if (!data.Dragon.isValid() || data.Dragon.getPassenger() == null || data.Dragon.getPassenger().getPassenger() == null)
			{
				data.Dragon.remove();
				toRemove.add(player);
			}
		}

		for (Player player : toRemove)
		{
			disable(player);
		}
	}

	public void SetName(String news)
	{
		for (DragonData dragon : getActiveMounts().values())
			dragon.Dragon.setCustomName(news);
	}

	public void setHealthPercent(double healthPercent)
	{
		for (DragonData dragon : getActiveMounts().values())
		{
			double health = healthPercent * dragon.Dragon.getMaxHealth();
			if (health <= 0.0)
			{
				health = 0.001;
			}

			dragon.Dragon.setHealth(health);
		}
	}

	@EventHandler
	public void HeroOwner(PlayerJoinEvent event)
	{
		if (Manager.getClientManager().Get(event.getPlayer()).hasPermission(Perm.HERO_MOUNT))
		{
			Manager.getDonationManager().Get(event.getPlayer()).addOwnedUnknownSalesPackage(getName());
		}
	}
}