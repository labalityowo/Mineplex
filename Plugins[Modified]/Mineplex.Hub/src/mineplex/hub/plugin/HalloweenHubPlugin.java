package mineplex.hub.plugin;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.titles.tracks.custom.ScrollAnimation;
import mineplex.core.updater.UpdateType;
import mineplex.core.utils.UtilScheduler;

public class HalloweenHubPlugin extends HubPlugin
{

	private final String[] NEWS_TEXT = new ScrollAnimation("HAPPY HALLOWEEN")
			.withPrimaryColour(ChatColor.GOLD)
			.withSecondaryColour(ChatColor.WHITE)
			.withTertiaryColour(ChatColor.YELLOW)
			.bold()
			.build();

	private int _newsIndex;

	public HalloweenHubPlugin()
	{
		super("Halloween");

		_newsManager.setEnabled(false);
		UtilScheduler.runEvery(UpdateType.FASTEST, this::displayNews);
	}

	@Override
	protected void setupWorld()
	{
		_manager.GetSpawn().getWorld().setTime(18000);
	}

	private void displayNews()
	{
		if (++_newsIndex == NEWS_TEXT.length)
		{
			_newsIndex = 0;
		}

		UtilTextTop.display(NEWS_TEXT[_newsIndex], UtilServer.getPlayers());
	}
}
