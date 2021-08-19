package mineplex.core.titles.tracks.standard;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class MobaKillsTrack extends Track
{

	public MobaKillsTrack()
	{
		super("hog-kills", "HOG Killer", "This track is unlocked by killing players in Heroes of GWEN");

		getRequirements()
				.addTier(new TrackTier(
						"Initiate Guardian",
						"Kill 100 players in Heroes of GWEN",
						this::getStat,
						100,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Lieutenant Guardian",
						"Kill 250 players in Heroes of GWEN",
						this::getStat,
						250,
						new TrackFormat(ChatColor.LIGHT_PURPLE)
				))
				.addTier(new TrackTier(
						"Captain Guardian",
						"Kill 500 players in Heroes of GWEN",
						this::getStat,
						500,
						new TrackFormat(ChatColor.BLUE)
				))
				.addTier(new TrackTier(
						"Noble Guardian",
						"Kill 1000 players in Heroes of GWEN",
						this::getStat,
						1000,
						new TrackFormat(ChatColor.GREEN)
				))
				.addTier(new TrackTier(
						"Champion Guardian",
						"Kill 5000 players in Heroes of GWEN",
						this::getStat,
						5000,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));

		getRequirements()
				.withRequirement(1, "Kill in Heroes of GWEN");
	}

	@Override
	public String getStatName()
	{
		return "Heroes of GWEN.Kills";
	}
}
