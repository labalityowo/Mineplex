package mineplex.core.titles.tracks.standard;

import java.util.EnumMap;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.event.EventHandler;

import mineplex.core.gadget.set.SetWisdom;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;
import mineplex.core.treasure.event.TreasureStartEvent;
import mineplex.core.treasure.types.TreasureType;

public class TreasureHunterTrack extends Track
{
	private static final EnumMap<TreasureType, Integer> POINTS = new EnumMap<>(TreasureType.class);

	static
	{
		POINTS.put(TreasureType.OLD, 1);
		POINTS.put(TreasureType.ANCIENT, 3);
		POINTS.put(TreasureType.MYTHICAL, 5);
		POINTS.put(TreasureType.ILLUMINATED, 10);
//		POINTS.put(TreasureType.FREEDOM, 25);
//		POINTS.put(TreasureType.HAUNTED, 25);
//		POINTS.put(TreasureType.CHRISTMAS, 25);
//		POINTS.put(TreasureType.TRICK_OR_TREAT, 25);
		POINTS.put(TreasureType.TRICK_OR_TREAT_2017, 25);
//		POINTS.put(TreasureType.THANKFUL, 25);
//		POINTS.put(TreasureType.GINGERBREAD, 25);
//		POINTS.put(TreasureType.LOVE_CHEST, 25);
//		POINTS.put(TreasureType.ST_PATRICKS, 25);
//		POINTS.put(TreasureType.SPRING, 25);
		POINTS.put(TreasureType.OMEGA, 50);
		POINTS.put(TreasureType.MINESTRIKE, 3);
		POINTS.put(TreasureType.MOBA, 25);
	}

	public TreasureHunterTrack()
	{
		super("treasure-hunter", "Treasure Hunter", "This track is unlocked by opening chests in the lobby");
		getRequirements()
				.addTier(new TrackTier(
						"Rookie Treasure Hunter",
						"Gain 100 Treasure Points",
						this::getStat,
						100,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Advanced Treasure Hunter",
						"Gain 250 Treasure Points",
						this::getStat,
						250,
						new TrackFormat(ChatColor.LIGHT_PURPLE)
				))
				.addTier(new TrackTier(
						"Veteran Treasure Hunter",
						"Gain 500 Treasure Points",
						this::getStat,
						500,
						new TrackFormat(ChatColor.BLUE, null)
				))
				.addTier(new TrackTier(
						"Legendary Treasure Hunter",
						"Gain 1,000 Treasure Points",
						this::getStat,
						1000,
						new TrackFormat(ChatColor.GREEN, null)
				))
				.addTier(new TrackTier(
						"Master Treasure Hunter",
						"Gain 2,000 Treasure Points",
						this::getStat,
						2000,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));

		POINTS.forEach((type, value) -> getRequirements().withRequirement(value, type.getName()));
		getRequirements()
				.withSetBonus(SetWisdom.class, 2);
	}

	@EventHandler
	public void onUseCosmetic(TreasureStartEvent event)
	{
		if (POINTS.containsKey(event.getTreasureType()))
		{
			int basePoints = POINTS.get(event.getTreasureType());

			if (isSetActive(event.getPlayer(), SetWisdom.class))
			{
				basePoints *= 2;
			}

			incrementFor(event.getPlayer(), basePoints);
		}
	}
}
