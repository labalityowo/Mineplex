package mineplex.core.gui.botton;

import mineplex.core.gui.GuiInventory;
import mineplex.core.gui.SimpleGuiItem;
import mineplex.core.itemstack.ItemStackFactory;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class BackBotton extends SimpleGuiItem {
	
	private GuiInventory _gui;
	
	public BackBotton(GuiInventory gui) {
		this(ItemStackFactory.Instance.CreateStack(Material.BED, (byte) 0, 1, ChatColor.DARK_GRAY + "<-- Go Back"), gui);
	}
	
	public BackBotton(ItemStack itemStack, GuiInventory gui)
	{
		super(itemStack);
		this._gui = gui;
	}
	
	@Override
	public void click(ClickType clickType)
	{
		getGui().openInventory();
	}
	
	public GuiInventory getGui()
	{
		return _gui;
	}
}
