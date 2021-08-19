package mineplex.core.titles.tracks.standard;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class MobaWarriorWinsTrack extends Track
{

	public MobaWarriorWinsTrack()
	{
		super("hog-warrior-wins", "Warrior HOG", "This track is unlocked by winning as a Warrior in Heroes of GWEN");

		getRequirements()
				.addTier(new TrackTier(
						"Tiny",
						"Win 10 games as a Warrior in Heroes of GWEN",
						this::getStat,
						10,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Skilled Fighter",
						"Win 50 games as a Warrior in Heroes of GWEN",
						this::getStat,
						50,
						new TrackFormat(ChatColor.LIGHT_PURPLE)
				))
				.addTier(new TrackTier(
						"Trooper",
						"Win 100 games as a Warrior in Heroes of GWEN",
						this::getStat,
						100,
						new TrackFormat(ChatColor.BLUE)
				))
				.addTier(new TrackTier(
						"Elite Fighter",
						"Win 250 games as a Warrior in Heroes of GWEN",
						this::getStat,
						250,
						new TrackFormat(ChatColor.GREEN)
				))
				.addTier(new TrackTier(
						"RRRRRRRAHH",
						"Win 500 games as a Warrior in Heroes of GWEN",
						this::getStat,
						500,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));

		getRequirements()
				.withRequirement(1, "Win as a Warrior in Heroes of GWEN");
	}

	@Override
	public String getStatName()
	{
		return "Heroes of GWEN.Warrior.Wins";
	}
}
