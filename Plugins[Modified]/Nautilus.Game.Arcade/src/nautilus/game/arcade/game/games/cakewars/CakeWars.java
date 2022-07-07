package nautilus.game.arcade.game.games.cakewars;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import mineplex.core.Managers;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.leaderboard.DynamicLeaderboard;
import mineplex.core.leaderboard.Leaderboard;
import mineplex.core.leaderboard.LeaderboardManager;
import mineplex.core.leaderboard.LeaderboardRepository.LeaderboardSQLType;
import mineplex.core.leaderboard.RotatingLeaderboard;
import mineplex.core.leaderboard.StaticLeaderboard;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.DeathMessageType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.PlayerKitGiveEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.cakewars.capturepoint.CakePointModule;
import nautilus.game.arcade.game.games.cakewars.general.CakeBatModule;
import nautilus.game.arcade.game.games.cakewars.general.CakePlayerModule;
import nautilus.game.arcade.game.games.cakewars.general.CakeSpawnerModule;
import nautilus.game.arcade.game.games.cakewars.island.CakeIslandModule;
import nautilus.game.arcade.game.games.cakewars.item.CakeItemModule;
import nautilus.game.arcade.game.games.cakewars.item.CakeSpecialItem;
import nautilus.game.arcade.game.games.cakewars.item.items.CakeDeployPlatform;
import nautilus.game.arcade.game.games.cakewars.item.items.CakeSheep;
import nautilus.game.arcade.game.games.cakewars.item.items.CakeWall;
import nautilus.game.arcade.game.games.cakewars.kits.KitCakeArcher;
import nautilus.game.arcade.game.games.cakewars.kits.KitCakeBuilder;
import nautilus.game.arcade.game.games.cakewars.kits.KitCakeFrosting;
import nautilus.game.arcade.game.games.cakewars.kits.KitCakeWarrior;
import nautilus.game.arcade.game.games.cakewars.shop.CakeItem;
import nautilus.game.arcade.game.games.cakewars.shop.CakeNetherItem;
import nautilus.game.arcade.game.games.cakewars.shop.CakeResource;
import nautilus.game.arcade.game.games.cakewars.shop.CakeShopItem;
import nautilus.game.arcade.game.games.cakewars.shop.CakeShopItemType;
import nautilus.game.arcade.game.games.cakewars.shop.CakeShopModule;
import nautilus.game.arcade.game.games.cakewars.shop.trap.CakeBearTrap;
import nautilus.game.arcade.game.games.cakewars.shop.trap.CakeTNTTrap;
import nautilus.game.arcade.game.games.cakewars.team.CakeTeam;
import nautilus.game.arcade.game.games.cakewars.team.CakeTeamModule;
import nautilus.game.arcade.game.games.cakewars.trackers.EatAllCakesTracker;
import nautilus.game.arcade.game.games.cakewars.trackers.EatFirstMinuteTracker;
import nautilus.game.arcade.game.games.cakewars.trackers.FirstBloodStatTracker;
import nautilus.game.arcade.game.games.cakewars.trackers.FloorIsLavaTracker;
import nautilus.game.arcade.game.games.cakewars.trackers.GetGoodStatTracker;
import nautilus.game.arcade.game.games.cakewars.trackers.OwnAllBeaconsTracker;
import nautilus.game.arcade.game.games.cakewars.trackers.Survive10Tracker;
import nautilus.game.arcade.game.games.cakewars.trackers.WinWithOneBiteTracker;
import nautilus.game.arcade.game.games.cakewars.trackers.WinWithinTimeTracker;
import nautilus.game.arcade.game.games.cakewars.trackers.WinWithoutKillingTracker;
import nautilus.game.arcade.game.games.cakewars.ui.CakeResourcePage;
import nautilus.game.arcade.game.games.cakewars.ui.CakeResourceStarPage;
import nautilus.game.arcade.game.modules.AbsorptionFix;
import nautilus.game.arcade.game.modules.CustomScoreboardModule;
import nautilus.game.arcade.game.modules.EnderPearlModule;
import nautilus.game.arcade.game.modules.capturepoint.CapturePointModule;
import nautilus.game.arcade.game.modules.chest.ChestLootModule;
import nautilus.game.arcade.game.modules.chest.ChestLootPool;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.game.modules.rejoin.RejoinModule;
import nautilus.game.arcade.game.modules.winstreak.WinStreakModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.chat.ChatStatData;
import nautilus.game.arcade.managers.lobby.current.NewGameLobbyManager;
import nautilus.game.arcade.scoreboard.GameScoreboard;
import nautilus.game.arcade.stats.WinWithoutDyingStatTracker;

public class CakeWars extends TeamGame
{

	private static final String[] DESCRIPTION =
			{
					C.cAqua + "Defend your Cake" + C.cWhite + " from the enemy teams.",
					C.cRed + "Eat the enemy's Cake" + C.cWhite + "!",
					"Control the " + C.cAqua + "Beacons" + C.cWhite + " to get more resources.",
					"Last team standing wins!"
			};
	private static final String[] TIPS =
			{
					"Controlling the beacons is essential to victory.",
					"Watch out for other teams in case they try to rush your Cake!",
					"Controlling the center beacon will spawn Nether Stars at your base, these can be used to give your whole team a bonus.",
					"Controlling the outer beacons will spawn Emeralds at your base, these can be used to buy strong weapons and armor.",
					"Players will respawn as long as their cake hasn't been eaten.",
					"Deploy Platforms can be used to cross large gaps quickly. Faster but more expensive than wool blocks.",
					"Purchasing the Resource Generator upgrade in the Nether Star shop increases the number of resources your generator creates.",
					"Balance attacking and defending.",
					"All players standing on the Resource Generator get the items generated.",
					"Don't want to see hologram and chat tips? Turn them off in /prefs.",
					"Watch out for Polly The Sheep, if you see her, kill her quick. Otherwise you might just lose your cake."
			};
	private static final int RESPAWN_TIME = 6;
	/**
	 * This value is the base value of knockback for Cake Wars. it is derived from log10(7).
	 */
	private static final double GAME_KNOCKBACK = 0.845;
	private static final LeaderboardManager LEADERBOARD_MANAGER = Managers.get(LeaderboardManager.class);

	private final Map<GameTeam, Location> _averages;
	private final Cache<Long, Player> _deathsInLastMinute;

	private final CakeTeamModule _cakeTeamModule;
	private final CakePlayerModule _cakePlayerModule;
	private final CakeSpawnerModule _cakeSpawnerModule;
	private final CakeShopModule _cakeShopModule;
	protected final ChestLootModule _chestLootModule;
	private final CustomScoreboardModule _customScoreboardModule;
	private final CapturePointModule _capturePointModule;
	private final CakePointModule _cakePointModule;

	private String _gameLengthString;
	private boolean _colourTick;

	public CakeWars(ArcadeManager manager)
	{
		this(manager, GameType.CakeWars4);
	}

	@SuppressWarnings("unchecked")
	public CakeWars(ArcadeManager manager, GameType gameType)
	{
		super(manager, gameType, new Kit[]
				{
						new KitCakeWarrior(manager),
						new KitCakeArcher(manager),
						new KitCakeBuilder(manager),
						new KitCakeFrosting(manager)
				}, DESCRIPTION);

		_averages = new HashMap<>(4);
		_deathsInLastMinute = CacheBuilder.newBuilder()
				.expireAfterWrite(60, TimeUnit.SECONDS)
				.build();

		AnnounceStay = false;
		BlockPlace = true;
		BlockBreak = true;
		DeathTeleport = false;
		DeathSpectateSecs = RESPAWN_TIME;
		DeathDropItems = true;
		StrictAntiHack = true;
		HungerSet = 20;
		InventoryClick = true;
		InventoryOpenChest = true;
		InventoryOpenBlock = true;
		ItemDrop = true;
		ItemPickup = true;
		GameTimeout = TimeUnit.HOURS.toMillis(1);
		WorldBoundary = false;
		_help = TIPS;

		registerStatTrackers(
				new EatAllCakesTracker(this),
				new EatFirstMinuteTracker(this),
				new FirstBloodStatTracker(this),
				new FloorIsLavaTracker(this),
				new GetGoodStatTracker(this),
				new OwnAllBeaconsTracker(this),
				new Survive10Tracker(this),
				new WinWithinTimeTracker(this, "WinIn10", TimeUnit.MINUTES.toMillis(10)),
				new WinWithoutDyingStatTracker(this, "NoDeaths"),
				new WinWithOneBiteTracker(this),
				new WinWithoutKillingTracker(this, "NoKills")
		);

		registerChatStats(
				Kills,
				Assists,
				Deaths,
				KDRatio,
				BlankLine,
				new ChatStatData("Bites", "Cake Bites", true),
				new ChatStatData("EatWholeCake", "Whole Cakes", true)
		);

		manager.GetDamage().setConstantKnockback(GAME_KNOCKBACK);
		manager.GetCreature().SetDisableCustomDrops(true);

		new AbsorptionFix()
				.register(this);

		new CompassModule()
				.register(this);

		new WinStreakModule()
				.register(this);

		new RejoinModule(manager)
				.register(this);

		_cakeTeamModule = new CakeTeamModule(this);
		_cakeTeamModule.register();

		new CakeIslandModule(this)
				.register();

		_cakePlayerModule = new CakePlayerModule(this);
		_cakePlayerModule.register();

		_cakeSpawnerModule = new CakeSpawnerModule(this);
		_cakeSpawnerModule.register();

		_cakeShopModule = new CakeShopModule(this);
		_cakeShopModule.register();

		_cakePointModule = new CakePointModule(this);
		_cakePointModule.register();

		new CakeBatModule(this)
				.register();

		new CakeItemModule(this)
				.register();

		_capturePointModule = new CapturePointModule();
		_capturePointModule.register(this);

		new EnderPearlModule()
				.setCooldown(TimeUnit.SECONDS.toMillis(7))
				.register(this);

		_chestLootModule = new ChestLootModule();

		_customScoreboardModule = new CustomScoreboardModule()
				.setSidebar((player, scoreboard) ->
				{
					switch (GetState())
					{
						case Prepare:
							writePrepare(player, scoreboard);
							break;
						case Live:
							writeLive(player, scoreboard);
							break;
						case End:
							writeEnd(player, scoreboard);
						default:
							break;
					}
				})
				.setPrefix((perspective, subject) ->
				{
					if (!IsAlive(subject))
					{
						return C.cGray;
					}

					GameTeam team = GetTeam(subject);

					return team.GetColor().toString();
				})
				.setUnderNameObjective(C.cRed + "❤")
				.setUnderName((perspective, subject) ->
						(int) (Math.ceil(subject.getHealth() / 2D)));
		_customScoreboardModule.register(this);
	}

	private void writePrepare(Player player, GameScoreboard scoreboard)
	{
		GameTeam team = GetTeam(player);

		scoreboard.writeNewLine();

		if (team == null)
		{
			scoreboard.write("You are a spectator...");
		}
		else
		{
			scoreboard.write(team.GetColor() + C.Bold + "Cake " + C.cWhiteB + "Wars");

			scoreboard.writeNewLine();

			scoreboard.write(team.GetColor() + C.Bold + "Your Team");

			for (Player other : team.GetPlayers(true))
			{
				scoreboard.write(other.getName());
			}
		}

		scoreboard.writeNewLine();
	}

	private void writeLive(Player player, GameScoreboard scoreboard)
	{
		scoreboard.writeNewLine();

		scoreboard.write(C.cYellowB + "Teams");

		for (GameTeam team : GetTeamList())
		{
			CakeTeam cakeTeam = _cakeTeamModule.getCakeTeam(team);
			boolean hasCake = cakeTeam != null && cakeTeam.canRespawn();
			String teamLine = "";

			if (hasCake)
			{
				teamLine += C.cGreenB + "✓";
			}
			else if (!team.IsTeamAlive())
			{
				teamLine += C.cRedB + "✘";
			}
			else
			{
				teamLine += C.cGreen + team.GetPlayers(true).size();
			}

			teamLine += " " + team.GetFormattedName();

			scoreboard.write(teamLine);
		}

		scoreboard.writeNewLine();

		String cakeRot = _cakeTeamModule.getCakeRotString();

		if (cakeRot != null)
		{
			scoreboard.write(C.cRedB + "Cake Rot");
			scoreboard.write(cakeRot);
		}
		else
		{
			scoreboard.write(C.cYellowB + "Time");
			scoreboard.write(UtilTime.MakeStr(System.currentTimeMillis() - GetStateTime()));
		}

		scoreboard.writeNewLine();
	}

	private void writeEnd(Player player, GameScoreboard scoreboard)
	{
		scoreboard.writeNewLine();

		if (WinnerTeam == null)
		{
			scoreboard.write("There was no winner");
			scoreboard.write("No cake for anyone :(");
		}
		else
		{
			GameTeam team = GetTeam(player);

			if (team != null && team.equals(WinnerTeam))
			{
				scoreboard.write((_colourTick ? C.cYellowB : C.cGoldB) + "WINNER WINNER");
				scoreboard.write((_colourTick ? C.cGoldB : C.cYellowB) + "CAKE FOR DINNER");
				scoreboard.writeNewLine();
			}

			scoreboard.write(WinnerTeam.GetColor() + C.Bold + WinnerTeam.GetName());

			for (Player other : WinnerTeam.GetPlayers(true))
			{
				scoreboard.write(other.getName());
			}

			scoreboard.writeNewLine();

			scoreboard.write(C.cYellowB + "Time");
			scoreboard.write(_gameLengthString);
		}

		scoreboard.writeNewLine();
	}

	@Override
	public void ParseData()
	{
		for (GameTeam team : GetTeamList())
		{
			_averages.put(team, UtilAlg.getAverageLocation(team.GetSpawns()));
		}

		generateChests();
		_chestLootModule.register(this);

		// Backwards compatibility with old cake wars maps
		int i = 1;
		for (Location location : WorldData.GetDataLocs("SILVER"))
		{
			WorldData.GetAllCustomLocs().computeIfAbsent("POINT Outer-" + i++ + " GREEN", k -> new ArrayList<>()).add(location);
		}
		for (Location location : WorldData.GetDataLocs("WHITE"))
		{
			WorldData.GetAllCustomLocs().computeIfAbsent("POINT Center GOLD", k -> new ArrayList<>()).add(location);
		}

		if (Manager.IsRewardStats() && Manager.GetLobby() instanceof NewGameLobbyManager)
		{
			Map<String, List<Location>> locations = ((NewGameLobbyManager) Manager.GetLobby()).getCustomLocs();

			if (!locations.containsKey("LEADERBOARDS"))
			{
				return;
			}

			Location rotator = locations.get("ROTATOR").get(0);
			Location topStreak = locations.get("TOP_STREAK").get(0);
			Location topWins = locations.get("TOP_WINS").get(0);
			Location topKills = locations.get("TOP_KILLS").get(0);

			String two = "Cake Wars Duos.";
			String four = "Cake Wars Standard.";
			String bestStreakDuos = two + WinStreakModule.BEST_STREAK_STAT;
			String killsDuos = two + "FinalKills";
			String winsDuos = two + "Wins";
			String bestStreak4 = four + WinStreakModule.BEST_STREAK_STAT;
			String kills4 = four + "FinalKills";
			String wins4 = four + "Wins";

			LEADERBOARD_MANAGER.registerIfNotExists("CAKE_WARS",
					new RotatingLeaderboard(LEADERBOARD_MANAGER, rotator)
							.addMode("Standard", Arrays.asList(

									new DynamicLeaderboard(LEADERBOARD_MANAGER, Collections.singletonList(
											new StaticLeaderboard(LEADERBOARD_MANAGER, "Best Win Streak", new Leaderboard(
													LeaderboardSQLType.ALL,
													bestStreak4
											), topStreak)
									)),

									new DynamicLeaderboard(LEADERBOARD_MANAGER, Arrays.asList(
											new StaticLeaderboard(LEADERBOARD_MANAGER, "Top Wins", new Leaderboard(
													LeaderboardSQLType.ALL,
													wins4
											), topWins),
											new StaticLeaderboard(LEADERBOARD_MANAGER, "Top Weekly Wins", new Leaderboard(
													LeaderboardSQLType.WEEKLY,
													wins4
											), topWins),
											new StaticLeaderboard(LEADERBOARD_MANAGER, "Top Daily Wins", new Leaderboard(
													LeaderboardSQLType.DAILY,
													wins4
											), topWins)
									)),

									new DynamicLeaderboard(LEADERBOARD_MANAGER, Arrays.asList(
											new StaticLeaderboard(LEADERBOARD_MANAGER, "Top Elimination Kills", new Leaderboard(
													LeaderboardSQLType.ALL,
													kills4
											), topKills),
											new StaticLeaderboard(LEADERBOARD_MANAGER, "Top Weekly Elimination Kills", new Leaderboard(
													LeaderboardSQLType.WEEKLY,
													kills4
											), topKills),
											new StaticLeaderboard(LEADERBOARD_MANAGER, "Top Daily Elimination Kills", new Leaderboard(
													LeaderboardSQLType.DAILY,
													kills4
											), topKills)
									))
							))
							.addMode("Duos", Arrays.asList(

									new DynamicLeaderboard(LEADERBOARD_MANAGER, Collections.singletonList(
											new StaticLeaderboard(LEADERBOARD_MANAGER, "Best Win Streak", new Leaderboard(
													LeaderboardSQLType.ALL,
													bestStreakDuos
											), topStreak)
									)),

									new DynamicLeaderboard(LEADERBOARD_MANAGER, Arrays.asList(
											new StaticLeaderboard(LEADERBOARD_MANAGER, "Top Wins", new Leaderboard(
													LeaderboardSQLType.ALL,
													winsDuos
											), topWins),
											new StaticLeaderboard(LEADERBOARD_MANAGER, "Top Weekly Wins", new Leaderboard(
													LeaderboardSQLType.WEEKLY,
													winsDuos
											), topWins),
											new StaticLeaderboard(LEADERBOARD_MANAGER, "Top Daily Wins", new Leaderboard(
													LeaderboardSQLType.DAILY,
													winsDuos
											), topWins)
									)),

									new DynamicLeaderboard(LEADERBOARD_MANAGER, Arrays.asList(
											new StaticLeaderboard(LEADERBOARD_MANAGER, "Top Elimination Kills", new Leaderboard(
													LeaderboardSQLType.ALL,
													killsDuos
											), topKills),
											new StaticLeaderboard(LEADERBOARD_MANAGER, "Top Weekly Elimination Kills", new Leaderboard(
													LeaderboardSQLType.WEEKLY,
													killsDuos
											), topKills),
											new StaticLeaderboard(LEADERBOARD_MANAGER, "Top Daily Elimination Kills", new Leaderboard(
													LeaderboardSQLType.DAILY,
													killsDuos
											), topKills)
									))

							))
			);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void scoreboardDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled() || event.GetDamageePlayer() == null || !IsLive())
		{
			return;
		}

		Manager.runSyncLater(() -> _customScoreboardModule.refreshAsSubject(event.GetDamageePlayer()), 0);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void scoreboardRegainHealth(EntityRegainHealthEvent event)
	{
		if (!(event.getEntity() instanceof Player) || !IsLive())
		{
			return;
		}

		Manager.runSyncLater(() -> _customScoreboardModule.refreshAsSubject((Player) event.getEntity()), 0);
	}

	@EventHandler
	public void kitGiveItems(PlayerKitGiveEvent event)
	{
		_customScoreboardModule.refreshAsSubject(event.getPlayer());
	}

	@EventHandler
	public void entityChangeBlock(EntityChangeBlockEvent event)
	{
		if (IsLive())
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void alternateColourTick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		_colourTick = !_colourTick;
	}

	@Override
	public void AnnounceEnd(GameTeam team)
	{
		_gameLengthString = UtilTime.MakeStr(System.currentTimeMillis() - getGameLiveTime());

		super.AnnounceEnd(team);
	}

	@Override
	public double GetKillsGems(Player killer, Player killed, boolean assist)
	{
		if (getDeathsInLastMinute(killed) > 1)
		{
			return 0;
		}

		return assist ? 1 : 3;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerDeath(PlayerDeathEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		Player player = event.getEntity();
		_deathsInLastMinute.put(System.currentTimeMillis(), player);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_deathsInLastMinute.asMap().values().removeIf(player -> player.equals(event.getPlayer()));
	}

	public int getDeathsInLastMinute(Player player)
	{
		return (int) _deathsInLastMinute.asMap().values().stream()
				.filter(player::equals)
				.count();
	}

	@Override
	public DeathMessageType GetDeathMessageType()
	{
		return DeathMessageType.Detailed;
	}

	@Override
	public String GetMode()
	{
		return "Standard";
	}

	public int getGeneratorRate(CakeResource resource, int current)
	{
		return current;
	}

	public List<CakeItem> generateItems(CakeResource resource)
	{
		switch (resource)
		{
			case BRICK:
				return Arrays.asList
						(
								// Iron Set
								new CakeShopItem(CakeShopItemType.HELMET, new ItemStack(Material.IRON_HELMET), 5),
								new CakeShopItem(CakeShopItemType.CHESTPLATE, new ItemStack(Material.IRON_CHESTPLATE), 8),
								new CakeShopItem(CakeShopItemType.LEGGINGS, new ItemStack(Material.IRON_LEGGINGS), 6),
								new CakeShopItem(CakeShopItemType.BOOTS, new ItemStack(Material.IRON_BOOTS), 5),

								// Sword
								new CakeShopItem(CakeShopItemType.SWORD, new ItemStack(Material.IRON_SWORD), 5),

								// Bow
								new CakeShopItem(CakeShopItemType.BOW, new ItemStack(Material.BOW), 12),

								// Pickaxe
								new CakeShopItem(CakeShopItemType.PICKAXE, new ItemStack(Material.IRON_PICKAXE), 8),

								// Axe
								new CakeShopItem(CakeShopItemType.AXE, new ItemStack(Material.IRON_AXE), 3),

								// Arrow
								new CakeShopItem(CakeShopItemType.OTHER, new ItemStack(Material.ARROW, 3), 12),

								// Blocks
								// Wool
								new CakeShopItem(CakeShopItemType.BLOCK, new ItemStack(Material.WOOL, 16), 3),

								// Coloured Clay
								new CakeShopItem(CakeShopItemType.BLOCK, new ItemStack(Material.STAINED_CLAY, 8), 8),

								// Wood
								new CakeShopItem(CakeShopItemType.BLOCK, new ItemStack(Material.WOOD, 8), 8),

								// End Stone
								new CakeShopItem(CakeShopItemType.BLOCK, new ItemStack(Material.ENDER_STONE, 8), 12),

								// Deploy Platform
								new CakeShopItem(CakeShopItemType.OTHER, CakeDeployPlatform.ITEM_STACK, 5),

								// Emerald
								new CakeShopItem(CakeShopItemType.OTHER, new ItemStack(Material.EMERALD), 20)
						);
			case EMERALD:
				return Arrays.asList
						(

								// Diamond Set
								new CakeShopItem(CakeShopItemType.HELMET, new ItemStack(Material.DIAMOND_HELMET), 10),
								new CakeShopItem(CakeShopItemType.CHESTPLATE, new ItemStack(Material.DIAMOND_CHESTPLATE), 24),
								new CakeShopItem(CakeShopItemType.LEGGINGS, new ItemStack(Material.DIAMOND_LEGGINGS), 16),
								new CakeShopItem(CakeShopItemType.BOOTS, new ItemStack(Material.DIAMOND_BOOTS), 10),

								// Sword
								new CakeShopItem(CakeShopItemType.SWORD, new ItemStack(Material.DIAMOND_SWORD), 5),

								// Pickaxe
								new CakeShopItem(CakeShopItemType.PICKAXE, new ItemStack(Material.DIAMOND_PICKAXE), 10),

								// Axe
								new CakeShopItem(CakeShopItemType.AXE, new ItemStack(Material.DIAMOND_AXE), 4),

								// Obsidian
								new CakeShopItem(CakeShopItemType.BLOCK, new ItemStack(Material.OBSIDIAN), 8),

								// Shears
								new CakeShopItem(CakeShopItemType.SHEARS, new ItemStack(Material.SHEARS), 5),

								// Golden Apple
								new CakeShopItem(CakeShopItemType.OTHER, new ItemStack(Material.GOLDEN_APPLE), 8),

								// Ender pearl
								new CakeShopItem(CakeShopItemType.OTHER, CakeShopModule.ENDER_PEARL, 7),

								// Rune of Holding
								new CakeShopItem(CakeShopItemType.OTHER, CakePlayerModule.RUNE_OF_HOLDING, 20),

								// Insta-Wall
								new CakeShopItem(CakeShopItemType.OTHER, CakeWall.ITEM_STACK, 2),

								new CakeShopItem(CakeShopItemType.OTHER, CakeSheep.ITEM_STACK, 8),

								// Traps
								new CakeTNTTrap(8),
								new CakeBearTrap(8)
						);
			case STAR:
				return Arrays.asList(CakeNetherItem.values());
			default:
				return Collections.emptyList();
		}
	}

	public List<CakeSpecialItem> generateSpecialItems()
	{
		return Arrays.asList
				(
						new CakeDeployPlatform(this),
						new CakeWall(this),
						new CakeSheep(this)
				);
	}

	public void generateChests()
	{
		_chestLootModule.registerChestType(CakeIslandModule.CHEST_TYPE, new ArrayList<>(),

				new ChestLootPool()
						.addItem(new ItemBuilder(Material.DIAMOND_SWORD)
								.addEnchantment(Enchantment.KNOCKBACK, 1)
								.setUnbreakable(true)
								.build())
						.addItem(new ItemBuilder(Material.BOW)
								.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1)
								.setUnbreakable(true)
								.build())
						.addItem(new ItemBuilder(Material.BOW)
								.addEnchantment(Enchantment.ARROW_DAMAGE, 2)
								.addEnchantment(Enchantment.ARROW_INFINITE, 1)
								.setDurability((short) (Material.BOW.getMaxDurability() - 9))
								.build())
						.addItem(new ItemBuilder(Material.BOW)
								.addEnchantment(Enchantment.ARROW_DAMAGE, 4)
								.setDurability((short) (Material.BOW.getMaxDurability() - 9))
								.build())
						.addItem(new ItemBuilder(Material.GOLD_PICKAXE)
								.setTitle(C.cGoldB + "The Golden Pickaxe")
								.setUnbreakable(true)
								.build())
						.addItem(new ItemBuilder(Material.DIAMOND_CHESTPLATE)
								.setUnbreakable(true)
								.build())
						.addItem(new ItemStack(Material.NETHER_STAR), 3, 4)
						.addItem(CakeShopModule.ENDER_PEARL, 1, 2)
						.addItem(new ItemStack(Material.GOLDEN_APPLE), 2, 3)
						.addItem(new ItemBuilder(Material.POTION)
								.setTitle(C.cAqua + "Speed Potion")
								.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30 * 20, 0))
								.build()),

				new ChestLootPool()
						.addItem(new ItemStack(Material.EMERALD), 4, 8),

				new ChestLootPool()
						.addItem(new ItemStack(Material.CLAY_BRICK), 8, 16)

		).destroyAfterOpened(30);
	}

	public CakeResourcePage getShopPage(CakeResource resource, Player player)
	{
		switch (resource)
		{
			case BRICK:
			case EMERALD:
				return new CakeResourcePage(getArcadeManager(), getCakeShopModule().getShop(), player, resource, getCakeShopModule().getItems().get(resource));
			case STAR:
				return new CakeResourceStarPage(getArcadeManager(), getCakeShopModule().getShop(), player, Arrays.asList(CakeNetherItem.values()));
			default:
				return null;
		}
	}

	public Location getAverageLocation(GameTeam team)
	{
		return _averages.get(team);
	}

	public boolean isNearSpawn(Block block)
	{
		return isNearSpawn(block.getLocation().add(0.5, 0, 0.5));
	}

	public boolean isNearSpawn(Location location)
	{
		for (List<Location> locations : WorldData.getAllSpawnLocations().values())
		{
			for (Location spawn : locations)
			{
				if (UtilMath.offsetSquared(location, spawn) < 9)
				{
					return true;
				}
			}
		}

		return false;
	}

	public ChestLootModule getChestLootModule()
	{
		return _chestLootModule;
	}

	public CapturePointModule getCapturePointModule()
	{
		return _capturePointModule;
	}

	public CakePointModule getCakePointModule()
	{
		return _cakePointModule;
	}

	public CakeTeamModule getCakeTeamModule()
	{
		return _cakeTeamModule;
	}

	public CakePlayerModule getCakePlayerModule()
	{
		return _cakePlayerModule;
	}

	public CakeShopModule getCakeShopModule()
	{
		return _cakeShopModule;
	}

	public CakeSpawnerModule getCakeSpawnerModule()
	{
		return _cakeSpawnerModule;
	}
}
