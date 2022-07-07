package mineplex.hub.modules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.preferences.Preference;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.visibility.VisibilityManager;
import mineplex.hub.HubManager;

@ReflectivelyCreateMiniPlugin
public class HubVisibilityManager extends MiniPlugin
{

	private static final int HIDE_SPAWN_RADIUS_SQUARED = 4;

	private final HubManager _manager;
	private final VisibilityManager _visibilityManager;

	private final Set<Player> _hiddenPlayers = new HashSet<>();

	public HubVisibilityManager()
	{
		super("Hub Visibility");

		_manager = require(HubManager.class);
		_visibilityManager = require(VisibilityManager.class);
	}

	public void addHiddenPlayer(Player player)
	{
		_hiddenPlayers.add(player);
	}

	public void removeHiddenPlayer(Player player)
	{
		_hiddenPlayers.remove(player);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		_hiddenPlayers.remove(player);
	}

	@EventHandler
	public void updateVisibility0(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		Collection<? extends Player> online = UtilServer.getPlayersCollection();

		for (Player subject : online)
		{
			boolean hideMe = shouldHide(subject);

			for (Player perspective : online)
			{
				// Don't hide themselves
				if (perspective.equals(subject))
				{
					continue;
				}

				// Not hiding the subject and has preference
				if (!hideMe && _manager.getPreferences().get(perspective).isActive(Preference.SHOW_PLAYERS))
				{
					_visibilityManager.showPlayer(perspective, subject, getName());
				}
				else
				{
					_visibilityManager.hidePlayer(perspective, subject, getName());
				}
			}
		}
	}

	private boolean shouldHide(Player subject)
	{
		return
				// Close to spawn
				closeToSpawn(subject) ||
				// Enabled Invisibility
				(_manager.getPreferences().get(subject).isActive(Preference.INVISIBILITY) && _manager.GetClients().Get(subject).hasPermission(Preference.INVISIBILITY)) ||
				// OR Player has been explicitly hidden
				_hiddenPlayers.contains(subject);
	}

	private boolean closeToSpawn(Player player)
	{
		return UtilMath.offset2dSquared(player.getLocation(), _manager.GetSpawn()) < HIDE_SPAWN_RADIUS_SQUARED;
	}
}