package mineplex.core.communities.gui.buttons;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.Callback;
import mineplex.core.communities.gui.buttons.CommunitiesGUIButton;

public class ActionButton extends CommunitiesGUIButton
{
	private Callback<ClickType> _onClick;
	
	public ActionButton(ItemStack icon, Callback<ClickType> onClick)
	{
		super(icon);
		
		_onClick = onClick;
	}

	@Override
	public void update() {}

	@Override
	public void handleClick(ClickType type)
	{
		_onClick.run(type);
	}
}