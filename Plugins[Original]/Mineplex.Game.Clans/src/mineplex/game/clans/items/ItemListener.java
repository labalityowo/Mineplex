package mineplex.game.clans.items;

import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilServer;
import mineplex.game.clans.clans.invsee.InvseeModifyOnlineInventoryEvent;
import mineplex.game.clans.items.attributes.AttributeContainer;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

/**
 * Listens for item-related trigger events and accordingly triggers appropriate
 * {@link PlayerGear} events for {@link CustomItem} abilities and attributes.
 *
 * @author MrTwiggy
 */
public class ItemListener implements Listener, Runnable
{
	private static final String PROJECTILE_META_TAG = "[CustomGearProj]";

	private JavaPlugin _plugin;

	public ItemListener(JavaPlugin plugin)
	{
		_plugin = plugin;
		_plugin.getServer().getScheduler().runTaskTimer(_plugin, this, 0, 20 * 60 * 5);
	}

	@Override
	public void run()
	{
		for (Player player : UtilServer.getPlayersCollection())
		{
			save(player, false);
		}
		GearManager.cleanup();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void on(PlayerQuitEvent event)
	{
		save(event.getPlayer(), true);
	}

	private void save(Player player, boolean remove)
	{
		for (ItemStack item : UtilInv.getItemsUncloned(player))
		{
			GearManager.save(item, remove);
		}
	}

	/**
	 * Handle the trigger of custom gear related effects and abilities.
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerAttack(CustomDamageEvent event)
	{
		if (event.IsCancelled()) return;    // Checks for Pre-Cancelled event and stops

		Player damager = event.GetDamagerPlayer(false);    // For non-ranged attacks
		Player damagee = event.GetDamageePlayer();
		Projectile projectile = event.GetProjectile();

		// Trigger custom gear effects for attacker melee weapons
		if (damager != null && event.GetCause() == DamageCause.ENTITY_ATTACK)
		{
			PlayerGear attackerGear = getGear(damager);
			attackerGear.onAttack(event);
		}

		// Trigger custom gear effects for defender armor
		if (damagee != null)
		{
			PlayerGear defenderGear = getGear(damagee);
			defenderGear.onAttacked(event);
		}

		// Trigger bow-related attribute effects properly
		if (projectile != null)
		{
			if (projectile.hasMetadata(PROJECTILE_META_TAG))
			{
				for (MetadataValue data : projectile.getMetadata(PROJECTILE_META_TAG))
				{
					AttributeContainer container = (AttributeContainer) data.value();

					for (ItemAttribute attribute : container.getAttributes())
					{
						attribute.onAttack(event);
					}
				}
			}
		}
	}

	/**
	 * Properly marks projectiles shot from a custom-gear bow so that it will properly trigger events.
	 *
	 * @param event
	 */
	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();
			PlayerGear gear = getGear(player);

			CustomItem weapon = gear.getWeapon();

			if (weapon != null)
			{
				// Copy weapon attributes onto projectile for later processing				
				AttributeContainer attributes = weapon.getAttributes();

				Entity projectile = event.getProjectile();
				projectile.setMetadata(PROJECTILE_META_TAG, new FixedMetadataValue(_plugin, attributes));
			}
		}
	}

	/**
	 * Handle weapon ability activation of custom gear.
	 *
	 * @param event
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		// Activate weapon interact abilities
		PlayerGear playerGear = getGear(event.getPlayer());
		playerGear.onInteract(event);
	}
	
	//Handle player gear caching
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onDrop(PlayerDropItemEvent event)
	{
		GearManager.getInstance().runSyncLater(() -> getGear(event.getPlayer()).updateCache(true), 1);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPickup(PlayerPickupItemEvent event)
	{
		GearManager.getInstance().runSyncLater(() -> getGear(event.getPlayer()).updateCache(true), 1);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onClick(InventoryClickEvent event)
	{
		if (Player.class.isInstance(event.getWhoClicked()))
		{
			GearManager.getInstance().runSyncLater(() -> getGear((Player)event.getWhoClicked()).updateCache(true), 1);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onJoin(PlayerJoinEvent event)
	{
		GearManager.getInstance().runSyncLater(() ->
		{
			if (event.getPlayer().isOnline())
			{
				getGear(event.getPlayer()).updateCache(true);
			}
		}, 5);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(PlayerRespawnEvent event)
	{
		GearManager.getInstance().runSyncLater(() -> getGear(event.getPlayer()).updateCache(true), 1);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onHold(PlayerItemHeldEvent event)
	{
		GearManager.getInstance().runSyncLater(() -> getGear(event.getPlayer()).updateCache(false), 1);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInvsee(InvseeModifyOnlineInventoryEvent event)
	{
		GearManager.getInstance().runSyncLater(() -> getGear(event.getModified()).updateCache(true), 1);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onEquipArmor(PlayerInteractEvent event)
	{
		if (UtilEvent.isAction(event, ActionType.R))
		{
			Player player = event.getPlayer();
			
			if (UtilItem.isHelmet(event.getItem()) && player.getInventory().getHelmet() == null)
			{
				GearManager.getInstance().runSyncLater(() -> getGear(player).updateCache(true), 1);
			}
			else if (UtilItem.isChestplate(event.getItem()) && player.getInventory().getChestplate() == null)
			{
				GearManager.getInstance().runSyncLater(() -> getGear(player).updateCache(true), 1);
			}
			else if (UtilItem.isLeggings(event.getItem()) && player.getInventory().getLeggings() == null)
			{
				GearManager.getInstance().runSyncLater(() -> getGear(player).updateCache(true), 1);
			}
			else if (UtilItem.isBoots(event.getItem()) && player.getInventory().getBoots() == null)
			{
				GearManager.getInstance().runSyncLater(() -> getGear(player).updateCache(true), 1);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onUseItemFrame(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked() instanceof ItemFrame)
		{
			boolean frameHas = ((ItemFrame)event.getRightClicked()).getItem() != null;
			boolean playerHas = event.getPlayer().getItemInHand() != null;
			
			if (frameHas || playerHas)
			{
				GearManager.getInstance().runSyncLater(() -> getGear(event.getPlayer()).updateCache(true), 1);
				return;
			}
		}
	}

	/**
	 * @param player - the player whose gear is to be fetched
	 * @return the {@link PlayerGear} associated with {@code player}.
	 */
	private PlayerGear getGear(Player player)
	{
		return GearManager.getInstance().getPlayerGear(player);
	}
}