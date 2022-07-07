package mineplex.core.bonuses.gui.buttons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.Managers;
import mineplex.core.bonuses.BonusManager;
import mineplex.core.common.Pair;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gui.GuiItem;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.youtube.YoutubeManager;

public class SpecificChannelButton implements GuiItem
{
	private final Player _player;
	private final YoutubeManager _youtubeManager;
	private Pair<String, String> _channel;
	private ItemStack _item;

	public SpecificChannelButton(Player player, YoutubeManager youtubeManager)
	{
		_player = player;
		_youtubeManager = youtubeManager;
	}

	@Override
	public void setup()
	{
		_channel = Pair.create("", "");
		if (_youtubeManager.canSpecificYoutube(_player))
		{
			_item = new ItemBuilder(Material.APPLE)
			.setTitle(C.cGreen + C.Bold + "Visit our featured creator " + C.cGoldB + _channel.getLeft())
			.addLore(
					C.cYellow + "Claim a Daily Reward",
					C.cWhite + "by checking out the latest Video",
					C.cWhite + "by " + C.cGold + _channel.getLeft() + C.cWhite + "!",
					C.cWhite + " ",
					C.cWhite + "Be sure and Subscribe if you",
					C.cWhite + "enjoy their videos!",
					" ",
					C.cGreen + "Click to visit " + C.cGold + _channel.getLeft() + C.cGreen + " on YouTube!"
			)
			.build();
		}
		else
		{
			_item = new ItemBuilder(Material.APPLE)
			.setTitle(C.cGreen + C.Bold + "Visit our featured creator " + C.cGoldB + _channel.getLeft())
			.addLore(
					C.cWhite + "Come back tomorrow for your",
					C.cWhite + "Daily Reward!",
					" ",
					C.cWhite + "Check out the latest Video",
					C.cWhite + "by " + C.cGold + _channel.getLeft() + C.cWhite + "!",
					" ",
					C.cWhite + "Be sure and Subscribe if you",
					C.cWhite + "enjoy their videos!",
					" ",
					C.cGreen + "Click to visit " + C.cGold + _channel.getLeft() + C.cGreen + " on YouTube!"
			)
			.build();
		}
	}

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
		if (_youtubeManager.canSpecificYoutube(_player))
		{
			message = "claim your Reward!";
			_youtubeManager.attemptSpecificYoutube(_player, Managers.get(BonusManager.class).ClansBonus, Managers.get(BonusManager.class).getClansHomeServer(_player).getRight());
		}
		else
		{
			message = "visit " + _channel.getLeft() + " on YouTube!";
		}
		
		UtilPlayer.message(_player, C.cGold + C.Bold + C.Strike + "=============================================");
		UtilPlayer.message(_player, "");

		new JsonMessage("      " + C.Bold + "Click to Open in Web Browser and " + message).click(ClickEvent.OPEN_URL, _channel.getRight()).sendToPlayer(_player);
		new JsonMessage( "      " + C.cGreen + C.Line + _channel.getRight().replace("?sub_confirmation=1", "")).click(ClickEvent.OPEN_URL, _channel.getRight()).sendToPlayer(_player);
		UtilPlayer.message(_player, "");
		UtilPlayer.message(_player, C.cGold + C.Bold + C.Strike + "=============================================");
	}

	@Override
	public ItemStack getObject()
	{
		return _item;
	}
}
