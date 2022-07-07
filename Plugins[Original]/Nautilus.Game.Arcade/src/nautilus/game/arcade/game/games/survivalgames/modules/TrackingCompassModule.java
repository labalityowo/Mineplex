package nautilus.game.arcade.game.games.survivalgames.modules;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.modules.Module;

public class TrackingCompassModule extends Module
{

	@EventHandler
	public void interactCompass(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (UtilPlayer.isSpectator(player) || itemStack == null || itemStack.getType() != Material.COMPASS)
		{
			return;
		}

		int uses = getUses(itemStack);

		if (uses == 0)
		{
			player.sendMessage(F.main("Game", "The compass breaks! No remaining uses!"));
			player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 1);
			player.setItemInHand(null);
			return;
		}
		else if (!Recharge.Instance.use(player, "Compass", 500, true, true))
		{
			return;
		}

		GameTeam team = getGame().GetTeam(player);

		for (Player nearby : UtilPlayer.getNearby(player.getLocation(), 256))
		{
			if (player.equals(nearby) || getGame().TeamMode && team.HasPlayer(nearby))
			{
				continue;
			}

			player.setCompassTarget(nearby.getLocation());
			player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
			player.setItemInHand(getCompass(--uses));
			player.sendMessage(F.main("Game", F.name(nearby.getName()) + " is " + F.count((int) UtilMath.offset(player, nearby)) + " blocks away. Your compass has " + F.count(uses) + " use" + (uses == 1 ? "": "s") + " left."));
			return;
		}
	}

	@EventHandler
	public void combineCompasses(InventoryClickEvent event)
	{
		Player player = (Player) event.getWhoClicked();
		ItemStack cursor = event.getCursor();
		ItemStack currentItem = event.getCurrentItem();

		if (UtilPlayer.isSpectator(player) || cursor == null || currentItem == null || cursor.getType() != Material.COMPASS || currentItem.getType() != Material.COMPASS)
		{
			return;
		}

		int a = getUses(cursor), b = getUses(currentItem);

		event.setCursor(getCompass(a + b));
		event.setCurrentItem(null);
		player.sendMessage(F.main("Game", "You combined two compasses."));
		player.playSound(player.getLocation(), Sound.ANVIL_USE, 1, 1);
	}

	private int getUses(ItemStack itemStack)
	{
		if (!itemStack.hasItemMeta())
		{
			return 0;
		}

		ItemMeta itemMeta = itemStack.getItemMeta();
		List<String> lore = itemMeta.getLore();

		if (lore.isEmpty())
		{
			return 0;
		}

		return Integer.parseInt(ChatColor.stripColor(lore.get(0)).replaceAll("\\D+", ""));
	}

	public ItemStack getCompass(int uses)
	{
		return new ItemBuilder(Material.COMPASS)
				.setTitle(C.cYellowB + "Tracking Compass")
				.addLore("Uses " + C.cYellow + uses, "Use this to find the location and", "distance of the nearest player!", "Click on another compass in your inventory to", "combine them!", "Id: " + C.cRed + UtilMath.r(1000))
				.build();
	}
}
