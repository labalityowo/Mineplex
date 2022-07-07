package mineplex.game.clans.clans.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.account.CoreClientManager;
import mineplex.core.account.event.OnlinePrimaryGroupUpdateEvent;
import mineplex.core.donation.DonationManager;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.PlayerScoreboard;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.ScoreboardData;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.ScoreboardManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.Clans;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.scoreboard.elements.ScoreboardElementClan;
import mineplex.game.clans.clans.scoreboard.elements.ScoreboardElementPlayer;
import mineplex.game.clans.clans.war.WarManager;
import mineplex.game.clans.clans.worldevent.WorldEventManager;
import mineplex.game.clans.tutorial.TutorialManager;

public class ClansScoreboardManager extends ScoreboardManager
{
	private ClansManager _clansManager;
	private WarManager _warManager;
	private WorldEventManager _worldEvent;
	private TutorialManager _tutorial;

	public ClansScoreboardManager(JavaPlugin plugin, ClansManager clansManager, WarManager warManager, WorldEventManager worldEvent, TutorialManager tutorial, CoreClientManager clientManager, DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, clansManager.getIncognitoManager());
		
		_clansManager = clansManager;
		_warManager = warManager;
		_worldEvent = worldEvent;
		_tutorial = tutorial;
		
		init();
	}
	
	private void init()
	{
		setTitle("Clans " + Clans.getMap());
		
		ScoreboardData data = getData("default", true);

		data.writeElement(new ScoreboardElementClan(_clansManager));
		data.writeElement(new ScoreboardElementPlayer(_clansManager));
//		data.writeElement(new ScoreboardElementPlayerCount(_clansManager));

		data.writeElement(_warManager);
		data.writeElement(_worldEvent);
		data.writeElement(_tutorial);

//		for (Tutorial tutorial : TutorialManager.Instance.getTutorials().values())
//		{
//			data.writeElement(tutorial);
//		}
	}
	
	@EventHandler
	public void onGamemodeChanged(PlayerGameModeChangeEvent event)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(_clansManager.getPlugin(), () ->
		{
			for (Player player : Bukkit.getOnlinePlayers())
			{
				refresh(player);
			}
		}, 20);
	}
	
	@EventHandler
	public void onRankUpdate(OnlinePrimaryGroupUpdateEvent event)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(_clansManager.getPlugin(), () ->
		{
			for (Player player : Bukkit.getOnlinePlayers())
			{
				refresh(player);
			}
		}, 20);
	}
	
	@EventHandler
	public void drawUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.FASTER) draw();
	}
	
	@Override
	protected PlayerScoreboard createScoreboard(Player player)
	{
		return new ClansPlayerScoreboard(this, _clansManager, player);
	}
	
	public void refresh(ClanInfo clanInfo)
	{
		for (Player player : clanInfo.getOnlinePlayers())
		{
			refresh(player);
		}
	}
	
	public void refresh(Player player)
	{
		((ClansPlayerScoreboard) getCurrentScoreboard(player)).refreshTeams(player);
	}
}