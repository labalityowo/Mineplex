package mineplex.gemhunters.util;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilServer;
import mineplex.core.monitor.LagMeter;
import mineplex.core.slack.SlackAPI;
import mineplex.core.slack.SlackMessage;
import mineplex.core.slack.SlackTeam;
import mineplex.gemhunters.loot.rewards.LootItemReward;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

public class SlackRewardBot
{

	private static final DecimalFormat FORMAT = new DecimalFormat("0.0");
	private static final String SLACK_CHANNEL_NAME = "#gem-hunters-logging";
	private static final String SLACK_USERNAME = "Gem Hunters";
	private static final String SLACK_ICON = "http://moppletop.github.io/mineplex/chest-image.png";

	private static LagMeter _lag;

	public static void logReward(Player player, LootItemReward reward, String status)
	{
		if (_lag == null)
		{
			_lag = Managers.get(LagMeter.class);
		}

		try
		{
			SlackAPI.getInstance().sendMessage(SlackTeam.DEVELOPER, SLACK_CHANNEL_NAME, new SlackMessage(SLACK_USERNAME, new URL(SLACK_ICON),
									"Rewarding a " + reward.getClass().getSimpleName() +
									"\nName: " + ChatColor.stripColor(reward.getItemStack().getItemMeta().getDisplayName()) +
									"\nPlayer: " + player.getName() +
									"\nStatus: *" + status + "*" +
									"\nServer: " + UtilServer.getServerName() + " " + UtilServer.getRegion().toString() +
									"\nTPS: " + FORMAT.format(_lag.getTicksPerSecond())),
					true);
		}
		catch (MalformedURLException e) {}
	}
}