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

public class UnluckyTrack extends Track
{
	private static final EnumMap<RewardRarity, Integer> POINTS = new EnumMap<>(RewardRarity.class);
	private static final Set<Material> IRON = new HashSet<>();
	private static final Set<Material> DIAMOND = new HashSet<>();

	static
	{
		POINTS.put(RewardRarity.COMMON, 1);
		POINTS.put(RewardRarity.UNCOMMON, 5);

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

	public UnluckyTrack()
	{
		super("unlucky", "Unlucky", "This track is unlocked by getting bad chest drops");
		getRequirements()
				.addTier(new TrackTier(
						"Unlucky",
						"Gain 1,000 Unlucky Points",
						this::getStat,
						1000,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Cursed",
						"Gain 2,000 Unlucky Points",
						this::getStat,
						2000,
						new TrackFormat(ChatColor.LIGHT_PURPLE)
				))
				.addTier(new TrackTier(
						"Accident Prone",
						"Gain 3,000 Unlucky Points",
						this::getStat,
						3000,
						new TrackFormat(ChatColor.BLUE, null)
				))
				.addTier(new TrackTier(
						"Corroded",
						"Gain 5,000 Unlucky Points",
						this::getStat,
						5000,
						new TrackFormat(ChatColor.GREEN, null)
				))
				.addTier(new TrackTier(
						"Things Don't Go My Way",
						"Gain 10,000 Unlucky Points",
						this::getStat,
						10000,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));

		getRequirements()
				.withRequirement(1, "Common Item")
				.withRequirement(5, "Uncommon Item")
				.withRequirement(1, "per game chest without", "Diamond/Iron/Food");
	}

	@EventHandler
	public void onUseCosmetic(TreasureStartEvent event)
	{
		for (Reward reward : event.getRewards())
		{
			if (!POINTS.containsKey(reward.getRarity()))
				continue;

			int basePoints = POINTS.get(reward.getRarity());

			incrementFor(event.getPlayer(), basePoints);
		}
	}

	public void handleLoot(Player player, Inventory inventory)
	{
		for (ItemStack item : inventory)
		{
			if (item != null)
			{
				if (IRON.contains(item.getType())) return;
				if (DIAMOND.contains(item.getType())) return;
				if (item.getType().isEdible()) return;
			}
		}

		incrementFor(player, 1);
	}
}
