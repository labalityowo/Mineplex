package mineplex.core.titles.tracks.standard;

import java.util.HashSet;
import java.util.Set;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;

import mineplex.core.gadget.set.SetMusic;
import mineplex.core.game.GameDisplay;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class PeacefulTrack extends Track
{
	private static final Set<GameDisplay> PEACEFUL_GAMES = new HashSet<>();

	static
	{
		PEACEFUL_GAMES.add(GameDisplay.SpeedBuilders);
		PEACEFUL_GAMES.add(GameDisplay.Build);
		PEACEFUL_GAMES.add(GameDisplay.Draw);
		PEACEFUL_GAMES.add(GameDisplay.DragonEscape);
		PEACEFUL_GAMES.add(GameDisplay.DragonEscapeTeams);
		PEACEFUL_GAMES.add(GameDisplay.Dragons);
		PEACEFUL_GAMES.add(GameDisplay.DragonsTeams);
		PEACEFUL_GAMES.add(GameDisplay.MonsterMaze);
	}

	public PeacefulTrack()
	{
		super("peaceful", "Peaceful", "This track is unlocked by winning games that do not require PvP to win");
		getRequirements()
				.addTier(new TrackTier(
						"Peaceful",
						"Gain 100 Peaceful Points",
						this::getStat,
						100,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Tranquil",
						"Gain 500 Peaceful Points",
						this::getStat,
						500,
						new TrackFormat(ChatColor.BLUE, null)
				))
				.addTier(new TrackTier(
						"Pacifist",
						"Gain 1,000 Peaceful Points",
						this::getStat,
						1000,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));

		getRequirements()
				.withRequirement(1, "per win in a", "Peaceful Game")
				.withSetBonus(SetMusic.class, 2);
	}

	/**
	 * Call this method when the specified Player has won a game
	 */
	public void wonGame(Player player, GameDisplay gameDisplay)
	{
		if (!PEACEFUL_GAMES.contains(gameDisplay))
			return;

		int points = 1;

		if (isSetActive(player, SetMusic.class))
		{
			points *= 2;
		}

		incrementFor(player, points);
	}
}
