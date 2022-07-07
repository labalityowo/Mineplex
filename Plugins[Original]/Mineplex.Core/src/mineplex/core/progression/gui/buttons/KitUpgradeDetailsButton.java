package mineplex.core.progression.gui.buttons;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.IconButton;
import mineplex.core.progression.ProgressiveKit;
import mineplex.core.progression.math.Calculations;

/**
 * Representing this kits Abilities in a GUI
 */
public class KitUpgradeDetailsButton extends IconButton
{

	private ItemStack _item;

	public KitUpgradeDetailsButton(ProgressiveKit kit, Player player, int level, List<String> details)
	{
		super(null);
		
		int current = kit.getUpgradeLevel(player.getUniqueId());
		
		ItemBuilder builder = new ItemBuilder(Material.STAINED_CLAY).setData((current >= level) ? (short)5 : (short)14);

		builder.setTitle(C.cYellow + "Level " + level + " upgrades");

		List<String> lore = Lists.newArrayList(" ");

		for (String detail : details)
		{
			lore.addAll(UtilText.splitLine(detail, LineFormat.LORE));
		}

		lore.add("");
		if (kit.usesXp())
		{
			lore.add(C.cGray + "Unlocks at kit level " + C.cGreen + Calculations.getLevelRequiredFor(level));
		}
		else
		{
			String currency = C.cGreen + "%cost% Gems";
			if (kit.crownsEnabled())
			{
				currency = C.cGold + "%cost% Crowns";
			}
			lore.add(C.cGray + "Unlocks with " + currency.replace("%cost%", Calculations.getGemsCostXpLess(level) + ""));
		}
		builder.setLore(lore.toArray(new String[lore.size()]));

		_item = builder.build();
	}

	@Override
	public ItemStack getItemStack()
	{
		return _item;
	}

	@Override
	public void setItemStack(ItemStack item)
	{
		_item = item;
	}

}
