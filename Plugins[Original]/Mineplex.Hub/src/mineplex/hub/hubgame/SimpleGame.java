package mineplex.hub.hubgame;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class SimpleGame extends HubGame
{

	private final List<Player> _players;

	public SimpleGame(HubGameManager manager, HubGameType type)
	{
		super(manager, type);

		_players = new ArrayList<>();
	}

	@Override
	public void onPlayerDeath(Player player)
	{
		player.teleport(getSpawn());
	}

	@Override
	public List<Player> getAlivePlayers()
	{
		return _players;
	}

	@Override
	public void onCleanupPlayer(Player player)
	{
		_players.remove(player);
	}
}
