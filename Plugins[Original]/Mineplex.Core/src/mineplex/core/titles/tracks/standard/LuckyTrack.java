package mineplex.core.titles.tracks.standard;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import mineplex.core.reward.Reward;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;
import mineplex.core.treasure.event.TreasureStartEvent;
import mineplex.core.treasure.reward.RewardRarity;
import mineplex.core.treasure.types.TreasureType;

public class LuckyTrack extends Track
{
	private static final EnumMap<TreasureType, Integer> MULTIPLIER = new EnumMap<>(TreasureType.class);
	private static final EnumMap<RewardRarity, Integer> POINTS = new EnumMap<>(RewardRarity.class);
	private static final Set<Material> IRON = new HashSet<>();
	private static final Set<Material> DIAMOND = new HashSet<>();

	static
	{
		POINTS.put(RewardRarity.RARE, 1);
		POINTS.put(RewardRarity.LEGENDARY, 5);
		POINTS.put(RewardRarity.MYTHICAL, 50);

//		MULTIPLIER.put(TreasureType.FREEDOM, 2);
//		MULTIPLIER.put(TreasureType.HAUNTED, 2);
//		MULTIPLIER.put(TreasureType.CHRISTMAS, 2);
//		MULTIPLIER.put(TreasureType.TRICK_OR_TREAT, 2);
//		MULTIPLIER.put(TreasureType.LOVE_CHEST, 2);
//		MULTIPLIER.put(TreasureType.ST_PATRICKS, 2);
//		MULTIPLIER.put(TreasureType.SPRING, 2);
//		MULTIPLIER.put(TreasureType.OMEGA, 3);

		IRON.add(Material.IRON_SPADE);
		IRON.add(Material.IRON_PICKAXE);
		IRON.add(Material.IRON_AXE);
		IRON.add(Material.IRON_SWORD);
		IRON.add(Material.IRON_HOE);
		IRON.add(Material.IRON_INGOT);
		IRON.add(Material.IRON_HELMET);
		IRON.add(Material.IRON_CHESTPLATE);
		IRON.add(Material.IRON_LEGGINGS);
		IRON.add(Material.IRON_BOOTS);

		DIAMOND.add(Material.DIAMOND_SPADE);
		DIAMOND.add(Material.DIAMOND_PICKAXE);
		DIAMOND.add(Material.DIAMOND_AXE);
		DIAMOND.add(Material.DIAMOND_SWORD);
		DIAMOND.add(Material.DIAMOND_HOE);
		DIAMOND.add(Material.DIAMOND);
		DIAMOND.add(Material.DIAMOND_HELMET);
		DIAMOND.add(Material.DIAMOND_CHESTPLATE);
		DIAMOND.add(Material.DIAMOND_LEGGINGS);
		DIAMOND.add(Material.DIAMOND_BOOTS);
	}

	public LuckyTrack()
	{
		super("lucky", "Lucky", "This track is unlocked by getting fortunate chest drops");
		getRequirements()
				.addTier(new TrackTier(
						"Lucky",
						"Gain 1,000 Lucky Points",
						this::getStat,
						1000,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Charmed",
						"Gain 2,000 Lucky Points",
						this::getStat,
						2000,
						new TrackFormat(ChatColor.LIGHT_PURPLE)
				))
				.addTier(new TrackTier(
						"Fortune Favored",
						"Gain 3,000 Lucky Points",
						this::getStat,
						3000,
						new TrackFormat(ChatColor.BLUE, null)
				))
				.addTier(new TrackTier(
						"Golden",
						"Gain 5,000 Lucky Points",
						this::getStat,
						5000,
						new TrackFormat(ChatColor.GREEN, null)
				))
				.addTier(new TrackTier(
						"Hashtag Blessed",
						"Gain 10,000 Lucky Points",
						this::getStat,
						10000,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));

		getRequirements()
				.withRequirement(1, "Rare Item")
				.withRequirement(5, "Legendary Item")
				.withRequirement(50, "Mythical Item")
				.withRequirement(1, "per game chest with", "Iron Item")
				.withRequirement(3, "per game chest with", "Diamond Item")
				.withBonus("Event Chests", "from", 2)
				.withBonus("Omega Chests", "from", 3);
	}

	@EventHandler
	public void onUseCosmetic(TreasureStartEvent event)
	{
		for (Reward reward : event.getRewards())
		{
			if (!POINTS.containsKey(reward.getRarity()))
				continue;

			int basePoints = POINTS.get(reward.getRarity());

			if (MULTIPLIER.get(event.getTreasureType()) != null)
				basePoints *= MULTIPLIER.get(event.getTreasureType());

			incrementFor(event.getPlayer(), basePoints);
		}
	}

	public void handleLoot(Player player, Inventory inventory)
	{
		boolean foundIron = false;
		boolean foundDiamond = false;
		boolean foundBow = false;
		for (ItemStack item : inventory)
		{
			if (item != null)
			{
				if (IRON.contains(item.getType())) foundIron = true;
				if (DIAMOND.contains(item.getType())) foundDiamond = true;
				if (item.getType() == Material.BOW) foundBow = true;
			}
		}

		if (foundIron) incrementFor(player, 1);
		if (foundDiamond) incrementFor(player, 3);
		if (foundBow) incrementFor(player, 1);
	}
}
