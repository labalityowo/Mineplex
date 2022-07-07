package mineplex.core.achievement.leveling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.MiniDbClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.achievement.Achievement;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.achievement.leveling.rewards.LevelCallbackReward;
import mineplex.core.achievement.leveling.rewards.LevelChestReward;
import mineplex.core.achievement.leveling.rewards.LevelCurrencyReward;
import mineplex.core.achievement.leveling.rewards.LevelDummyReward;
import mineplex.core.achievement.leveling.rewards.LevelGadgetReward;
import mineplex.core.achievement.leveling.rewards.LevelGameAmplifierReward;
import mineplex.core.achievement.leveling.rewards.LevelReward;
import mineplex.core.achievement.leveling.ui.LevelRewardShop;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.MorphOcelot;
import mineplex.core.gadget.gadgets.particle.ParticleChickenWings;
import mineplex.core.gadget.gadgets.particle.ParticleEnderDragonWings;
import mineplex.core.gadget.gadgets.particle.ParticleMacawWings;
import mineplex.core.gadget.gadgets.particle.ParticleWitchsCure;
import mineplex.core.gadget.gadgets.taunts.EasyModeTaunt;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.core.treasure.types.TreasureType;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommandManager;

@ReflectivelyCreateMiniPlugin
public class LevelingManager extends MiniDbClientPlugin<List<Integer>> implements CommandCallback<LevelBroadcastServerCommand>
{

	private static final int MAX_LEVEL = 100;
	private static final int EXCLUSIVE_REWARDS_LEVEL = 80;

	public static int getMaxLevel()
	{
		return MAX_LEVEL;
	}

	private final AchievementManager _achievementManager;
	private final GadgetManager _gadgetManager;
	private final LevelingRepository _repository;
	private final LevelRewardShop _shop;

	private final Map<Integer, List<LevelReward>> _rewards;

	public LevelingManager()
	{
		super("Level Rewards");

		_achievementManager = require(AchievementManager.class);
		_gadgetManager = require(GadgetManager.class);
		_repository = new LevelingRepository();
		_shop = new LevelRewardShop(this, ClientManager, require(DonationManager.class));

		_rewards = new HashMap<>(getMaxLevel());

		ServerCommandManager.getInstance().registerCommandType(LevelBroadcastServerCommand.class, this);

		populateRewards();
	}

	private void populateRewards()
	{
		// 1-9
		addCurrencyReward(1, GlobalCurrency.GEM, 100);
		addCurrencyReward(2, GlobalCurrency.GEM, 200);
		addChestReward(3, TreasureType.OLD, 1);
		addCurrencyReward(4, GlobalCurrency.GEM, 250);
		addCurrencyReward(5, GlobalCurrency.GEM, 250);
		addChestReward(6, TreasureType.OLD, 2);
		addCurrencyReward(7, GlobalCurrency.GEM, 250);
		addCurrencyReward(8, GlobalCurrency.GEM, 250);
		addCurrencyReward(9, GlobalCurrency.GEM, 500);
		// 10-19
		addChestReward(10, TreasureType.OLD, 5);
		addCurrencyReward(11, GlobalCurrency.GEM, 750);
		addCurrencyReward(12, GlobalCurrency.GEM, 250);
		addCurrencyReward(12, GlobalCurrency.TREASURE_SHARD, 500);
		addCurrencyReward(13, GlobalCurrency.GEM, 250);
		addCurrencyReward(13, GlobalCurrency.TREASURE_SHARD, 500);
		addCurrencyReward(14, GlobalCurrency.GEM, 1000);
		addChestReward(15, TreasureType.OLD, 7);
		addCurrencyReward(16, GlobalCurrency.GEM, 2000);
		addCurrencyReward(17, GlobalCurrency.GEM, 500);
		addCurrencyReward(17, GlobalCurrency.TREASURE_SHARD, 1000);
		addCurrencyReward(18, GlobalCurrency.GEM, 500);
		addCurrencyReward(18, GlobalCurrency.TREASURE_SHARD, 1000);
		addChestReward(18, TreasureType.OLD, 8);
		addCurrencyReward(19, GlobalCurrency.GEM, 5000);
		// 20-29
		addGadgetReward(20, MorphOcelot.class);
		addChestReward(20, TreasureType.OLD, 10);
		addCurrencyReward(21, GlobalCurrency.GEM, 5000);
		addCurrencyReward(22, GlobalCurrency.GEM, 500);
		addCurrencyReward(22, GlobalCurrency.TREASURE_SHARD, 1000);
		addChestReward(23, TreasureType.ANCIENT, 1);
		addCurrencyReward(24, GlobalCurrency.GEM, 500);
		addCurrencyReward(24, GlobalCurrency.TREASURE_SHARD, 1000);
		addChestReward(25, TreasureType.ANCIENT, 5);
		addCurrencyReward(26, GlobalCurrency.GEM, 10000);
		addCurrencyReward(27, GlobalCurrency.GEM, 1000);
		addCurrencyReward(27, GlobalCurrency.TREASURE_SHARD, 2000);
		addCurrencyReward(28, GlobalCurrency.TREASURE_SHARD, 2000);
		addChestReward(28, TreasureType.OLD, 2);
		addChestReward(29, TreasureType.ANCIENT, 3);
		// 30-39
		addCurrencyReward(30, GlobalCurrency.GEM, 20000);
		addCurrencyReward(31, GlobalCurrency.GEM, 1000);
		addCurrencyReward(32, GlobalCurrency.TREASURE_SHARD, 2500);
		addChestReward(33, TreasureType.ANCIENT, 1);
		addCurrencyReward(34, GlobalCurrency.GEM, 2000);
		addCurrencyReward(34, GlobalCurrency.TREASURE_SHARD, 2500);
		addCurrencyReward(35, GlobalCurrency.GEM, 30000);
		addCurrencyReward(36, GlobalCurrency.TREASURE_SHARD, 500);
		addChestReward(37, TreasureType.ANCIENT, 5);
		addCurrencyReward(38, GlobalCurrency.TREASURE_SHARD, 1000);
		addCurrencyReward(39, GlobalCurrency.TREASURE_SHARD, 500);
		addChestReward(39, TreasureType.OLD, 5);
		// 40-49
		addGadgetReward(40, ParticleWitchsCure.class);
		addCurrencyReward(41, GlobalCurrency.TREASURE_SHARD, 1000);
		addCurrencyReward(42, GlobalCurrency.TREASURE_SHARD, 1000);
		addChestReward(42, TreasureType.OLD, 5);
		addChestReward(43, TreasureType.ANCIENT, 6);
		addCurrencyReward(44, GlobalCurrency.TREASURE_SHARD, 1000);
		addChestReward(45, TreasureType.ANCIENT, 10);
		addCurrencyReward(46, GlobalCurrency.TREASURE_SHARD, 1000);
		addCurrencyReward(47, GlobalCurrency.TREASURE_SHARD, 1000);
		addChestReward(48, TreasureType.OLD, 5);
		addChestReward(48, TreasureType.ANCIENT, 7);
		addCurrencyReward(49, GlobalCurrency.TREASURE_SHARD, 1000);
		// 50-59
		addChestReward(50, TreasureType.OMEGA, 1);
		addChestReward(51, TreasureType.ANCIENT, 10);
		addCurrencyReward(52, GlobalCurrency.TREASURE_SHARD, 1500);
		addCurrencyReward(53, GlobalCurrency.TREASURE_SHARD, 1500);
		addChestReward(53, TreasureType.OLD, 10);
		addChestReward(54, TreasureType.ANCIENT, 5);
		addCurrencyReward(55, GlobalCurrency.TREASURE_SHARD, 1500);
		addCurrencyReward(56, GlobalCurrency.TREASURE_SHARD, 1500);
		addChestReward(57, TreasureType.ANCIENT, 10);
		addCurrencyReward(58, GlobalCurrency.TREASURE_SHARD, 2500);
		addChestReward(59, TreasureType.ANCIENT, 10);
		// 60-69
		addGadgetReward(60, ParticleChickenWings.class);
		addChestReward(60, TreasureType.MYTHICAL, 1);
		addCurrencyReward(61, GlobalCurrency.TREASURE_SHARD, 1000);
		addCurrencyReward(62, GlobalCurrency.TREASURE_SHARD, 1000);
		addChestReward(62, TreasureType.ANCIENT, 1);
		addChestReward(63, TreasureType.MYTHICAL, 2);
		addCurrencyReward(64, GlobalCurrency.TREASURE_SHARD, 1000);
		addCurrencyReward(65, GlobalCurrency.TREASURE_SHARD, 1000);
		addChestReward(65, TreasureType.ANCIENT, 1);
		addChestReward(66, TreasureType.MYTHICAL, 3);
		addCurrencyReward(67, GlobalCurrency.TREASURE_SHARD, 1000);
		addChestReward(67, TreasureType.ANCIENT, 1);
		addCurrencyReward(68, GlobalCurrency.TREASURE_SHARD, 1000);
		addChestReward(68, TreasureType.ANCIENT, 5);
		addCurrencyReward(69, GlobalCurrency.TREASURE_SHARD, 1000);
		// 70-79
		addChestReward(70, TreasureType.MYTHICAL, 6);
		addCurrencyReward(71, GlobalCurrency.TREASURE_SHARD, 1000);
		addCurrencyReward(72, GlobalCurrency.TREASURE_SHARD, 1000);
		addChestReward(72, TreasureType.ANCIENT, 5);
		addCurrencyReward(73, GlobalCurrency.TREASURE_SHARD, 1000);
		addChestReward(74, TreasureType.MYTHICAL, 5);
		addGameAmplifierReward(75, 1);
		addCurrencyReward(75, GlobalCurrency.TREASURE_SHARD, 2500);
		addCurrencyReward(76, GlobalCurrency.TREASURE_SHARD, 2500);
		addChestReward(77, TreasureType.MYTHICAL, 10);
		addCurrencyReward(78, GlobalCurrency.TREASURE_SHARD, 2500);
		addCurrencyReward(79, GlobalCurrency.TREASURE_SHARD, 2500);
		addChestReward(79, TreasureType.ANCIENT, 10);
		// 80-89
		addGadgetReward(80, EasyModeTaunt.class);
		addChestReward(80, TreasureType.ILLUMINATED, 1);
		addChestReward(80, TreasureType.OMEGA, 1);
		addChestReward(81, TreasureType.MYTHICAL, 5);
		addCurrencyReward(82, GlobalCurrency.TREASURE_SHARD, 2500);
		addCurrencyReward(83, GlobalCurrency.TREASURE_SHARD, 2500);
		addCurrencyReward(84, GlobalCurrency.TREASURE_SHARD, 2500);
		addChestReward(85, TreasureType.ILLUMINATED, 1);
		addChestReward(85, TreasureType.MYTHICAL, 10);
		addCurrencyReward(86, GlobalCurrency.TREASURE_SHARD, 2500);
		addCurrencyReward(87, GlobalCurrency.TREASURE_SHARD, 2500);
		addChestReward(87, TreasureType.MYTHICAL, 3);
		addCurrencyReward(88, GlobalCurrency.TREASURE_SHARD, 2500);
		addCurrencyReward(89, GlobalCurrency.TREASURE_SHARD, 2500);
		// 90-99
		addGadgetReward(90, ParticleMacawWings.class);
		addChestReward(90, TreasureType.ILLUMINATED, 1);
		addChestReward(90, TreasureType.MYTHICAL, 10);
		addCurrencyReward(91, GlobalCurrency.TREASURE_SHARD, 5000);
		addCurrencyReward(92, GlobalCurrency.TREASURE_SHARD, 5000);
		addChestReward(92, TreasureType.MYTHICAL, 3);
		addCurrencyReward(93, GlobalCurrency.TREASURE_SHARD, 5000);
		addChestReward(93, TreasureType.MYTHICAL, 5);
		addCurrencyReward(94, GlobalCurrency.TREASURE_SHARD, 2500);
		addChestReward(95, TreasureType.ILLUMINATED, 5);
		addCurrencyReward(96, GlobalCurrency.TREASURE_SHARD, 10000);
		addCurrencyReward(97, GlobalCurrency.TREASURE_SHARD, 10000);
		addChestReward(97, TreasureType.MYTHICAL, 10);
		addCurrencyReward(98, GlobalCurrency.TREASURE_SHARD, 10000);
		addChestReward(98, TreasureType.MYTHICAL, 20);
		addCurrencyReward(99, GlobalCurrency.TREASURE_SHARD, 15000);
		// 100
		addGadgetReward(100, ParticleEnderDragonWings.class);
		addChestReward(100, TreasureType.OMEGA, 5);
		addDummyReward(100, C.cWhite + "10" + C.cPurple + " Different Level Colors");
		addCallbackReward(100, C.cRed + "Server Announcement", player ->
		{
			player.closeInventory();
			new LevelBroadcastServerCommand(player.getName()).publish();
		});
	}

	private void addCurrencyReward(int level, GlobalCurrency type, int amount)
	{
		addReward(level, new LevelCurrencyReward(type, amount));
	}

	private void addChestReward(int level, TreasureType type, int amount)
	{
		addReward(level, new LevelChestReward(type, amount));
	}

	private void addGameAmplifierReward(int level, int amount)
	{
		addReward(level, new LevelGameAmplifierReward(amount));
	}

	private void addGadgetReward(int level, Class<? extends Gadget> clazz)
	{
		addReward(level, new LevelGadgetReward(_gadgetManager.getGadget(clazz)));
	}

	private void addDummyReward(int level, String description)
	{
		addReward(level, new LevelDummyReward(description));
	}

	private void addCallbackReward(int level, String description, Consumer<Player> callback)
	{
		addReward(level, new LevelCallbackReward(description, callback));
	}

	private void addReward(int level, LevelReward reward)
	{
		_rewards.computeIfAbsent(level, k -> new ArrayList<>(2)).add(reward);
	}

	@Override
	public void run(LevelBroadcastServerCommand command)
	{
		UtilTextMiddle.display(C.cRedB + command.getPlayer(), C.cGold + C.Scramble + "!!!" + C.cWhite + " Reached Level " + C.cRedB + "100 " + C.cGold + C.Scramble + "!!!",10, 60, 20, UtilServer.getPlayers());

		for (Player player : UtilServer.getPlayersCollection())
		{
			player.playSound(player.getLocation(), Sound.FIREWORK_TWINKLE, 1, 1);
		}
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		List<Integer> levels = new ArrayList<>(50);

		try
		{
			while (resultSet.next())
			{
				levels.add(resultSet.getInt("level"));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		Set(uuid, levels);
	}

	@Override
	protected List<Integer> addPlayer(UUID uuid)
	{
		return new ArrayList<>();
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT level FROM accountLevelReward WHERE accountId=" + accountId + ";";
	}

	public int getLevel(Player player)
	{
		return _achievementManager.getMineplexLevelNumber(player, false);
	}

	public int getFirstUnclaimedLevel(Player player)
	{
		List<Integer> claimed = Get(player);

		return _rewards.keySet().stream()
				.filter(level -> !claimed.contains(level))
				.sorted()
				.findFirst()
				.orElse(-1);
	}

	public List<LevelReward> getLevelRewards(int level)
	{
		return _rewards.get(level);
	}

	public Map<Integer, List<LevelReward>> getLevelRewards()
	{
		return _rewards;
	}

	public ItemStack getLevelItem(Player player, List<LevelReward> rewards, int level)
	{
		boolean claimed = hasClaimed(player, level);
		boolean canClaim = canClaim(player, level);
		boolean exclusive = level >= EXCLUSIVE_REWARDS_LEVEL;

		Material material;
		byte data = 0;
		String title = "Level " + Achievement.getExperienceString(level);
		String bottomLine;
		ItemBuilder builder = new ItemBuilder(Material.AIR);

		// Already claimed
		if (claimed)
		{
			material = Material.INK_SACK;
			data = 8;
			title = C.cRed + title;
			bottomLine = C.cRed + "You have already claimed this reward.";
		}
		// Hasn't claimed and could claim
		else if (canClaim)
		{
			material = exclusive ? Material.DIAMOND_BLOCK : Material.EMERALD_BLOCK;
			title = C.cGreen + title;
			bottomLine = C.cGreen + "Click to claim this reward!";
			builder.setGlow(true);
		}
		// Hasn't claimed and can't claim
		else
		{
			material = exclusive ? Material.OBSIDIAN : Material.REDSTONE_BLOCK;
			title = C.cRed + title;
			bottomLine = C.cRed + "You aren't a high enough level to claim this reward.";
		}

		builder.setType(material);
		builder.setData(data);
		builder.setTitle(title);
		builder.addLore("");

		boolean hide = !canClaim && exclusive;

		for (LevelReward reward : rewards)
		{
			String lore = C.cWhite + " - ";

			if (hide)
			{
				lore += C.cRed + C.Scramble + "?????";
			}
			else
			{
				lore += reward.getDescription();
			}

			builder.addLore(lore);
		}

		builder.addLore("", bottomLine);

		return builder.build();
	}

	public void claim(Player player, int level, ShopPageBase menu)
	{
		runAsync(() ->
		{
			boolean success = _repository.claimReward(ClientManager.getAccountId(player), level);

			if (success)
			{
				runSync(() ->
				{
					Get(player).add(level);
					List<LevelReward> rewards = getLevelRewards(level);
					player.sendMessage(F.main(_plugin.getName(), "You claimed rewards for level " + F.elem(level) + ":"));
					rewards.forEach(reward ->
					{
						reward.claim(player);
						player.sendMessage(F.main(getName(), reward.getDescription()));
						menu.refresh();
					});
				});
			}
		});
	}

	public boolean hasClaimed(Player player, int level)
	{
		return Get(player).contains(level);
	}

	public boolean canClaim(Player player, int level)
	{
		return getLevel(player) >= level;
	}

	public long getUnclaimedLevels(Player player)
	{
		return _rewards.keySet().stream()
				.filter(level -> !hasClaimed(player, level) && canClaim(player, level))
				.count();
	}

	public List<Entry<Integer, List<LevelReward>>> getSortedRewards()
	{
		return _rewards.entrySet().stream()
				.sorted(Comparator.comparingInt(Entry::getKey))
				.collect(Collectors.toList());
	}

	public LevelRewardShop getShop()
	{
		return _shop;
	}
}
