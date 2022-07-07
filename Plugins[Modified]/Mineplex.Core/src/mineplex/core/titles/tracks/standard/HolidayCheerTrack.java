package mineplex.core.titles.tracks.standard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.Managers;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.ItemGadgetUseEvent;
import mineplex.core.gadget.event.PlayerUseCoalEvent;
import mineplex.core.gadget.gadgets.item.ItemBow;
import mineplex.core.gadget.gadgets.item.ItemCandy;
import mineplex.core.gadget.gadgets.item.ItemFlowerGift;
import mineplex.core.gadget.gadgets.item.ItemFreezeCannon;
import mineplex.core.gadget.gadgets.item.ItemLovePotion;
import mineplex.core.gadget.gadgets.item.ItemSnowball;
import mineplex.core.gadget.set.SetCanadian;
import mineplex.core.gadget.set.SetCupidsLove;
import mineplex.core.gadget.set.SetFreedom;
import mineplex.core.gadget.set.SetFrostLord;
import mineplex.core.gadget.set.SetHalloween;
import mineplex.core.gadget.set.SetSpring;
import mineplex.core.gadget.set.suits.SetReindeerSuit;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetSet;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;
import mineplex.core.treasure.event.TreasureStartEvent;
import mineplex.core.treasure.types.TreasureType;

public class HolidayCheerTrack extends Track
{
	private static final Set<TreasureType> HOLIDAY_CHESTS = new HashSet<>();
	private static final Set<Class<? extends GadgetSet>> HOLIDAY_SETS = new HashSet<>();
	private static final Map<Class<? extends Gadget>, Integer> POINTS = new HashMap<>();

	static
	{
		POINTS.put(ItemFlowerGift.class, 1);
		POINTS.put(ItemBow.class, 1);
		POINTS.put(ItemSnowball.class, 1);
		POINTS.put(ItemFreezeCannon.class, 1);
		POINTS.put(ItemLovePotion.class, 10);
		POINTS.put(ItemCandy.class, 5);

		HOLIDAY_CHESTS.add(TreasureType.CHRISTMAS);
		HOLIDAY_CHESTS.add(TreasureType.FREEDOM);
		HOLIDAY_CHESTS.add(TreasureType.HAUNTED);
		HOLIDAY_CHESTS.add(TreasureType.THANKFUL);
		HOLIDAY_CHESTS.add(TreasureType.TRICK_OR_TREAT);
		HOLIDAY_CHESTS.add(TreasureType.TRICK_OR_TREAT_2017);
		HOLIDAY_CHESTS.add(TreasureType.GINGERBREAD);
		HOLIDAY_CHESTS.add(TreasureType.LOVE);
		HOLIDAY_CHESTS.add(TreasureType.ST_PATRICKS);
		HOLIDAY_CHESTS.add(TreasureType.SPRING);

		HOLIDAY_SETS.add(SetFreedom.class);
		HOLIDAY_SETS.add(SetCupidsLove.class);
		HOLIDAY_SETS.add(SetFrostLord.class);
		HOLIDAY_SETS.add(SetSpring.class);
		HOLIDAY_SETS.add(SetHalloween.class);
		HOLIDAY_SETS.add(SetCanadian.class);
	}

	private final GadgetManager _gadgetManager = Managers.require(GadgetManager.class);

	public HolidayCheerTrack()
	{
		super("holiday-cheer", "Holiday Cheer", "This track is unlocked by participating in Holiday Events");
		getRequirements()
				.addTier(new TrackTier(
						"School's Out",
						"Gain 1,000 Holiday Points",
						this::getStat,
						1000,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Every Day's a Holiday",
						"Gain 2,000 Holiday Points",
						this::getStat,
						2000,
						new TrackFormat(ChatColor.LIGHT_PURPLE)
				))
				.addTier(new TrackTier(
						"I Party With Pumplings",
						"Gain 3,000 Holiday Points",
						this::getStat,
						3000,
						new TrackFormat(ChatColor.BLUE, null)
				))
				.addTier(new TrackTier(
						"Celebration Addict",
						"Gain 5,000 Holiday Points",
						this::getStat,
						5000,
						new TrackFormat(ChatColor.GREEN, null)
				))
				.addTier(new TrackTier(
						"Has Santa's Number",
						"Gain 10,000 Holiday Points",
						this::getStat,
						10000,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));

		getRequirements()
				.withRequirement(100, "Special Holiday Event Chests")
				.withRequirement(5, "game played in Holiday Games")
				.withRequirement(25, "win in Holiday Games");

		getRequirements()
				.withRequirement(POINTS.get(ItemLovePotion.class), _gadgetManager.getGadget(ItemLovePotion.class).getName())
				.withRequirement(1, "Holiday Gadget");

		HOLIDAY_SETS.forEach(clazz -> getRequirements().withSetBonus(clazz, 2));
	}

	@EventHandler
	public void onUseCosmetic(TreasureStartEvent event)
	{
		if (!HOLIDAY_CHESTS.contains(event.getTreasureType()))
			return;

		int points = 100;

		for (Class<? extends GadgetSet> set : HOLIDAY_SETS)
		{
			if (isSetActive(event.getPlayer(), set))
			{
				points *= 2;
				break;
			}
		}

		incrementFor(event.getPlayer(), points);
	}

	@EventHandler
	public void onUseCosmetic(ItemGadgetUseEvent event)
	{
		if (!POINTS.containsKey(event.getGadget().getClass()))
			return;

		int basePoints = POINTS.get(event.getGadget().getClass());

		for (Class<? extends GadgetSet> set : HOLIDAY_SETS)
		{
			if (isSetActive(event.getPlayer(), set))
			{
				basePoints *= 2;
				break;
			}
		}

		if (basePoints != 0)
		{
			incrementFor(event.getPlayer(), basePoints);
		}
	}

	@EventHandler
	public void onUseCosmetic(PlayerUseCoalEvent event)
	{
		int basePoints = event.getCost();

		for (Class<? extends GadgetSet> set : HOLIDAY_SETS)
		{
			if (isSetActive(event.getPlayer(), set))
			{
				basePoints *= 2;
				break;
			}
		}

		if (basePoints != 0)
		{
			incrementFor(event.getPlayer(), basePoints);
		}
	}

	/**
	 * Call this method when the specified Player has won a holiday-themed game
	 */
	public void wonGame(Player player)
	{
		int basePoints = 25;

		for (Class<? extends GadgetSet> set : HOLIDAY_SETS)
		{
			if (isSetActive(player, set))
			{
				basePoints *= 2;
				break;
			}
		}

		incrementFor(player, basePoints);
	}

	/**
	 * Call this method when the specified Player has won a round in a holiday-themed game
	 */
	public void wonRound(Player player)
	{
		int basePoints = 5;

		for (Class<? extends GadgetSet> set : HOLIDAY_SETS)
		{
			if (isSetActive(player, set))
			{
				basePoints *= 2;
				break;
			}
		}

		incrementFor(player, basePoints);
	}
}
