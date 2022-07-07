package nautilus.game.arcade.kit.perks;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilItem;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.kit.Perk;

public class PerkItemBreakdown extends Perk
{
	private static final Random random = new Random();

	// 90 seconds
	private static final long ABILITY_COOLDOWN = 5000;

	public PerkItemBreakdown()
	{
		super("Item Breakdown", new String[] {
				C.cYellow + "Left Click " + C.mBody + "a crafting table",
				"with weapons, armor, and some food",
				"to scavenge it for parts!",
				"",
				"You will also receive some XP",
				"when you break items down."
		});
	}

	@EventHandler
	public void onWorkbenchClick(PlayerInteractEvent event)
	{
		if (event.getPlayer() == null)
		{
			return;
		}

		if (event.getAction() != Action.LEFT_CLICK_BLOCK)
		{
			return;
		}

		if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.WORKBENCH)
		{
			return;
		}

		Player player = event.getPlayer();

		if (!hasPerk(player))
		{
			return;
		}

		ItemStack item = player.getItemInHand();

		if (item == null || item.getType() == Material.AIR)
		{
			return;
		}

		int maxReturn;
		if (UtilItem.isHelmet(item))
		{
			maxReturn = 5;
		}
		else if (UtilItem.isChestplate(item))
		{
			maxReturn = 8;
		}
		else if (UtilItem.isLeggings(item))
		{
			maxReturn = 7;
		}
		else if (UtilItem.isBoots(item))
		{
			maxReturn = 4;
		}
		else if (UtilItem.isSword(item))
		{
			maxReturn = 2;
		}
		else if (UtilItem.isAxe(item))
		{
			maxReturn = 3;
		}
		else if (item.getType() == Material.BOW)
		{
			// for the string that makes the bow
			maxReturn = 3;
		}
		else if (item.getType() == Material.FISHING_ROD)
		{
			maxReturn = 2;
		}
		else if (item.getType() == Material.RAW_CHICKEN || item.getType() == Material.COOKED_CHICKEN)
		{
			maxReturn = 2;
		}
		else if (item.getType() == Material.RAW_BEEF || item.getType() == Material.COOKED_BEEF)
		{
			maxReturn = 2;
		}
		else
		{
			if (Recharge.Instance.usable(player, GetName()))
			{
				player.sendMessage(F.main(GetName(), "You can't break that item down."));
			}
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), ABILITY_COOLDOWN, true, false))
		{
			return;
		}

		// Multiply max return by the percent of durability remaining to get the new max return,
		// but it can't be less than 1.
		// Turns out SG does not have durability, but keep this in case we ever add it.
		//maxReturn = Math.min(1, (int) Math.floor(maxReturn * ((item.getType().getMaxDurability() - item.getDurability()) / item.getType().getMaxDurability())));

		int returnAmount = random.nextInt(maxReturn) + 1;

		Material returnMat;

		if (item.getType() == Material.BOW || item.getType() == Material.FISHING_ROD)
		{
			returnMat = Material.STRING;
		}
		else if (UtilItem.isDiamondProduct(item))
		{
			returnMat = Material.DIAMOND;
		}
		else if (UtilItem.isIronProduct(item))
		{
			returnMat = Material.IRON_INGOT;
		}
		else if (UtilItem.isChainmailProduct(item))
		{
			// 50% chance to be 0 or 1
			returnMat = random.nextInt(2) == 1 ? Material.IRON_INGOT : Material.GOLD_INGOT;

			returnAmount = Math.floorDiv(returnAmount, 2);
		}
		else if (UtilItem.isGoldProduct(item))
		{
			returnMat = Material.GOLD_INGOT;
		}
		else if (UtilItem.isStoneProduct(item))
		{
			returnMat = Material.FLINT;
		}
		else if (UtilItem.isLeatherProduct(item))
		{
			returnMat = Material.LEATHER;
		}
		else if (UtilItem.isWoodProduct(item))
		{
			returnMat = Material.STICK;
		}
		else if (item.getType() == Material.RAW_CHICKEN || item.getType() == Material.COOKED_CHICKEN)
		{
			returnMat = Material.FEATHER;
		}
		else if (item.getType() == Material.RAW_BEEF || item.getType() == Material.COOKED_BEEF)
		{
			returnMat = Material.LEATHER;
		}
		else
		{
			player.sendMessage(F.main(GetName(), "Something went wrong, couldn't break down your item."));
			return;
		}

		ItemStack returningItem = new ItemStack(returnMat, Math.max(returnAmount, 1));

		if (item.getAmount() == 1)
		{
			player.setItemInHand(new ItemStack(Material.AIR));
		}
		else
		{
			item.setAmount(item.getAmount() - 1);
		}

		// Give up to 9 XP
		player.giveExp(random.nextInt(10));

		player.getInventory().addItem(returningItem);

		player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1F, 1F);

		// "You broke your Diamond Sword into 3 Diamonds!"
		player.sendMessage(F.main(GetName(),
				"You broke your "
						+ F.elem(ItemStackFactory.Instance.GetName(item, true))
						+ C.mBody + " into "
						+ F.elem(returnAmount
									+ " "
									+ ItemStackFactory.Instance.GetName(returnMat, (byte) 0, true))
						+ C.mBody + "!"));
	}
}
