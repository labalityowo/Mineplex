package mineplex.gemhunters.death.quitnpc;

import mineplex.core.Managers;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilGameProfile;
import mineplex.gemhunters.death.event.QuitNPCDespawnEvent;
import mineplex.gemhunters.economy.EconomyModule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

public class QuitNPC implements Listener
{
	// Managers
	private final DisguiseManager _disguise;

	// Time
	private final long _start;
	private final long _quitMills;

	// Entity
	private final Skeleton _entity;
	private final ArmorStand _hologram;

	// Player
	private final String _name;
	private final UUID _uuid;
	private final PlayerInventory _inventory;

	private final int _gems;

	public QuitNPC(Player player, long quitMills)
	{
		// Managers
		_disguise = Managers.get(DisguiseManager.class);

		// Time
		_start = System.currentTimeMillis();
		_quitMills = quitMills;

		// Entity
		_entity = player.getWorld().spawn(player.getLocation(), Skeleton.class);
		_entity.setHealth(player.getHealth());
		_entity.setMaxHealth(player.getMaxHealth());
		_entity.getEquipment().setArmorContents(player.getInventory().getArmorContents());
		UtilEnt.vegetate(_entity, true);

		_hologram = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
		_hologram.setCustomNameVisible(true);
		_hologram.setVisible(false);
		_hologram.setSmall(true);

		_entity.setPassenger(_hologram);

		// Disguise
		DisguisePlayer disguise = new DisguisePlayer(_entity, UtilGameProfile.getGameProfile(player));
		_disguise.disguise(disguise);

		// Player
		_name = player.getName();
		_uuid = player.getUniqueId();
		_inventory = player.getInventory();
		_gems = Managers.get(EconomyModule.class).Get(player);

		UtilServer.RegisterEvents(this);
	}

	public void despawn(boolean pluginRemove)
	{
		QuitNPCDespawnEvent event = new QuitNPCDespawnEvent(this, pluginRemove);

		UtilServer.CallEvent(event);

		if (event.isCancelled())
		{
			return;
		}

		_entity.remove();
		_hologram.remove();

		UtilServer.Unregister(this);
	}

	public void dropItems()
	{
		Location location = _entity.getLocation().add(0, 1, 0);

		for (ItemStack itemStack : _inventory.getContents())
		{
			if (itemStack == null || itemStack.getType() == Material.AIR)
			{
				continue;
			}

			location.getWorld().dropItemNaturally(location, itemStack);
		}

		for (ItemStack itemStack : _inventory.getArmorContents())
		{
			if (itemStack == null || itemStack.getType() == Material.AIR)
			{
				continue;
			}

			location.getWorld().dropItemNaturally(location, itemStack);
		}
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		if (UtilTime.elapsed(_start, _quitMills))
		{
			despawn(true);
		}
		else
		{
			_hologram.setCustomName("Quitting in " + UtilTime.MakeStr(_start + _quitMills - System.currentTimeMillis()));
		}
	}

	@EventHandler
	public void entityDeath(EntityDeathEvent event)
	{
		if (!_entity.equals(event.getEntity()))
		{
			return;
		}

		Player killer = event.getEntity().getKiller();

		if (killer != null)
		{
			Managers.get(EconomyModule.class).addToStore(killer, "Killing " + F.name(_name + "'s") + " NPC", (int) (_gems * EconomyModule.GEM_KILL_FACTOR));
		}

		event.getDrops().clear();
		_entity.setHealth(_entity.getMaxHealth());
		dropItems();
		despawn(false);
	}

	@EventHandler
	public void entityCombust(EntityCombustEvent event)
	{
		if (!_entity.equals(event.getEntity()))
		{
			return;
		}

		event.setCancelled(true);
	}

	public String getName()
	{
		return _name;
	}

	public UUID getUniqueId()
	{
		return _uuid;
	}
}