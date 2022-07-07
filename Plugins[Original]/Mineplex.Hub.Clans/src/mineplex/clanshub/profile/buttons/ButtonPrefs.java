package mineplex.clanshub.profile.buttons;

import mineplex.clanshub.profile.gui.GUIProfile;
import mineplex.core.common.util.C;
import mineplex.core.gui.GuiItem;
import mineplex.core.itemstack.ItemBuilder;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Button to open preferences menu
 */
public class ButtonPrefs implements GuiItem
{
	private GUIProfile _profile;
	private Player _player;

	public ButtonPrefs(GUIProfile profile, Player player)
	{
		_profile = profile;
		_player = player;
	}

	@Override
	public void click(ClickType clickType)
	{
		_profile.getPrefManager().openMenu(_player);
	}

	@Override
	public ItemStack getObject()
	{
		return new ItemBuilder(Material.REDSTONE_COMPARATOR).setTitle(C.Reset + C.cYellow + "Preferences").addLore(new String[]
			{
					"",
					C.cWhite + "Set your preferences to your liking",
					C.cWhite + "so you can enjoy the game more!",

					"",
					C.cWhite + "Type " + C.cGreen + "/prefs" + C.cWhite + " to access this anywhere!"
			}).build();
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