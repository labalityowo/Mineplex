package nautilus.game.arcade.game.games.uhc;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.NextTickListEntry;
import net.minecraft.server.v1_8_R3.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_8_R3.util.HashTreeSet;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.spigotmc.ActivationRange;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.boosters.event.BoosterItemGiveEvent;
import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.CombatLog;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GamePrepareCountdownCommence;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerPrepareTeleportEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.uhc.components.UHCBorder;
import nautilus.game.arcade.game.games.uhc.components.UHCFreezer;
import nautilus.game.arcade.game.games.uhc.components.UHCSpeedMode;
import nautilus.game.arcade.game.games.uhc.stat.CollectFoodStat;
import nautilus.game.arcade.game.games.uhc.stat.HalfHeartHealStat;
import nautilus.game.arcade.game.games.uhc.stat.HoeCraftingStat;
import nautilus.game.arcade.game.games.uhc.stat.LuckyMinerStat;
import nautilus.game.arcade.game.modules.AbsorptionFix;
import nautilus.game.arcade.game.modules.OreVeinEditorModule;
import nautilus.game.arcade.game.modules.PlayerHeadModule;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;

public abstract class UHC extends Game
{
	public enum Perm implements Permission
	{
		DEBUG_ENTITIES_COMMAND,
		DEBUG_CHUNK_COMMAND,
		DEBUG_WORLDINFO_COMMAND,
		DEBUG_STARTPVP_COMMAND,
		DEBUG_UHCGAMES_COMMAND,
		DEBUG_SETCOMBATLOGTIMEOUT_COMMAND,
		DEBUG_DEATHMATCH_COMMAND,
		DEBUG_CALLCHUNKS_COMMAND,
		DEBUG_GC_COMMAND,
	}

	private static int _gamesRun = 0;

	public static final int VIEW_DISTANCE = 5;

	// The number of milliseconds after which PVP should be enabled
	// Initialised in constructor
	public static int SAFE_TIME;

	// The number of milliseconds after which Deathmatch should start.
	// Initialised in constructor
	public static int MINING_TIME;

	// The maximum/starting arena size
	public static final int MAX_ARENA_SIZE = 1000;

	// The deathmatch arena size
	public static final int DEATHMATCH_ARENA_SIZE = 200;

	// The time for the border to shrink to 0 during deathmatch.
	public static final int DEATHMATCH_TIME_SECONDS = 600;

	// The pre-deathmatch teleporting time
	public static final int PRE_DEATHMATCH_TIME_SECONDS = 11;

	// The amount of damage to give from hitting the world border
	public static final int WORLD_BORDER_DAMAGE = 2;
	
	// The distance a player needs to be away from the vertical border to see the particles
	public static final int WORLD_BORDER_PARTICLES_DISTANCE = 20;
	
	// The number of ticks to delay before teleporting each player
	public static final long DELAY_BETWEEN_PLAYER_TELEPORT = 5L;

	// The Objective representing the tab list health display
	private Objective _healthObjective;

	// The number of players which have been teleported
	private int _teleportedPlayers = -1;
	// The total number of players to teleport
	private int _totalPlayers;
	// Whether players are teleporting currently
	private volatile boolean _isTeleporting = false;

	// Border
	private UHCBorder _border;

	// Freeze manager
	private UHCFreezer _freezer;

	// Pre-deathmatch
	private int _secondsSincePreDeathmatch;

	// UHC State
	private UHCState _state;

	// Speed Mode
	private UHCSpeedMode _speedMode;

	public UHC(ArcadeManager manager)
	{
		this(manager, GameType.UHC, false);

		registerChatStats(Kills, Assists, BlankLine, DamageTaken, DamageDealt);
	}

	@SuppressWarnings("unchecked")
	public UHC(ArcadeManager manager, GameType type, boolean speedMode)
	{
		super(manager, type, new Kit[]{new KitUHC(manager)},

				new String[]{"10 minutes of no PvP", "Only Golden Apples restore health", "Ores can only be found in caves", "Borders shrink over time", "Last player/team alive wins!"});

		_gamesRun++;

		SAFE_TIME = (int) TimeUnit.MINUTES.toMillis(10);
		MINING_TIME = (int) (TimeUnit.HOURS.toMillis(1) + SAFE_TIME);

		if (speedMode)
		{
			_speedMode = new UHCSpeedMode(this);
		}

		_state = UHCState.SAFE;

		Prepare = false;

		HideTeamSheep = true;

		StrictAntiHack = true;

		GameTimeout = TimeUnit.HOURS.toMillis(2);

		DamagePvP = false;

		ItemDrop = true;
		ItemPickup = true;

		BlockBreak = true;
		BlockPlace = true;

		InventoryOpenBlock = true;
		InventoryOpenChest = true;
		InventoryClick = true;

		DeathOut = true;
		QuitOut = false;

		CreatureAllow = true;

		AnnounceStay = false;

		DeathMessages = false;
		DeathTeleport = false;

		SoupEnabled = false;

		WorldBoundaryKill = false;

		GemBoosterEnabled = false;
		GemDoubleEnabled = false;
		GemHunterEnabled = false;

		WorldBoneMeal = true;

		GadgetsDisabled = true;
		AllowParticles = false;

		WorldTimeSet = -1;

		WorldLeavesDecay = true;
		WorldBlockGrow = true;
		WorldSoilTrample = true;
		WorldBoneMeal = true;
		WorldChunkUnload = true;

		ShowEveryoneSpecChat = false;

		CraftRecipes();

		// Disable Custom Mob Drops (and EXP Disable)
		Manager.GetCreature().SetDisableCustomDrops(true);

		// Disable Anti-Stack
		setItemMerge(true);

		// Damage Settings
		Manager.GetDamage().SetEnabled(false);

		_healthObjective = Scoreboard.getScoreboard().registerNewObjective("Health", "health");
		_healthObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

		// World Border
		_border = new UHCBorder(this, MAX_ARENA_SIZE);

		// Player Freezer
		_freezer = new UHCFreezer(this);

		new PlayerHeadModule().register(this);
		new CompassModule()
				.setGiveCompassToAlive(true)
				.setGiveCompass(false)
				.register(this);
		new OreVeinEditorModule().removeNonAirVeins().register(this);
		new AbsorptionFix()
			.register(this);

		registerStatTrackers(new CollectFoodStat(this), new HoeCraftingStat(this), new LuckyMinerStat(this), new HalfHeartHealStat(this));

		registerDebugCommand("startpvp", Perm.DEBUG_STARTPVP_COMMAND, PermissionGroup.ADMIN, (caller, args) ->
		{
			if (!IsLive())
			{
				UtilPlayer.message(caller, F.main("Debug", "You can't start the game right now!"));
				return;
			}
			if (_state.isPVP())
			{
				UtilPlayer.message(caller, F.main("Debug", "PvP has already been started!"));
				return;
			}

			MINING_TIME -= SAFE_TIME;
			SAFE_TIME = 0;

			startPvp();
			UtilPlayer.message(caller, F.main("Debug", "Started PvP"));
		});

		registerDebugCommand("worldinfo", Perm.DEBUG_WORLDINFO_COMMAND, PermissionGroup.ADMIN, (caller, args) ->
		{
			if (args == null || args.length == 0)
			{
				UtilPlayer.message(caller, F.main("Debug", "Loaded worlds:"));
				UtilPlayer.message(caller, F.desc("Bukkit Worlds", Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.joining(", "))));
				UtilPlayer.message(caller, F.desc("NMS Worlds", MinecraftServer.getServer().worlds.stream().map(WorldServer::getWorldData).map(
						net.minecraft.server.v1_8_R3.WorldData::getName).collect(Collectors.joining(", "))));
				return;
			}
			if (args[0].equals("info"))
			{
				if (args.length > 1)
				{
					String worldName = args[1];
					World targetWorld = null;

					if (worldName.startsWith("b:"))
					{
						targetWorld = Bukkit.getWorlds().stream().filter(world -> world.getName().replace(" ", "").equals(worldName.substring(2))).findAny().orElse(null);
					} else if (worldName.startsWith("n:"))
					{
						WorldServer world = MinecraftServer.getServer().worlds.stream().filter(ws -> ws.getWorldData().getName().replace(" ", "").equals(worldName.substring(2))).findAny().orElse(
								null);
						if (world != null)
						{
							targetWorld = world.getWorld();
						}
					} else
					{
						UtilPlayer.message(caller, F.main("Debug", "No world type specified"));
						return;
					}

					if (targetWorld != null)
					{
						WorldServer nmsWorld = ((CraftWorld) targetWorld).getHandle();
						Chunk[] chunks = targetWorld.getLoadedChunks();
						UtilPlayer.message(caller, F.main("Debug", "World info for " + targetWorld.getName()));
						UtilPlayer.message(caller, F.desc("Chunks", String.valueOf(chunks.length)));
						UtilPlayer.message(caller, F.desc("Entities", String.valueOf(targetWorld.getEntities().size())));
						UtilPlayer.message(caller, F.desc("Tile Entities", String.valueOf(Arrays.stream(chunks).map(Chunk::getTileEntities).map(Arrays::asList).mapToLong(Collection::size).sum())));
						UtilPlayer.message(caller, F.desc("View Distance", String.valueOf(nmsWorld.spigotConfig.viewDistance)));
						UtilPlayer.message(caller, F.desc("Unload queue size", String.valueOf(nmsWorld.chunkProviderServer.unloadQueue.size())));

						try
						{
							Field f = nmsWorld.getClass().getDeclaredField("M");
							f.setAccessible(true);
							HashTreeSet<NextTickListEntry> m = (HashTreeSet<NextTickListEntry>) f.get(nmsWorld);

							UtilPlayer.message(caller, F.desc("Pending tick", String.valueOf(m.size())));
						} catch (ReflectiveOperationException e)
						{
							e.printStackTrace();
						}
					} else
					{
						UtilPlayer.message(caller, F.main("Debug", "That world was not found"));
					}
				} else
				{
					UtilPlayer.message(caller, F.main("Debug", "No world specified"));
				}
			} else if (args[0].equals("chunks"))
			{

				if (args.length > 1)
				{
					String worldName = args[1];
					World targetWorld = null;

					if (worldName.startsWith("b:"))
					{
						targetWorld = Bukkit.getWorlds().stream().filter(world -> world.getName().replace(" ", "").equals(worldName.substring(2))).findAny().orElse(null);
					} else if (worldName.startsWith("n:"))
					{
						WorldServer world = MinecraftServer.getServer().worlds.stream().filter(ws -> ws.getWorldData().getName().replace(" ", "").equals(worldName.substring(2))).findAny().orElse(
								null);
						if (world != null)
						{
							targetWorld = world.getWorld();
						}
					} else
					{
						UtilPlayer.message(caller, F.main("Debug", "No world type specified"));
						return;
					}

					if (targetWorld != null)
					{
						String message = Arrays.stream(targetWorld.getLoadedChunks()).map(chunk -> "(" + chunk.getX() + "," + chunk.getZ() + ")").collect(Collectors.joining(","));
						System.out.println("Chunks: " + message);
						if (message.getBytes(StandardCharsets.UTF_8).length < 32767)
						{
							caller.sendMessage(message);
						}
					} else
					{
						UtilPlayer.message(caller, F.main("Debug", "That world was not found"));
					}
				} else
				{
					UtilPlayer.message(caller, F.main("Debug", "No world specified"));
				}
			}
			return;
		});
		registerDebugCommand("uhcgames", Perm.DEBUG_UHCGAMES_COMMAND, PermissionGroup.ADMIN, (caller, args) ->
		{
			UtilPlayer.message(caller, F.main("Debug", "As of now, there have been " + _gamesRun + " games played"));
		});
		registerDebugCommand("uhcgc", Perm.DEBUG_GC_COMMAND, PermissionGroup.DEV, (caller, args) ->
		{
			System.gc();
			UtilPlayer.message(caller, F.main("Debug", "Cleaned up!"));
		});
		registerDebugCommand("dm", Perm.DEBUG_DEATHMATCH_COMMAND, PermissionGroup.ADMIN, (caller, args) ->
		{
			SAFE_TIME = 0;
			MINING_TIME = 1000;
			startPreDeathmatch();
			UtilPlayer.message(caller, F.main("Debug", "Starting deathmatch"));
		});
		registerDebugCommand("uhcentities", Perm.DEBUG_ENTITIES_COMMAND, PermissionGroup.DEV, (caller, args) ->
		{
			for (Entity entity : caller.getNearbyEntities(5.0, 5.0, 5.0))
			{
				net.minecraft.server.v1_8_R3.Entity nms = ((CraftEntity) entity).getHandle();
				String debug = "Entity: " + entity.getType() + " id:" + nms.getId() + " inac:" + ActivationRange.checkIfActive(nms);
				debug += " at:" + nms.activatedTick + " dac:" + nms.defaultActivationState;

				int x = MathHelper.floor(nms.locX);
				int z = MathHelper.floor(nms.locZ);

				net.minecraft.server.v1_8_R3.Chunk chunk = nms.world.getChunkIfLoaded(x >> 4, z >> 4);
				debug += " c:" + chunk + " il:" + (chunk != null ? chunk.areNeighborsLoaded(1) : "null");
				caller.sendMessage(debug);
			}
		});
		registerDebugCommand("uhcchunk", Perm.DEBUG_CHUNK_COMMAND, PermissionGroup.DEV, (caller, args) ->
		{
			net.minecraft.server.v1_8_R3.Chunk chunk = ((CraftChunk) caller.getLocation().getChunk()).getHandle();
			try
			{
				Field neighbors = chunk.getClass().getDeclaredField("neighbors");
				neighbors.setAccessible(true);
				int n = neighbors.getInt(chunk);

				for (int x = -1; x < 2; x++)
				{
					for (int z = -1; z < 2; z++)
					{
						if (x == 0 && z == 0)
						{
							continue;
						}

						int mask = 0x1 << (x * 5 + z + 12);

						boolean should = chunk.world.getChunkIfLoaded(chunk.locX + x, chunk.locZ + z) != null;
						boolean is = (n & mask) == mask;
						if (is && should)
						{
							caller.sendMessage(ChatColor.GREEN + "Chunk " + (chunk.locX + x) + "," + (chunk.locZ + z) + " (" + x + "," + z + ") is a neighbor");
						} else if (is && !should)
						{
							caller.sendMessage(ChatColor.RED + "Chunk " + (chunk.locX + x) + "," + (chunk.locZ + z) + " (" + x + "," + z + ") is a neighbor but should not be");
						} else if (!is && should)
						{
							caller.sendMessage(ChatColor.RED + "Chunk " + (chunk.locX + x) + "," + (chunk.locZ + z) + " (" + x + "," + z + ") is not a neighbor but should be");
						} else if (!is && !should)
						{
							caller.sendMessage(ChatColor.GREEN + "Chunk " + (chunk.locX + x) + "," + (chunk.locZ + z) + " (" + x + "," + z + ") is not a neighbor");
						}
					}
				}
			} catch (Throwable t)
			{
				t.printStackTrace();
			}
		});
		registerDebugCommand("uhcallchunks", Perm.DEBUG_CALLCHUNKS_COMMAND, PermissionGroup.DEV, (caller, args) ->
		{
			for (net.minecraft.server.v1_8_R3.Chunk chunk : ((CraftWorld) caller.getWorld()).getHandle().chunkProviderServer.chunks.values())
			{
				try
				{
					Field neighbors = chunk.getClass().getDeclaredField("neighbors");
					neighbors.setAccessible(true);
					int n = neighbors.getInt(chunk);

					for (int x = -1; x < 2; x++)
					{
						for (int z = -1; z < 2; z++)
						{
							if (x == 0 && z == 0)
							{
								continue;
							}

							int mask = 0x1 << (x * 5 + z + 12);

							boolean should = chunk.world.getChunkIfLoaded(chunk.locX + x, chunk.locZ + z) != null;
							boolean is = (n & mask) == mask;
							if (is && !should)
							{
								caller.sendMessage(ChatColor.RED + "Chunk " + (chunk.locX + x) + "," + (chunk.locZ + z) + " (" + x + "," + z + ") relative to " + (chunk.locX) + "," + chunk.locZ
										+ " is a neighbor but should not be");
							} else if (!is && should)
							{
								caller.sendMessage(ChatColor.RED + "Chunk " + (chunk.locX + x) + "," + (chunk.locZ + z) + " (" + x + "," + z + ") relative to " + (chunk.locX) + "," + chunk.locZ
										+ " is not a neighbor but should be");
							}
						}
					}
				} catch (Throwable t)
				{
					t.printStackTrace();
				}
			}

			caller.sendMessage("Done");
		});
	}

	@Override
	public void recruit()
	{
		// todo load chunks sync here if necessary
		Location spawn = GetRandomSpawn(WorldData.World.getSpawnLocation(), true);
		WorldData.World.setSpawnLocation(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());

		SetState(GameState.Recruit);
	}

	public void createSpawns()
	{
		createSpawns(new Callback<Boolean>()
		{

			@Override
			public void run(Boolean data)
			{
			}
		});
	}

	public void createSpawns(Callback<Boolean> callback)
	{
		// Wipe Spawns
		for (GameTeam team : GetTeamList())
		{
			team.GetSpawns().clear();
		}

		double border = _border.getMaxCords();

		// Solo Game
		if (!TeamMode)
		{
			List<Player> players = GetPlayers(true);
			GameTeam gameTeam = GetTeamList().get(0);

			getArcadeManager().runSyncTimer(new BukkitRunnable()
			{

				@Override
				public void run()
				{
					if (gameTeam.GetSpawns().size() < Math.max(Manager.GetPlayerFull(), GetPlayers(true).size()))
					{
						Location loc = GetRandomSpawn(null, false);

						// Dynamically scale distance requirement based on how
						// many teams need to fit
						double dist = (2 * border) / (Math.sqrt(players.size()) + 3);

						// Ensure distance between Teams - 500 Attempts
						for (int i = 0; i < 500; i++)
						{
							boolean clash = false;

							for (Location otherSpawn : gameTeam.GetSpawns())
							{
								if (UtilMath.offset(loc, otherSpawn) < dist)
								{
									clash = true;
									break;
								}
							}

							if (!clash)
								break;

							loc = GetRandomSpawn(null, false);
						}

						gameTeam.GetSpawns().add(loc);
					}
					else
					{
						cancel();
						callback.run(true);
					}
				}
			}, 0L, 1L);
		}
		else
		{
			getArcadeManager().runSyncTimer(new BukkitRunnable()
			{

				AtomicInteger currentTeamId = new AtomicInteger();
				double border = _border.getMaxCords();

				@Override
				public void run()
				{
					GameTeam team = GetTeamList().get(currentTeamId.get());

					Location loc = GetRandomSpawn(null, false);

					// Dynamically scale distance requirement based on how many
					// teams need to fit
					double dist = (2 * border) / (Math.sqrt(GetTeamList().size()) + 3);

					// Ensure distance between Teams - 500 Attempts
					for (int i = 0; i < 500; i++)
					{
						boolean clash = false;

						for (GameTeam otherTeam : GetTeamList())
						{
							if (otherTeam.GetSpawns().isEmpty())
								continue;

							if (UtilMath.offset(loc, otherTeam.GetSpawn()) < dist)
							{
								clash = true;
								break;
							}
						}

						if (!clash)
							break;

						loc = GetRandomSpawn(null, false);
					}

					while (team.GetSpawns().size() < 20)
					{
						team.GetSpawns().add(GetRandomSpawn(loc, true));
					}

					currentTeamId.getAndIncrement();

					if (currentTeamId.get() >= GetTeamList().size())
					{
						cancel();
						callback.run(true);
					}
				}
			}, 0L, 1L);
		}
	}

	@Override
	public void ParseData()
	{
		WorldData.MinX = -MAX_ARENA_SIZE;
		WorldData.MinZ = -MAX_ARENA_SIZE;
		WorldData.MaxX = MAX_ARENA_SIZE;
		WorldData.MaxZ = MAX_ARENA_SIZE;

		WorldData.World.getEntities().forEach(Entity::remove);

		_border.prepare();

		Manager.runSyncLater(() -> {
			if (!Manager.GetGame().equals(this))
			{
				System.out.println("Game was switched! Stop spawn generation!");
				return;
			}

			Announce(C.cGreenB + "Generating spawns... there may be some lag for a few moments");
			createSpawns();
		}, 5 * 20);
	}

	@Override
	public boolean loadNecessaryChunks(long timeout)
	{
		return true;
	}

	@EventHandler
	public void end(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
		{
			return;
		}

		_border = null;
	}

	@EventHandler
	public void outsideBorder(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !IsLive() || _state == UHCState.TELEPORTING)
		{
			return;
		}

		double border = _border.getMaxCords();

		for (Player player : UtilServer.getPlayers())
		{
			Location loc = player.getLocation();

			boolean toLow = loc.getY() - _border.getYMin() < WORLD_BORDER_PARTICLES_DISTANCE;
			boolean toHigh = _border.getYMax() - loc.getY() < WORLD_BORDER_PARTICLES_DISTANCE;
			
			if (toLow)
			{
				UtilParticle.PlayParticle(ParticleType.FLAME, new Location(loc.getWorld(), loc.getX(), _border.getYMin(), loc.getZ()), 5F, 1F, 5F, 0.001F, 75, ViewDist.NORMAL, player);
			}
			else if (toHigh)
			{
				UtilParticle.PlayParticle(ParticleType.FLAME, new Location(loc.getWorld(), loc.getX(), _border.getYMax(), loc.getZ()), 5F, 1F, 5F, 0.001F, 75, ViewDist.NORMAL, player);
			}
			
			// Bump Players Back In
			if (loc.getX() > border || loc.getX() < -border || loc.getZ() > border || loc.getZ() < -border || loc.getY() < _border.getYMin() || loc.getY() > _border.getYMax())
			{
				if (Recharge.Instance.use(player, "Hit by Border", 1000, false, false))
				{
					Entity bottom = player;
					while (bottom.getVehicle() != null)
						bottom = bottom.getVehicle();

					UtilAction.velocity(bottom, UtilAlg.getTrajectory2d(loc, GetSpectatorLocation()), 1.2, true, 0.4, 0, 10, true);

					if (!UtilPlayer.isSpectator(player))
					{
						Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.CUSTOM, WORLD_BORDER_DAMAGE, false, false, true, "Nether Field", "Vaporize");

						player.getWorld().playSound(loc, Sound.NOTE_BASS, 2f, 1f);
						player.getWorld().playSound(loc, Sound.NOTE_BASS, 2f, 1f);

						player.sendMessage(C.cRedB + "STAY WITHIN THE BORDER!");
					}
				}
			}
		}
	}

	@EventHandler
	public void endPortalTransfer(final PlayerPortalEvent event)
	{
		if (event.getCause() == TeleportCause.END_PORTAL)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void TimeUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !IsLive())
		{
			return;
		}

		if (UtilTime.elapsed(GetStateTime(), SAFE_TIME) && _state == UHCState.SAFE)
		{
			startPvp();
		}
		else if (UtilTime.elapsed(GetStateTime(), MINING_TIME) && _state == UHCState.MINING && isSpeedMode())
		{
			startPreDeathmatch();
		}
	}

	public void updateActionbar()
	{
		if (!IsLive())
		{
			return;
		}

		String message = null;
		long timeSinceStart = System.currentTimeMillis() - GetStateTime();

		if (timeSinceStart < SAFE_TIME)
		{
			message = C.cYellow + "PVP enabled in " + C.Bold + UtilTime.MakeStr(SAFE_TIME - timeSinceStart);
		}
		else if (timeSinceStart < MINING_TIME && isSpeedMode())
		{
			message = C.cRed + "Deathmatch starts in " + C.Bold + UtilTime.MakeStr(MINING_TIME - timeSinceStart);
		}

		if (message != null)
		{
			for (Player player : UtilServer.getPlayers())
			{
				if (player.getItemInHand().getType() == Material.COMPASS)
				{
					continue;
				}

				UtilTextBottom.display(message, player);
			}
		}
	}

	public void startPvp()
	{
		if (_state.isPVP())
		{
			return;
		}

		_state = UHCState.MINING;
		UtilTextMiddle.display(null, C.cYellow + "PvP has been enabled!", 5, 80, 5);

		for (Player player : UtilServer.getPlayers())
		{
			player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1f, 1f);
		}

		DamagePvP = true;

		getModule(CompassModule.class).setGiveCompass(true);
	}

	public void startPreDeathmatch()
	{
		Announce(C.cRedB + "Deathmatch is starting... Players are being teleported!", false);

		// Set the state
		_state = UHCState.TELEPORTING;

		// Freeze all players
		for (Player player : GetPlayers(true))
		{
			_freezer.freeze(player);
		}

		// Toggle temporary game settings
		Damage = false;

		// Set time
		WorldTimeSet = 0;

		// Remove all monsters
		for (Entity entity : WorldData.World.getEntities())
		{
			if (entity instanceof Monster)
			{
				entity.remove();
			}
		}

		// Set the border
		_border.setSize(DEATHMATCH_ARENA_SIZE, 0);

		// Recreate spawns
		createSpawns(new Callback<Boolean>()
		{

			@Override
			public void run(Boolean data)
			{
				for (GameTeam gameTeam : GetTeamList())
				{
					gameTeam.SpawnTeleport();
				}

				for (Player player : UtilServer.getPlayers())
				{
					if (!IsAlive(player))
					{
						player.teleport(SpectatorSpawn);
					}
				}
			}
		});
	}

	public void startDeathmatch()
	{
		Announce(C.cRedB + "Fight!", false);

		 UtilTextMiddle.display(C.cRedB + "Watch Out", "The border is closing in on all sides!");
		
		// Set the state
		_state = UHCState.DEATHMATCH;

		for (Player player : UtilServer.getPlayers())
		{
			player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1f, 1f);
		}

		// Unfreeze all players
		_freezer.unfreeze();

		// Toggle temporary game settings
		Damage = true;

		// Set the border
		_border.setSize(32, DEATHMATCH_TIME_SECONDS);
	}

	@EventHandler
	public void deathmatchTimer(UpdateEvent event)
	{
		// Checks to see if it is pre-deathmatch
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		if (_state == UHCState.TELEPORTING)
		{
			_secondsSincePreDeathmatch++;

			if (_secondsSincePreDeathmatch < PRE_DEATHMATCH_TIME_SECONDS)
			{
				int seconds = PRE_DEATHMATCH_TIME_SECONDS - _secondsSincePreDeathmatch;

				Announce(C.cRedB + "Deathmatch starting in " + seconds + " second" + (seconds == 1 ? "" : "s"), false);
			}
			else if (_secondsSincePreDeathmatch == PRE_DEATHMATCH_TIME_SECONDS)
			{
				startDeathmatch();
			}
		}
		else if (_state == UHCState.DEATHMATCH)
		{
			_border.advanceYBorder();
		}
	}

	@EventHandler
	public void EarlyGameUpdate(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.FAST)
			return;

		if (_state.isPVP())
			return;

		WorldData.World.setTime(2000);

		for (Player player : GetPlayers(true))
		{
			player.setSaturation(3f);
			player.setExhaustion(0f);
			player.setFoodLevel(20);
		}
	}

	@EventHandler
	public void GameStart(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
			return;

		WorldData.World.setTime(2000);

		// Kill Evil Mobs
		for (Entity ent : WorldData.World.getEntities())
		{
			if (!(ent instanceof Monster))
				continue;

			ent.remove();
		}

		// Hunger
		for (Player player : GetPlayers(true))
		{
			player.setSaturation(4f);
			player.setExhaustion(0f);
		}
	}

	@EventHandler
	public void gameLive(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		if (!isSpeedMode())
		{
			_border.setSize(32, MINING_TIME / 1000);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void on(ChunkUnloadEvent event)
	{
		if (!IsLive())
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void on(PlayerKickEvent event)
	{
		// Don't kick players while teleporting. Probably NCP trying to kick for
		// fly or something
		if (_isTeleporting)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void PlayerPrepare(GameStateChangeEvent event)
	{
		final Game game = event.GetGame();

		if (event.GetState() != GameState.Prepare)
			return;

		Manager.GetChat().setChatSilence(1000 * 120, false);

		_isTeleporting = true;

		Map<Player, GameTeam> playerTeams = game.GetPlayers(true).stream().collect(Collectors.toMap(Function.identity(), this::GetTeam));
		List<Player> players = new ArrayList<>(playerTeams.keySet());

		Location zero = WorldData.World.getSpawnLocation();

		for (Player player : players)
		{
			player.teleport(zero);

			// Update scoreboard
			_healthObjective.getScore(player.getName()).setScore((int) player.getMaxHealth());

			game.addPlayerInTime(player);

			Manager.Clear(player);
			UtilInv.Clear(player);

			// Heal
			player.setHealth(player.getMaxHealth());
			// Resistance and regen
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30 * 20, 128), true);
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 30 * 20, 128), true);

			game.ValidateKit(player, game.GetTeam(player));

			if (game.GetKit(player) != null)
				game.GetKit(player).ApplyKit(player);
		}

		Announce(C.cGreen + C.Bold + "Please wait, you will be teleported soon", false);

		_totalPlayers = players.size();

		Map<UUID, Location> teleportedLocations = new HashMap<>();

		getArcadeManager().runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				_teleportedPlayers++;
				if (_teleportedPlayers >= players.size())
				{
					Announce(C.cGreen + C.Bold + "The game will start in 5 seconds", false);
					Manager.runSyncLater(() -> {
						try
						{
							for (Player player : players)
							{
								GameTeam team = game.GetTeam(player);
								if (team != null)
								{
									if (teleportedLocations.get(player.getUniqueId()) != null)
									{
										team.SpawnTeleport(player, teleportedLocations.get(player.getUniqueId()));
									}
								}

								// Heal
								player.setHealth(player.getMaxHealth());
								// Resistance and regen
								player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 128), true);
								player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10 * 20, 128), true);
							}

							teleportedLocations.clear();
						}
						finally
						{
							game.AnnounceGame();
							game.StartPrepareCountdown();

							// Event
							GamePrepareCountdownCommence gamePrepareCountdownCommence = new GamePrepareCountdownCommence(game);
							UtilServer.getServer().getPluginManager().callEvent(gamePrepareCountdownCommence);

							_isTeleporting = false;
						}
					}, 5 * 20L);
					cancel();
					return;
				}

				Player player = players.get(_teleportedPlayers);
				GameTeam team = game.GetTeam(player);

				// This could happen if the player left (and rejoined) while
				// teleporting
				// Team maps based on player as a key
				if (team != null)
				{
					// Save where they teleported
					teleportedLocations.put(player.getUniqueId(), team.SpawnTeleport(player));

					// Event
					PlayerPrepareTeleportEvent playerStateEvent = new PlayerPrepareTeleportEvent(game, player);
					UtilServer.getServer().getPluginManager().callEvent(playerStateEvent);
				}
				else
				{
					GetLocationStore().put(player.getName(), playerTeams.get(player).GetSpawn());
				}
			}
		}, 5 * 20L, DELAY_BETWEEN_PLAYER_TELEPORT);

		// Spectators Move
		for (Player player : UtilServer.getPlayers())
		{
			if (Manager.GetGame().IsAlive(player))
				continue;

			Manager.addSpectator(player, true);
		}

	}

	@Override
	public Location GetSpectatorLocation()
	{
		if (SpectatorSpawn != null)
		{
			return SpectatorSpawn;
		}

		SpectatorSpawn = new Location(WorldData.World, 0, 60, 0);

		SpectatorSpawn = SpectatorSpawn.getBlock().getLocation().add(0.5, 0.1, 0.5);

		while (SpectatorSpawn.getBlock().getType() != Material.AIR || SpectatorSpawn.getBlock().getRelative(BlockFace.UP).getType() != Material.AIR)
			SpectatorSpawn.add(0, 1, 0);

		return SpectatorSpawn;
	}

	public Location GetRandomSpawn(Location around, boolean sameChunk)
	{
		int tries = 0;

		Chunk chunk = around == null ? null : around.getChunk();

		while (true)
		{
			tries++;

			if (tries > 100)
			{
				System.out.println("Tries > 100, returning spawn");
				return WorldData.World.getSpawnLocation();
			}

			Block targetBlock;

			if (around == null)
			{
				double currentBorder = _border.getMaxCords();

				// Return a int from 0 - 1800, then remove 900 so its a int from
				// -900 to 900
				int x = (int) (UtilMath.r((int) (1.8 * currentBorder)) - (0.9 * currentBorder));
				int z = (int) (UtilMath.r((int) (1.8 * currentBorder)) - (0.9 * currentBorder));

				targetBlock = UtilBlock.getHighest(WorldData.World, x, z, null);
			}
			else
			{
				if (sameChunk)
				{
					if (tries > 20)
					{
						System.out.println("----------- WARNING ---------------");
						System.out.println("DISABLED SAMECHUNK CHECK AFTER 20 ATTEMPTS");
						sameChunk = false;
						continue;
					}

					targetBlock = UtilBlock.getHighest(WorldData.World, chunk.getBlock(UtilMath.r(15), 0, UtilMath.r(15)).getLocation());
				}
				else
				{
					targetBlock = UtilBlock.getHighest(WorldData.World, around.getBlockX() - 4 + UtilMath.r(tries < 10 ? 8 : 30), around.getBlockZ() - 4 + UtilMath.r(tries < 10 ? 8 : 30), null);
				}
			}

			if (sameChunk && around != null && !around.getChunk().equals(targetBlock.getChunk()))
			{
				System.out.println("Failed same chunk check: " + around.getChunk() + " " + targetBlock.getChunk() + " " + tries);
				continue;
			}

			// Check Validity

			// Liquid
			if (targetBlock.getRelative(BlockFace.DOWN).isLiquid())
			{
				System.out.println("Failed liquid check: " + targetBlock.getRelative(BlockFace.DOWN) + " " + tries);
				continue;
			}

			// Suffocated
			if (CraftMagicNumbers.getBlock(targetBlock.getRelative(BlockFace.UP)).w())
			{
				System.out.println("Failed suffocation check: " + targetBlock.getRelative(BlockFace.UP) + " " + tries);
				continue;
			}

			return targetBlock.getLocation().add(0.5, 0.5, 0.5);
		}
	}

	public void placeItemsInChest(Collection<ItemStack> drops, Location location)
	{
		// Place their items in a chest
		Block block = location.getBlock();

		block.setType(Material.CHEST);
		block.getRelative(BlockFace.NORTH).setType(Material.CHEST);

		Chest chest = (Chest) block.getState();
		Inventory inventory = chest.getInventory();
		int i = 0;

		for (ItemStack itemStack : drops)
		{
			inventory.setItem(i++, itemStack);
		}
	}

	@EventHandler
	public void GhastDrops(EntityDeathEvent event)
	{
		if (event.getEntity() instanceof Ghast)
		{
			event.getDrops().clear();
			event.getDrops().add(ItemStackFactory.Instance.CreateStack(Material.GOLD_INGOT, 1));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void PlayerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();

		GameTeam team = GetTeam(player);
		if (team == null)
			return;

		// Lightning
		Location loc = player.getLocation();
		player.getWorld().strikeLightningEffect(loc);

		// Gems
		if (IsLive())
		{
			long timeAlive = System.currentTimeMillis() - GetStateTime();
			AddGems(player, timeAlive / 60000d, "Survived " + UtilTime.MakeStr(timeAlive), false, false);

			placeItemsInChest(event.getDrops(), loc);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void PlayerDeathMessage(CombatDeathEvent event)
	{
		if (!(event.GetEvent().getEntity() instanceof Player))
			return;

		Player dead = (Player) event.GetEvent().getEntity();

		CombatLog log = event.GetLog();

		Player killer = null;
		if (log.GetKiller() != null)
			killer = UtilPlayer.searchExact(log.GetKiller().GetName());

		// Simple
		if (killer != null)
		{
			Announce(Manager.GetColor(dead) + C.Bold + dead.getName() + C.cGray + C.Bold + " was killed by " + Manager.GetColor(killer) + C.Bold + killer.getName() + C.cGray + C.Bold + ".");
		}
		else
		{
			if (log.GetAttackers().isEmpty())
			{
				Announce(Manager.GetColor(dead) + C.Bold + dead.getName() + C.cGray + C.Bold + " has died by unknown causes.");
			}

			else
			{
				Announce(Manager.GetColor(dead) + C.Bold + dead.getName() + C.cGray + C.Bold + " was killed by " + log.GetAttackers().getFirst().GetName() + ".");
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerKick(PlayerKickEvent event)
	{
		event.setLeaveMessage(null);
	}

	@EventHandler
	public void CreatureCull(UpdateEvent event)
	{
		if (!InProgress())
			return;

		if (event.getType() != UpdateType.SLOW)
			return;

		Map<EntityType, ArrayList<Entity>> ents = new HashMap<>();

		for (Entity ent : WorldData.World.getEntities())
		{
			if (!ents.containsKey(ent.getType()))
				ents.put(ent.getType(), new ArrayList<Entity>());

			ents.get(ent.getType()).add(ent);
		}

		for (EntityType type : ents.keySet())
		{
			ArrayList<Entity> entList = ents.get(type);
			int count = 0;

			if (type == EntityType.DROPPED_ITEM)
				continue;

			while (entList.size() > 500)
			{
				Entity ent = entList.remove(UtilMath.r(entList.size()));
				ent.remove();
				count++;
			}

			if (count > 0)
				System.out.println("Removed " + count + " " + type);
		}
	}

	private void CraftRecipes()
	{
		ShapelessRecipe goldMelon = new ShapelessRecipe(new ItemStack(Material.SPECKLED_MELON, 1));
		goldMelon.addIngredient(1, Material.MELON);
		goldMelon.addIngredient(1, Material.GOLD_BLOCK);
		UtilServer.getServer().addRecipe(goldMelon);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void CraftGlisteringMelon(PrepareItemCraftEvent event)
	{
		if (event.getRecipe().getResult() == null)
			return;

		Material type = event.getRecipe().getResult().getType();

		if (type != Material.SPECKLED_MELON)
			return;

		CraftingInventory inv = event.getInventory();

		// Allow FULL BLOCK Gold Melon
		for (ItemStack item : inv.getMatrix())
			if (item != null && item.getType() != Material.AIR)
				if (item.getType() == Material.GOLD_BLOCK)
					return;

		inv.setResult(null);
	}

	@EventHandler
	public void HealthChange(EntityRegainHealthEvent event)
	{
		if (event.getRegainReason() == RegainReason.SATIATED)
			event.setCancelled(true);
	}

	@EventHandler
	public void NetherObsidianCancel(BlockPlaceEvent event)
	{
		if (event.getBlock().getWorld().getEnvironment() == Environment.NETHER)
		{
			if (event.getBlock().getType() == Material.OBSIDIAN)
			{
				UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot place " + F.elem("Obsidian") + " in the " + F.elem("Nether") + "."));
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void Commands(PlayerCommandPreprocessEvent event)
	{
		if (event.getMessage().startsWith("/kill"))
			event.setCancelled(true);
	}

	@EventHandler
	public void on(InventoryClickEvent event)
	{
		Player clicker = (Player) event.getWhoClicked();
		if (!IsAlive(clicker))
			event.setCancelled(true);
	}

	@EventHandler
	public void on(BoosterItemGiveEvent event)
	{
		if (IsAlive(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void clearCreeperExplode(EntityExplodeEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void clearCreeperExplodeReenable(EntityExplodeEvent event)
	{
		event.setCancelled(false);
	}

	@EventHandler
	public void stopPoison(EntityDamageEvent event)
	{
		if (event.getCause() != DamageCause.POISON || !(event.getEntity() instanceof LivingEntity))
		{
			return;
		}

		((LivingEntity) event.getEntity()).removePotionEffect(PotionEffectType.POISON);
		event.setCancelled(true);
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;

		boolean end = false;

		// Solo
		if (GetTeamList().size() == 1)
		{
			if (GetPlayers(true).size() <= 1)
			{
				List<Player> places = GetTeamList().get(0).GetPlacements(true);

				// Announce
				AnnounceEnd(places);

				// Gems
				if (places.size() >= 1)
					AddGems(places.get(0), 20, "1st Place", false, false);

				if (places.size() >= 2)
					AddGems(places.get(1), 15, "2nd Place", false, false);

				if (places.size() >= 3)
					AddGems(places.get(2), 10, "3rd Place", false, false);

				for (Player player : GetPlayers(false))
					if (player.isOnline())
						AddGems(player, 10, "Participation", false, false);

				end = true;
			}
		}
		else
		{
			// Online Teams
			List<GameTeam> teamsAlive = GetTeamList().stream().filter(team -> team.GetPlayers(true).size() > 0).collect(Collectors.toList());

			if (teamsAlive.size() <= 1)
			{
				if (teamsAlive.size() > 0)
				{
					for (Player player : teamsAlive.get(0).GetPlayers(false))
					{
						long ingameTime = getPlayerIngameTime(player);
						if (ingameTime == 0)
							ingameTime = GetStateTime();

						int gems = Math.round((System.currentTimeMillis() - ingameTime) / 1000);
						AddGems(player, gems, "Winning Team", false, false);
					}
				}

				if (teamsAlive.size() > 0)
				{
					AnnounceEnd(teamsAlive.get(0));
				}

				end = true;
			}
		}

		if (end)
		{
			_border.stop();

			// End
			SetState(GameState.End);
		}
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !InProgress())
		{
			return;
		}

		// Take this time to update the actionbar
		updateActionbar();

		Scoreboard.reset();
		Scoreboard.writeNewLine();

		// Solo
		if (GetTeamList().size() == 1)
		{
			if (GetPlayers(true).size() < 8)
			{
				Scoreboard.writeGroup(GetPlayers(true), player -> Pair.create(GetTeam(player).GetColor() + player.getName(), (int) player.getHealth()), true);
			}
			else
			{
				Scoreboard.write(C.cYellow + C.Bold + "Players");
				Scoreboard.write(GetPlayers(true).size() + " Alive");
			}
		}
		// Team
		else
		{
			if (GetPlayers(true).size() < 7)
			{
				Scoreboard.writeGroup(GetPlayers(true), player -> Pair.create(GetTeam(player).GetColor() + player.getName(), (int) player.getHealth()), true);
			}
			else
			{
				Scoreboard.write(C.cYellow + C.Bold + "Teams");
				Scoreboard.write(GetTeamList().stream().filter(GameTeam::IsTeamAlive).count() + " Alive");
			}
		}

		Scoreboard.writeNewLine();
		Scoreboard.write(C.cYellow + C.Bold + "Status");
		if (GetState() == GameState.Prepare)
		{
			int players = _teleportedPlayers + 1;
			if (players > _totalPlayers)
				players = _totalPlayers;
			Scoreboard.write("Teleporting Players (" + players + "/" + _totalPlayers + ")");
		}
		else if (GetState() == GameState.Live)
		{
			Scoreboard.write("Live (" + UtilTime.MakeStr(System.currentTimeMillis() - GetStateTime()) + ")");
		}
		else
		{
			Scoreboard.write("Finished");
		}

		double currentBorder = _border.getMaxCords();

		Scoreboard.writeNewLine();
		Scoreboard.write(C.cYellow + C.Bold + "Borders");
		Scoreboard.write("-" + (int) currentBorder + " to " + "+" + (int) currentBorder);
		if (_state == UHCState.DEATHMATCH)
		{
			Scoreboard.write("Vert: " + (int) _border.getYMin() + " to " + (int) _border.getYMax());
		}

		Scoreboard.draw();

		for (Player player : GetPlayers(true))
		{
			_healthObjective.getScore(player.getName()).setScore((int) player.getHealth());
		}
	}

	@Override
	public double GetKillsGems(Player killer, Player killed, boolean assist)
	{
		if (killer.equals(killed))
			return 0;

		if (GetTeam(killer) != null && GetTeam(killed) != null && GetTeam(killer).equals(GetTeam(killed)) && TeamMode)
			return 0;

		if (assist)
			return 40;

		return 200;
	}

	public void addUHCAchievement(Player player, String achievement)
	{
		if (!Manager.IsRewardStats())
		{
			return;
		}

		Map<String, Integer> stats = GetStats().get(player);
		String gameType = null;

		if (isSpeedMode())
		{
			gameType = "Ultra Hardcore Speed";
		}
		else
		{
			gameType = "Ultra Hardcore";
		}

		stats.put(gameType + "." + achievement, 1);
	}

	public UHCBorder getBorder()
	{
		return _border;
	}

	public boolean isSpeedMode()
	{
		return _speedMode != null;
	}

	public enum UHCState
	{
		SAFE, MINING, TELEPORTING, DEATHMATCH;

		public boolean isPVP()
		{
			return this != SAFE;
		}
	}
}