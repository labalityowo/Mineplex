package mineplex.hub.plugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.UtilServer;
import mineplex.core.newnpc.NewNPCManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.hub.HubManager;
import mineplex.hub.hubgame.HubGame;
import mineplex.hub.hubgame.HubGameManager;
import mineplex.hub.hubgame.duel.Duels;
import mineplex.hub.hubgame.tron.Tron;
import mineplex.hub.news.NewsManager;

/**
 * Hub Plugins are effectively managers for specific events within the hub. Such as a Halloween or Christmas variants of the hub.
 * HubPlugin means that lots of
 * <pre>
 *     if (HubPlugin == HubPlugin.SomeHubVariant)
 * </pre>
 *
 * checks aren't needed within the entire hub, making it cleaner and easier to maintain.
 */
public class HubPlugin extends MiniPlugin
{

	private static final PotionEffect NIGHT_VISION = new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false);

	protected final HubManager _manager;
	protected final HubGameManager _hubGameManager;
	protected final NewsManager _newsManager;
	protected final NewNPCManager _npcManager;

	public HubPlugin()
	{
		this("Normal");
	}

	public HubPlugin(String moduleName)
	{
		super(moduleName);

		_manager = require(HubManager.class);
		_hubGameManager = require(HubGameManager.class);
		_newsManager = require(NewsManager.class);
		_npcManager = require(NewNPCManager.class);

		setupWorld();
		addHubGames();
	}

	protected void setupWorld()
	{
		_manager.GetSpawn().getWorld().setTime(6000);
	}

	protected void addHubGames()
	{
		addHubGame(new Tron(_hubGameManager));
		addHubGame(new Duels(_hubGameManager));
	}

	protected final void addHubGame(HubGame game)
	{
		_hubGameManager.addGame(game);
	}

	@EventHandler
	public void playerNightVision(PlayerJoinEvent event)
	{
		event.getPlayer().addPotionEffect(NIGHT_VISION);
	}

	@EventHandler
	public void playerNightVision(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
		{
			return;
		}

		for (Player player : UtilServer.getPlayersCollection())
		{
			if (!player.hasPotionEffect(PotionEffectType.NIGHT_VISION))
			{
				player.addPotionEffect(NIGHT_VISION);
			}
		}
	}

}
