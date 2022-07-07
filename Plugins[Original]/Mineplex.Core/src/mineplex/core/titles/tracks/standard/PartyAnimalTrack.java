package mineplex.core.titles.tracks.standard;

import java.util.HashMap;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.event.EventHandler;

import mineplex.core.gadget.event.ItemGadgetUseEvent;
import mineplex.core.gadget.gadgets.item.ItemCoinBomb;
import mineplex.core.gadget.gadgets.item.ItemFirework;
import mineplex.core.gadget.gadgets.item.ItemPartyPopper;
import mineplex.core.gadget.set.SetParty;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class PartyAnimalTrack extends Track
{
	private static final Map<Class<? extends Gadget>, Integer> POINTS = new HashMap<>();

	static
	{
		POINTS.put(ItemFirework.class, 1);
		POINTS.put(ItemPartyPopper.class, 20);
		POINTS.put(ItemCoinBomb.class, 50);
	}

	public PartyAnimalTrack()
	{
		super("party-animal", "Party Animal", "This track is unlocked by partying with your friends and celebrating!");
		getRequirements()
				.addTier(new TrackTier(
						"Party Animal",
						"Gain 10,000 Party Points",
						this::getStat,
						10000,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Can't Stop Won't Stop",
						"Gain 25,000 Party Points",
						this::getStat,
						25000,
						new TrackFormat(ChatColor.BLUE, null)
				))
				.addTier(new TrackTier(
						"Life is a Party",
						"Gain 50,000 Party Points",
						this::getStat,
						50000,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));

		getRequirements()
				.withRequirement(1, "for using", "Fireworks")
				.withRequirement(20, "for using" ,"Party Poppers")
				.withRequirement(50, "for using", "Treasure Party Bombs")
				.withSetBonus(SetParty.class, 2);
	}

	@EventHandler
	public void onUseCosmetic(ItemGadgetUseEvent event)
	{
		if (!POINTS.containsKey(event.getGadget().getClass()))
			return;

		int basePoints = POINTS.get(event.getGadget().getClass());

		if (isSetActive(event.getPlayer(), SetParty.class))
		{
			basePoints = basePoints * 2;
		}

		if (basePoints != 0)
		{
			incrementFor(event.getPlayer(), basePoints);
		}
	}
}
