package mineplex.core.titles.tracks.standard;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class MobaHunterWinsTrack extends Track
{

	public MobaHunterWinsTrack()
	{
		super("hog-hunter-wins", "Hunter HOG", "This track is unlocked by winning as a Hunter in Heroes of GWEN");

		getRequirements()
				.addTier(new TrackTier(
						"Arrows?",
						"Win 10 games as a Hunter in Heroes of GWEN",
						this::getStat,
						10,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Skilled Archer",
						"Win 50 games as a Hunter in Heroes of GWEN",
						this::getStat,
						50,
						new TrackFormat(ChatColor.LIGHT_PURPLE)
				))
				.addTier(new TrackTier(
						"Marksman",
						"Win 100 games as a Hunter in Heroes of GWEN",
						this::getStat,
						100,
						new TrackFormat(ChatColor.BLUE)
				))
				.addTier(new TrackTier(
						"Elite Hunter",
						"Win 250 games as a Hunter in Heroes of GWEN",
						this::getStat,
						250,
						new TrackFormat(ChatColor.GREEN)
				))
				.addTier(new TrackTier(
						"360 No scope",
						"Win 500 games as a Hunter in Heroes of GWEN",
						this::getStat,
						500,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));

		getRequirements()
				.withRequirement(1, "Win as a Hunter in Heroes of GWEN");
	}

	@Override
	public String getStatName()
	{
		return "Heroes of GWEN.Hunter.Wins";
	}
}
