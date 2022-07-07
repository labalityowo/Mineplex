package mineplex.core.bonuses.gui.buttons;

import mineplex.core.Managers;
import mineplex.core.bonuses.BonusManager;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gui.GuiItem;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.youtube.YoutubeManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class YoutubeButton implements GuiItem
{
	private static final ItemStack DISABLED_ICON = new ItemBuilder(Material.APPLE)
			.setTitle(C.cGreen + C.Bold + "Visit us on YouTube")
			.addLore(
					C.cWhite + "Come back tomorrow for your",
					C.cWhite + "Daily Reward!",
					" ",
					C.cWhite + "Check out the latest Video",
					C.cWhite + "on the MineplexGames Channel!",
					" ",
					C.cWhite + "Be sure and Subscribe so you",
					C.cWhite + "don't miss a video!",
					" ",
					C.cGreen + "Click to visit us on YouTube!"
			)
			.build();
	private static final ItemStack ENABLED_ICON = new ItemBuilder(Material.APPLE)
			.setTitle(C.cGreen + C.Bold + "Visit us on YouTube")
			.addLore(
					C.cYellow + "Claim your Daily Reward",
					C.cWhite + "by checking out the latest Video",
					C.cWhite + "on the MineplexGames Channel!",
					C.cWhite + " ",
					C.cWhite + "Be sure and Subscribe so you",
					C.cWhite + "don't miss a video!",
					" ",
					C.cGreen + "Click to visit us on YouTube!"
			)
			.build();
	private final Player _player;
	private final YoutubeManager _youtubeManager;

	public YoutubeButton(Player player, YoutubeManager youtubeManager)
	{
		this._player = player;
		this._youtubeManager = youtubeManager;
	}

	@Override
	public void setup() {}

	@Override
	public void close() {}

	@Override
	public void click(ClickType clickType)
	{
		if (!Recharge.Instance.use(_player, "Use Youtube Button", 1000, false, false))
		{
			return;
		}
		
		_player.closeInventory();
		
		_player.playSound(_player.getLocation(), Sound.NOTE_PLING, 1, 1.6f);
		
		final String message;
		if (_youtubeManager.canYoutube(_player))
		{
			message = "claim YouTube Prize!";
			_youtubeManager.attemptYoutube(_player, Managers.get(BonusManager.class).ClansBonus, Managers.get(BonusManager.class).getClansHomeServer(_player).getRight());
		}
		else
		{
			message = "visit our YouTube page!";
		}
		
		UtilPlayer.message(_player, C.cGold + C.Bold + C.Strike + "=============================================");
		UtilPlayer.message(_player, "");

		new JsonMessage("      " + C.Bold + "Click to Open in Web Browser and " + message).click(ClickEvent.OPEN_URL, "http://file.mineplex.com/ads.php").sendToPlayer(_player);
		new JsonMessage( "      " + C.cGreen + C.Line + "http://youtube.com/mineplexgamesofficial").click(ClickEvent.OPEN_URL, "http://file.mineplex.com/ads.php").sendToPlayer(_player);
		UtilPlayer.message(_player, "");
		UtilPlayer.message(_player, C.cGold + C.Bold + C.Strike + "=============================================");
	}

	@Override
	public ItemStack getObject()
	{
		return _youtubeManager.canYoutube(_player) ? ENABLED_ICON : DISABLED_ICON;
	}
}