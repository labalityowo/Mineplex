package mineplex.core.titles.tracks.standard;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;

import mineplex.core.gadget.set.SetVampire;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class WarriorTrack extends Track
{
	public WarriorTrack()
	{
		super("warrior", "Warrior", "This track is unlocked by earning kills in PvP games");
		getRequirements()
				.addTier(new TrackTier(
						"Warrior in Training",
						"Gain 500 Warrior Points",
						this::getStat,
						500,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Savage",
						"Gain 1,000 Warrior Points",
						this::getStat,
						1000,
						new TrackFormat(ChatColor.LIGHT_PURPLE)
				))
				.addTier(new TrackTier(
						"Berserker",
						"Gain 2,000 Warrior Points",
						this::getStat,
						2000,
						new TrackFormat(ChatColor.BLUE, null)
				))
				.addTier(new TrackTier(
						"Duelist",
						"Gain 5,000 Warrior Points",
						this::getStat,
						5000,
						new TrackFormat(ChatColor.GREEN, null)
				))
				.addTier(new TrackTier(
						"Champion",
						"Gain 10,000 Warrior Points",
						this::getStat,
						10000,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));

		getRequirements()
				.withRequirement(1, "Kill")
				.withSetBonus(SetVampire.class, 2);
	}

	/**
	 * Call this method when the specified Player has earned kills
	 */
	public void earnedKill(Player player, int kills)
	{
		if (kills <= 0) return;

		if (isSetActive(player, SetVampire.class))
			kills *= 2;

		incrementFor(player, kills);
	}
}
