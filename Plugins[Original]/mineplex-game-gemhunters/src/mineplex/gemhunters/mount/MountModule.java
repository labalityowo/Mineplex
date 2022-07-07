package mineplex.gemhunters.mount;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;

import mineplex.core.MiniClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseInsentient;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.GadgetEnableEvent;
import mineplex.core.gadget.gadgets.gamemodifiers.gemhunters.GemHuntersMountGadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.game.GameDisplay;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilVariant;
import mineplex.gemhunters.loot.LootItem;
import mineplex.gemhunters.loot.LootModule;
import mineplex.gemhunters.mount.command.MountSkinsCommand;
import mineplex.gemhunters.mount.command.SpawnMountCommand;

@ReflectivelyCreateMiniPlugin
public class MountModule extends MiniClientPlugin<MountData>
{

	public enum Perm implements Permission
	{

		MOUNT_SKINS_COMMAND,
		SPAWN_MOUNT_COMMAND
	}

	private static final ItemStack SADDLE = new ItemStack(Material.SADDLE);
	private static final int HEALTH = 40;
	private static final int MAX_DIST = 25 * 25;
	private static final int MIN_DIST = 4 * 4;

	private final CosmeticManager _cosmetic;
	private final DisguiseManager _disguise;
	private final LootModule _loot;
	private final GadgetManager _gadget;

	private MountModule()
	{
		super("Mount");

		_cosmetic = require(CosmeticManager.class);
		_disguise = require(DisguiseManager.class);
		_loot = require(LootModule.class);
		_gadget = require(GadgetManager.class);

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.PLAYER.setPermission(Perm.MOUNT_SKINS_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.SPAWN_MOUNT_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new MountSkinsCommand(this));
		addCommand(new SpawnMountCommand(this));
	}

	@Override
	protected MountData addPlayer(UUID uuid)
	{
		return null;
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		Set(player, new MountData(player));
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		LootItem lootItem = _loot.fromItemStack(player.getItemInHand());

		if (lootItem == null || lootItem.getMetadata() == null)
		{
			return;
		}

		String metadata = lootItem.getMetadata();
		String[] split = metadata.split(" ");

		if (split.length < 2 || !split[0].equals("MOUNT"))
		{
			return;
		}

		String cooldownString = split[1];
		long cooldown;

		try
		{
			cooldown = TimeUnit.MINUTES.toMillis(Integer.parseInt(cooldownString));
		}
		catch (IllegalArgumentException e)
		{
			return;
		}

		if (!Recharge.Instance.usable(player, getName(), true) || !Recharge.Instance.use(player, "Mount Interact", 250, false, false))
		{
			return;
		}

		MountData data = Get(player);

		if (data.getEntity() != null)
		{
			player.sendMessage(F.main(_moduleName, "You already have an active mount."));
			return;
		}

		spawnHorse(player, data, lootItem.getItemStack(), cooldown);
	}

	public void spawnHorse(Player player, MountData data, ItemStack itemStack, long cooldown)
	{
		Location location = player.getLocation().add(0, 1, 0);
		GemHuntersMountGadget gadget = (GemHuntersMountGadget) _gadget.getGameCosmeticManager().getActiveCosmetic(
				player,
				GameDisplay.GemHunters,
				"Mount Skins"
		);

		Horse horse;

		if (gadget == null)
		{
			horse = UtilVariant.spawnHorse(location, Variant.HORSE);
			horse.setColor(Color.BROWN);
			horse.setStyle(Style.NONE);
		}
		else
		{
			horse = gadget.getType().spawn(location, _disguise);
		}

		String name = player.getName();
		horse.setCustomName(name + "'" + (name.charAt(name.length() - 1) == 's' ? "" : "s") + " Mount");
		horse.setCustomNameVisible(true);
		horse.setJumpStrength(1);
		horse.getInventory().setSaddle(SADDLE);
		horse.getInventory().setArmor(new ItemStack(itemStack.getType()));
		horse.setDomestication(1);
		horse.setMaxDomestication(1);
		horse.setOwner(player);
		horse.setTamed(true);
		horse.setCarryingChest(true);
		horse.setMaxHealth(HEALTH);
		horse.setHealth(HEALTH);
		UtilEnt.vegetate(horse);

		DisguiseBase disguise = _disguise.getActiveDisguise(horse);

		if (disguise != null && disguise instanceof DisguiseInsentient)
		{
			((DisguiseInsentient) disguise).setName(name);
		}

		data.onSpawn(horse, cooldown, itemStack);
		player.sendMessage(F.main(_moduleName, "You spawned your " + F.name(getName()) + "."));
	}

	@EventHandler
	public void horseDeath(EntityDeathEvent event)
	{
		if (!(event.getEntity() instanceof Horse))
		{
			return;
		}

		Horse horse = (Horse) event.getEntity();

		for (MountData data : GetValues())
		{
			if (data.getEntity() == null || !horse.equals(data.getEntity()))
			{
				continue;
			}

			event.getDrops().clear();
			event.setDroppedExp(0);
			Recharge.Instance.use(data.getPlayer(), getName(), data.getCooldown(), true, true);
			Recharge.Instance.Get(data.getPlayer()).get(getName()).Item = data.getItem();
			return;
		}
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();

		for (MountData data : GetValues())
		{
			if (!player.equals(data.getPlayer()) || data.getEntity() == null)
			{
				continue;
			}

			data.getEntity().remove();
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		for (MountData data : GetValues())
		{
			if (!player.equals(data.getPlayer()) || data.getEntity() == null)
			{
				continue;
			}

			data.getEntity().remove();
			return;
		}
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		for (MountData data : GetValues())
		{
			Player player = data.getPlayer();
			Horse horse = data.getEntity();

			if (horse == null)
			{
				continue;
			}
			else if (horse.isDead() || !horse.isValid())
			{
				data.onRemove();
			}

			double offset = UtilMath.offsetSquared(player, horse);

			if (offset > MAX_DIST)
			{
				horse.teleport(player);
			}
			else if (offset > MIN_DIST)
			{
				UtilEnt.CreatureMove(horse, player.getLocation(), 2);
			}
		}
	}

	@EventHandler
	public void horseInteract(PlayerInteractEntityEvent event)
	{
		if (!(event.getRightClicked() instanceof Horse))
		{
			return;
		}

		Player player = event.getPlayer();
		Horse horse = (Horse) event.getRightClicked();

		for (MountData data : GetValues())
		{
			if (player.equals(data.getPlayer()) || !horse.equals(data.getEntity()))
			{
				continue;
			}

			player.sendMessage(F.main(_moduleName, "This is not your mount."));
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void horseDamage(EntityDamageByEntityEvent event)
	{
		Entity damager = event.getDamager();
		Entity damaged = event.getEntity();

		if (!(damager instanceof Player && damaged instanceof Horse))
		{
			return;
		}

		for (MountData data : GetValues())
		{
			if (!damager.equals(data.getPlayer()) || !damaged.equals(data.getEntity()))
			{
				continue;
			}

			event.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void horseInventoryClick(InventoryClickEvent event)
	{
		if (event.getClickedInventory() != null && event.getClickedInventory() instanceof HorseInventory)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void updateSkins(UpdateEvent event)
	{
		for (Player player : UtilServer.getPlayersCollection())
		{
			MountData data = Get(player);

			if (data.getEntity() == null)
			{
				return;
			}

			GemHuntersMountGadget gadget = (GemHuntersMountGadget) _gadget.getGameCosmeticManager().getActiveCosmetic(
					player,
					GameDisplay.GemHunters,
					"Mount Skins"
			);

			if (gadget == null)
			{
				continue;
			}

			gadget.getType().onUpdate(event, data.getEntity());
		}
	}

	@EventHandler
	public void gadgetEquip(GadgetEnableEvent event)
	{
		if (event.getGadget().getGadgetType() != GadgetType.GAME_MODIFIER)
		{
			event.setCancelled(true);
		}
	}

	public CosmeticManager getCosmeticManager()
	{
		return _cosmetic;
	}

	public GadgetManager getGadgetManager()
	{
		return _gadget;
	}
}
