package mineplex.clanshub.profile.buttons;

import mineplex.clanshub.profile.gui.GUIProfile;
import mineplex.core.common.util.C;
import mineplex.core.gui.GuiItem;
import mineplex.core.itemstack.ItemStackFactory;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * Button to open stats menu
 */
public class ButtonStats implements GuiItem
{
	private GUIProfile _profile;
	private Player _player;

	public ButtonStats(GUIProfile profile, Player player)
	{
		_profile = profile;
		_player = player;
	}

	@Override
	public void click(ClickType clickType)
	{
		_profile.getAchievementManager().openShop(_player);
	}

	@Override
	public ItemStack getObject()
	{
		ItemStack item = ItemStackFactory.Instance.CreateStack(Material.SKULL_ITEM, (byte) 3, 1, 
				ChatColor.RESET + C.cYellow + "Stats and Achievements", 
				new String[] 
						{
			"",
			C.cWhite + "View your Statistics and Achievements",
			C.cWhite + "for all of the games on Mineplex!",
			
			"",
			C.cWhite + "Type " + C.cGreen + "/stats" + C.cWhite + " to access this anywhere!"
						});
		SkullMeta meta = ((SkullMeta) item.getItemMeta());
		meta.setOwner(_player.getName());
		item.setItemMeta(meta);
		return item;
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