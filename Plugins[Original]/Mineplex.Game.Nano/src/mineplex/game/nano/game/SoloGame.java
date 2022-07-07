package mineplex.game.nano.game;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.components.team.GameTeam;

public abstract class SoloGame extends Game
{

	protected GameTeam _playersTeam;

	public SoloGame(NanoManager manager, GameType gameType, String[] description)
	{
		super(manager, gameType, description);

		_damageComponent.setTeamSelf(true);

		_scoreboardComponent.setSidebar((player, scoreboard) ->
		{
			scoreboard.writeNewLine();

			List<Player> alive = getAlivePlayers();
			scoreboard.write(C.cYellowB + "Players");

			if (alive.size() > 11)
			{
				scoreboard.write(alive.size() + " Alive");
			}
			else
			{
				alive.forEach(other -> scoreboard.write((other.equals(player) ? C.cGreen : "") + other.getName()));
			}

			scoreboard.writeNewLine();

			scoreboard.draw();
		});
	}

	@Override
	protected void createTeams()
	{
		_playersTeam = addTeam(new GameTeam(this, "Players", ChatColor.YELLOW, Color.YELLOW, DyeColor.YELLOW, getPlayerTeamSpawns()));
	}

	public GameTeam getPlayersTeam()
	{
		return _playersTeam;
	}

	public List<Location> getPlayerTeamSpawns()
	{
		return _mineplexWorld.getGoldLocations("Green");
	}

	@Override
	public boolean endGame()
	{
		return getAlivePlayers().size() <= 1;
	}

	@Override
	protected GamePlacements createPlacements()
	{
		return GamePlacements.fromTeamPlacements(_playersTeam.getPlaces(true));
	}
}
