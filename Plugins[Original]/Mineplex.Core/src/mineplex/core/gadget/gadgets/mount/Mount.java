package mineplex.core.gadget.gadgets.mount;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public abstract class Mount<T extends MountData> extends Gadget
{

	protected final Map<Player, T> _active = new HashMap<>();

	protected boolean BouncyCollisions = false;

	public Mount(GadgetManager manager, String name, String[] description, int cost, Material material, byte displayData)
	{
		super(manager, GadgetType.MOUNT, name, description, cost, material, displayData);
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		player.leaveVehicle();
		player.eject();
		Manager.removeGadgetType(player, getGadgetType(), this);
		T data = spawnMount(player);
		_active.put(player, data);

		for (Entity entity : data.getEntityParts())
		{
			UtilEnt.addFlag(entity, UtilEnt.FLAG_ENTITY_COMPONENT);
		}

		if (message)
		{
			UtilPlayer.message(player, F.main(Manager.getName(), "You spawned " + F.elem(getName()) + "."));
		}
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		Manager.removeActive(player, this);
		MountData data = _active.remove(player);

		for (Entity entity : data.getEntityParts())
		{
			entity.remove();
		}

		if (message)
		{
			UtilPlayer.message(player, F.main(Manager.getName(), "You despawned " + F.elem(getName()) + "."));
		}
	}

	@Override
	public boolean isActive(Player player)
	{
		return _active.containsKey(player);
	}

	public abstract T spawnMount(Player player);

	@EventHandler
	public void updateBounce(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !BouncyCollisions)
		{
			return;
		}

		//Collide
		for (T data : getActiveMounts().values())
		{
			List<Entity> parts = data.getEntityParts();

			if (parts == null || parts.isEmpty())
			{
				continue;
			}

			Entity head = parts.get(0);

			if (head == null || !(head.getPassenger() instanceof Player))
			{
				continue;
			}

			Player player = (Player) head.getPassenger();

			if (!Recharge.Instance.usable(player, getName() + " Collide"))
			{
				continue;
			}

			for (T otherData : getActiveMounts().values())
			{
				List<Entity> otherParts = otherData.getEntityParts();

				if (otherParts == null || otherParts.isEmpty())
				{
					continue;
				}

				Entity otherHead = otherParts.get(0);

				if (otherHead.equals(head) || otherHead.getPassenger() == null || !(otherHead.getPassenger() instanceof Player))
				{
					continue;
				}

				Player otherPlayer = (Player) otherHead.getPassenger();

				if (!Recharge.Instance.usable(otherPlayer, getName() + " Collide"))
				{
					continue;
				}

				//Collide
				if (UtilMath.offsetSquared(head, otherHead) > 4)
				{
					continue;
				}

				Recharge.Instance.useForce(player, getName() + " Collide", 500);
				Recharge.Instance.useForce(otherPlayer, getName() + " Collide", 500);

				Vector direction = UtilAlg.getTrajectory(otherHead, head);

				for (Entity part : parts)
				{
					UtilAction.velocity(part, direction, 1.2, false, 0, 0.8, 10, true);
				}

				direction.setX(-direction.getX());
				direction.setZ(-direction.getZ());

				for (Entity part : otherParts)
				{
					UtilAction.velocity(part, direction, 1.2, false, 0, 0.8, 10, true);
				}

				otherHead.getWorld().playSound(otherHead.getLocation(), Sound.SLIME_WALK, 1f, 0.75f);
			}
		}
	}

	@EventHandler
	public void interactMount(PlayerInteractEntityEvent event)
	{
		Player player = event.getPlayer();
		Entity clicked = event.getRightClicked();
		T data = getMountData(clicked);

		if (data == null)
		{
			return;
		}

		event.setCancelled(true);

		List<Entity> parts = data.getEntityParts();
		boolean first = true;

		for (Entity entity : parts)
		{
			if (entity.equals(clicked) && shouldRide(player, data, first))
			{
				player.leaveVehicle();
				player.eject();

				setPassenger(player, clicked, event);
				return;
			}

			first = false;
		}

		player.sendMessage(F.main(Manager.getName(), "This is not your mount!"));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void target(EntityDamageEvent event)
	{
		for (T data : _active.values())
		{
			if (data.isPartOfMount(event.getEntity()))
			{
				event.setCancelled(true);
			}
		}
	}

	protected boolean shouldRide(Player player, T data, boolean head)
	{
		return data.ownsMount(player);
	}

	protected void setPassenger(Player player, Entity clicked, PlayerInteractEntityEvent event)
	{
		clicked.setPassenger(player);
	}

	protected T getMountData(Entity ent)
	{
		for (T data : getActiveMounts().values())
		{
			if (data.isPartOfMount(ent))
			{
				return data;
			}
		}
		return null;
	}

	public final Map<Player, T> getActiveMounts()
	{
		return _active;
	}
}
