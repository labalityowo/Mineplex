package mineplex.core.bonuses.gui.buttons;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.gui.GuiItem;
import mineplex.core.reward.RewardData;

public class RewardButton implements GuiItem
{
	private RewardData _data;

	public RewardButton(RewardData data)
	{
		_data = data;
	}

	@Override
	public void setup()
	{

	}

	@Override
	public void close()
	{

	}

	@Override
	public void click(ClickType clickType)
	{
		// Do nothing
	}

	@Override
	public ItemStack getObject()
	{
		ItemStack stack = _data.getDisplayItem();
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(_data.getFriendlyName());
		stack.setItemMeta(meta);
		return stack;
	}
}
