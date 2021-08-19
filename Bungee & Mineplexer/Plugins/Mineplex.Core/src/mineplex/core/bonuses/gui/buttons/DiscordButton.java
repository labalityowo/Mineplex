package mineplex.core.bonuses.gui.buttons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gui.GuiItem;
import mineplex.core.itemstack.ItemBuilder;

public class DiscordButton implements GuiItem
{
	private static final ItemStack ICON = new ItemBuilder(Material.STAINED_GLASS)
			// Cyan
			.setData((short) 9)
			.setTitle(C.cGreen + C.Bold + "Visit our Discord Server")
			.addLore(
					C.cWhite + "Check out our official Discord server where",
					C.cWhite + "you can find news, changelogs and giveaways,",
					C.cWhite + "give direct feedback to our admins, and more!",
					" ",
					C.cGreen + "Click to visit our Discord!"
			)
			.build();
	
	private static final String URL = "http://discord.mineplex.com";
	private final Player _player;

	public DiscordButton(Player player)
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
		UtilPlayer.message(_player, "");
		new JsonMessage("      " + C.Bold + "Click to Open in Web Browser").click(ClickEvent.OPEN_URL, URL).sendToPlayer(_player);
		new JsonMessage("      " + C.cGreen + C.Line + URL).click(ClickEvent.OPEN_URL, URL).sendToPlayer(_player);
		UtilPlayer.message(_player, "");
		UtilPlayer.message(_player, C.cGold + C.Bold + C.Strike + "=============================================");
	}

	@Override
	public ItemStack getObject()
	{
		return ICON;
	}
}