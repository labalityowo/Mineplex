package mineplex.mavericks.review;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.common.util.UtilServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * A simple manager for formating the chat
 */
public class SimpleChatManager extends MiniPlugin
{	
	private CoreClientManager _coreClientManager;
	private AchievementManager _achievementManager;

	public SimpleChatManager(JavaPlugin plugin, CoreClientManager coreClientManager, AchievementManager achivementManager)
	{
		super("Chat Format Manager", plugin);
		_coreClientManager = coreClientManager;
		_achievementManager = achivementManager;
	}
	
	@EventHandler
	public void PlayerChat(AsyncPlayerChatEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		Player player = event.getPlayer();
		String playerName = player.getName();

		//Level Prefix
		String levelStr = _achievementManager.getMineplexLevel(player);
		
		PermissionGroup group = _coreClientManager.Get(player).getRealOrDisguisedPrimaryGroup();

		//Rank Prefix
		String rankStr = "";
		if (!group.getDisplay(false, false, false, false).isEmpty())
		{
			rankStr = group.getDisplay(true, true, true, false) + " ";
		}
		
		TextComponent rankComponent = new TextComponent(rankStr);
		TextComponent playerNameText = new TextComponent(ChatColor.YELLOW + playerName);
		TextComponent component = new TextComponent();

		rankComponent.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(group.getDisplay(true, true, true, true) + ChatColor.WHITE + "\n" + group.getDescription()).create()));

		component.setText(levelStr);
		component.addExtra(rankComponent);
		component.addExtra(playerNameText);
		component.addExtra(" " + ChatColor.WHITE + event.getMessage());

//			JsonMessage jsonMessage = new JsonMessage(levelStr)
//					.extra(JSONObject.escape(rankStr)).hover("show_text", rank.getColor() + rank.getTag(true, true) + ChatColor.WHITE + "\n" + rank.getDescription())
//					.add(JSONObject.escape(C.cYellow + playerName + " " + ChatColor.WHITE + event.getMessage()));

		for (Player other : UtilServer.getPlayers())
		{

			// event.setMessage(event.getMessage());
			// event.setFormat(levelStr + rankStr + C.cYellow + playerName + " " + C.cWhite + "%2$s");
			if (!event.isCancelled())
			{
				other.spigot().sendMessage(component);
			}
		}
		Bukkit.getConsoleSender().sendMessage(component.toLegacyText());
		event.setCancelled(true);
	}
}