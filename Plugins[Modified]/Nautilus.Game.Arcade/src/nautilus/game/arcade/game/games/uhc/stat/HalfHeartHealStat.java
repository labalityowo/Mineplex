package nautilus.game.arcade.game.games.uhc.stat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.uhc.UHC;
import nautilus.game.arcade.stats.StatTracker;

public class HalfHeartHealStat extends StatTracker<UHC>
{

	private Set<UUID> _players = new HashSet<>();

	public HalfHeartHealStat(UHC game)
	{
		super(game);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Player player : getGame().GetPlayers(true))
		{
			if (player.getHealth() < 2 && !_players.contains(player.getUniqueId()))
			{
				_players.add(player.getUniqueId());
			}
			else if (player.getHealth() >= 20 && _players.contains(player.getUniqueId()))
			{
				getGame().addUHCAchievement(player, "Die");
				_players.remove(player.getUniqueId());
			}
		}
	}

}
