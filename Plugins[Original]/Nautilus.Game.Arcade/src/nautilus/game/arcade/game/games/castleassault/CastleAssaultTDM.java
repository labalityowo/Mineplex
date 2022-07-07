package nautilus.game.arcade.game.games.castleassault;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dispenser;
import org.bukkit.material.MaterialData;

import mineplex.core.Managers;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
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
import nautilus.game.arcade.events.FirstBloodEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.GemData;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.castleassault.data.KillStreakData;
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

public class CastleAssaultTDM extends TeamGame
{
	private static final int MAX_FLINT_AND_STEEL_USES = 4;
	private static final int ITEMS_PER_CHEST = 5;
	private static final long TIME_TILL_REFILL = 2 * 60 * 1000;
	private static final int KILLS_TO_WIN = 50;
	
	private long _lastRefill;
	
	private ItemBuilder _flintAndSteel;
	
	private Map<Player, KillStreakData> _streakData = new WeakHashMap<>();
	private Map<GameTeam, Integer> _teamKills = new HashMap<>();
	
	private List<Block> _chests = new ArrayList<>();
	
	private ChestLoot _rangedGear = new ChestLoot(true);
	private ChestLoot _rodsAndGaps = new ChestLoot(true);
	private ChestLoot _potionGearCommon = new ChestLoot(true);
	private ChestLoot _potionGearRare = new ChestLoot(true);
	private ChestLoot _miscGear = new ChestLoot();
	
	private boolean _writeScoreboard = true;
	
	@SuppressWarnings("deprecation")
	public CastleAssaultTDM(ArcadeManager manager)
	{
		super(manager, GameType.CastleAssaultTDM,
			new Kit[]
			{
				//new KitAlchemist(manager),
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
				"Work with your team",
				"To slay the enemy.",
				"First team to 50 kills",
				"Wins the game and glory!"
			}
		);
		
		_help = new String[]
		{
			"Purchase kit upgrades by earning and spending crowns from games",
			"Each kit has special starter items, be sure to use them to your advantage in fights",
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
		
		new CompassModule()
			.setGiveCompass(true)
			.setGiveCompassToSpecs(true)
			.setGiveCompassToAlive(false)
			.register(this);

		getModule(GameSummaryModule.class)
				.replaceComponent(GameSummaryComponentType.GEMS, new GemSummaryComponent(this::GetGems, C.cGold, "Crowns"));

		_flintAndSteel = new ItemBuilder(Material.FLINT_AND_STEEL).setData((short) (Material.FLINT_AND_STEEL.getMaxDurability() - MAX_FLINT_AND_STEEL_USES));
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
			_rangedGear.addLoot(new ItemStack(Material.BOW), 3);
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
			_potionGearRare.addLoot(new ItemBuilder(Material.POTION).setData((short)8195).build(), 2);
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
		GameTeam red = GetTeam(ChatColor.RED);
		GameTeam blue = GetTeam(ChatColor.AQUA);
		Scoreboard.write(red.GetFormattedName() + " Team Kills");
		Scoreboard.write(_teamKills.get(red) + "/" + KILLS_TO_WIN);
		Scoreboard.writeNewLine();
		Scoreboard.write(blue.GetFormattedName() + " Team Kills");
		Scoreboard.write(_teamKills.get(blue) + "/" + KILLS_TO_WIN);
		Scoreboard.draw();
	}
	
	public void writeFinalScoreboard(String winner, int kills)
	{
		_writeScoreboard = false;
		Scoreboard.reset();
		Scoreboard.writeNewLine();
		Scoreboard.write(winner + C.cWhite + " has won");
		Scoreboard.write(C.cWhite + "with " + C.cGreen + kills + C.cWhite + " kills!");
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
		_teamKills.put(red, 0);
		GameTeam blue = GetTeam(ChatColor.AQUA);
		_teamKills.put(blue, 0);
		this.CreatureAllowOverride = true;
		for (Kit kit : GetKits())
		{
			List<Location> spawns = WorldData.GetCustomLocs(kit.GetName().toUpperCase());
			for (Location spawn : spawns)
			{
				kit.getGameKit().createNPC(spawn);
			}
		}
		this.CreatureAllowOverride = false;
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
				AnnounceEnd(winner);
				writeFinalScoreboard(winner.GetColor() + winner.GetName(), _teamKills.get(winner));
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
			if (_teamKills.get(blue) >= KILLS_TO_WIN)
			{
				AnnounceEnd(blue);
				writeFinalScoreboard(blue.GetColor() + blue.GetName(), _teamKills.get(blue));
				for (GameTeam team : GetTeamList())
				{
					for (Player player : team.GetPlayers(true))
					{
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
			if (_teamKills.get(red) >= KILLS_TO_WIN)
			{
				AnnounceEnd(red);
				writeFinalScoreboard(red.GetColor() + red.GetName(), _teamKills.get(red));
				for (GameTeam team : GetTeamList())
				{
					for (Player player : team.GetPlayers(true))
					{
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
			Manager.GetDamage().SetEnabled(false);
			Manager.GetExplosion().setEnabled(false);
			Manager.GetCreature().SetDisableCustomDrops(true);
		}
		
		if (event.GetState() == GameState.End)
		{
			Manager.GetDamage().SetEnabled(true);
			Manager.GetExplosion().setEnabled(true);
			Manager.GetCreature().SetDisableCustomDrops(false);
			Managers.get(LeaderboardManager.class).unregisterLeaderboard("TOP_CASTLEASSAULTTDM_DAILY_WINS");
			Managers.get(LeaderboardManager.class).unregisterLeaderboard("TOP_CASTLEASSAULTTDM_DAILY_KILLS");
			Managers.get(LeaderboardManager.class).unregisterLeaderboard("TOP_CASTLEASSAULTTDM_WINS");
			Managers.get(LeaderboardManager.class).unregisterLeaderboard("TOP_CASTLEASSAULTTDM_KILLS");
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
		_teamKills.merge(GetTeam(player), 1, Integer::sum);
		
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
			
			double damage = 8 * mult;
			damage = damage * (1 - (blastProtEPF / 25));
			
			double knockbackReduction = 1 - (highestBlastProt * 0.15);
			
			near.damage(damage, event.getEntity());
			UtilAction.velocity(near, UtilAlg.getTrajectory(event.getEntity().getLocation(), near.getLocation()), 1 * mult * knockbackReduction, false, 0, mult * knockbackReduction, 10, true);
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
		if (event.getView().getTopInventory() != null && event.getView().getTopInventory().getType() == InventoryType.CHEST)
		{
			ItemStack current = event.getCurrentItem();
			if (event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD)
			{
				current = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
			}
			
			if (current != null && current.hasItemMeta())
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
	}
	
	@EventHandler
	public void onFirstBlood(FirstBloodEvent event)
	{
		if (!IsLive())
		{
			return;
		}
		
		AddStat(event.getPlayer(), "FirstBlood", 1, true, false);
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
	public void onDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		
		_streakData.getOrDefault(player, new KillStreakData()).reset();
	}
}