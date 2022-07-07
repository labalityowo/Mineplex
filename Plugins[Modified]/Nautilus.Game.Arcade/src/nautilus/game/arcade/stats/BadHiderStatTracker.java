package nautilus.game.arcade.stats;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.hideseek.HideSeek;
import nautilus.game.arcade.game.games.hideseek.forms.CreatureForm;

public class BadHiderStatTracker extends StatTracker<HideSeek>
{
	private final Set<UUID> _disqualified = new HashSet<>();

	public BadHiderStatTracker(HideSeek game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onChangeForm(HideSeek.PlayerChangeFormEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.getForm() instanceof CreatureForm)
			_disqualified.add(event.getPlayer().getUniqueId());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		_disqualified.add(event.getEntity().getUniqueId());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.End)
		{
			for (Player player : getGame().getHiders().GetPlayers(true))
			{
				if (!_disqualified.contains(player.getUniqueId()))
					addStat(player, "BadHider", 1, true, false);
			}
		}
	}
}
