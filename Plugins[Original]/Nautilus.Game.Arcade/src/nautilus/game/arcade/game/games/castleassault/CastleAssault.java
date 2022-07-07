package nautilus.game.arcade.game.games.castleassault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dispenser;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import mineplex.core.Managers;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.leaderboard.Leaderboard;
import mineplex.core.leaderboard.LeaderboardManager;
import mineplex.core.leaderboard.LeaderboardRepository.LeaderboardSQLType;
import mineplex.core.leaderboard.StaticLeaderboard;
import mineplex.core.loot.ChestLoot;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.DeathMessageType;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.GemData;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.cakewars.trackers.FirstBloodStatTracker;
import nautilus.game.arcade.game.games.castleassault.data.KillStreakData;
import nautilus.game.arcade.game.games.castleassault.data.ObjectiveTNTSpawner;
import nautilus.game.arcade.game.games.castleassault.data.TeamCrystal;
import nautilus.game.arcade.game.games.castleassault.data.TeamKing;
import nautilus.game.arcade.game.games.castleassault.kits.KitAlchemist;
import nautilus.game.arcade.game.games.castleassault.kits.KitArcher;
import nautilus.game.arcade.game.games.castleassault.kits.KitDemolitionist;
import nautilus.game.arcade.game.games.castleassault.kits.KitFighter;
import nautilus.game.arcade.game.games.castleassault.kits.KitPlayer;
import nautilus.game.arcade.game.games.castleassault.kits.KitTank;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.game.modules.gamesummary.GameSummaryComponentType;
import nautilus.game.arcade.game.modules.gamesummary.GameSummaryModule;
import nautilus.game.arcade.game.modules.gamesummary.components.GemSummaryComponent;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.lobby.current.NewGameLobbyManager;

public class CastleAssault extends TeamGame
{
	private static final int MAX_FLINT_AND_STEEL_USES = 4;
	private static final int ITEMS_PER_CHEST = 5;
	private static final long TIME_TILL_REFILL = 2 * 60 * 1000;

	private long _lastRefill;

	private ItemBuilder _flintAndSteel;
	private ItemBuilder _wearableTnt;

	private Map<Player, KillStreakData> _streakData = new WeakHashMap<>();
	private Map<GameTeam, List<TeamCrystal>> _crystals = new HashMap<>();
	private Map<GameTeam, TeamKing> _kings = new HashMap<>();
	private List<Player> _tntCarry = new ArrayList<>();

	private List<Block> _chests = new ArrayList<>();

	private ChestLoot _rangedGear = new ChestLoot(true);
	private ChestLoot _rodsAndGaps = new ChestLoot(true);
	private ChestLoot _potionGearCommon = new ChestLoot(true);
	private ChestLoot _potionGearRare = new ChestLoot(true);
	private ChestLoot _miscGear = new ChestLoot();

	private ObjectiveTNTSpawner _tntSpawner;
	private Map<GameTeam, Integer> _teamKills = new HashMap<>();

	private boolean _writeScoreboard = true;

	private boolean _killsAreObjective = false;

	@SuppressWarnings("deprecation")
	public CastleAssault(ArcadeManager manager)
	{
		super(manager, GameType.CastleAssault,
			new Kit[]
			{
				new KitAlchemist(manager),
				new KitArcher(manager),
				new KitDemolitionist(manager),
				//new KitEnchanter(manager),
				new KitFighter(manager),
				//new KitHardline(manager),
				//new KitNinja(manager),
				new KitTank(manager)
			},
			new String[]
			{
				"Destroy enemy sentry crystals with running TNT",
				"After the crystals are destroyed you must slay their king",
				"First team to kill the enemy king wins",
				"Chests refill every 2 minutes",
				"TNT Respawns every 1 minute"
			}
		);

		_help = new String[]
		{
			"Use the TNT spawning platforms to run TNT to the enemy crystals to destroy them!",
			"The enemy king is invulnerable until you destroy the two sentry crystals on each sentry tower",
			"Go on Kill Streaks to earn Kill Streak Rewards to obtain better armor & weapons!",
			"Chests refill every 2 minutes with potions, golden applegates, fishing rods, and other useful PvP items!"
		};

		this.HungerSet = 20;
		this.DeathOut = false;
		this.DeathSpectateSecs = 5;
		this.CreatureAllow = false;
		this.DeathDropItems = false;
		this.WorldWeatherEnabled = false;
		this.AllowParticles = false;
		this.SoupEnabled = false;
		this.InventoryClick = true;
		this.InventoryOpenChest = true;
		this.InventoryOpenBlock = true;
		this.ItemDrop = true;
		this.ItemPickup = true;
		this.AllowFlintAndSteel = true;
		this.BlockPlaceAllow.add(Material.FIRE.getId());
		this.CrownsEnabled = true;
		this.FirstKillReward = 20;
		this.GemKillDeathRespawn = 1;
		this.GameTimeout = -1;
		this.SplitKitXP = true;

		registerStatTrackers(new FirstBloodStatTracker(this));

		new CompassModule()
			.setGiveCompass(true)
			.setGiveCompassToSpecs(true)
			.setGiveCompassToAlive(false)
			.register(this);

		getModule(GameSummaryModule.class)
				.replaceComponent(GameSummaryComponentType.GEMS, new GemSummaryComponent(this::GetGems, C.cGold, "Crowns"));

		_flintAndSteel = new ItemBuilder(Material.FLINT_AND_STEEL).setData((short) (Material.FLINT_AND_STEEL.getMaxDurability() - MAX_FLINT_AND_STEEL_USES));
		_wearableTnt = new ItemBuilder(Material.TNT).setTitle(C.cRed + "TNT").addLore(C.cRedB + "Right Click with Weapon to " + F.name("Detonate"));
		generateLoot();

		if (manager.IsRewardStats() && manager.GetLobby() instanceof NewGameLobbyManager)
		{
			LeaderboardManager leaderboardManager = Managers.get(LeaderboardManager.class);
			Map<String, List<Location>> lobbyCustomLocs = ((NewGameLobbyManager)manager.GetLobby()).getCustomLocs();

			if (lobbyCustomLocs.containsKey("TOP_DAILY_WINS"))
			{
				Location loc = lobbyCustomLocs.get("TOP_DAILY_WINS").get(0);
				leaderboardManager.registerIfNotExists("TOP_CA_DAILY_WINS", new StaticLeaderboard(
						leaderboardManager,
						"Top Daily Wins",
						new Leaderboard(
								LeaderboardSQLType.DAILY,
								GetName() + ".Wins"
						),
						loc));
			}
			if (lobbyCustomLocs.containsKey("TOP_DAILY_KILLS"))
			{
				Location loc = lobbyCustomLocs.get("TOP_DAILY_KILLS").get(0);
				leaderboardManager.registerIfNotExists("TOP_CA_DAILY_KILLS", new StaticLeaderboard(
						leaderboardManager,
						"Top Daily Kills",
						new Leaderboard(
								LeaderboardSQLType.DAILY,
								GetName() + ".Kills"
						),
						loc));
			}
			if (lobbyCustomLocs.containsKey("TOP_WINS"))
			{
				Location loc = lobbyCustomLocs.get("TOP_WINS").get(0);
				leaderboardManager.registerIfNotExists("TOP_CA_WINS", new StaticLeaderboard(
						leaderboardManager,
						"Top Wins",
						new Leaderboard(
								LeaderboardSQLType.ALL,
								GetName() + ".Wins"
						),
						loc));
			}
			if (lobbyCustomLocs.containsKey("TOP_KILLS"))
			{
				Location loc = lobbyCustomLocs.get("TOP_KILLS").get(0);
				leaderboardManager.registerIfNotExists("TOP_CA_KILLS", new StaticLeaderboard(
						leaderboardManager,
						"Top Kills",
						new Leaderboard(
								LeaderboardSQLType.ALL,
								GetName() + ".Kills"
						),
						loc));
			}
		}
	}

	private void generateLoot()
	{
		{
			_rangedGear.addLoot(new ItemStack(Material.EGG), 3, 5, 9);
			_rangedGear.addLoot(Material.ARROW, 3, 8, 16);
		}
		{
			_rodsAndGaps.addLoot(new ItemStack(Material.FISHING_ROD), 3);
			_rodsAndGaps.addLoot(new ItemBuilder(Material.GOLDEN_APPLE).setTitle(C.cPurple + "Golden Applegate").build(), 3);
		}
		{
			_potionGearCommon.addLoot(new ItemBuilder(Material.POTION).setData((short)8194).build(), 2);
			_potionGearCommon.addLoot(new ItemBuilder(Material.POTION).setData((short)16417).build(), 2);
		}
		{
			_potionGearRare.addLoot(new ItemBuilder(Material.POTION).setData((short)8193).build(), 2);
		}
		{
			_miscGear.addLoot(new ItemStack(Material.ENDER_PEARL), 2);
			_miscGear.addLoot(new ItemStack(Material.WATER_BUCKET), 2);
			_miscGear.addLoot(_flintAndSteel.build(), 2);
			_miscGear.addLoot(new ItemStack(Material.SNOW_BALL, 16), 3);
		}
	}

	private void fillChest(Block block)
	{
		if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)
		{
			return;
		}
		Chest chest = (Chest) block.getState();

		chest.getBlockInventory().clear();
		int[] slots = UtilMath.random.ints(ITEMS_PER_CHEST, 0, chest.getBlockInventory().getSize()).toArray();

		for (int slot : slots)
		{
			double chance = UtilMath.random.nextDouble();
			double subChance = UtilMath.random.nextDouble();

			ChestLoot loot = _miscGear;
			if (chance <= 0.6)
			{
				loot = _rodsAndGaps;
			}
			if (chance <= 0.5)
			{
				loot = _potionGearCommon;
				if (subChance <= 0.45)
				{
					loot = _potionGearRare;
				}
			}
			if (chance <= 0.3)
			{
				loot = _rangedGear;
			}
			chest.getBlockInventory().setItem(slot, loot.getLoot());
		}
	}

	public ItemStack getNewFlintAndSteel(boolean kitItem)
	{
		if (kitItem)
		{
			return _flintAndSteel.clone().setLore(C.cGold + "Kit Item").build();
		}
		return _flintAndSteel.build();
	}

	public void writeScoreboard()
	{
		if (!_writeScoreboard)
		{
			return;
		}
		Scoreboard.reset();
		Scoreboard.write(C.cDRedB + GetName());
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cGreenB + "Chest Refill");
		long refillTime = _lastRefill + TIME_TILL_REFILL - System.currentTimeMillis();
		if (!IsLive())
		{
			refillTime = TIME_TILL_REFILL;
		}
		Scoreboard.write(UtilTime.MakeStr(refillTime));
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cGreenB + "TNT Spawn");
		long tntTime = _tntSpawner.getNextTNT();
		if (!IsLive())
		{
			tntTime = 60000;
		}
		if (_tntSpawner.isSpawned())
		{
			Scoreboard.write("Spawned");
		}
		else
		{
			Scoreboard.write(UtilTime.MakeStr(tntTime));
		}
		Scoreboard.writeNewLine();
		GameTeam red = GetTeam(ChatColor.RED);
		long redCrystals = _crystals.get(red).stream().filter(TeamCrystal::isActive).count();
		GameTeam blue = GetTeam(ChatColor.AQUA);
		long blueCrystals = _crystals.get(blue).stream().filter(TeamCrystal::isActive).count();
		if (redCrystals > 0)
		{
			Scoreboard.write(_kings.get(red).getName(true));
			Scoreboard.write(redCrystals + "/2 Crystals Active");
		}
		else
		{
			Scoreboard.write(_kings.get(red).getName(true) + " Health");
			Scoreboard.write(_kings.get(red).getHealth() + "");
		}
		Scoreboard.writeNewLine();
		if (blueCrystals > 0)
		{
			Scoreboard.write(_kings.get(blue).getName(true));
			Scoreboard.write(blueCrystals + "/2 Crystals Active");
		}
		else
		{
			Scoreboard.write(_kings.get(blue).getName(true) + " Health");
			Scoreboard.write(_kings.get(blue).getHealth() + "");
		}
		Scoreboard.draw();
	}

	public void writeFinalScoreboard(String deadKing, String winKing, String warrior)
	{
		_writeScoreboard = false;
		Scoreboard.reset();
		Scoreboard.writeNewLine();
		Scoreboard.write(deadKing + "'s " + C.cWhite + "castle has been conquered");
		Scoreboard.write(C.cWhite + "by " + winKing + "'s " + C.cWhite + "army with the help of");
		Scoreboard.write(warrior + C.cWhite + "!");
		Scoreboard.writeNewLine();

		Scoreboard.draw();
	}

	@Override
	public void ParseData()
	{
		for (Location chestLoc : WorldData.GetDataLocs("BROWN"))
		{
			Block block = chestLoc.getBlock();
			block.setType(Material.CHEST);
			fillChest(block);
			_chests.add(block);
		}
		GameTeam red = GetTeam(ChatColor.RED);
		GameTeam blue = GetTeam(ChatColor.AQUA);
		Location redKing = WorldData.GetDataLocs("RED").get(0);
		Location blueKing = WorldData.GetDataLocs("BLUE").get(0);
		Vector redBlue = UtilAlg.getTrajectory(redKing, blueKing);
		Vector blueRed = UtilAlg.getTrajectory(blueKing, redKing);
		redKing.setPitch(UtilAlg.GetPitch(redBlue));
		redKing.setYaw(UtilAlg.GetYaw(redBlue));
		blueKing.setPitch(UtilAlg.GetPitch(blueRed));
		blueKing.setYaw(UtilAlg.GetYaw(blueRed));
		_crystals.put(red, Arrays.asList(new TeamCrystal(red, WorldData.GetDataLocs("PINK").get(0)), new TeamCrystal(red, WorldData.GetDataLocs("PINK").get(1))));
		_crystals.put(blue, Arrays.asList(new TeamCrystal(blue, WorldData.GetDataLocs("LIGHT_BLUE").get(0)), new TeamCrystal(blue, WorldData.GetDataLocs("LIGHT_BLUE").get(1))));
		this.CreatureAllowOverride = true;
		_kings.put(red, new TeamKing(red, "King Jon", redKing));
		_kings.put(blue, new TeamKing(blue, "King Ryan", blueKing));
		for (Kit kit : GetKits())
		{
			List<Location> spawns = WorldData.GetCustomLocs(kit.GetName().toUpperCase());
			for (Location spawn : spawns)
			{
				kit.getGameKit().createNPC(spawn);
			}
		}
		this.CreatureAllowOverride = false;
		_tntSpawner = new ObjectiveTNTSpawner(WorldData.GetDataLocs("BLACK"));
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
		{
			return;
		}

		List<GameTeam> teamsAlive = new ArrayList<>();

		for (GameTeam team : GetTeamList())
		{
			if (team.GetPlayers(true).size() > 0)
			{
				teamsAlive.add(team);
			}
		}

		if (teamsAlive.size() <= 1)
		{
			//Announce
			if (teamsAlive.size() > 0)
			{
				GameTeam winner = teamsAlive.get(0);
				TeamKing king = _kings.get(winner);
				TeamKing dead = _kings.values().stream().filter(k -> k.getOwner().GetColor() != king.getOwner().GetColor()).findFirst().get();
				AnnounceEnd(winner);
				writeFinalScoreboard(dead.getName(false), king.getName(false), dead.getOwner().GetColor() + "Quitters");
				for (GameTeam team : GetTeamList())
				{
					for (Player player : team.GetPlayers(true))
					{
						if (team.GetColor() == winner.GetColor())
						{
							AddGems(player, 100, "Winning Team", false, false);
						}
						else
						{
							AddGems(player, 50, "Losing Team", false, false);
						}
						if (player.isOnline())
						{
							AddGems(player, 10, "Participation", false, false);
						}

						int crowns = 0;
						for (Entry<String, GemData> data : GetGems(player).entrySet())
						{
							if (data.getKey().equals("Kills"))
							{
								crowns += data.getValue().Gems;
							}
						}

						{
							int streak = _streakData.getOrDefault(player, new KillStreakData()).getBestStreak();
							if (streak >= 2 && streak < 4)
							{
								AddGems(player, 0.5 * crowns, streak + " Player Kill Streak", false, false);
							}
							else if (streak >= 4 && streak < 6)
							{
								AddGems(player, 1 * crowns, streak + " Player Kill Streak", false, false);
							}
							else if (streak >= 6 && streak < 8)
							{
								AddGems(player, 1.5 * crowns, streak + " Player Kill Streak", false, false);
							}
							else if (streak >= 8)
							{
								AddGems(player, 2 * crowns, streak + " Player Kill Streak", false, false);
							}
						}
					}
				}
			}

			SetState(GameState.End);
		}
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event) {};

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}
		if (IsLive())
		{
			if (UtilTime.elapsed(_lastRefill, TIME_TILL_REFILL))
			{
				_lastRefill = System.currentTimeMillis();
				_chests.forEach(this::fillChest);
				Bukkit.broadcastMessage(C.cGreenB + "Chests have refilled!");
			}

			GameTeam red = GetTeam(ChatColor.RED);
			GameTeam blue = GetTeam(ChatColor.AQUA);
			TeamKing redKing = _kings.get(red);
			TeamKing blueKing = _kings.get(blue);
			redKing.update(_crystals.get(red).stream().filter(TeamCrystal::isActive).count() > 0);
			if (redKing.isDead())
			{
				AnnounceEnd(blue);
				writeFinalScoreboard(redKing.getName(false), blueKing.getName(false), blue.GetColor() + redKing.getLastDamager());
				for (GameTeam team : GetTeamList())
				{
					for (Player player : team.GetPlayers(true))
					{
						if (player.getName().equals(redKing.getLastDamager()))
						{
							AddGems(player, 20, "King Slayer", false, false);
						}
						if (team.GetColor() == ChatColor.AQUA)
						{
							AddGems(player, 100, "Winning Team", false, false);
						}
						else
						{
							AddGems(player, 50, "Losing Team", false, false);
						}
						if (player.isOnline())
						{
							AddGems(player, 10, "Participation", false, false);
						}

						int crowns = 0;
						for (Entry<String, GemData> data : GetGems(player).entrySet())
						{
							if (data.getKey().equals("Kills"))
							{
								crowns += data.getValue().Gems;
							}
						}

						{
							int streak = _streakData.getOrDefault(player, new KillStreakData()).getBestStreak();
							if (streak >= 2 && streak < 4)
							{
								AddGems(player, 0.5 * crowns, streak + " Player Kill Streak", false, false);
							}
							else if (streak >= 4 && streak < 6)
							{
								AddGems(player, 1 * crowns, streak + " Player Kill Streak", false, false);
							}
							else if (streak >= 6 && streak < 8)
							{
								AddGems(player, 1.5 * crowns, streak + " Player Kill Streak", false, false);
							}
							else if (streak >= 8)
							{
								AddGems(player, 2 * crowns, streak + " Player Kill Streak", false, false);
							}
						}
					}
				}
				SetState(GameState.End);
				return;
			}
			blueKing.update(_crystals.get(blue).stream().filter(TeamCrystal::isActive).count() > 0);
			if (blueKing.isDead())
			{
				AnnounceEnd(red);
				writeFinalScoreboard(blueKing.getName(false), redKing.getName(false), red.GetColor() + blueKing.getLastDamager());
				for (GameTeam team : GetTeamList())
				{
					for (Player player : team.GetPlayers(true))
					{
						if (player.getName().equals(blueKing.getLastDamager()))
						{
							AddGems(player, 20, "King Slayer", false, false);
						}
						if (team.GetColor() == ChatColor.RED)
						{
							AddGems(player, 100, "Winning Team", false, false);
						}
						else
						{
							AddGems(player, 50, "Losing Team", false, false);
						}
						if (player.isOnline())
						{
							AddGems(player, 10, "Participation", false, false);
						}

						int crowns = 0;
						for (Entry<String, GemData> data : GetGems(player).entrySet())
						{
							if (data.getKey().equals("Kills"))
							{
								crowns += data.getValue().Gems;
							}
						}

						{
							int streak = _streakData.getOrDefault(player, new KillStreakData()).getBestStreak();
							if (streak >= 2 && streak < 4)
							{
								AddGems(player, 0.5 * crowns, streak + " Player Kill Streak", false, false);
							}
							else if (streak >= 4 && streak < 6)
							{
								AddGems(player, 1 * crowns, streak + " Player Kill Streak", false, false);
							}
							else if (streak >= 6 && streak < 8)
							{
								AddGems(player, 1.5 * crowns, streak + " Player Kill Streak", false, false);
							}
							else if (streak >= 8)
							{
								AddGems(player, 2 * crowns, streak + " Player Kill Streak", false, false);
							}
						}
					}
				}
				SetState(GameState.End);
				return;
			}

			_tntSpawner.update();
			if (!_killsAreObjective)
			{
				if (_tntSpawner.isSpawned())
				{
					_killsAreObjective = true;
				}
			}
		}
		if (InProgress())
		{
			writeScoreboard();
		}
	}

	@EventHandler
	public void onEditSettings(GameStateChangeEvent event)
	{
		if (event.GetGame() != this)
		{
			return;
		}

		if (event.GetState() == GameState.Live)
		{
			_lastRefill = System.currentTimeMillis();
			_tntSpawner.onStart();
			Manager.GetDamage().SetEnabled(false);
			Manager.GetExplosion().setEnabled(false);
			Manager.GetCreature().SetDisableCustomDrops(true);
		}

		if (event.GetState() == GameState.End)
		{
			Manager.GetDamage().SetEnabled(true);
			Manager.GetExplosion().setEnabled(true);
			Manager.GetCreature().SetDisableCustomDrops(false);
		}

		if (event.GetState() == GameState.Dead)
		{
			Managers.get(LeaderboardManager.class).unregisterLeaderboard("TOP_CASTLEASSAULT_DAILY_WINS");
			Managers.get(LeaderboardManager.class).unregisterLeaderboard("TOP_CASTLEASSAULT_DAILY_KILLS");
			Managers.get(LeaderboardManager.class).unregisterLeaderboard("TOP_CASTLEASSAULT_WINS");
			Managers.get(LeaderboardManager.class).unregisterLeaderboard("TOP_CASTLEASSAULT_KILLS");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleDeath(CombatDeathEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		event.SetBroadcastType(DeathMessageType.Detailed);
	}

	@EventHandler
	public void disableDamageLevel(CustomDamageEvent event)
	{
		event.SetDamageToLevel(false);
	}

	@EventHandler
	public void BlockFade(BlockFadeEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void BlockBurn(BlockBurnEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void BlockDecay(LeavesDecayEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void hangingBreak(HangingBreakEvent event)
	{
		if (event.getEntity() instanceof ItemFrame || event.getEntity() instanceof Painting)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void noFlow(BlockFromToEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		Block block = event.getBlock();
		if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER || block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA)
		{
			event.setCancelled(true);
		}
		if (event.getToBlock().getType() == Material.ICE)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockChange(BlockFormEvent e)
	{
		if (!IsLive())
		{
			return;
		}
		if (e.getNewState().getType() == Material.ICE)
		{
			e.setCancelled(true);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled=true)
	public void onPlayerEmptyBucket(PlayerBucketEmptyEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		Player player = event.getPlayer();
		if (player.getItemInHand().getType() == Material.WATER_BUCKET)
		{
			player.getItemInHand().setType(Material.BUCKET);
			Block block = event.getBlockClicked().getRelative(event.getBlockFace());
			if (block.getType().toString().contains("LAVA") || (block.getType().toString().contains("WATER") && block.getType() != Material.WATER_LILY))
			{
				event.setCancelled(true);
				player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
				return;
			}
			for (BlockFace bf : BlockFace.values())
			{
				Block relative = block.getRelative(bf);
				if (relative.getType().toString().contains("LAVA") || (relative.getType().toString().contains("WATER") && relative.getType() != Material.WATER_LILY))
				{
					event.setCancelled(true);
					player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
				}
			}
		}
		else if (player.getItemInHand().getType() == Material.LAVA_BUCKET)
		{
			event.setCancelled(true);
			Block block = event.getBlockClicked().getRelative(event.getBlockFace());
			player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled=true)
	public void onPlayerFillBucket(PlayerBucketFillEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		Player player = event.getPlayer();
		Block block = event.getBlockClicked().getRelative(event.getBlockFace());
		if (block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA)
		{
			event.setCancelled(true);
			player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onBlockDispense(BlockDispenseEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (event.getItem().getType() == Material.WATER_BUCKET)
		{
			Block dispenser = event.getBlock();

			MaterialData mat = dispenser.getState().getData();
			Dispenser disp_mat = (Dispenser)mat;
			BlockFace face = disp_mat.getFacing();
			Block block = dispenser.getRelative(face);
			if (block.getType().toString().contains("LAVA") || (block.getType().toString().contains("WATER") && block.getType() != Material.WATER_LILY))
			{
				event.setCancelled(true);
				return;
			}
			for (BlockFace bf : BlockFace.values())
			{
				if (block.getRelative(bf).getType().toString().contains("LAVA") || (block.getRelative(bf).getType().toString().contains("WATER") && block.getRelative(bf).getType() != Material.WATER_LILY))
				{
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (event.GetLog().GetKiller() == null)
		{
			return;
		}

		if (!event.GetLog().GetKiller().IsPlayer())
		{
			return;
		}

		Player player = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());
		if (player == null)
		{
			return;
		}

		AddStat(player, GetKit(player).GetName() + "KitKills", 1, false, false);
		if (_killsAreObjective)
		{
			_teamKills.merge(GetTeam(player), 1, Integer::sum);
			if (GetTeam(player).GetColor() == ChatColor.RED)
			{
				GameTeam enemy = GetTeam(ChatColor.AQUA);
				TeamCrystal[] crystals = _crystals.get(enemy).stream().filter(TeamCrystal::isActive).toArray(size -> new TeamCrystal[size]);
				if (crystals.length > 0)
				{
					if (_teamKills.getOrDefault(GetTeam(player), 0) % 20 == 0)
					{
						crystals[UtilMath.r(crystals.length)].destroy();
						if (crystals.length > 1)
						{
							Bukkit.broadcastMessage(F.main("Game", "One of " + F.elem(enemy.GetFormattedName() + "'s Crystals") + " has been destroyed!"));
						}
						else
						{
							Bukkit.broadcastMessage(F.main("Game", "All of " + F.elem(enemy.GetFormattedName() + "'s Crystals") + " have been destroyed and " + F.elem(_kings.get(enemy).getName(false)) + " is now vulnerable!"));
						}
					}
				}
				else
				{
					_kings.get(enemy).handleDamage(player.getName(), 10, true);
				}
			}
			else
			{
				GameTeam enemy = GetTeam(ChatColor.RED);
				TeamCrystal[] crystals = _crystals.get(enemy).stream().filter(TeamCrystal::isActive).toArray(size -> new TeamCrystal[size]);
				if (crystals.length > 0)
				{
					if (_teamKills.getOrDefault(GetTeam(player), 0) % 20 == 0)
					{
						crystals[UtilMath.r(crystals.length)].destroy();
						if (crystals.length > 1)
						{
							Bukkit.broadcastMessage(F.main("Game", "One of " + F.elem(enemy.GetFormattedName() + "'s Crystals") + " has been destroyed!"));
						}
						else
						{
							Bukkit.broadcastMessage(F.main("Game", "All of " + F.elem(enemy.GetFormattedName() + "'s Crystals") + " have been destroyed and " + F.elem(_kings.get(enemy).getName(false)) + " is now vulnerable!"));
						}
					}
				}
				else
				{
					_kings.get(enemy).handleDamage(player.getName(), 10, true);
				}
			}
		}

		if (UtilPlayer.isSpectator(player))
		{
			return;
		}

		player.setLevel(player.getLevel() + 1);
		player.getInventory().addItem(new ItemBuilder(Material.GOLDEN_APPLE).setTitle(C.cPurple + "Golden Applegate").build());
		KillStreakData data = _streakData.computeIfAbsent(player, (key) -> new KillStreakData());
		boolean hardLine = GetKit(player).GetName().equals("Hardline");
		if (data.addKill(hardLine))
		{
			AddStat(player, "KillStreak", 1, false, false);
			((KitPlayer)GetKit(player)).awardKillStreak(player, hardLine ? (data.getKills() + 1) : data.getKills());
		}
		if (UtilMath.isEven(data.getKills()))
		{
			Bukkit.broadcastMessage(F.main("Game", C.cGreen + C.Bold + player.getName() + ChatColor.RESET + " is on a " + F.elem(C.cDPurple + data.getKills() + " player Kill Streak") + "!"));
		}
	}

	@EventHandler
	public void TNTExplosion(ExplosionPrimeEvent event)
	{
		if (!event.getEntity().hasMetadata("THROWER"))
		{
			return;
		}
		float radius = event.getRadius();
		event.setRadius(0f);

		String thrower = UtilEnt.GetMetadata(event.getEntity(), "THROWER");
		Player player = UtilPlayer.searchExact(thrower);
		if (player == null)
		{
			return;
		}
		if (GetTeam(player) == null)
		{
			return;
		}
		if (GetKit(player).GetName().equals("Demolitionist"))
		{
			radius += 3;
		}

		Map<Player, Double> nearby = UtilPlayer.getInRadius(event.getEntity().getLocation(), radius);
		for (Player near : nearby.keySet())
		{
			if (UtilPlayer.isSpectator(near))
			{
				continue;
			}
			if (near.getEntityId() != player.getEntityId() && GetTeam(near).GetColor() == GetTeam(player).GetColor())
			{
				continue;
			}
			if (near.getEntityId() == player.getEntityId() && event.getEntity().hasMetadata("OBJECTIVE_TNT"))
			{
				continue;
			}

			double mult = nearby.get(near);

			int highestBlastProt = 0;
			int blastProtEPF = 0;
			for (ItemStack item : near.getInventory().getArmorContents())
			{
				if (item != null && item.getEnchantments().containsKey(Enchantment.PROTECTION_EXPLOSIONS))
				{
					blastProtEPF += (2 * item.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS));
					if (item.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS) > highestBlastProt)
					{
						highestBlastProt = item.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS);
					}
				}
			}
			blastProtEPF = Math.min(blastProtEPF, 20);

			double damage = 10 * mult;
			damage = damage * (1 - (blastProtEPF / 25));

			double knockbackReduction = 1 - (highestBlastProt * 0.15);

			near.damage(damage, event.getEntity());
			UtilAction.velocity(near, UtilAlg.getTrajectory(event.getEntity().getLocation(), near.getLocation()), 1 * mult * knockbackReduction, false, 0, mult * knockbackReduction, 10, true);
		}

		if (event.getEntity().hasMetadata("OBJECTIVE_TNT"))
		{
			List<TeamCrystal> crystals = new ArrayList<>();

			for (List<TeamCrystal> c : _crystals.values())
			{
				crystals.addAll(c);
			}
			for (TeamCrystal crystal : crystals)
			{
				if (crystal.isActive() && !crystal.getOwner().HasPlayer(player) && UtilMath.offset(event.getEntity().getLocation(), crystal.getLocation()) <= radius)
				{
					crystal.destroy();
					AddGems(player, 40, "Crystal Destruction", false, true);
					long remaining = crystals.stream().filter(b -> b.getOwner().GetColor() == crystal.getOwner().GetColor()).filter(TeamCrystal::isActive).count();
					if (remaining > 0)
					{
						Bukkit.broadcastMessage(F.main("Game", "One of " + F.elem(crystal.getOwner().GetFormattedName() + "'s Crystals") + " has been destroyed!"));
					}
					else
					{
						Bukkit.broadcastMessage(F.main("Game", "All of " + F.elem(crystal.getOwner().GetFormattedName() + "'s Crystals") + " have been destroyed and " + F.elem(_kings.get(crystal.getOwner()).getName(false)) + " is now vulnerable!"));
					}
				}
			}
			for (TeamKing king : _kings.values())
			{
				if (king.isDead() && !king.getOwner().HasPlayer(player) && UtilMath.offset(event.getEntity().getLocation(), king.getLocation()) <= radius)
				{
					king.handleDamage(player.getName(), 50);
				}
			}
		}
	}

	@EventHandler
	public void TNTThrow(PlayerInteractEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.L))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!UtilInv.IsItem(player.getItemInHand(), Material.TNT, (byte) 0))
		{
			return;
		}

		if (!IsAlive(player))
		{
			return;
		}

		event.setCancelled(true);

		if (!Manager.GetGame().CanThrowTNT(player.getLocation()))
		{
			// Inform
			UtilPlayer.message(event.getPlayer(), F.main(GetName(), "You cannot use " + F.item("Throwing TNT") + " here."));
			return;
		}

		UtilInv.remove(player, Material.TNT, (byte) 0, 1);
		UtilInv.Update(player);

		TNTPrimed tnt = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), TNTPrimed.class);

		tnt.setFuseTicks(60);

		double throwMult = 1;

		if (GetKit(player).GetName().equals("Demolitionist"))
		{
			throwMult = ((KitDemolitionist)GetKit(player)).getThrowMultiplier(player);
		}

		UtilAction.velocity(tnt, player.getLocation().getDirection(), 0.5 * throwMult, false, 0, 0.1, 10, false);

		UtilEnt.SetMetadata(tnt, "THROWER", player.getName());
	}

	@EventHandler
	public void onLaunch(ProjectileHitEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (event.getEntity() instanceof EnderPearl && event.getEntity().getShooter() != null && event.getEntity().getShooter() instanceof Entity)
		{
			Entity shooter = (Entity) event.getEntity().getShooter();
			if (_tntCarry.contains(shooter))
			{
				return;
			}
			Location teleport = event.getEntity().getLocation();
			teleport.setPitch(shooter.getLocation().getPitch());
			teleport.setYaw(shooter.getLocation().getYaw());
			shooter.teleport(teleport);
		}
		if (event.getEntity() instanceof Arrow)
		{
			Manager.runSyncLater(event.getEntity()::remove, 1L);
		}
	}

	@EventHandler
	public void onOpenChest(PlayerInteractEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CHEST)
		{
			if (UtilPlayer.isSpectator(event.getPlayer()))
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onDamage(EntityDamageEvent event)
	{
		if (!IsLive())
		{
			return;
		}
		if (event.getEntity() instanceof EnderCrystal)
		{
			event.setCancelled(true);
			return;
		}
		if (event.getEntity() instanceof Zombie)
		{
			event.setCancelled(true);
			if (event instanceof EntityDamageByEntityEvent)
			{
				if (!event.getEntity().getCustomName().contains("Ryan") && !event.getEntity().getCustomName().contains("Jon"))
				{
					return;
				}
				GameTeam owner = event.getEntity().getCustomName().contains("Ryan") ? GetTeam(ChatColor.AQUA) : GetTeam(ChatColor.RED);
				EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
				if (e.getDamager() instanceof Player)
				{
					Player p = (Player) e.getDamager();
					if (UtilPlayer.isSpectator(p))
					{
						return;
					}
					if (owner.HasPlayer(p))
					{
						return;
					}
					if (_crystals.get(owner).stream().filter(TeamCrystal::isActive).count() > 0)
					{
						UtilPlayer.message(p, F.main("Game", "You cannot attack the enemy king until your team has destroyed his protective crystals!"));
						return;
					}
					TeamKing king = _kings.get(owner);
					if (king.handleDamage(p.getName(), e.getDamage()))
					{
						for (Player alert : owner.GetPlayers(true))
						{
							if (Recharge.Instance.use(alert, "KingDamageAlert", 5000, false, false))
							{
								alert.playSound(alert.getLocation(), Sound.ANVIL_LAND, 10, 3);
								alert.sendMessage(king.getName(true) + " is under attack!");
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		ItemStack drop = event.getItemDrop().getItemStack();
		if (drop.hasItemMeta() && drop.getItemMeta().hasLore() && drop.getItemMeta().getLore().stream().map(ChatColor::stripColor).anyMatch(lore -> lore.equals("Kit Item")))
		{
			event.setCancelled(true);
			return;
		}

		event.getItemDrop().remove();
	}

	@EventHandler
	public void craftItem(PrepareItemCraftEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (event.getInventory().getResult().getType() == Material.FLINT_AND_STEEL)
		{
			event.getInventory().setResult(_flintAndSteel.build());
		}
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent event)
	{
		if (!IsLive())
		{
			return;
		}
		ItemStack current = event.getCurrentItem();
		if (event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD)
		{
			current = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
		}
		if (current == null || !current.hasItemMeta())
		{
			return;
		}
		if (current.getItemMeta().hasDisplayName() && current.getItemMeta().getDisplayName().equals(C.cRed + "TNT"))
		{
			event.setCancelled(true);
			return;
		}
		if (event.getView().getTopInventory() != null && event.getView().getTopInventory().getType() == InventoryType.CHEST)
		{
			if (current.getItemMeta().hasLore())
			{
				for (String lore : current.getItemMeta().getLore())
				{
					if (ChatColor.stripColor(lore).equalsIgnoreCase("Kit Item"))
					{
						event.setCancelled(true);
						break;
					}
				}
			}

		}
	}

	@EventHandler
	public void onPearl(PlayerInteractEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		if (event.getItem() != null && event.getItem().getType() == Material.ENDER_PEARL)
		{
			Player player = (Player) event.getPlayer();
			if (!Recharge.Instance.use(player, "Enderpearl", 4000, true, true))
			{
				event.setCancelled(true);
				player.updateInventory();
			}
		}
	}

	@EventHandler
	public void onItemDespawn(ItemDespawnEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (_tntSpawner.isSpawned())
		{
			if (_tntSpawner.getItem().getEntityId() == event.getEntity().getEntityId())
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPickup(PlayerPickupItemEvent event)
	{
		if (!IsLive())
		{
			return;
		}
		if (UtilPlayer.isSpectator(event.getPlayer()))
		{
			return;
		}

		if (_tntSpawner.isSpawned() && event.getItem().getEntityId() == _tntSpawner.getItem().getEntityId())
		{
			event.setCancelled(true);
			if (!_tntCarry.contains(event.getPlayer()))
			{
				_tntSpawner.pickup();
				_tntCarry.add(event.getPlayer());
				event.getPlayer().setMetadata("OLD_HELM", new FixedMetadataValue(UtilServer.getPlugin(), event.getPlayer().getInventory().getHelmet()));
				event.getPlayer().setMetadata("TNT_START", new FixedMetadataValue(UtilServer.getPlugin(), System.currentTimeMillis()));
				event.getPlayer().getInventory().setHelmet(_wearableTnt.build());
				UtilPlayer.message(event.getPlayer(), F.main("Game", "You picked up " + F.skill("TNT") + "."));
				UtilPlayer.message(event.getPlayer(), F.main("Game", F.elem("Right-Click") + " to detonate yourself."));
				UtilPlayer.message(event.getPlayer(), F.main("Game", "Run to the enemy Crystal and Detonate to destroy it."));
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void TNTUse(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!_tntCarry.contains(player))
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		event.setCancelled(true);

		player.getInventory().setHelmet((ItemStack) player.getMetadata("OLD_HELM").get(0).value());
		player.removeMetadata("OLD_HELM", UtilServer.getPlugin());
		player.removeMetadata("TNT_START", UtilServer.getPlugin());
		_tntCarry.remove(player);

		TNTPrimed tnt = player.getWorld().spawn(player.getEyeLocation(), TNTPrimed.class);
		UtilEnt.SetMetadata(tnt, "THROWER", player.getName());
		UtilEnt.SetMetadata(tnt, "OBJECTIVE_TNT", true);
		tnt.setFuseTicks(0);
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill("Detonate") + "."));
	}

	@EventHandler
	public void TNTExpire(UpdateEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		Bukkit.getOnlinePlayers().forEach(player ->
		{
			player.getInventory().remove(_wearableTnt.build());
		});

		Iterator<Player> tntIterator = _tntCarry.iterator();

		while (tntIterator.hasNext())
		{
			Player player = tntIterator.next();

			if (player.isDead() || UtilTime.elapsed(player.getMetadata("TNT_START").get(0).asLong(), 60000))
			{
				TNTPrimed tnt = player.getWorld().spawn(player.getEyeLocation(), TNTPrimed.class);
				UtilEnt.SetMetadata(tnt, "THROWER", player.getName());
				UtilEnt.SetMetadata(tnt, "OBJECTIVE_TNT", true);
				tnt.setFuseTicks(0);

				if (!player.isDead())
				{
					player.getInventory().setHelmet((ItemStack) player.getMetadata("OLD_HELM").get(0).value());
				}
				player.removeMetadata("OLD_HELM", UtilServer.getPlugin());
				player.removeMetadata("TNT_START", UtilServer.getPlugin());

				tntIterator.remove();
				continue;
			}

			List<TeamCrystal> crystals = new ArrayList<>();

			for (List<TeamCrystal> c : _crystals.values())
			{
				crystals.addAll(c);
			}
			for (TeamCrystal crystal : crystals)
			{
				if (crystal.isActive() && !crystal.getOwner().HasPlayer(player) && UtilMath.offset(player.getLocation(), crystal.getLocation()) <= 3)
				{
					TNTPrimed tnt = player.getWorld().spawn(player.getEyeLocation(), TNTPrimed.class);
					UtilEnt.SetMetadata(tnt, "THROWER", player.getName());
					UtilEnt.SetMetadata(tnt, "OBJECTIVE_TNT", true);
					tnt.setFuseTicks(0);

					if (!player.isDead())
					{
						player.getInventory().setHelmet((ItemStack) player.getMetadata("OLD_HELM").get(0).value());
					}
					player.removeMetadata("OLD_HELM", UtilServer.getPlugin());
					player.removeMetadata("TNT_START", UtilServer.getPlugin());

					tntIterator.remove();
				}
			}

			UtilTextBottom.display(GetTeam(player).GetColor() + player.getName() + " has the TNT!", UtilServer.getPlayers());
			UtilFirework.playFirework(player.getEyeLocation(), Type.BURST, GetTeam(player).GetColorBase(), false, false);
			if (player.getInventory().getHelmet() == null || player.getInventory().getHelmet().getType() == Material.AIR)
			{
				player.getInventory().setHelmet(_wearableTnt.build());
			}
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();

		_streakData.getOrDefault(player, new KillStreakData()).reset();

		if (!_tntCarry.contains(player))
		{
			return;
		}

		player.removeMetadata("OLD_HELM", UtilServer.getPlugin());
		player.removeMetadata("TNT_START", UtilServer.getPlugin());
		_tntCarry.remove(player);
		TNTPrimed tnt = player.getWorld().spawn(player.getEyeLocation(), TNTPrimed.class);
		UtilEnt.SetMetadata(tnt, "THROWER", player.getName());
		UtilEnt.SetMetadata(tnt, "OBJECTIVE_TNT", true);
		tnt.setFuseTicks(0);
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill("Detonate") + "."));
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (!_tntSpawner.canPlaceFireAt(event.getBlock()))
		{
			event.setCancelled(true);
		}
	}
}