package mineplex.core.bonuses.gui.buttons;

import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gui.GuiItem;
import mineplex.core.itemstack.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class SocialMediaButton implements GuiItem
{
	private static final ItemStack ICON = new ItemBuilder(Material.LAPIS_BLOCK)
			.setTitle(C.cGreen + C.Bold + "Visit our Social Media")
			.addLore(
					C.cWhite + "Be sure to \"Like\" us on",
					C.cWhite + "Facebook for Giveaways, Announcements,",
					C.cWhite + "and Much More!",
					" ",
					C.cWhite + "Check out and follow Mineplex on",
					C.cWhite + "Twitter for Giveaways, Announcements,",
					C.cWhite + "Teasers, and Tips!",
					" ",
					C.cGreen + "Click to visit us on Facebook and Twitter!"
			)
			.build();
	
	private final Player _player;

	public SocialMediaButton(Player player)
	{
		_player = player;
	}

	@Override
	public void setup() {}

	@Override
	public void close() {}

	@Override
	public void click(ClickType clickType)
	{
		_player.closeInventory();
		
		_player.playSound(_player.getLocation(), Sound.NOTE_PLING, 1, 1.6f);
		
		UtilPlayer.message(_player, C.cGold + C.Bold + C.Strike + "=============================================");
		// FB
		UtilPlayer.message(_player, "");
		new JsonMessage("      " + C.Bold + "Click to Open in Web Browser").click(ClickEvent.OPEN_URL, "https://www.facebook.com/MineplexGames").sendToPlayer(_player);
		new JsonMessage("      " + C.cGreen + C.Line + "https://www.facebook.com/MineplexGames").click(ClickEvent.OPEN_URL, "https://www.facebook.com/MineplexGames").sendToPlayer(_player);
		UtilPlayer.message(_player, "");
		// Twitter
		UtilPlayer.message(_player, "");
		new JsonMessage("      " + C.Bold + "Click to Open in Web Browser").click(ClickEvent.OPEN_URL, "https://www.twitter.com/Mineplex").sendToPlayer(_player);
		new JsonMessage("      " + C.cGreen + C.Line + "https://www.twitter.com/Mineplex").click(ClickEvent.OPEN_URL, "https://www.twitter.com/Mineplex").sendToPlayer(_player);
		UtilPlayer.message(_player, "");

		UtilPlayer.message(_player, C.cGold + C.Bold + C.Strike + "=============================================");
	}

	@Override
	public ItemStack getObject()
	{
		return ICON;
	}
}