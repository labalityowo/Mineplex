package mineplex.core.gadget.gadgets.mount.types;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.mount.Mount;
import mineplex.core.gadget.gadgets.mount.SingleEntityMountData;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MountCart extends Mount<SingleEntityMountData<Minecart>>
{

	public MountCart(GadgetManager manager)
	{
		super(manager,
				"Minecart",
				UtilText.splitLineToArray(C.cGray + "Cruise around town in your shiny new Minecart RX Turbo!", LineFormat.LORE),
				15000,
				Material.MINECART,
				(byte)0
		);
		
		BouncyCollisions = true;
	}

	@Override
	public SingleEntityMountData<Minecart> spawnMount(Player player)
	{
		return new SingleEntityMountData<>(player, player.getWorld().spawn(player.getLocation().add(0, 2, 0), Minecart.class));
	}

	@EventHandler
	public void updateMovement(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		//Bounce
		for (SingleEntityMountData<Minecart> cartData : getActiveMounts().values())
		{
			Minecart cart = cartData.getEntity();

			if (cart.getPassenger() == null || !(cart.getPassenger() instanceof Player) || !UtilEnt.isGrounded(cart) || !Recharge.Instance.usable(cartData.getOwner(), getName() + " Collide"))
			{
				continue;
			}

			Player player = (Player) cart.getPassenger();
			UtilAction.velocity(cart, UtilAlg.getTrajectory2d(player.getEyeLocation(), UtilPlayer.getTargetLocation(player, 5)), 1.4, true, 0, 0, 1, false);

			if (Math.random() > 0.9)
			{
				cart.getWorld().playSound(cart.getLocation(), Sound.MINECART_BASE, 0.05f, 2f);
			}
		}
	}

	@EventHandler
	public void vehicleEnter(VehicleEnterEvent event)
	{
		if (event.getEntered() instanceof Player)
		{
			return;
		}

		for (SingleEntityMountData<Minecart> data : getActiveMounts().values())
		{
			if (data.getEntity().equals(event.getVehicle()))
			{
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void cancelBreak(VehicleDamageEvent event)
	{
		for (SingleEntityMountData<Minecart> data : getActiveMounts().values())
		{
			if (data.getEntity().equals(event.getVehicle()))
			{
				event.setCancelled(true);
				return;
			}
		}
	}
}
