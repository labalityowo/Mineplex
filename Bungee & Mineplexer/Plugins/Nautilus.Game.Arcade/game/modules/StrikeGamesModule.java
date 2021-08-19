package nautilus.game.arcade.game.modules;

import mineplex.core.common.util.UtilItem;
import nautilus.game.arcade.game.games.minestrike.GunModule;
import nautilus.game.arcade.game.games.minestrike.items.grenades.FlashBang;
import nautilus.game.arcade.game.games.minestrike.items.grenades.Grenade;
import nautilus.game.arcade.game.games.minestrike.items.grenades.HighExplosive;
import nautilus.game.arcade.game.games.minestrike.items.grenades.Incendiary;
import nautilus.game.arcade.game.games.minestrike.items.grenades.Molotov;
import nautilus.game.arcade.game.games.minestrike.items.grenades.Smoke;
import nautilus.game.arcade.game.games.minestrike.items.guns.Gun;
import nautilus.game.arcade.game.games.minestrike.items.guns.GunStats;
import nautilus.game.arcade.game.games.minestrike.items.guns.GunType;
import nautilus.game.arcade.game.games.minestrike.items.guns.Shotgun;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class StrikeGamesModule extends Module
{

	private final Map<Player, ItemStack> _helmets;
	private final GunModule _gunModule;

	public StrikeGamesModule(GunModule gunModule)
	{
		_gunModule = gunModule;
		_helmets = new HashMap<>();
	}

	@EventHandler
	public void eatingGrenades(PlayerItemConsumeEvent event)
	{
		Material material = event.getItem().getType();

		switch (material)
		{
			case POTATO_ITEM:
			case CARROT_ITEM:
			case APPLE:
			case PORK:
			case GRILLED_PORK:
				event.setCancelled(true);
				break;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void addHelmet(PlayerToggleSneakEvent event)
	{
		Player player = event.getPlayer();
		ItemStack helmet = player.getInventory().getHelmet();

		if (!getGame().IsLive() || !getGame().IsAlive(player) || _gunModule.getScoped().containsKey(player) || helmet == null)
		{
			return;
		}

		_helmets.put(event.getPlayer(), helmet);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void pumpkinDrop(PlayerDeathEvent event)
	{
		Player player = event.getEntity();

		event.getDrops().removeIf(item -> item.getType() == Material.PUMPKIN || item.getType() == Material.PUMPKIN_STEM);

		if (_helmets.containsKey(player))
		{
			event.getDrops().add(_helmets.get(player));
		}
	}

	@EventHandler
	public void weaponEnchantment(EnchantItemEvent event)
	{
		if (!UtilItem.isArmor(event.getItem()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void addEquipment(InventoryClickEvent event)
	{
		if (event.getCurrentItem() == null || !(event.getWhoClicked() instanceof Player) || event.getClickedInventory() instanceof PlayerInventory)
		{
			return;
		}

		Player player = (Player) event.getWhoClicked();
		ItemStack stack = event.getCurrentItem();
		for (GunStats stat : GunStats.values())
		{
			if (stat.getSkin() == stack.getType())
			{
				Gun gun;
				if (stat.getGunType() == GunType.SHOTGUN)
				{
					gun = new Shotgun(stat, _gunModule);
				}
				else
				{
					gun = new Gun(stat, _gunModule);
				}
				gun.setStack(stack);
				gun.updateWeaponName(player, null, false);
				gun.addID();
				_gunModule.registerGun(gun, player);
				return;
			}
		}

		Grenade grenade = null;

		switch (stack.getType())
		{
			case APPLE:
				grenade = new HighExplosive();
				break;
			case CARROT_ITEM:
				grenade = new FlashBang();
				break;
			case POTATO_ITEM:
				grenade = new Smoke();
				break;
			case PORK:
				grenade = new Incendiary();
				break;
			case GRILLED_PORK:
				grenade = new Molotov();
				break;
		}

		if (grenade == null)
		{
			return;
		}

		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(grenade.getName());
		stack.setItemMeta(meta);
		grenade.setStack(stack);
		_gunModule.registerGrenade(grenade, player);
	}

	@EventHandler
	public void triggerPickup(PlayerPickupItemEvent event)
	{
		if (!getGame().InProgress() || !getGame().IsAlive(event.getPlayer()))
		{
			return;
		}

		//Guns
		Gun gun = _gunModule.getDroppedGuns().get(event.getItem());
		if (gun != null)
		{
			_gunModule.deregisterDroppedGun(gun);
			_gunModule.registerGun(gun, event.getPlayer());
			gun.setStack(event.getItem().getItemStack());
		}

		//Grenades
		Grenade grenade = _gunModule.getDroppedGrenades().get(event.getItem());
		if (grenade != null)
		{
			_gunModule.deregisterDroppedGrenade(grenade);
			_gunModule.registerGrenade(grenade, event.getPlayer());
			grenade.setStack(event.getItem().getItemStack());
		}
	}

	@EventHandler
	public void triggerDrop(PlayerDropItemEvent event)
	{
		if (!getGame().InProgress())
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = event.getItemDrop().getItemStack();

		//Guns
		Gun gun = _gunModule.getGunInHand(player, itemStack);

		if (gun != null)
		{
			gun.drop(_gunModule, player, false, false);
			event.getItemDrop().remove();
			player.setItemInHand(null);
			return;
		}

		//Grenades
		Grenade grenade = _gunModule.getGrenadeInHand(player, itemStack);
		if (grenade != null)
		{
			grenade.drop(_gunModule, player, false, false);
			event.getItemDrop().remove();
			player.setItemInHand(null);
		}
	}
}
