package mineplex.game.nano.game.components.team;

import java.util.List;

import org.bukkit.entity.Player;

public interface TeamComponent
{

	GameTeam addTeam(GameTeam gameTeam);

	List<GameTeam> getTeams();

	void joinTeam(Player player, GameTeam team);

	GameTeam getTeam(Player player);

	boolean isAlive(Player player);

	void respawnPlayer(Player player, GameTeam team);

	boolean hasRespawned(Player player);

	List<Player> getAllPlayers();

	List<Player> getAlivePlayers();

}
