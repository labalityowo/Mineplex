package mineplex.core.gadget.types;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.disguise.disguises.DisguiseAgeable;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseZombie;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.balloons.BalloonEntity;
import mineplex.core.gadget.gadgets.balloons.BalloonType;

public class BalloonGadget extends Gadget
{

	private static final int MAX_BALLOONS = 3;

	static
	{
		try
		{
			UtilEnt.registerEntityType(BalloonEntity.class, EntityType.SLIME, "Balloon");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private final BalloonType _balloonType;
	private final Map<Player, BalloonEntity> _data;

	public BalloonGadget(GadgetManager manager, BalloonType type)
	{
		super(manager, GadgetType.BALLOON, type.getName() + " Balloon", type.getDescription(), type.getCost(), type.getDisplayMaterial(), type.getDisplayData());

		_balloonType = type;
		_data = new HashMap<>();
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		long active = Manager.getGadgets(getGadgetType())
				.stream()
				.filter(gadget -> gadget.isActive(player))
				.count();

		if (active >= MAX_BALLOONS)
		{
			Manager.removeActive(player, this);
			player.sendMessage(F.main(Manager.getName(), "You already have the maximum amount of balloons active!"));
			return;
		}

		_active.add(player);

		Location location = player.getLocation();
		ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);

		stand.setGravity(false);
		UtilEnt.ghost(stand, true, false);
		UtilEnt.addFlag(stand, UtilEnt.FLAG_NO_REMOVE);
		UtilEnt.addFlag(stand, UtilEnt.FLAG_ENTITY_COMPONENT);

		BalloonEntity balloonEntity = BalloonEntity.spawn(stand, player, _balloonType.getClazz() != null);

		Slime slime = (Slime) balloonEntity.getBukkitEntity();
		slime.setSize(-1);

		UtilEnt.vegetate(slime, true);
		UtilEnt.ghost(slime, true, true);
		UtilEnt.addFlag(slime, UtilEnt.FLAG_NO_REMOVE);
		UtilEnt.addFlag(slime, UtilEnt.FLAG_ENTITY_COMPONENT);

		if (_balloonType.getClazz() == null)
		{
			stand.setVisible(false);
			stand.setHelmet(_balloonType.getHelmet());
		}
		else
		{
			try
			{
				DisguiseBase disguise = _balloonType.getClazz().getConstructor(Entity.class).newInstance(stand);

				if (disguise instanceof DisguiseAgeable)
				{
					((DisguiseAgeable) disguise).setBaby();
				}
				else if (disguise instanceof DisguiseZombie)
				{
					((DisguiseZombie) disguise).setBaby(true);
				}

				Manager.getDisguiseManager().disguise(disguise);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		_data.put(player, balloonEntity);

		if (message)
		{
			player.sendMessage(F.main(Manager.getName(), "You spawned " + F.vowelAN(getName()) + " " + F.name(getName()) + "."));
		}
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		BalloonEntity data = _data.remove(player);

		if (data == null)
		{
			return;
		}

		data.remove();
		_active.remove(player);

		if (message)
		{
			player.sendMessage(F.main(Manager.getName(), "You despawned " + F.vowelAN(getName()) + " " + F.name(getName()) + "."));
		}
	}

	@EventHandler
	public void interactEntity(PlayerInteractEntityEvent event)
	{
		for (BalloonEntity entity : _data.values())
		{
			if (entity.isPartOfEntity(event.getRightClicked()))
			{
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void teleport(PlayerTeleportEvent event)
	{
		if (event.getCause() == TeleportCause.PLUGIN)
		{
			disable(event.getPlayer(), false);
		}
	}

	public BalloonType getBalloonType()
	{
		return _balloonType;
	}
}
