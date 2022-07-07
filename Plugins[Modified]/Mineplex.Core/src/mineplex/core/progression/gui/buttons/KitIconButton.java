package mineplex.core.progression.gui.buttons;

import com.google.common.collect.Lists;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.progression.ProgressiveKit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * This is the display icon for the Kit inside of the main GUI
 */
public class KitIconButton extends KitButton
{

	private ItemStack _itemStack;

	public KitIconButton(ProgressiveKit kit, Player player)
	{
		super(kit, null);

		ItemBuilder builder = new ItemBuilder(kit.getIcon());

		builder.setTitle(C.cYellow + kit.getDisplayName());

		List<String> lore = Lists.newArrayList(" ");

		boolean foundPerkStart = false;
		for (String s : kit.getDescription())
		{
			if (!foundPerkStart)
			{
				lore.add(ChatColor.GRAY + s);
				if (s.equalsIgnoreCase(" ") || s.isEmpty() || s.equalsIgnoreCase(""))
				{
					foundPerkStart = true;
				}
			} else
			{
				lore.add(ChatColor.WHITE + s);
			}
		}

		builder.setLore(lore.toArray(new String[lore.size()]));

		if (kit.isSelected(player.getUniqueId()))
		{
			builder.addEnchantment(UtilInv.getDullEnchantment(), 1);
		}

		_itemStack = builder.build();
	}

	@Override
	public ItemStack getItemStack()
	{
		return _itemStack;
	}

	@Override
	public void setItemStack(ItemStack itemStack)
	{
		_itemStack = itemStack;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		getKit().onSelected(player.getUniqueId());
	}
}
