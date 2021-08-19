package nautilus.game.arcade.game.games.survivalgames;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Egg;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.inventory.BeaconInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.NameTagVisibility;

import com.google.common.collect.Lists;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.survivalgames.kit.KitArcher;
import nautilus.game.arcade.game.games.survivalgames.kit.KitAssassin;
import nautilus.game.arcade.game.games.survivalgames.kit.KitAxeman;
import nautilus.game.arcade.game.games.survivalgames.kit.KitBarbarian;
import nautilus.game.arcade.game.games.survivalgames.kit.KitBeastmaster;
import nautilus.game.arcade.game.games.survivalgames.kit.KitBomber;
import nautilus.game.arcade.game.games.survivalgames.kit.KitBrawler;
import nautilus.game.arcade.game.games.survivalgames.kit.KitHorseman;
import nautilus.game.arcade.game.games.survivalgames.kit.KitKnight;
import nautilus.game.arcade.game.games.survivalgames.kit.KitNecromancer;
import nautilus.game.arcade.game.games.survivalgames.misison.BowHorseKillTracker;
import nautilus.game.arcade.game.games.survivalgames.modules.BorderModule;
import nautilus.game.arcade.game.games.survivalgames.modules.FurnaceLootModule;
import nautilus.game.arcade.game.games.survivalgames.modules.SupplyDropModule;
import nautilus.game.arcade.game.games.survivalgames.modules.TrackingCompassModule;
import nautilus.game.arcade.game.modules.CustomScoreboardModule;
import nautilus.game.arcade.game.modules.EXPForKillsModule;
import nautilus.game.arcade.game.modules.ThrowableTNTModule;
import nautilus.game.arcade.game.modules.chest.ChestLootModule;
import nautilus.game.arcade.game.modules.chest.ChestLootPool;
import nautilus.game.arcade.game.modules.combattracker.CombatData;
import nautilus.game.arcade.game.modules.combattracker.CombatTrackerModule;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.game.modules.winstreak.WinStreakModule;
import nautilus.game.arcade.game.modules.worldmap.WorldMapModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.managers.chat.ChatStatData;
import nautilus.game.arcade.scoreboard.GameScoreboard;
import nautilus.game.arcade.stats.KillsWithinTimeLimitStatTracker;
import nautilus.game.arcade.stats.SimultaneousSkeletonStatTracker;
import nautilus.game.arcade.stats.WinWithoutWearingArmorStatTracker;

public abstract class SurvivalGamesNew extends Game
{

	private static final long PREPARE_TIME = TimeUnit.SECONDS.toMillis(15);
	private static final long REFILL_TIME = TimeUnit.MINUTES.toMillis(7);
	private static final long REFILL_INFORM_TIME = TimeUnit.MINUTES.toMillis(3);
	private static final long KIT_COOLDOWN = TimeUnit.SECONDS.toMillis(20);
	private static final long END_DAMAGE_TIME = TimeUnit.MINUTES.toMillis(20);
	private static final int MAX_ITEM_SPAWN_DISTANCE_SQUARED = 36;
	private static final String START_EFFECT_REASON = "Start Effect";
	private static final int START_EFFECT_DURATION = 30;
	private static final int ENCHANTMENT_TABLES = 5;
	private static final int CRAFTING_TABLES = 10;
	private static final int DISTANCE_NON_ASSASSIN = 576;
	private static final int DISTANCE_ASSASSIN = 64;
	private static final FireworkEffect DEATH_EFFECT = FireworkEffect.builder()
			.with(Type.BALL)
			.withColor(Color.RED)
			.build();

	private List<Location> _chests;
	private SupplyDropModule _supplyDrop;
	private final CombatTrackerModule _combatTrackerModule;
	private final CustomScoreboardModule _customScoreboardModule;
	private boolean _refilled;

	SurvivalGamesNew(ArcadeManager manager, GameType gameType, String[] gameDesc)
	{
		super(manager, gameType, new Kit[]
				{
						new KitAxeman(manager),
						new KitKnight(manager),
						new KitArcher(manager),
						new KitBrawler(manager),
						new KitAssassin(manager),
						new KitBeastmaster(manager),
						new KitBomber(manager),
						new KitNecromancer(manager),
						new KitBarbarian(manager),
						new KitHorseman(manager)
				}, gameDesc);

		AnnounceStay = false;
		StrictAntiHack = true;

		HideTeamSheep = true;
		ReplaceTeamsWithKits = true;

		DeathDropItems = true;
		QuitDropItems = true;

		DamageSelf = true;
		DeadBodies = true;

		ItemDrop = true;
		ItemPickup = true;

		InventoryClick = true;
		InventoryOpenBlock = true;
		InventoryOpenChest = true;

		PrepareTime = PREPARE_TIME;
		PlaySoundGameStart = false;

		WorldTimeSet = 0;
		WorldBoundaryKill = false;

		BlockBreakAllow.add(Material.WEB.getId());
		BlockPlaceAllow.add(Material.WEB.getId());

		BlockBreakAllow.add(Material.LEAVES.getId());
		BlockBreakAllow.add(Material.LEAVES_2.getId());

		BlockPlaceAllow.add(Material.CAKE_BLOCK.getId());
		BlockBreakAllow.add(Material.CAKE_BLOCK.getId());

		BlockBreakAllow.add(Material.LONG_GRASS.getId());
		BlockBreakAllow.add(Material.RED_ROSE.getId());
		BlockBreakAllow.add(Material.YELLOW_FLOWER.getId());
		BlockBreakAllow.add(Material.BROWN_MUSHROOM.getId());
		BlockBreakAllow.add(Material.RED_MUSHROOM.getId());
		BlockBreakAllow.add(Material.DEAD_BUSH.getId());
		BlockBreakAllow.add(Material.CARROT.getId());
		BlockBreakAllow.add(Material.POTATO.getId());
		BlockBreakAllow.add(Material.DOUBLE_PLANT.getId());
		BlockBreakAllow.add(Material.CROPS.getId());
		BlockBreakAllow.add(Material.SAPLING.getId());
		BlockBreakAllow.add(Material.VINE.getId());
		BlockBreakAllow.add(Material.WATER_LILY.getId());

		_help = new String[]
				{
						"Use a Compass to find and kill enemies!",
						"You lose Speed 2 at start of game if you attack.",
						"Avoid enemies who have better gear than you!",
						"Supply drops appear on your item map!"
				};

		manager.GetCreature().SetDisableCustomDrops(true);

		registerStatTrackers
				(
						new WinWithoutWearingArmorStatTracker(this),
						new KillsWithinTimeLimitStatTracker(this, 3, 60, "Bloodlust"),
						new SimultaneousSkeletonStatTracker(this, 5)
				);

		registerMissions
				(
						new BowHorseKillTracker(this)
				);

		registerChatStats
				(
						Kills,
						Assists,
						BlankLine,
						DamageTaken,
						DamageDealt,
						BlankLine,
						new ChatStatData("SupplyDropsOpened", "Supply Drops Opened", true)
				);

		new CompassModule()
				.register(this);

		new WinStreakModule()
				.register(this);

		new EXPForKillsModule()
				.register(this);

		_supplyDrop = new SupplyDropModule();
		_supplyDrop.register(this);

		_combatTrackerModule = new CombatTrackerModule();
		_combatTrackerModule.register(this);

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
							break;
					}
				})
				.setPrefix((perspective, subject) ->
				{
					GameTeam team = GetTeam(subject);

					return team == null || !team.IsAlive(subject) ? C.cGray : team.GetColor().toString();
				})
				.setNameTagVisibility((perspective, subject) ->
				{
					if (!IsLive() || !IsAlive(perspective) || !IsAlive(subject))
					{
						return NameTagVisibility.ALWAYS;
					}

					GameTeam perspectiveTeam = GetTeam(perspective);
					GameTeam subjectTeam = GetTeam(subject);
					Kit kit = GetKit(subject);

					if (perspectiveTeam == null || subjectTeam == null || perspectiveTeam.equals(subjectTeam) && TeamMode)
					{
						return NameTagVisibility.ALWAYS;
					}
					else
					{
						return UtilMath.offsetSquared(perspective, subject) > (kit instanceof KitAssassin ? DISTANCE_ASSASSIN : DISTANCE_NON_ASSASSIN) || !perspective.hasLineOfSight(subject) ? NameTagVisibility.NEVER : NameTagVisibility.ALWAYS;
					}
				});
		_customScoreboardModule.register(this);
	}

	private void writePrepare(Player player, GameScoreboard scoreboard)
	{
		GameTeam team = GetTeam(player);

		scoreboard.writeNewLine();

		scoreboard.write((team == null ? "" : team.GetColor()) + C.Bold + "Survival Games");
		scoreboard.write(WorldData.MapName);

		scoreboard.writeNewLine();

		scoreboard.write(C.cYellowB + "Tributes");
		scoreboard.write(GetPlayers(true).size() + " Players");

		if (team != null && TeamMode)
		{
			scoreboard.writeNewLine();
			scoreboard.write(team.GetFormattedName());

			team.GetPlayers(false).forEach(teammate -> scoreboard.write(teammate.getName()));
		}

		scoreboard.writeNewLine();
	}

	private void writeLive(Player player, GameScoreboard scoreboard)
	{
		GameTeam team = GetTeam(player);
		List<Player> alive = GetPlayers(true);
		Location supplyDrop = _supplyDrop.getCurrentDrop();
		long supplyIn = (SupplyDropModule.TIME - WorldTimeSet) * 50 / 4;
		long chestRefillIn = Math.max(0, GetStateTime() + REFILL_TIME - System.currentTimeMillis());
		String event, in;

		if (supplyDrop != null)
		{
			event = "Supply Drop";
			in = "(" + supplyDrop.getBlockX() + ", " + supplyDrop.getBlockZ() + ")";
		}
		else if (!_refilled && chestRefillIn < REFILL_INFORM_TIME)
		{
			event = "Chest Refill";
			in = UtilTime.MakeStr(chestRefillIn);
		}
		else
		{
			event = "Supply Drop";
			in = UtilTime.MakeStr(supplyIn);
		}

		scoreboard.writeNewLine();

		scoreboard.write(C.cYellowB + "Tributes");

		if (TeamMode)
		{
			List<GameTeam> aliveTeams = GetTeamList().stream()
					.filter(GameTeam::IsTeamAlive)
					.collect(Collectors.toList());

			if (alive.size() < 5)
			{
				for (GameTeam teams : GetTeamList())
				{
					for (Player teamMember : teams.GetPlayers(true))
					{
						scoreboard.write(teams.GetColor() + teamMember.getName());
					}
				}
			}
			else if (aliveTeams.size() < 5)
			{
				for (GameTeam teams : aliveTeams)
				{
					scoreboard.write(teams.GetPlayers(true).size() + " " + teams.GetColor() + teams.GetName());
				}
			}
			else
			{
				scoreboard.write(alive.size() + " Alive");
			}
		}
		else
		{
			if (alive.size() > 6)
			{
				scoreboard.write(alive.size() + " Alive");
			}
			else
			{
				alive.forEach(other -> scoreboard.write(other.getName()));
			}
		}

		writeStats(player, team, scoreboard);

		scoreboard.writeNewLine();

		scoreboard.write(C.cRedB + event);
		scoreboard.write(in);
	}

	private void writeEnd(Player player, GameScoreboard scoreboard)
	{
		List<Player> winners = getWinners();

		writeStats(player, GetTeam(player), scoreboard);

		if (winners == null)
		{
			return;
		}

		scoreboard.writeNewLine();
		scoreboard.write(C.cYellowB + "Winner" + (TeamMode ? "s" : ""));

		for (Player winner : winners)
		{
			scoreboard.write(winner.getName());
		}
	}

	private void writeStats(Player player, GameTeam team, GameScoreboard scoreboard)
	{
		if (team != null)
		{
			scoreboard.writeNewLine();

			scoreboard.write(C.cGoldB + "Stats");

			CombatData combatData = _combatTrackerModule.getCombatData(player);

			scoreboard.write("Kills: " + C.cGreen + combatData.getKills());
			scoreboard.write("Assists: " + C.cGreen + combatData.getAssits());

			if (TeamMode)
			{
				int teamKills = 0, teamAssists = 0;

				for (Player teammate : team.GetPlayers(false))
				{
					CombatData teammateCombatData = _combatTrackerModule.getCombatData(teammate);

					teamKills += teammateCombatData.getKills();
					teamAssists += teammateCombatData.getAssits();
				}

				scoreboard.write("Team Kills: " + C.cGreen + teamKills);
				scoreboard.write("Team Assists: " + C.cGreen + teamAssists);
			}
		}
	}


	@Override
	public void ScoreboardUpdate(UpdateEvent event)
	{
	}

	@Override
	public void ParseData()
	{
		new BorderModule()
				.register(this);

		setupLoot();

		new WorldMapModule(WorldData, new SurvivalGamesMapRenderer(this))
				.register(this);
	}

	private void setupLoot()
	{
		ThrowableTNTModule tntModule = new ThrowableTNTModule()
				.setThrowStrength(0.3);
		tntModule.register(this);
		ItemStack tnt = tntModule.getTntItem();

		TrackingCompassModule compassModule = new TrackingCompassModule();
		compassModule.register(this);

		new FurnaceLootModule()
				.register(this);

		Location center = GetSpectatorLocation();
		_chests = WorldData.GetCustomLocs(String.valueOf(Material.CHEST.getId()));
		List<Location> midChests = _chests.stream()
				.filter(location -> UtilMath.offset2dSquared(location, center) < 64)
				.collect(Collectors.toList());
		_chests.removeAll(midChests);

		ChestLootModule lootModule = new ChestLootModule();

		setupTier1Loot(lootModule, compassModule, tnt, _chests);
		setupTier2Loot(lootModule, compassModule, tnt, midChests);

		lootModule.register(this);
	}

	protected void setupTier1Loot(ChestLootModule lootModule, TrackingCompassModule compassModule, ItemStack tnt, List<Location> chests)
	{
		lootModule.registerChestType("Tier 1", chests, 0.35,

				new ChestLootPool()
						.addItem(new ItemStack(Material.WOOD_AXE), 240)
						.addItem(new ItemStack(Material.WOOD_SWORD), 210)
						.addItem(new ItemStack(Material.STONE_AXE), 180)
						.addItem(new ItemStack(Material.STONE_SWORD))
						.setUnbreakable(true)
				,

				new ChestLootPool()
						.addItem(new ItemStack(Material.LEATHER_HELMET))
						.addItem(new ItemStack(Material.LEATHER_CHESTPLATE))
						.addItem(new ItemStack(Material.LEATHER_LEGGINGS))
						.addItem(new ItemStack(Material.LEATHER_BOOTS))
						.addItem(new ItemStack(Material.GOLD_HELMET), 75)
						.addItem(new ItemStack(Material.GOLD_CHESTPLATE), 75)
						.addItem(new ItemStack(Material.GOLD_LEGGINGS), 75)
						.addItem(new ItemStack(Material.GOLD_BOOTS), 75)
						.addItem(new ItemStack(Material.CHAINMAIL_HELMET), 30)
						.addItem(new ItemStack(Material.CHAINMAIL_CHESTPLATE), 30)
						.addItem(new ItemStack(Material.CHAINMAIL_LEGGINGS), 30)
						.addItem(new ItemStack(Material.CHAINMAIL_BOOTS), 30)
						.setAmountsPerChest(1, 2)
						.setUnbreakable(true)
				,

				new ChestLootPool()
						.addItem(new ItemStack(Material.FISHING_ROD))
						.addItem(new ItemStack(Material.BOW), 60)
						.addItem(new ItemStack(Material.ARROW), 1, 3, 50)
						.addItem(new ItemStack(Material.SNOW_BALL), 1, 2)
						.addItem(new ItemStack(Material.EGG), 1, 2)
						.setUnbreakable(true)
				,

				new ChestLootPool()
						.addItem(new ItemStack(Material.BAKED_POTATO), 1, 3)
						.addItem(new ItemStack(Material.COOKED_BEEF), 1, 2)
						.addItem(new ItemStack(Material.COOKED_CHICKEN), 1, 3)
						.addItem(new ItemStack(Material.CARROT_ITEM), 1, 3)
						.addItem(new ItemStack(Material.WHEAT), 1, 3)
						.addItem(new ItemStack(Material.APPLE), 1, 3)
						.addItem(new ItemStack(Material.PORK), 1, 3)
						.addItem(new ItemStack(Material.MUSHROOM_SOUP), 80)
				,

				new ChestLootPool()
						.addItem(new ItemStack(Material.EXP_BOTTLE), 1, 2)
						.addItem(new ItemStack(Material.STICK), 1, 2)
						.addItem(new ItemStack(Material.BOAT), 50)
						.addItem(new ItemStack(Material.FLINT), 1, 2, 70)
						.addItem(new ItemStack(Material.FEATHER), 1, 2, 70)
						.addItem(new ItemStack(Material.GOLD_INGOT), 1, 1, 80)
						.addItem(compassModule.getCompass(5))
						.addItem(tnt, 50)
		);
	}

	protected void setupTier2Loot(ChestLootModule lootModule, TrackingCompassModule compassModule, ItemStack tnt, List<Location> chests)
	{
		lootModule.registerChestType("Tier 2", chests,

				new ChestLootPool()
						.addItem(new ItemStack(Material.WOOD_AXE), 240)
						.addItem(new ItemStack(Material.WOOD_SWORD), 210)
						.addItem(new ItemStack(Material.STONE_AXE), 180)
						.addItem(new ItemStack(Material.STONE_SWORD))
						.addItem(new ItemStack(Material.IRON_AXE))
						.setUnbreakable(true)
				,

				new ChestLootPool()
						.addItem(new ItemStack(Material.LEATHER_HELMET))
						.addItem(new ItemStack(Material.LEATHER_CHESTPLATE))
						.addItem(new ItemStack(Material.LEATHER_LEGGINGS))
						.addItem(new ItemStack(Material.LEATHER_BOOTS))
						.addItem(new ItemStack(Material.GOLD_HELMET), 75)
						.addItem(new ItemStack(Material.GOLD_CHESTPLATE), 75)
						.addItem(new ItemStack(Material.GOLD_LEGGINGS), 75)
						.addItem(new ItemStack(Material.GOLD_BOOTS), 75)
						.addItem(new ItemStack(Material.CHAINMAIL_HELMET), 75)
						.addItem(new ItemStack(Material.CHAINMAIL_CHESTPLATE), 75)
						.addItem(new ItemStack(Material.CHAINMAIL_LEGGINGS), 75)
						.addItem(new ItemStack(Material.CHAINMAIL_BOOTS), 75)
						.addItem(new ItemStack(Material.IRON_HELMET), 50)
						.addItem(new ItemStack(Material.IRON_CHESTPLATE), 50)
						.addItem(new ItemStack(Material.IRON_LEGGINGS), 50)
						.addItem(new ItemStack(Material.IRON_BOOTS), 50)
						.setAmountsPerChest(1, 2)
						.setUnbreakable(true)
				,

				new ChestLootPool()
						.addItem(new ItemStack(Material.FISHING_ROD))
						.addItem(new ItemStack(Material.BOW), 50)
						.addItem(new ItemStack(Material.ARROW), 1, 3, 50)
						.addItem(new ItemStack(Material.SNOW_BALL), 1, 2)
						.addItem(new ItemStack(Material.EGG), 1, 2)
						.setUnbreakable(true)
				,

				new ChestLootPool()
						.addItem(new ItemStack(Material.BAKED_POTATO), 1, 3)
						.addItem(new ItemStack(Material.COOKED_BEEF), 1, 2)
						.addItem(new ItemStack(Material.COOKED_CHICKEN), 1, 3)
						.addItem(new ItemStack(Material.CARROT_ITEM), 1, 3)
						.addItem(new ItemStack(Material.WHEAT), 1, 3)
						.addItem(new ItemStack(Material.APPLE), 1, 3)
						.addItem(new ItemStack(Material.PORK), 1, 3)
						.addItem(new ItemStack(Material.MUSHROOM_SOUP), 80)
						.addItem(new ItemStack(Material.CAKE), 80)
				,

				new ChestLootPool()
						.addItem(new ItemStack(Material.EXP_BOTTLE), 1, 2)
						.addItem(new ItemStack(Material.STICK), 1, 2)
						.addItem(new ItemStack(Material.BOAT), 50)
						.addItem(new ItemStack(Material.FLINT), 1, 2, 70)
						.addItem(new ItemStack(Material.FEATHER), 1, 2, 70)
						.addItem(new ItemStack(Material.GOLD_INGOT), 1, 1, 80)
						.addItem(compassModule.getCompass(5))
						.addItem(new ItemStack(Material.IRON_INGOT), 50)
						.addItem(new ItemStack(Material.DIAMOND), 50)
						.addItem(tnt, 50)

		);
	}

	public void setupSupplyDropLoot(Map<Integer, List<ItemStack>> items)
	{
		items.put(1, Lists.newArrayList
				(
						new ItemStack(Material.IRON_BOOTS),
						new ItemStack(Material.IRON_SWORD),
						new ItemStack(Material.DIAMOND_AXE)
				));
		items.put(2, Lists.newArrayList
				(
						new ItemStack(Material.IRON_CHESTPLATE),
						new ItemStack(Material.IRON_LEGGINGS),
						new ItemStack(Material.DIAMOND_HELMET),
						new ItemStack(Material.DIAMOND_BOOTS)
				));
		items.put(3, Lists.newArrayList
				(
						new ItemStack(Material.DIAMOND_CHESTPLATE),
						new ItemStack(Material.DIAMOND_LEGGINGS),
						new ItemStack(Material.DIAMOND_SWORD)
				));
	}

	@EventHandler
	public void updateScoreboard(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		_customScoreboardModule.refresh();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void createTables(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		for (int i = 0; i < ENCHANTMENT_TABLES; i++)
		{
			createRandomBlock(Material.ENCHANTMENT_TABLE);
		}

		for (int i = 0; i < CRAFTING_TABLES; i++)
		{
			createRandomBlock(Material.WORKBENCH);
		}
	}

	private void createRandomBlock(Material type)
	{
		MapUtil.QuickChangeBlockAt(UtilAlg.Random(_chests), type);
	}

	@EventHandler
	public void kitCooldowns(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			Kit kit = GetKit(player);

			for (Perk perk : kit.GetPerks())
			{
				Recharge.Instance.useForce(player, perk.GetName(), KIT_COOLDOWN);
			}
		}

		Manager.runSyncLater(() -> Announce(F.main("Game", "You can now use " + F.skill("Kit Abilities") + ".")), KIT_COOLDOWN / 50);

		Manager.runSyncLater(() ->
		{
			for (Player player : Manager.GetGame().GetPlayers(true))
			{
				if (GetKit(player) instanceof KitArcher)
				{
					player.sendMessage(F.main("Game", "You received your kit " + F.item("Bow") + "."));
					player.getInventory().addItem(KitArcher.BOW);
				}
			}
		}, 90 * 20);
	}

	@EventHandler
	public void timeUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !IsLive())
		{
			return;
		}

		if (WorldTimeSet > 22000 || WorldTimeSet < 14000)
		{
			WorldTimeSet += 4;
		}
		else
		{
			WorldTimeSet += 16;
		}

		WorldTimeSet %= 24000;
	}

	@EventHandler
	public void startEffect(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		ItemStack mapItem = getModule(WorldMapModule.class).getMapItem();

		for (Player player : GetPlayers(true))
		{
			player.playSound(player.getLocation(), Sound.DONKEY_DEATH, 0.8F, 0);

			Manager.GetCondition().Factory()
					.Speed(START_EFFECT_REASON, player, player, START_EFFECT_DURATION, 1, false, false, false);
			Manager.GetCondition().Factory()
					.HealthBoost(START_EFFECT_REASON, player, player, START_EFFECT_DURATION, 1, false, false, false);

			player.setHealth(player.getMaxHealth());
			player.getInventory().setItem(8, mapItem);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void damage(CustomDamageEvent event)
	{
		Player damager = event.GetDamagerPlayer(true);

		event.SetDamageToLevel(false);

		boolean egg = event.GetProjectile() instanceof Egg;
		boolean snowball = event.GetProjectile() instanceof Snowball;

		if (egg || snowball)
		{
			event.AddMod(event.GetDamagerPlayer(true).getName(), (egg ? "Egg" : "Snowball"), 0.5, true);
		}

		if (damager != null && !event.isCancelled())
		{
			Manager.GetCondition().EndCondition(damager, ConditionType.SPEED, START_EFFECT_REASON);
		}
	}

	@EventHandler
	public void boatPlace(VehicleCreateEvent event)
	{
		if (!(event.getVehicle() instanceof Boat))
		{
			return;
		}

		for (Block block : UtilBlock.getSurrounding(event.getVehicle().getLocation().getBlock(), true))
		{
			if (block.isLiquid())
			{
				return;
			}
		}

		event.getVehicle().remove();
	}

	@EventHandler
	public void inventoryOpen(InventoryOpenEvent event)
	{
		if (event.getInventory().getType() == InventoryType.BREWING || event.getInventory() instanceof BeaconInventory)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void preventItemSpawning(ItemSpawnEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (event.getEntity().getItemStack().getType() == Material.MAP)
		{
			event.setCancelled(true);
			return;
		}

		for (Player player : GetPlayers(true))
		{
			if (UtilMath.offsetSquared(event.getEntity(), player) < MAX_ITEM_SPAWN_DISTANCE_SQUARED)
			{
				return;
			}
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void preventCrafting(PrepareItemCraftEvent event)
	{
		ItemStack result = event.getInventory().getResult();

		if (result == null)
		{
			return;
		}

		Material type = result.getType();

		if (type == Material.BUCKET || type == Material.GOLDEN_APPLE || type == Material.FLINT_AND_STEEL || type.isBlock())
		{
			event.getInventory().setResult(null);
		}
	}

	@EventHandler
	public void craftedItems(CraftItemEvent event)
	{
		ItemStack itemStack = event.getCurrentItem();

		if (UtilItem.isWeapon(itemStack) || UtilItem.isArmor(itemStack) || itemStack.getType() == Material.FISHING_ROD)
		{
			UtilItem.makeUnbreakable(itemStack);
		}
	}

	@EventHandler
	public void updateRefill(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || _refilled || !UtilTime.elapsed(GetStateTime(), REFILL_TIME))
		{
			return;
		}

		_refilled = true;
		getModule(ChestLootModule.class)
				.refill();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void explosion(EntityExplodeEvent event)
	{
		event.blockList().clear();
	}

	@EventHandler
	public void blockBurn(BlockBurnEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void blockDecay(LeavesDecayEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void blockFade(BlockFadeEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void blockSpread(BlockSpreadEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void itemFrameBreak(HangingBreakEvent event)
	{
		if (event.getEntity() instanceof ItemFrame)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void blockBreak(BlockBreakEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		Block block = event.getBlock();

		if (UtilBlock.airFoliage(event.getBlock()))
		{
			event.setCancelled(true);
			block.setType(Material.AIR);
		}

		if (event.getBlock().getType() == Material.LEAVES)
		{
			Location location = block.getLocation().add(0.5, 0.5, 0.5);

			event.setCancelled(true);
			block.setType(Material.AIR);

			if (Math.random() < 0.05)
			{
				location.getWorld().dropItemNaturally(location, new ItemStack(Material.STICK));
			}
		}
	}

	@EventHandler
	public void explosionDamage(CustomDamageEvent event)
	{
		Player damagee = event.GetDamageePlayer();

		if (event.GetCause() != DamageCause.ENTITY_EXPLOSION || damagee == null || !UtilEnt.isInWater(damagee))
		{
			return;
		}

		event.AddMod("Water Explosion", -event.GetDamage() * 0.4);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void entityDeath(EntityDeathEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			Location location = event.getEntity().getLocation();

			for (int i = 1; i <= 3; i++)
			{
				UtilFirework.launchFirework(location, DEATH_EFFECT, null, i);
			}

			return;
		}

		event.setDroppedExp(0);
		event.getDrops().clear();
	}

	@EventHandler
	public void updateEndDamage(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !IsLive() || !UtilTime.elapsed(GetStateTime(), END_DAMAGE_TIME))
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.CUSTOM, 2, false, true, true, GetName(), "End Game");
		}
	}

	@Override
	public double GetKillsGems(Player killer, Player killed, boolean assist)
	{
		return assist ? 3 : 12;
	}

	public SupplyDropModule getSupplyDrop()
	{
		return _supplyDrop;
	}
}
