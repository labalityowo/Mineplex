package nautilus.game.arcade.game.games.survivalgames.kit.necroinventory;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilItem;
import mineplex.core.lifetimes.ListenerComponent;

import nautilus.game.arcade.kit.perks.PerkSkeletons;

public class NecroInventoryMenu extends ListenerComponent
{
	private final Map<Player, NecroInventoryPage> _pageMap;
	private final PerkSkeletons _perk;

	public NecroInventoryMenu(PerkSkeletons perk)
	{
		_pageMap = new WeakHashMap<>();
		_perk = perk;
	}

	public void openInventory(Player player, Skeleton minion)
	{
		NecroInventoryPage inventoryPage = new NecroInventoryPage(player, minion);

		_pageMap.put(player, inventoryPage);

		inventoryPage.openInventory();
	}

	private boolean shouldCallInteract(InventoryInteractEvent event)
	{
		if (!(event.getWhoClicked() instanceof Player))
		{
			return false;
		}

		if (!_pageMap.containsKey(event.getWhoClicked()))
		{
			return false;
		}

		NecroInventoryPage inventoryPage = _pageMap.get(event.getWhoClicked());

		return inventoryPage.matchesInventory(event.getInventory());
	}

	@EventHandler
	public void skeletonClick(PlayerInteractEntityEvent event)
	{
		if (event.getPlayer() == null || event.getRightClicked() == null)
		{
			return;
		}

		if (!(event.getRightClicked() instanceof Skeleton))
		{
			return;
		}

		if (!_perk.hasPerk(event.getPlayer()))
		{
			return;
		}

		Skeleton skeleton = (Skeleton) event.getRightClicked();

		List<Skeleton> skeletons = _perk.getSkeletons(event.getPlayer());

		if (skeletons == null || !skeletons.contains(skeleton))
		{
			return;
		}

		ItemStack itemInHand = event.getPlayer().getItemInHand();

		// Don't open the skeleton inventory if it's a weapon/fishing rod
		if (UtilItem.isWeapon(itemInHand)
				|| itemInHand.getType() == Material.FISHING_ROD
				|| itemInHand.getType() == Material.BOW
				|| itemInHand.getType() == Material.EGG
				|| itemInHand.getType() == Material.SNOW_BALL)
		{
			event.getPlayer().sendMessage(F.main("Game", C.mBody + "You can't open a minion inventory with that item in your hand."));
			return;
		}

		event.setCancelled(true);
		openInventory(event.getPlayer(), skeleton);
	}

	@Override
	public void deactivate()
	{
		super.deactivate();
		_pageMap.values().forEach(NecroInventoryPage::cleanup);
		_pageMap.clear();
	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent event)
	{
		if (!shouldCallInteract(event))
		{
			return;
		}

		NecroInventoryPage inventoryPage = _pageMap.get(event.getWhoClicked());

		inventoryPage.inventoryClick(event);
	}

	@EventHandler
	public void inventoryDrag(InventoryDragEvent event)
	{
		if (!shouldCallInteract(event))
		{
			return;
		}

		NecroInventoryPage inventoryPage = _pageMap.get(event.getWhoClicked());

		inventoryPage.inventoryDrag(event);
	}

	@EventHandler
	public void inventoryClose(InventoryCloseEvent event)
	{
		if (event.getPlayer() == null || !(event.getPlayer() instanceof Player))
		{
			return;
		}

		if (!_pageMap.containsKey(event.getPlayer()))
		{
			return;
		}

		NecroInventoryPage inventoryPage = _pageMap.get(event.getPlayer());

		if (!inventoryPage.matchesInventory(event.getInventory()))
		{
			return;
		}

		_pageMap.remove(event.getPlayer());
		inventoryPage.cleanup();
	}
}