package mineplex.core.titles.tracks.standard;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class MobaMageWinsTrack extends Track
{

	public MobaMageWinsTrack()
	{
		super("hog-mage-wins", "Mage HOG", "This track is unlocked by winning as a Mage in Heroes of GWEN");

		getRequirements()
				.addTier(new TrackTier(
						"Magician",
						"Win 10 games as a Mage in Heroes of GWEN",
						this::getStat,
						10,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Skilled Conjurer",
						"Win 50 games as a Mage in Heroes of GWEN",
						this::getStat,
						50,
						new TrackFormat(ChatColor.LIGHT_PURPLE)
				))
				.addTier(new TrackTier(
						"Nercomancer",
						"Win 100 games as a Mage in Heroes of GWEN",
						this::getStat,
						100,
						new TrackFormat(ChatColor.BLUE)
				))
				.addTier(new TrackTier(
						"Elite Conjurer",
						"Win 250 games as a Mage in Heroes of GWEN",
						this::getStat,
						250,
						new TrackFormat(ChatColor.GREEN)
				))
				.addTier(new TrackTier(
						"BURN BEAM",
						"Win 500 games as a Mage in Heroes of GWEN",
						this::getStat,
						500,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));

		getRequirements()
				.withRequirement(1, "Win as a Mage in Heroes of GWEN");
	}

	@Override
	public String getStatName()
	{
		return "Heroes of GWEN.Mage.Wins";
	}
}
