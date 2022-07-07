package nautilus.game.arcade.game.games.speedbuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.minecraft.server.v1_8_R3.PacketPlayOutGameStateChange;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.material.Bed;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.disguise.disguises.DisguiseGuardian;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.GameTeam.PlayerState;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.speedbuilders.data.BuildData;
import nautilus.game.arcade.game.games.speedbuilders.data.DemolitionData;
import nautilus.game.arcade.game.games.speedbuilders.data.MobData;
import nautilus.game.arcade.game.games.speedbuilders.data.RecreationData;
import nautilus.game.arcade.game.games.speedbuilders.events.PerfectBuildEvent;
import nautilus.game.arcade.game.games.speedbuilders.kits.DefaultKit;
import nautilus.game.arcade.game.games.speedbuilders.stattrackers.DependableTracker;
import nautilus.game.arcade.game.games.speedbuilders.stattrackers.FirstBuildTracker;
import nautilus.game.arcade.game.games.speedbuilders.stattrackers.PerfectionistTracker;
import nautilus.game.arcade.game.games.speedbuilders.stattrackers.SpeediestBuilderizerTracker;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.chat.ChatStatData;
import nautilus.game.arcade.stats.BlockPlaceStatTracker;

public class SpeedBuilders extends SoloGame
{
	public enum Perm implements Permission
	{
		DEBUG_SETNEXT_COMMAND,
	}

	private static final String GUARDIAN_NAME = "Gwen the Guardian";

	//Build Size and some other values used commonly
	public int BuildSize = 7;
	public int BuildSizeDiv2 = BuildSize / 2;
	public int BuildSizeMin1 = BuildSize - 1;
	public int BuildSizePow3 = BuildSize * BuildSize * BuildSize;

	public boolean InstaBreak = true;

	private SpeedBuildersState _state = SpeedBuildersState.VIEWING;
	private long _stateTime = System.currentTimeMillis();

	private int _roundsPlayed;

	private int _buildCountStage;
	private int _viewCountStage;

	private int _buildTimeTracker = 40;
	private int _buildTime = 40;
	private int _viewTime = 10;

	private Location _buildMiddle;

	private ArrayList<BuildData> _buildData = new ArrayList<BuildData>();
	private ArrayList<BuildData> _usedBuilds = new ArrayList<>();
	private BuildData _currentBuild;

	private BlockState[][] _defaultMiddleGround = new BlockState[BuildSize][BuildSize];
	private ArrayList<Entity> _middleMobs = new ArrayList<Entity>();

	private NautHashMap<Player, RecreationData> _buildRecreations = new NautHashMap<Player, RecreationData>();

	private ArmorStand _judgeEntity;
	private DisguiseGuardian _judgeDisguise;
	private Location _judgeSpawn;
	private ArmorStand _judgeLaserTarget;

	private ArrayList<RecreationData> _toEliminate = new ArrayList<RecreationData>();
	private long _lastElimination;
	private boolean _eliminating;
	// Track the time we switch to review so we can give players 8 seconds to look around
	private long _reviewStartTime;

	private NautHashMap<Player, Long> _perfectBuild = new NautHashMap<Player, Long>();
	private boolean _allPerfect;

	private Location _lookTarget;
	private ArmorStand _lookStand;
	private long _targetReached;
	private long _stayTime;
	private RecreationData _lastRecreationTarget;
	private double _standMoveProgress;
	private Location _standStart;

	private BuildData _nextBuild;

	public SpeedBuilders(ArcadeManager manager)
	{
		super(manager, GameType.SpeedBuilders,
				new Kit[]
						{
								new DefaultKit(manager)
						},
				new String[]
						{
								"Recreate the build shown to you.",
								"The least correct build is eliminated.",
								"Last person left wins!"
						});

		Damage = false;

		HungerSet = 20;
		HealthSet = 20;

		DeathMessages = false;

		FixSpawnFacing = false;

		AllowParticles = false;

		InventoryClick = true;
		
		AnticheatDisabled = true;

		registerStatTrackers(
				new DependableTracker(this),
				new FirstBuildTracker(this),
				new PerfectionistTracker(this),
				new SpeediestBuilderizerTracker(this),
				new BlockPlaceStatTracker(this, new Material[]{})
		);

		registerChatStats(
				new ChatStatData("BlocksPlaced", "Blocks Placed", true),
				new ChatStatData("BlocksBroken", "Blocks Broken", true)
		);

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);

		registerDebugCommand("setnext", Perm.DEBUG_SETNEXT_COMMAND, PermissionGroup.BUILDER, (caller, args) ->
		{
			if (!UtilServer.isTestServer())
			{
				UtilPlayer.message(caller, F.main("Build", C.cYellow + "You can only use this on testing servers!"));

				return;
			}

			if (args.length == 0)
			{
				UtilPlayer.message(caller, F.main("Build", C.cYellow + "You need to specify a next build!"));

				return;
			}

			String buildName = Arrays.asList(args).stream().collect(Collectors.joining(" "));

			BuildData build = null;

			for (BuildData buildData : _buildData)
			{
				if (buildData.BuildText.toUpperCase().startsWith(buildName.toUpperCase()))
				{
					build = buildData;

					break;
				}
			}

			if (build == null)
			{
				UtilPlayer.message(caller, F.main("Build", "That build does not exist!"));
			} else
			{
				_nextBuild = build;

				UtilPlayer.message(caller, F.main("Build", "Set next build to " + F.elem(build.BuildText)));
			}
		});
	}

	@Override
	public void ParseData()
	{
		_buildMiddle = WorldData.GetDataLocs("RED").get(0).clone().subtract(0.5, 0, 0.5);
		
		_judgeSpawn = _buildMiddle.clone().add(0.5, BuildSize, 0.5);
		
		Location groundMin = _buildMiddle.clone().subtract(BuildSizeDiv2, 1, BuildSizeDiv2);
		
		for (int x = 0; x < BuildSize; x++)
		{
			for (int z = 0; z < BuildSize; z++)
			{
				_defaultMiddleGround[x][z] = groundMin.clone().add(x, 0, z).getBlock().getState();
			}
		}
		
		for (Entry<String, List<Location>> entry : WorldData.GetAllCustomLocs().entrySet())
		{
			BuildData buildData = new BuildData(entry.getValue().get(0).clone().subtract(0.5, 0, 0.5), ChatColor.translateAlternateColorCodes('&', entry.getKey()), this);
			boolean add = false;
			for (int x = 0; x < BuildSize && !add; x++)
			{
				for (int y = 0; y < BuildSize && !add; y++)
				{
					for (int z = 0; z < BuildSize && !add; z++)
					{
						if (buildData.Build[x][y][z] != null && buildData.Build[x][y][z].getType() != Material.AIR)
							add = true;
					}
				}
			}
			
			if (!buildData.Mobs.isEmpty())
				add = true;

			if (add)
				_buildData.add(buildData);
		}
		
		for (Location loc : WorldData.GetDataLocs("YELLOW"))
		{
			loc.subtract(0.5, 0, 0.5);
		}
		
		for (Location loc : GetTeamList().get(0).GetSpawns())
		{
			loc.setDirection(UtilAlg.getTrajectory(loc, _buildMiddle.clone().add(0.5, 0, 0.5)));
		}
	}

	public void setSpeedBuilderState(SpeedBuildersState state)
	{
		_state = state;
		_stateTime = System.currentTimeMillis();
	}

	public SpeedBuildersState getSpeedBuilderState()
	{
		return _state;
	}

	public long getSpeedBuilderStateTime()
	{
		return _stateTime;
	}

	public int getRoundsPlayed()
	{
		return _roundsPlayed;
	}

	public void clearCenterArea(boolean resetGround)
	{
		Location buildMin = _buildMiddle.clone().subtract(BuildSizeDiv2, 0, BuildSizeDiv2);
		Location buildMax = _buildMiddle.clone().add(BuildSizeDiv2, BuildSizeMin1, BuildSizeDiv2);
		
		for (Block block : UtilBlock.getInBoundingBox(buildMin, buildMax))
		{
			MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR);
		}
		
		for (Entity entity : _middleMobs)
		{
			entity.remove();
		}
		
		_middleMobs.clear();
		
		if (resetGround)
		{
			for (int x = 0; x < BuildSize; x++)
			{
				for (int z = 0; z < BuildSize; z++)
				{
					MapUtil.QuickChangeBlockAt(buildMin.clone().add(x, -1, z), _defaultMiddleGround[x][z].getType(), _defaultMiddleGround[x][z].getRawData());
				}
			}
		}
	}

	public void pasteBuildInCenter(BuildData buildData)
	{
		clearCenterArea(true);
		
		Location groundMin = _buildMiddle.clone().subtract(BuildSizeDiv2, 1, BuildSizeDiv2);
		
		for (int x = 0; x < BuildSize; x++)
		{
			for (int z = 0; z < BuildSize; z++)
			{
				MapUtil.QuickChangeBlockAt(groundMin.clone().add(x, 0, z), buildData.Ground[x][z].getType(), buildData.Ground[x][z].getRawData());
			}
		}
		
		Location buildMin = _buildMiddle.clone().subtract(BuildSizeDiv2, 0, BuildSizeDiv2);
		
		for (int x = 0; x < BuildSize; x++)
		{
			for (int y = 0; y < BuildSize; y++)
			{
				for (int z = 0; z < BuildSize; z++)
				{
					MapUtil.QuickChangeBlockAt(buildMin.clone().add(x, y, z), buildData.Build[x][y][z].getType(), buildData.Build[x][y][z].getRawData());
				}
			}
		}
		
		CreatureAllowOverride = true;
		
		for (MobData mobData : buildData.Mobs)
		{
			Location loc = buildMin.clone().add(mobData.DX + 0.5, mobData.DY, mobData.DZ + 0.5);
			
			Entity entity = loc.getWorld().spawnEntity(loc, mobData.EntityType);
			
			UtilEnt.vegetate(entity, true);
			UtilEnt.ghost(entity, true, false);
			
			_middleMobs.add(entity);
		}
		
		CreatureAllowOverride = false;
	}

	public void spawnJudge()
	{
		CreatureAllowOverride = true;
		
		_judgeEntity = _judgeSpawn.getWorld().spawn(_judgeSpawn, ArmorStand.class);
		
		CreatureAllowOverride = false;
		
		_judgeEntity.setVisible(false);
		_judgeEntity.setGravity(false);
		_judgeEntity.setSmall(true);
		
		_judgeDisguise = new DisguiseGuardian(_judgeEntity);

		_judgeDisguise.setElder(true);
		_judgeDisguise.setCustomNameVisible(true);
		_judgeDisguise.setName(GUARDIAN_NAME);
		
		Manager.GetDisguise().disguise(_judgeDisguise);
	}

	public void despawnJudge()
	{
		Manager.GetDisguise().undisguise(_judgeEntity);
		
		_judgeEntity.remove();
		
		_judgeDisguise = null;
		_judgeEntity = null;
	}

	public void judgeTargetLocation(Location loc)
	{
		if (loc == null)
		{
			if (_judgeLaserTarget == null)
				return;
			
			_judgeLaserTarget.remove();
			
			_judgeLaserTarget = null;
			
			_judgeDisguise.setTarget(0);
			
			Manager.GetDisguise().updateDisguise(_judgeDisguise);
		}
		else
		{
			if (_judgeLaserTarget != null)
				judgeTargetLocation(null);
			
			CreatureAllowOverride = true;
			
			_judgeLaserTarget = _judgeEntity.getWorld().spawn(loc, ArmorStand.class);
			
			CreatureAllowOverride = false;
			
			_judgeLaserTarget.setVisible(false);
			_judgeLaserTarget.setGravity(false);
			_judgeLaserTarget.setSmall(true);
			
			UtilEnt.CreatureLook(_judgeEntity, _judgeLaserTarget.getLocation());
			
			_judgeDisguise.setTarget(_judgeLaserTarget.getEntityId());
			
			Manager.GetDisguise().updateDisguise(_judgeDisguise);
		}
	}

	public void moveToGuardians(Player player, boolean elimination)
	{
		if (elimination)
		{
			GetTeamList().get(0).SetPlacement(player, PlayerState.OUT);
			GetTeamList().get(0).RemovePlayer(player);
		}
		
		GetTeamList().get(1).AddPlayer(player, true);
		
		DisguiseGuardian disguise = new DisguiseGuardian(player);
		disguise.setName(C.cGray + player.getName());
		disguise.setCustomNameVisible(true);
		
		Manager.GetDisguise().disguise(disguise);
		
		player.setGameMode(GameMode.SURVIVAL);
		
		player.setAllowFlight(true);
		player.setFlying(true);
		
		EndCheck();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPrepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
			return;
		
		//Add 1 spawn so it doesn't freak out
		ArrayList<Location> spawns = new ArrayList<Location>();
		spawns.add(GetSpectatorLocation());
		
		GameTeam guardians = new GameTeam(this, "Guardians", ChatColor.GRAY, spawns);
		
		AddTeam(guardians);
		
		spawnJudge();
		//GUARDIAN LAZORZ WILL ROXORZ YOUR BOXORZ
		
		ArrayList<Player> players = GetPlayers(true);
		
		for (int i = 0; i < players.size(); i++)
		{
			if (i >= WorldData.GetDataLocs("YELLOW").size())
			{
				GetTeamList().get(0).RemovePlayer(players.get(i));
				Manager.addSpectator(players.get(i), true);
			}
		}
	}
	
	

	@EventHandler
	public void onLive(GameStateChangeEvent event)
	{
		if (!IsLive())
			return;
		
		if (WorldData.GetDataLocs("YELLOW").size() < GetTeamList().get(0).GetPlayers(true).size())
		{
			Announce(C.Bold + "Too many players...");
			SetState(GameState.End);
			return;
		}
		
		if (_nextBuild != null)
			_currentBuild = _nextBuild;
		else
			_currentBuild = UtilAlg.Random(_buildData, _usedBuilds);
		
		_nextBuild = null;
		_usedBuilds.add(_currentBuild);
		_buildTime = _currentBuild.getBuildTime(_buildTimeTracker);
		
		HashSet<Location> usedBuildLocs = new HashSet<Location>();
		
		for (Player player : GetTeamList().get(0).GetPlayers(true))
		{
			Location buildLoc = UtilAlg.findClosest(player.getLocation(), WorldData.GetDataLocs("YELLOW"));
			Location spawnLoc = UtilAlg.findClosest(buildLoc, GetTeamList().get(0).GetSpawns());
			
			_buildRecreations.put(player, new RecreationData(this, player, buildLoc, spawnLoc));
			
			_buildRecreations.get(player).pasteBuildData(_currentBuild);
			
			usedBuildLocs.add(buildLoc);
		}
		
		for (Location loc : WorldData.GetDataLocs("YELLOW"))
		{
			if (!usedBuildLocs.contains(loc))
			{
				HashSet<Block> blocks = UtilBlock.findConnectedBlocks(loc.getBlock(), loc.getBlock(), null, 2000, 8);
				
				Manager.GetExplosion().BlockExplosion(blocks, loc, false, true);
			}
		}
		
		for (Player player : GetTeamList().get(0).GetPlayers(true))
		{
			UtilPlayer.message(player, F.main("Build", "Recreate the build shown."));
		}
		
		UtilTextMiddle.display("", C.cGold + _currentBuild.BuildText, 0, 80, 10);
		
		_roundsPlayed++;
		
		setSpeedBuilderState(SpeedBuildersState.VIEWING);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (GetTeamList().size() > 1 && GetTeamList().get(1).HasPlayer(event.getPlayer()))
			GetTeamList().get(1).RemovePlayer(event.getPlayer());
		
		RecreationData recreation = null;
		
		if (_buildRecreations.containsKey(event.getPlayer()))
			recreation = _buildRecreations.remove(event.getPlayer());
		
		if (recreation != null)
		{
			HashSet<Block> blocks = UtilBlock.findConnectedBlocks(recreation.OriginalBuildLocation.getBlock(), recreation.OriginalBuildLocation.getBlock(), null, 2000, 8);

			//Sets should remove duplicates
			blocks.addAll(recreation.getBlocks());

			Manager.GetExplosion().BlockExplosion(blocks, recreation.getMidpoint(), false, true);
			
			recreation.clearBuildArea(false);
			
			if (_toEliminate.contains(recreation))
				_toEliminate.remove(recreation);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (!_buildRecreations.containsKey(event.getPlayer()))
			return;
		
		if (_perfectBuild.containsKey(event.getPlayer()))
		{
			event.setCancelled(true);
			return;
		}
		
		if (_buildRecreations.get(event.getPlayer()).isQueuedForDemolition(event.getBlock()))
		{
			event.setCancelled(true);
			return;
		}
		
		if (_buildRecreations.get(event.getPlayer()).inBuildArea(event.getBlock()) && event.getBlock().getType() != Material.BED_BLOCK)
			return;
		else if (event.getBlock().getType() == Material.BED_BLOCK)
		{
			Bed bed = (Bed) event.getBlock().getState().getData();
			
			if (bed.isHeadOfBed())
			{
				Block foot = event.getBlock().getRelative(bed.getFacing().getOppositeFace());
				
				if (_buildRecreations.get(event.getPlayer()).inBuildArea(foot))
					return;
			}
			else
			{
				Block head = event.getBlock().getRelative(bed.getFacing());
				
				if (_buildRecreations.get(event.getPlayer()).inBuildArea(head))
					return;
			}
		}
		
		event.setCancelled(true);
		UtilPlayer.message(event.getPlayer(), F.main("Build", "Cannot build outside your area!"));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBuildFinish(final BlockPlaceEvent event)
	{
		checkPerfectBuild(event.getPlayer());
	}

	//This is here because if you open a door then close it you won't be informed of a perfect build
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void interactInformSuccess(PlayerInteractEvent event)
	{
		checkPerfectBuild(event.getPlayer());
	}

	public void checkPerfectBuild(Player player)
	{
		Manager.runSyncLater(() ->
		{
			if (!IsLive() || _state != SpeedBuildersState.BUILDING || !_buildRecreations.containsKey(player) || _perfectBuild.containsKey(player))
			{
				return;
			}

			if (_buildRecreations.get(player).calculateScoreFromBuild(_currentBuild) == _currentBuild.getPerfectScore())
			{
				long timeElapsed = System.currentTimeMillis() - _stateTime;
				PerfectBuildEvent perfectBuildEvent = new PerfectBuildEvent(player, timeElapsed, SpeedBuilders.this);

				Bukkit.getServer().getPluginManager().callEvent(perfectBuildEvent);

				player.playSound(player.getEyeLocation(), Sound.LEVEL_UP, 10F, 1F);

				String time = UtilTime.convertString(timeElapsed, 1, UtilTime.TimeUnit.SECONDS);
				Announce(F.main("Build", F.name(player.getName()) + " got a perfect build in " + F.time(time) + "!"));

				_perfectBuild.put(player, System.currentTimeMillis());

				if (_perfectBuild.size() == _buildRecreations.size())
				{
					// Everyone has a perfect build
					_allPerfect = true;
				}
				else
				{
					// Don't display middle text if everyone now has a perfect build
					UtilTextMiddle.display("", C.cGreen + "Perfect Match", 0, 30, 10, player);
				}
			}
		}, 0);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPickupItem(PlayerPickupItemEvent event)
	{
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (!_buildRecreations.containsKey(event.getPlayer()))
			return;
		
		if (_buildRecreations.get(event.getPlayer()).DroppedItems.containsKey(event.getItem()))
			_buildRecreations.get(event.getPlayer()).DroppedItems.remove(event.getItem());
		else
			event.setCancelled(true);
	}

	@EventHandler
	public void stopItemMerge(ItemMergeEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void stopMoveOffArea(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (!IsLive())
			return;
		
		for (Player player : GetPlayers(true))
		{
			if (!_buildRecreations.containsKey(player))
				continue;
			
			RecreationData recreation = _buildRecreations.get(player);
			double dist = UtilMath.offsetSquared(player.getLocation(), recreation.OriginalBuildLocation.clone().add(0.5, 0, 0.5));
			
			for (Location loc : WorldData.GetDataLocs("YELLOW"))
			{
				if (loc.equals(recreation.OriginalBuildLocation))
					continue;
				
				double distFromOther = UtilMath.offsetSquared(player.getLocation(), loc.clone().add(0.5, 0, 0.5));
				
				if (player.getGameMode() != GameMode.SPECTATOR && (dist > distFromOther || player.getLocation().getY() < recreation.OriginalBuildLocation.getY() - 2))
				{
					player.teleport(recreation.PlayerSpawn);

					UtilPlayer.message(player, F.main("Build", "You cannot leave your area!"));
					UtilTextMiddle.display("", C.cRed + "You cannot leave your area!", 0, 30, 10, player);

					break;
				}
			}
		}
	}

	@EventHandler
	public void stopGuardiansBuildEnter(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;
		
		if (!IsLive())
			return;
		
		for (Player player : GetTeamList().get(1).GetPlayers(false))
		{
			for (RecreationData recreation : _buildRecreations.values())
			{
				Vector vec = UtilAlg.getTrajectory(recreation.getMidpoint(), player.getLocation());
				
				if (UtilMath.offsetSquared(player.getLocation(), recreation.getMidpoint()) < 64)
				{
					Location tpLoc = recreation.getMidpoint().add(vec.clone().multiply(8));
					tpLoc.setDirection(player.getLocation().getDirection());
					
					//First tp out this combats hacked clients with anti-KB
					player.teleport(tpLoc);
					
					//Then apply velocity as normal
					UtilAction.velocity(player, vec, 1.8, false, 0, 0.4, vec.length(), false);
					
					player.playSound(player.getEyeLocation(), Sound.NOTE_PLING, 10F, 0.5F);
				}
			}
		}
	}

	@EventHandler
	public void border(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (!InProgress())
			return;
		
		Location specLocation = GetSpectatorLocation();
		
		//This can be done like this cause nobody should be outside
		for (Player player : UtilServer.getPlayers())
		{
			if (!isInsideMap(player))
				player.teleport(specLocation);
		}		
	}

	@EventHandler
	public void stateUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!IsLive())
			return;
		
		if (_state == SpeedBuildersState.VIEWING)
		{
			if (UtilTime.elapsed(_stateTime, _viewTime * 1000))
			{
				for (RecreationData recreation : _buildRecreations.values())
				{
					recreation.breakAndDropItems();
				}
				
				ItemPickup = true;
				BlockPlace = true;
				
				_buildCountStage = 0;
				
				//Sometimes it doesn't show in the update method
				UtilTextMiddle.display("", C.cRed + "View Time Over!", 0, 30, 10);
				
				for (Player player : GetTeamList().get(0).GetPlayers(true))
				{
					UtilPlayer.message(player, F.main("Build", "Recreate the build shown."));
				}
				
				setSpeedBuilderState(SpeedBuildersState.BUILDING);
			}
		}
		else if (_state == SpeedBuildersState.BUILDING)
		{
			if (UtilTime.elapsed(_stateTime, _buildTime * 1000) || _allPerfect)
			{
				for (RecreationData recreation : _buildRecreations.values())
				{
					for (Item item : recreation.DroppedItems.keySet())
					{
						item.remove();
					}
					
					recreation.DroppedItems.clear();
					
					UtilInv.Clear(recreation.Player);
				}
				
				//Sometimes it stops on 0.1 and has one bar green
				UtilTextBottom.displayProgress("Time Left:", 0, UtilTime.MakeStr(0), UtilServer.getPlayers());

				if (_allPerfect)
				{
					UtilTextMiddle.display("", C.cAqua + GUARDIAN_NAME + " is Impressed!", 0, 100, 10);
					_allPerfect = false;
				}
				else
				{
					//Sometimes doesn't show in the update method
					UtilTextMiddle.display("", C.cRed + "TIME'S UP!", 0, 30, 10);

					Manager.runSyncLater(new Runnable()
					{
						@Override
						public void run()
						{
							UtilTextMiddle.display("", C.cAqua + GUARDIAN_NAME + " is Judging", 0, 40, 10);
						}
					}, 40L);
				}
				
				for (Player player : UtilServer.getPlayers())
				{
//					player.setGameMode(GameMode.SPECTATOR);
//					player.setSpectatorTarget(_judgeEntity);
					
//					Manager.GetCondition().Factory().Cloak("Guardian POV", player, null, 999999999, false, false);
					
					PacketPlayOutGameStateChange packet = new PacketPlayOutGameStateChange(10, 0.0F);
					UtilPlayer.sendPacket(player, packet);
				}
				
				_perfectBuild.clear();
				
				ItemPickup = false;
				BlockPlace = false;
				
				RecreationData lowest = null;
				int lowestScore = -1;

				boolean allPerfectMatch = !_buildRecreations.isEmpty();
				boolean allPlayersEqual = true;
				
				for (RecreationData recreation : _buildRecreations.values())
				{
					int score = recreation.calculateScoreFromBuild(_currentBuild);

					if (lowest != null && lowestScore != score)
					{
						allPlayersEqual = false;
					}
					
					if (lowest == null || lowestScore > score)
					{
						lowest = recreation;
						lowestScore = score;
					}
	
					if (score != _currentBuild.getPerfectScore())
						allPerfectMatch = false;
					
					if (recreation.isEmptyBuild(_currentBuild))
						_toEliminate.add(recreation);
				}
				
				if (!allPerfectMatch && !allPlayersEqual && lowest != null && !_toEliminate.contains(lowest))
					_toEliminate.add(lowest);
				
				if (!_toEliminate.isEmpty())
				{
					Manager.runSyncLater(new Runnable()
					{
						@Override
						public void run()
						{
							for (Player player : GetTeamList().get(0).GetPlayers(true))
							{
								if (!_buildRecreations.containsKey(player))
									return;
								
								int percent = (int) (((double) _buildRecreations.get(player).calculateScoreFromBuild(_currentBuild) / _currentBuild.getPerfectScore()) * 100d);
								
								UtilTextMiddle.display("", getPercentPrefix(percent) + "You scored " + percent + " Percent", 0, 40, 10, player);
							}
						}
					}, 130L);
				}
				
				_lastElimination = System.currentTimeMillis();
				_reviewStartTime = System.currentTimeMillis();
				
				pasteBuildInCenter(_currentBuild);
				
				setSpeedBuilderState(SpeedBuildersState.REVIEWING);

				for (Player player : GetTeamList().get(0).GetPlayers(true))
				{
					player.setGameMode(GameMode.SPECTATOR);
//					player.setAllowFlight(true);
//					player.setFlying(true);
				}
			}
		}
		else if (_state == SpeedBuildersState.REVIEWING)
		{	
			if (_toEliminate.isEmpty())
			{
				if (!UtilTime.elapsed(_lastElimination, 3000))
					return;
				
				clearCenterArea(true);
				
				if (_nextBuild != null)
					_currentBuild = _nextBuild;
				else
					_currentBuild = UtilAlg.Random(_buildData, _usedBuilds);
				
				_nextBuild = null;
				_usedBuilds.add(_currentBuild);
				_buildTime = _currentBuild.getBuildTime(_buildTimeTracker);

				for (Player player : GetTeamList().get(0).GetPlayers(true))
				{
					player.setGameMode(GameMode.SURVIVAL);
//					player.setAllowFlight(false);
//					player.setFlying(false);
				}
				
				for (RecreationData recreation : _buildRecreations.values())
				{
					recreation.Player.teleport(recreation.PlayerSpawn);
					
					recreation.pasteBuildData(_currentBuild);
				}

//				Location specLocation = GetSpectatorLocation();
//				double avgDist = 0;
//
//				//Add up all the distances
//				for (Location loc : GetTeamList().get(0).GetSpawns())
//				{
//					avgDist += UtilMath.offset2d(specLocation, loc);
//				}
//
//				//Get the average by dividing
//				avgDist /= GetTeamList().get(0).GetSpawns().size();
//				
//				for (Player player : UtilServer.getPlayers())
//				{
//					player.setGameMode(GameMode.SURVIVAL);
//					
//					Manager.GetCondition().EndCondition(player, ConditionType.CLOAK, "Guardian POV");
//					
//					if (_buildRecreations.containsKey(player))
//						player.teleport(_buildRecreations.get(player).PlayerSpawn);
//					
//					if (!IsAlive(player) || GetTeamList().get(1).HasPlayer(player))
//					{
//						player.setAllowFlight(true);
//						player.setFlying(true);
//
//						Location toTeleport = specLocation.clone();
//
//						//Spread players by getting a random x and z in that radius
//						toTeleport.setX(toTeleport.getX() + (Math.random() * avgDist * 2 - avgDist));
//						toTeleport.setZ(toTeleport.getZ() + (Math.random() * avgDist * 2 - avgDist));
//
//						toTeleport.setDirection(UtilAlg.getTrajectory(toTeleport, _buildMiddle.clone().add(0.5, 0, 0.5)));
//						
//						player.teleport(toTeleport);
//					}
//				}
				
				_roundsPlayed++;

				if (_buildTimeTracker > 1)
					_buildTimeTracker--;
				
				_viewCountStage = 0;
				
				for (Player player : GetTeamList().get(0).GetPlayers(true))
				{
					UtilPlayer.message(player, F.main("Build", "You will recreate this build."));
				}
				
				UtilTextMiddle.display("", C.cGold + _currentBuild.BuildText, 0, 80, 10);
				
				setSpeedBuilderState(SpeedBuildersState.VIEWING);
			}
			else
			{
				if (UtilTime.elapsed(_reviewStartTime, 10000) && UtilTime.elapsed(_lastElimination, 2000) && !_eliminating)
				{
					//Eliminate in order This also means that the empty builds are eliminated first
					final RecreationData eliminating = _toEliminate.get(0);
					
					judgeTargetLocation(eliminating.OriginalBuildLocation.clone().subtract(0, 1.7, 0));
					
					UtilTextMiddle.display("", C.cRed + eliminating.Player.getName() + C.cGreen + " was eliminated!", 0, 30, 10);
					
					_eliminating = true;
					
					Manager.runSyncLater(new Runnable()
					{
						@Override
						public void run()
						{
							_lastElimination = System.currentTimeMillis();
							
							_eliminating = false;
							
							if (!_toEliminate.contains(eliminating))
								return;
							
							WorldData.World.playEffect(eliminating.getMidpoint(), Effect.EXPLOSION_HUGE, 0);

							for (Player player : UtilServer.getPlayers())
							{
								player.playSound(player.getEyeLocation(), Sound.ZOMBIE_REMEDY, 1F, 1F);
							}
							
							HashSet<Block> blocks = UtilBlock.findConnectedBlocks(eliminating.OriginalBuildLocation.getBlock(), eliminating.OriginalBuildLocation.getBlock(), null, 2000, 8);

							//Sets should remove duplicates
							blocks.addAll(eliminating.getBlocks());

							Manager.GetExplosion().BlockExplosion(blocks, eliminating.getMidpoint(), false, true);
							
							eliminating.clearBuildArea(false);
							eliminating.removeHologram();
							
							judgeTargetLocation(null);
							
							_toEliminate.remove(eliminating);
							
							_buildRecreations.remove(eliminating.Player);
							
							moveToGuardians(eliminating.Player, true);
						}
					}, 100L);
				}
				else if (!_eliminating)
				{
					double speed = 10d;
					
					double oX = -Math.sin(_judgeEntity.getTicksLived() / speed) * 12;
					double oY = 0;
					double oZ = Math.cos(_judgeEntity.getTicksLived() / speed) * 12;
					
					Location toLook = _judgeEntity.getLocation().add(new Vector(oX, oY, oZ));
					
					UtilEnt.CreatureLook(_judgeEntity, toLook);
				}
			}
		}
	}

	private String getPercentPrefix(int percent)
	{
		if (percent >= 75)
			return C.cAqua;
		else if (percent >= 50)
			return C.cGreen;
		else if (percent >= 25)
			return C.cYellow;
		else
			return C.cRed;
	}

	@EventHandler
	public void buildTimeProgressBar(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!IsLive())
			return;
		
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		long timeLeft = 1000 * _buildTime - (System.currentTimeMillis() - _stateTime);
		
		if (timeLeft < 0)
			timeLeft = 0;
		
		UtilTextBottom.displayProgress("Time Left", timeLeft / (_buildTime * 1000.0D), UtilTime.MakeStr(timeLeft), UtilServer.getPlayers());
	}

	@EventHandler
	public void buildEndCountdown(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!IsLive())
			return;
		
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (UtilTime.elapsed(_stateTime, 1000 * _buildCountStage))
		{
			ArrayList<Player> players = new ArrayList<Player>(UtilServer.getServer().getOnlinePlayers());
			
			for (Entry<Player, Long> entry : _perfectBuild.entrySet())
			{
				if (!UtilTime.elapsed(entry.getValue(), 5000))
					players.remove(entry.getKey());
			}
			
			if (_buildCountStage == _buildTime)
				UtilTextMiddle.display("", C.cRed + "TIME'S UP!", 0, 30, 10);
			else if (_buildCountStage >= _buildTime - 5)
				UtilTextMiddle.display("", C.cGreen + (_buildTime - _buildCountStage), 0, 30, 10, players.toArray(new Player[players.size()]));
			
			if (_buildCountStage >= _buildTime - 5)
			{
				for (Player player : UtilServer.getPlayers())
				{
					player.playSound(player.getEyeLocation(), Sound.NOTE_PLING, 1F, 1F - (float) (0.1 * (_buildTime - _buildCountStage)));
				}
			}
			
			_buildCountStage++;
		}
	}

	@EventHandler
	public void viewCountdown(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!IsLive())
			return;
		
		if (_state != SpeedBuildersState.VIEWING)
			return;
		
		if (UtilTime.elapsed(_stateTime, _viewCountStage * 1000))
		{
			if (_viewCountStage == _viewTime)
				UtilTextMiddle.display("", C.cRed + "View Time Over!", 0, 30, 10);
			else if (_viewCountStage > 3)
				UtilTextMiddle.display("", C.cGreen + (_viewTime - _viewCountStage), 0, 30, 10);
			
			if (_viewCountStage > 3)
			{
				for (Player player : UtilServer.getPlayers())
				{
					player.playSound(player.getEyeLocation(), Sound.NOTE_PLING, 1F, 1F - (float) (0.1 * (_viewTime - _viewCountStage)));
				}
			}
			
			_viewCountStage++;
		}
	}

	@EventHandler
	public void markBlockForDemolition(PlayerInteractEvent event)
	{
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (!_buildRecreations.containsKey(event.getPlayer()))
			return;
		
		if (!UtilEvent.isAction(event, ActionType.L_BLOCK))
			return;
		
		if (_perfectBuild.containsKey(event.getPlayer()))
			return;
		
		if (!_buildRecreations.get(event.getPlayer()).inBuildArea(event.getClickedBlock()))
			return;
		
		if (event.getClickedBlock().getType() == Material.AIR)
			return;
		
		_buildRecreations.get(event.getPlayer()).addToDemolition(event.getClickedBlock());
	}

	@EventHandler
	public void markMobForDemolition(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Player))
			return;
		
		Player player = (Player) event.getDamager();
		
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (!_buildRecreations.containsKey(player))
			return;
		
		if (_perfectBuild.containsKey(player))
			return;
		
		if (!_buildRecreations.get(player).inBuildArea(event.getEntity().getLocation()))
			return;
		
		boolean hasMobType = false;
		
		for (MobData mobData : _currentBuild.Mobs)
		{
			if (mobData.EntityType == event.getEntityType())
			{
				hasMobType = true;
				
				break;
			}
		}
		
		if (!hasMobType)
			return;
		
		_buildRecreations.get(player).addToDemolition(event.getEntity());
	}

	@EventHandler
	public void stopBabyEgg(PlayerInteractEntityEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getPlayer().getItemInHand().getType() == Material.MONSTER_EGG)
			event.setCancelled(true);
	}

	@EventHandler
	public void updateDemolitionBlocks(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (RecreationData recreation : _buildRecreations.values())
		{
			ArrayList<DemolitionData> blocksForDemolition = new ArrayList<DemolitionData>(recreation.BlocksForDemolition);
			
			for (DemolitionData demolition : blocksForDemolition)
			{
				if (_state != SpeedBuildersState.BUILDING || _perfectBuild.containsKey(demolition.Parent.Player))
					demolition.cancelBreak();
				else
					demolition.update();
			}
		}
	}

	@EventHandler
	public void preventBlockGrowth(BlockGrowEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void preventStructureGrowth(StructureGrowEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void judgeLooking(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;
		
		if (!InProgress())
			return;
		
		if (_state != SpeedBuildersState.BUILDING && _state != SpeedBuildersState.VIEWING)
			return;
		
		if (_buildRecreations.isEmpty())
			return;
		
		if (_lookTarget == null || (UtilTime.elapsed(_targetReached, _stayTime) && _standMoveProgress > 1))
		{
			
			RecreationData target = null;
			
			do
			{
				target = _buildRecreations.get(UtilAlg.Random(_buildRecreations.keySet()));
			}
			while (target.equals(_lastRecreationTarget) && _buildRecreations.size() > 1);
			
			_lookTarget = target.getMidpoint().subtract(UtilAlg.getTrajectory(_judgeEntity.getEyeLocation(), target.getMidpoint()).multiply(5));
			_stayTime = UtilMath.rRange(4000, 8000);
			_lastRecreationTarget = target;
			_standMoveProgress = 0;
			
			if (_lookStand != null)
				_standStart = _lookStand.getLocation();
		}
		
		if (_lookStand == null)
		{
			_lookStand = WorldData.World.spawn(_judgeEntity.getEyeLocation().add(_judgeEntity.getEyeLocation().getDirection().multiply(10)), ArmorStand.class);
			
			_lookStand.setGravity(false);
			_lookStand.setSmall(true);
			_lookStand.setVisible(false);
			_lookStand.setGhost(true);
			_lookStand.setMarker(false);
			
			_standStart = _lookStand.getLocation();
		}
		
		if (_standMoveProgress > 1)
			return;
		
		Location newLoc = _standStart.clone().add(UtilAlg.getTrajectory(_standStart, _lookTarget).multiply(UtilMath.offset(_standStart, _lookTarget) * _standMoveProgress));
		
		moveEntity(newLoc, _lookStand);
		
		UtilEnt.CreatureLook(_judgeEntity, _lookStand);
		
		_standMoveProgress += 0.2;
		
		if (_standMoveProgress > 1)
			_targetReached = System.currentTimeMillis();
	}

	private void moveEntity(Location loc, Entity entity)
	{
		double dx = loc.getX() - entity.getLocation().getX();
		double dy = loc.getY() - entity.getLocation().getY();
		double dz = loc.getZ() - entity.getLocation().getZ();
		
		((CraftEntity) entity).getHandle().move(dx, dy, dz);
	}

	@EventHandler
	public void specNightVision(UpdateEvent event)
	{
		if (!InProgress())
			return;
		
		if (event.getType() != UpdateType.SEC)
			return;
		
		for (Player player : UtilServer.getPlayers())
		{
			if (UtilPlayer.isSpectator(player) || (GetTeamList().size() > 1 && GetTeamList().get(1).HasPlayer(player)))
			{
				player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false), true);
				player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, true, false), true);
			}
		}
	}

//	@EventHandler
//	public void stopJudgeUnspec(UpdateEvent event)
//	{
//		if (event.getType() != UpdateType.TICK)
//			return;
//		
//		if (!IsLive())
//			return;
//		
//		if (_state != SpeedBuilderState.REVIEWING)
//			return;
//		
//		for (Player player : UtilServer.getPlayers())
//		{
//			player.setGameMode(GameMode.SPECTATOR);
//			player.setSpectatorTarget(_judgeEntity);
//			
//			if (!Manager.GetCondition().HasCondition(player, ConditionType.CLOAK, "Guardian POV"))
//				Manager.GetCondition().Factory().Cloak("Guardian POV", player, null, 999999999, false, false);
//		}
//	}

	@EventHandler
	public void stopGuardianSpecPickup(PlayerPickupItemEvent event)
	{
		if (GetState().ordinal() < GameState.Prepare.ordinal())
			return;
		
		if (Manager.isSpectator(event.getPlayer()) || GetTeamList().get(1).HasPlayer(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void stopGuardianSpecPlace(BlockPlaceEvent event)
	{
		if (GetState().ordinal() < GameState.Prepare.ordinal())
			return;
		
		if (Manager.isSpectator(event.getPlayer()) || GetTeamList().get(1).HasPlayer(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void stopEntityChangeBlock(EntityChangeBlockEvent event)
	{
		if (!IsLive())
			return;
		
		// Falling blocks disappear for some reason so we update to make it reappear
		event.getBlock().getState().update(true, false);
		
		event.setCancelled(true);
	}

	@EventHandler
	public void stopBlockFade(BlockFadeEvent event)
	{
		if (!IsLive())
			return;
		
		event.setCancelled(true);
	}

	@EventHandler
	public void stopBlockBurn(BlockBurnEvent event)
	{
		if (!IsLive())
			return;
		
		event.setCancelled(true);
	}

	@EventHandler
	public void stopLeavesDecay(LeavesDecayEvent event)
	{
		if (!IsLive())
			return;
		
		event.setCancelled(true);
	}

	@EventHandler
	public void stopBlockForm(BlockFormEvent event)
	{
		if (!IsLive())
			return;
		
		event.setCancelled(true);
	}

	@EventHandler
	public void stopBlockSpread(BlockSpreadEvent event)
	{
		if (!IsLive())
			return;
		
		event.setCancelled(true);
	}

	@EventHandler
	public void stopLiquidLeaks(BlockFromToEvent event)
	{
		for (RecreationData recreation : _buildRecreations.values())
		{
			if ((recreation.inBuildArea(event.getBlock()) && !recreation.inBuildArea(event.getToBlock())) || (!recreation.inBuildArea(event.getBlock()) && recreation.inBuildArea(event.getToBlock())))
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void stopPhysics(BlockPhysicsEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getBlock().isLiquid())
			return;
		
		event.setCancelled(true);
	}

	@EventHandler
	public void stopInventoryPickup(InventoryPickupItemEvent event)
	{
		if (!IsLive())
			return;
		
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
	{
		if (!IsLive())
			return;
		
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (!_buildRecreations.containsKey(event.getPlayer()))
			return;
		
		if (_perfectBuild.containsKey(event.getPlayer()))
		{
			event.setCancelled(true);
			return;
		}
		
		Block liquid = event.getBlockClicked().getRelative(event.getBlockFace());
		
		if (!_buildRecreations.get(event.getPlayer()).inBuildArea(liquid))
		{
			event.setCancelled(true);
			
			UtilPlayer.message(event.getPlayer(), F.main("Build", "Cannot build outside your area!"));
		}
		else 
		{
			if (liquid.getType() == Material.STATIONARY_WATER || liquid.getType() == Material.WATER)
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerBucketFill(PlayerBucketFillEvent event)
	{
		if (!IsLive())
			return;
		
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (!_buildRecreations.containsKey(event.getPlayer()))
			return;
		
		if (_perfectBuild.containsKey(event.getPlayer()))
		{
			event.setCancelled(true);
			return;
		}
		
		Block liquid = event.getBlockClicked().getRelative(event.getBlockFace());
		
		if (!_buildRecreations.get(event.getPlayer()).inBuildArea(liquid))
		{
			event.setCancelled(true);
			
			UtilPlayer.message(event.getPlayer(), F.main("Build", "Cannot build outside your area!"));
		}
	}

	@EventHandler
	public void addMob(PlayerInteractEvent event)
	{
		if (!IsLive())
			return;
		
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
			return;
		
		if (event.getItem() == null)
			return;
		
		if (event.getItem().getType() != Material.MONSTER_EGG)
			return;
		
		if (!_buildRecreations.containsKey(event.getPlayer()))
			return;
		
		EntityType type = EntityType.fromId(event.getItem().getDurability());
		
		Block block = event.getClickedBlock().getRelative(event.getBlockFace());
		
		if (!_buildRecreations.get(event.getPlayer()).inBuildArea(block))
			return;
		
		CreatureAllowOverride = true;
		
		Entity entity = block.getWorld().spawnEntity(block.getLocation().add(0.5, 0, 0.5), type);
		
		UtilEnt.vegetate(entity, true);
		UtilEnt.ghost(entity, true, false);
		
		CreatureAllowOverride = false;
		
		_buildRecreations.get(event.getPlayer()).Mobs.add(entity);
		
		UtilInv.remove(event.getPlayer(), Material.MONSTER_EGG, (byte) event.getItem().getDurability(), 1);
	}

	@EventHandler
	public void stopCombust(EntityCombustEvent event)
	{
		if (!IsLive())
			return;
		
		event.setCancelled(true);
	}

	@EventHandler
	public void moveSetFlight(PlayerMoveEvent event)
	{
		if (!IsLive())
			return;
		
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (!GetTeamList().get(0).HasPlayer(event.getPlayer()))
			return;
		
		if (UtilEnt.isGrounded(event.getPlayer()) && !event.getPlayer().isFlying())
			event.getPlayer().setAllowFlight(true);
	}

	@EventHandler
	public void flightToggleJump(PlayerToggleFlightEvent event)
	{
		if (!GetTeamList().get(0).HasPlayer(event.getPlayer()))
			return;
		
		event.setCancelled(true);
		
		event.getPlayer().setAllowFlight(false);
		
		event.getPlayer().playSound(event.getPlayer().getEyeLocation(), Sound.GHAST_FIREBALL, 1f, 1f);
		
		UtilAction.velocity(event.getPlayer(), new Vector(0, 1, 0));
	}
	
	@EventHandler
	public void fixDoorToggling(PlayerInteractEvent event)
	{
		if (!IsLive())
			return;
		
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (!_buildRecreations.containsKey(event.getPlayer()))
			return;
		
		if (_perfectBuild.containsKey(event.getPlayer()))
		{
			event.setCancelled(true);
			return;
		}
		
		if (event.getPlayer().getItemInHand() == null)
		{
			event.setCancelled(true);
			return;
		}

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK)
		{
			return;
		}
		
		Block block = event.getClickedBlock().getRelative(event.getBlockFace());
		
		if (!_buildRecreations.get(event.getPlayer()).inBuildArea(block))
		{
			event.setCancelled(true);
			UtilPlayer.message(event.getPlayer(), F.main("Build", "Cannot build outside your area!"));
		}
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;
		
		GameTeam playersTeam = GetTeamList().get(0);

		if (playersTeam.GetPlayers(true).size() <= 1)
		{	
			List<Player> places = playersTeam.GetPlacements(true);
			
			//Announce
			AnnounceEnd(places);

			//Gems
			if (places.size() >= 1)
				AddGems(places.get(0), 20, "1st Place", false, false);

			if (places.size() >= 2)
				AddGems(places.get(1), 15, "2nd Place", false, false);

			if (places.size() >= 3)
				AddGems(places.get(2), 10, "3rd Place", false, false);

			ArrayList<Player> participants = new ArrayList<Player>();
			
			ArrayList<Player> guardians = GetTeamList().get(1).GetPlayers(false);
			
			participants.addAll(playersTeam.GetPlayers(true));
			
			guardians.retainAll(playersTeam.GetPlacements(true));
			
			participants.addAll(guardians);
			
			for (Player player : participants)
				if (player.isOnline())
					AddGems(player, 10, "Participation", false, false);

			//End
			SetState(GameState.End);
		}
	}

	@Override
	public List<Player> getLosers()
	{
		List<Player> winners = getWinners();

		if (winners == null)
			return null;
		
		if (GetTeamList().size() < 2)
			return new ArrayList<Player>();

		List<Player> losers = GetTeamList().get(1).GetPlayers(false);

		losers.removeAll(winners);
		losers.retainAll(GetTeamList().get(0).GetPlacements(true));

		return losers;
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (GetTeamList().isEmpty())
			return;
		
		Scoreboard.reset();
		
		Scoreboard.writeNewLine();
		
		Scoreboard.write(C.cYellowB + "Build");
		
		if (_currentBuild == null)
			Scoreboard.write("(None)");
		else
			Scoreboard.write(C.cWhite + _currentBuild.BuildText);
		
		Scoreboard.writeNewLine();
		
		Scoreboard.write(C.cYellowB + "Round");
		Scoreboard.write("" + _roundsPlayed);
		
		Scoreboard.writeNewLine();
		
		List<Player> playersAlive = GetTeamList().get(0).GetPlayers(true);
		
		List<Player> playersDead = new ArrayList<Player>();
		
		if (GetTeamList().size() > 1)
			playersDead.addAll(GetTeamList().get(1).GetPlayers(false));
		
		Scoreboard.write(C.cYellowB + "Players");
		
		for (Player player : playersAlive)
		{
			Scoreboard.write(player.getName());
		}
		
		for (Player player : playersDead)
		{
			Scoreboard.write(C.cDGray + player.getName());
		}
		
		Scoreboard.draw();
	}

	public Location getJudgeSpawn()
	{
		return _judgeSpawn;
	}
}
