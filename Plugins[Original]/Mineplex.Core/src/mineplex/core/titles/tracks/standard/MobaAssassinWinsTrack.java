package mineplex.core.titles.tracks.standard;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class MobaAssassinWinsTrack extends Track
{

	public MobaAssassinWinsTrack()
	{
		super("hog-assassin-wins", "Assassin HOG", "This track is unlocked by winning as an Assassin in Heroes of GWEN");

		getRequirements()
				.addTier(new TrackTier(
						"Clumsy",
						"Win 10 games as an Assassin in Heroes of GWEN",
						this::getStat,
						10,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Skilled Assassin",
						"Win 50 games as an Assassin in Heroes of GWEN",
						this::getStat,
						50,
						new TrackFormat(ChatColor.LIGHT_PURPLE)
				))
				.addTier(new TrackTier(
						"Ninja",
						"Win 100 games as an Assassin in Heroes of GWEN",
						this::getStat,
						100,
						new TrackFormat(ChatColor.BLUE)
				))
				.addTier(new TrackTier(
						"Elite Assassin",
						"Win 250 games as an Assassin in Heroes of GWEN",
						this::getStat,
						250,
						new TrackFormat(ChatColor.GREEN)
				))
				.addTier(new TrackTier(
						"I need healing",
						"Win 500 games as an Assassin in Heroes of GWEN",
						this::getStat,
						500,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));

		getRequirements()
				.withRequirement(1, "Win as an Assassin in Heroes of GWEN");
	}

	@Override
	public String getStatName()
	{
		return "Heroes of GWEN.Assassin.Wins";
	}
}
