package mineplex.core.titles.tracks.standard;

import java.util.HashMap;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.event.EventHandler;

import mineplex.core.gadget.event.ItemGadgetUseEvent;
import mineplex.core.gadget.event.PlayerConsumeMelonEvent;
import mineplex.core.gadget.gadgets.item.ItemLovePotion;
import mineplex.core.gadget.set.SetCandyCane;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class SweetToothTrack extends Track
{
	private static final Map<Class<? extends Gadget>, Integer> POINTS = new HashMap<>();

	static
	{
		POINTS.put(ItemLovePotion.class, 75);
	}

	public SweetToothTrack()
	{
		super("sweet-tooth", "Sweet Tooth", "This track is unlocked by consuming Watermelon and other Sweets!");
		getRequirements()
				.addTier(new TrackTier(
						"Sweet Tooth",
						"Consume 10,000 Sweet Points",
						this::getStat,
						10000,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Cavity Prone",
						"Consume 25,000 Sweet Points",
						this::getStat,
						25000,
						new TrackFormat(ChatColor.BLUE, null)
				))
				.addTier(new TrackTier(
						"Candy Addict",
						"Consume 50,000 Sweet Points",
						this::getStat,
						50000,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));

		getRequirements()
				.withRequirement(1, "Watermelon consumed")
				.withRequirement(75, "Love Potion consumed")
				.withSetBonus(SetCandyCane.class, 2);
	}

	@EventHandler
	public void onUseCosmetic(ItemGadgetUseEvent event)
	{
		int basePoints = POINTS.getOrDefault(event.getGadget().getClass(), 0);

		if (isSetActive(event.getPlayer(), SetCandyCane.class)) basePoints *= 2;

		if (basePoints != 0)
		{
			incrementFor(event.getPlayer(), basePoints);
		}
	}

	@EventHandler
	public void onConsumeMelon(PlayerConsumeMelonEvent event)
	{
		int basePoints = 1;

		if (isSetActive(event.getPlayer(), SetCandyCane.class)) basePoints *= 2;

		incrementFor(event.getPlayer(), basePoints);
	}
}
