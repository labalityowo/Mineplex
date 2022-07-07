package mineplex.core.gui;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class SimpleGuiItem extends ItemStack implements GuiItem {

	public SimpleGuiItem(Material type, int amount, short damage)
	{
		super(type, amount, damage);
	}

	public SimpleGuiItem(Material type, int amount)
	{
		super(type, amount);
	}

	public SimpleGuiItem(Material type)
	{
		super(type);
	}

	public SimpleGuiItem(ItemStack itemStack)
	{
		super(itemStack);
	}

	@Override
	public void click(ClickType clickType)
	{
	}

	@Override
	public ItemStack getObject()
	{
		return this;
	}

	@Override
	public void setup()
	{
	}

	@Override
	public void close()
	{
	}

}
