package mineplex.core.gadget.gadgets.mount.types;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.mount.Mount;
import mineplex.core.gadget.gadgets.mount.SingleEntityMountData;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MountPumpkin extends Mount<SingleEntityMountData>
{

	private static final ItemStack HELMET = new ItemStack(Material.JACK_O_LANTERN);

	public MountPumpkin(GadgetManager manager)
	{
		super(manager,
				"Pumpkin Mount",
				UtilText.splitLinesToArray(new String[]
						{
								C.cGray + "One of the Pumpkin King's flying minions.",
								"",
								C.cBlue + "Earned by defeating the Pumpkin King",
								C.cBlue + "in the 2017 Halloween Horror Event."
						}, LineFormat.LORE),
				CostConstants.NO_LORE,
				Material.JACK_O_LANTERN,
				(byte) 0
		);
	}

	@Override
	public SingleEntityMountData spawnMount(Player player)
	{
		ArmorStand stand = player.getWorld().spawn(player.getLocation(), ArmorStand.class);

		stand.setCustomName(player.getName() + "'s Pumpkin Mount");
		stand.setGravity(false);
		stand.setVisible(false);
		stand.setHelmet(HELMET);
		stand.setPassenger(player);
		stand.getWorld().playSound(stand.getLocation(), Sound.FIZZ, 1, 0.8F);

		return new SingleEntityMountData<>(player, stand);
	}

	@EventHandler
	public void updateLocation(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Set<Player> toRemove = new HashSet<>();

		for (Player player : getActiveMounts().keySet())
		{
			SingleEntityMountData data = getActiveMounts().get(player);

			if (data == null)
			{
				toRemove.add(player);
				continue;
			}

			if (!data.getEntity().isValid() || data.getEntity().getPassenger() == null)
			{
				data.getEntity().remove();
				toRemove.add(player);
				continue;
			}

			Location playerLocation = player.getLocation();
			Location location = data.getEntity().getLocation().add(playerLocation.getDirection());
			((CraftEntity) data.getEntity()).getHandle().setPositionRotation(location.getX(), location.getY(), location.getZ(), playerLocation.getYaw(), playerLocation.getPitch());
		}

		for (Player player : toRemove)
		{
			disable(player);
		}
	}

	@EventHandler
	public void updateParticles(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player player : getActiveMounts().keySet())
		{
			Entity entity = getActiveMounts().get(player).getEntity();

			UtilParticle.PlayParticleToAll(ParticleType.FLAME, entity.getLocation().add(0, 1.4, 0), 0.2F, 0.2F, 0.2F, 0.05F, 4, ViewDist.NORMAL);
		}
	}
}