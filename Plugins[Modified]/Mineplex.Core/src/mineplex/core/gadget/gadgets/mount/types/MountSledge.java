package mineplex.core.gadget.gadgets.mount.types;

import java.time.Month;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.mount.Mount;
import mineplex.core.gadget.gadgets.mount.MountData;
import mineplex.core.gadget.gadgets.mount.types.MountSledge.MountSledgeData;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MountSledge extends Mount<MountSledgeData>
{

	private static final ItemStack FRONT = new ItemStack(Material.SPRUCE_WOOD_STAIRS);
	private static final ItemStack BACK = new ItemStack(Material.WOOD_STEP, 1, (short) 0, (byte) 1);

	public MountSledge(GadgetManager manager)
	{
		super(manager, "Sledge Mount",
				UtilText.splitLineToArray(C.cGray + "A classic snow sledge crafted from the finest trees.", LineFormat.LORE),
				CostConstants.POWERPLAY_BONUS, Material.SPRUCE_WOOD_STAIRS, (byte) 0);

		setPPCYearMonth(YearMonth.of(2017, Month.DECEMBER));
	}

	@Override
	public MountSledgeData spawnMount(Player player)
	{
		Location location = player.getLocation().subtract(0, 0.7, 0);
		ArmorStand front = player.getWorld().spawn(location, ArmorStand.class);
		ArmorStand back = player.getWorld().spawn(location, ArmorStand.class);

		front.setVisible(false);
		front.setGravity(false);
		back.setVisible(false);
		back.setGravity(false);

		front.getEquipment().setHelmet(FRONT);
		back.getEquipment().setHelmet(BACK);

		back.setPassenger(player);

		return new MountSledgeData(player, front, back);
	}

	@EventHandler
	public void updateMovement(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Set<Player> toRemove = new HashSet<>();

		for (MountSledgeData data : _active.values())
		{
			if (data.getOwner().getVehicle() == null)
			{
				toRemove.add(data.getOwner());
				continue;
			}

			ArmorStand front = data.Front;
			ArmorStand back = data.Back;

			Location playerLocation = data.getOwner().getLocation();
			playerLocation.setPitch(0);
			Vector direction = playerLocation.getDirection();

			if (UtilBlock.fullSolid(playerLocation.clone().add(direction).getBlock()))
			{
				continue;
			}

			direction.multiply(0.5);

			Location newLocation = back.getLocation().add(direction);
			((CraftEntity) back).getHandle().setPositionRotation(newLocation.getX(), newLocation.getY(), newLocation.getZ(), playerLocation.getYaw(), 0);

			newLocation.add(direction.multiply(1.6));
			newLocation.setYaw(playerLocation.getYaw());
			front.teleport(newLocation);

			UtilParticle.PlayParticleToAll(ParticleType.SNOW_SHOVEL, playerLocation, 0.3F, 0, 0.3F, 0.1F, 2, ViewDist.NORMAL);
		}

		toRemove.forEach(this::disable);
	}

	class MountSledgeData extends MountData
	{

		final ArmorStand Front;
		final ArmorStand Back;

		MountSledgeData(Player player, ArmorStand front, ArmorStand back)
		{
			super(player);

			Front = front;
			Back = back;
		}

		@Override
		public List<Entity> getEntityParts()
		{
			return Arrays.asList(Front, Back);
		}
	}
}
