package nautilus.game.arcade.game.games.typewars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.hologram.Hologram;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.typewars.kits.KitTypeWarsBase;
import nautilus.game.arcade.game.games.typewars.kits.KitTyper;
import nautilus.game.arcade.game.games.typewars.spells.SpellKillEverything;
import nautilus.game.arcade.game.games.typewars.stats.DemonStatsTracker;
import nautilus.game.arcade.game.games.typewars.stats.DumbledontStatTracker;
import nautilus.game.arcade.game.games.typewars.stats.HoarderStatTracker;
import nautilus.game.arcade.game.games.typewars.stats.KillsStatTracker;
import nautilus.game.arcade.game.games.typewars.stats.PerfectionistStatTracker;
import nautilus.game.arcade.game.games.typewars.stats.TimeInGameTracker;
import nautilus.game.arcade.game.games.typewars.stats.WaitForItStatTracker;
import nautilus.game.arcade.game.games.typewars.tutorial.TutorialTypeWars;
import nautilus.game.arcade.game.modules.TeamArmorModule;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.gametutorial.events.GameTutorialEndEvent;
import nautilus.game.arcade.gametutorial.events.GameTutorialStartEvent;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.chat.ChatStatData;
import nautilus.game.arcade.world.WorldData;

public class TypeWars extends TeamGame
{
	public enum Perm implements Permission
	{
		DEBUG_MONEY_COMMAND,
		DEBUG_BOSS_COMMAND,
	}

	private List<Minion> _activeMinions;
	private List<Minion> _deadMinions;
	private List<Minion> _finishedMinions;
	
	private Map<Player, Integer> _moneyMap;
	
	private long _lastSpawnedRed;
	private long _timeToSpawnRed;
	
	private long _lastSpawnedBlue;
	private long _timeToSpawnBlue;
	
	private List<Player> _pendingNukes;
	
	private Map<GameTeam, List<Location>> _lineGrowth;
	private Map<GameTeam, List<Location>> _lineShorten;
	private Map<GameTeam, List<Location>> _minionSpawns;
	
	private Map<GameTeam, List<Location>> _giantAttackZones;
	private Map<GameTeam, Giant> _giants;
	private Map<Giant, Location> _giantLocs;
	private Map<GameTeam, Integer> _minionsSpawned;
	private Map<GameTeam, Long> _giantsAttacked;
	
	private Set<Player> _playerTitles;
	
	public TypeWars(ArcadeManager manager)
	{
		super(manager, GameType.TypeWars,
				new Kit[]
						{
								new KitTyper(manager),
						},
				new String[]
						{
								"Protect your Giant from enemy minions.",
								"Type minions names to kill them and get money.",
								"Spend money on Spells and Minion Spawns.",
								"You get ONE free Giant Smash per game.",
								"Kill your enemies Giant before they kill yours!"
						});

		new StaffKillMonitorManager(this);

		this.DeathOut = false;
		this.DamageTeamSelf = false;
		this.DamageSelf = false;
		this.DamageTeamOther = false;
		this.DeathSpectateSecs = 0;
		this.HungerSet = 20;
		this.WorldBoundaryKill = true;

		this.WorldTimeSet = 6000;
		this.DamageEvP = false;
		this.DamagePvE = false;
		this.DamagePvP = false;
		this.Damage = false;
		this.CreatureAllow = false;
		this.PrepareTime = 50000;
		this.PrepareFreeze = false;
		this.PlaySoundGameStart = false;
		this.EnableTutorials = true;
		this.PrepareFreeze = true;
		this.AllowParticles = false;

		_activeMinions = new ArrayList<>();
		_deadMinions = new ArrayList<>();
		_finishedMinions = new ArrayList<>();
		_minionSpawns = new HashMap<>();
		_moneyMap = new HashMap<>();
		_timeToSpawnRed = 30000;
		_timeToSpawnBlue = 30000;
		_lineGrowth = new HashMap<>();
		_lineShorten = new HashMap<>();
		_pendingNukes = new ArrayList<>();
		_giantAttackZones = new HashMap<>();
		_giants = new HashMap<>();
		_minionsSpawned = new HashMap<>();
		_giantsAttacked = new HashMap<>();
		_playerTitles = new HashSet<>();
		_giantLocs = new HashMap<>();

		_animationTicks = 0;
		_nukeFrame = 0;

		registerStatTrackers(
				new DemonStatsTracker(this),
				new DumbledontStatTracker(this),
				new HoarderStatTracker(this),
				new PerfectionistStatTracker(this),
				new WaitForItStatTracker(this),
				new KillsStatTracker(this),
				new TimeInGameTracker(this)
		);

		registerChatStats(
				new ChatStatData("MinionKills", "Kills", true)
		);

		manager.GetCreature().SetDisableCustomDrops(true);

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);

		new TeamArmorModule()
				.giveTeamArmor()
				.giveHotbarItem()
				.register(this);

		registerDebugCommand("money", Perm.DEBUG_MONEY_COMMAND, PermissionGroup.ADMIN, (caller, args) ->
		{
			_moneyMap.put(caller, 1000);
			UtilPlayer.message(caller, F.main("Money", "You got some Money"));
		});
		registerDebugCommand("boss", Perm.DEBUG_BOSS_COMMAND, PermissionGroup.DEV, (caller, args) ->
		{
			if (!IsPlaying(caller))
				return;

			GameTeam teams = GetTeam(caller);
			for (GameTeam team : GetTeamList())
			{
				if (team == teams)
					continue;

				int rdm = UtilMath.r(_minionSpawns.get(teams).size());
				TypeWars.this.CreatureAllowOverride = true;
				Minion minion = new Minion(Manager, _minionSpawns.get(teams).get(rdm), _minionSpawns.get(team).get(rdm), teams, caller, true, MinionSize.BOSS.getRandomType(), rdm);
				_activeMinions.add(minion);
				TypeWars.this.CreatureAllowOverride = false;
				UtilPlayer.message(caller, F.main("Boss", "You have spawned a Boss"));
			}
			;
		});
	}

	@EventHandler
	public void stateChange(GameStateChangeEvent event)
	{
		for (Player player : GetPlayers(true))
		{
			UtilAction.velocity(player, 0.1, 0.1, 0.1, false);
			_moneyMap.put(player, 0);
			_playerTitles.add(player);
		}
		
		if (event.GetState() == GameState.Prepare)
		{
			initSpawns();
			prepareGiants();
		}
		if (event.GetState() != GameState.Live)
			return;
		
		for (GameTeam team : GetTeamList())
		{
			_lineGrowth.put(team, new ArrayList<Location>());
			_lineShorten.put(team, new ArrayList<Location>());
			_minionsSpawned.put(team, 0);
		}
		
		_timeToSpawnRed = 6000 / GetTeamList().get(0).GetPlayers(true).size();
		_timeToSpawnBlue = 6000 / GetTeamList().get(1).GetPlayers(true).size();
		
		_lastSpawnedRed = System.currentTimeMillis();
		_lastSpawnedBlue = System.currentTimeMillis();
	}
	
	public void prepareGiants()
	{
		Location blue = WorldData.GetDataLocs("PURPLE").get(0).clone();
		Location red = WorldData.GetDataLocs("LIME").get(0).clone();
		
		red.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(red, blue)));
		blue.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(blue, red)));
		
		red.getBlock().setType(Material.STONE);
		blue.getBlock().setType(Material.STONE);
		
		red.add(0, 2, 0);
		blue.add(0, 2, 0);
		
		int i = 0;
		for (GameTeam team : GetTeamList())
		{
			Location loc = red;
			if (i == 1)
				loc = blue;

			this.CreatureAllowOverride = true;
			Giant giant = loc.getWorld().spawn(loc, Giant.class);
			_giantLocs.put(giant, loc.clone());
			this.CreatureAllowOverride = false;
			giant.setRemoveWhenFarAway(false);
			UtilEnt.vegetate(giant, true);
			UtilEnt.ghost(giant, true, false);
			
			ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
			LeatherArmorMeta helmetmeta = (LeatherArmorMeta) helmet.getItemMeta();
			helmetmeta.setColor(team.GetColorBase());
			helmet.setItemMeta(helmetmeta);
			
			ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
			LeatherArmorMeta chestmeta = (LeatherArmorMeta) chest.getItemMeta();
			chestmeta.setColor(team.GetColorBase());
			chest.setItemMeta(chestmeta);
			
			ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
			LeatherArmorMeta leggingsmeta = (LeatherArmorMeta) leggings.getItemMeta();
			leggingsmeta.setColor(team.GetColorBase());
			leggings.setItemMeta(leggingsmeta);
			
			ItemStack stack = new ItemStack(Material.LEATHER_BOOTS);
			LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();
			meta.setColor(team.GetColorBase());
			stack.setItemMeta(meta);	
			
			giant.getEquipment().setHelmet(helmet);
			giant.getEquipment().setChestplate(chest);
			giant.getEquipment().setLeggings(leggings);
			giant.getEquipment().setBoots(stack);
			
			giant.setMaxHealth(100);
			giant.setHealth(100);
			_giants.put(team, giant);
			i++;
		}
		for (GameTeam team : GetTeamList())
		{
			for (GameTeam otherTeam : GetTeamList())
			{
				if (team != otherTeam)
				{
					for (Location location : _giantAttackZones.get(team))
					{
						Location giantLoc = _giants.get(otherTeam).getLocation();
						location.setYaw(UtilAlg.GetYaw(new Vector(giantLoc.getBlockX() - location.getBlockX(), giantLoc.getBlockY() - location.getBlockY(), giantLoc.getBlockZ() - location.getBlockZ())));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void fixGiants(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (GetState() != GameState.Prepare)
			return;
		
		for (Giant giant : _giantLocs.keySet())
		{
			giant.teleport(_giantLocs.get(giant));
		}
	}
	
	@EventHandler
	public void Players(UpdateEvent event)
	{
		if (GetState() != GameState.Live && GetState() != GameState.End)
			return;
		
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Player player : GetPlayers(true))
		{
			Recharge.Instance.Reset(player, "Chat Message");
			
			player.setAllowFlight(true);
			player.setFlying(true);
			UtilTextBottom.display(C.cGreen + "You have $" + _moneyMap.get(player), player);
			
			for (Minion minion : _activeMinions)
			{
				if (UtilMath.offset(minion.getEntity().getLocation(), player.getLocation()) < 1)
				{
					UtilAction.velocity(player, UtilAlg.getTrajectory(minion.getEntity().getLocation(), player.getLocation()), 1, true, 1, 1, 1, true);
				}
			}
		}	
	}
	
	@EventHandler
	public void ForcefieldUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		for (Minion minion : _activeMinions)
		{
			for (Player other : UtilServer.getPlayers())
			{
				if (UtilMath.offset(other, minion.getEntity()) > 3)
					continue;

				if (Recharge.Instance.use(other, "Forcefield Bump", 500, false, false))
				{		
					UtilAction.velocity(other, UtilAlg.getTrajectory2d(minion.getEntity(), other), 1.6, true, 0.8, 0, 10, true);
					other.getWorld().playSound(other.getLocation(), Sound.CHICKEN_EGG_POP, 2f, 0.5f);
				}
			}
		}
		for (Giant giant : _giants.values())
		{
			for (Player other : UtilServer.getPlayers())
			{
				if (UtilMath.offset(other, giant) > 10)
					continue;

				if (Recharge.Instance.use(other, "Forcefield Bump", 500, false, false))
				{		
					UtilAction.velocity(other, UtilAlg.getTrajectory2d(giant, other), 1.6, true, 0.8, 0, 10, true);
					other.getWorld().playSound(other.getLocation(), Sound.CHICKEN_EGG_POP, 2f, 0.5f);
				}
			}
		}
	}
	
	private Location _tutorialLocationRed, _tutorialLocationBlue;
	
	private void initSpawns()
	{
		WorldData data = WorldData;

		((CraftWorld) data.World).getHandle().spigotConfig.animalActivationRange = 200;
		((CraftWorld) data.World).getHandle().spigotConfig.monsterActivationRange = 200;
		
		((CraftWorld) data.World).getHandle().spigotConfig.animalTrackingRange = 200;
		((CraftWorld) data.World).getHandle().spigotConfig.monsterTrackingRange = 200;
		
		_minionSpawns.put(GetTeamList().get(0), (ArrayList<Location>)data.GetDataLocs("RED").clone());
		_minionSpawns.put(GetTeamList().get(1), (ArrayList<Location>)data.GetDataLocs("LIGHT_BLUE").clone());
		
		_giantAttackZones.put(GetTeamList().get(0), (ArrayList<Location>) data.GetDataLocs("MAGENTA").clone());
		_giantAttackZones.put(GetTeamList().get(1), (ArrayList<Location>) data.GetDataLocs("ORANGE").clone());
		
		Location blue = WorldData.GetDataLocs("LIME").get(0);
		Location red = WorldData.GetDataLocs("PURPLE").get(0);
		
		ArrayList<Location> tutorialLocations = UtilShapes.getLinesDistancedPoints(red, blue, 1);
		_tutorialLocationRed = tutorialLocations.get(20).clone().add(0, 15, 0);
		_tutorialLocationBlue = tutorialLocations.get(tutorialLocations.size() - 20).clone().add(0, 15, 0);
	}
	
	@EventHandler
	public void tutorialStart(GameTutorialStartEvent event)
	{	
		Location targetRed, targetBlue;
		ArrayList<Location> locations = UtilShapes.getLinesDistancedPoints(_minionSpawns.get(GetTeamList().get(0)).get(4), _minionSpawns.get(GetTeamList().get(1)).get(4), 1);
		
		targetRed = locations.get(locations.size() - 3);
		targetBlue = locations.get(3);	
		if (event.getTutorial().getTeam() == GetTeamList().get(1))
		{
			event.getTutorial().getPhase(1).setLocation(_tutorialLocationRed);
			event.getTutorial().getPhase(1).setTarget(targetRed);
		}
		else
		{
			event.getTutorial().getPhase(1).setLocation(_tutorialLocationBlue);
			event.getTutorial().getPhase(1).setTarget(targetBlue);
		}
	}
	
	@EventHandler
	public void tutorialEnd(final GameTutorialEndEvent event)
	{
		Manager.runSyncLater(new Runnable()
		{
			@Override
			public void run()
			{
				for (Player player : event.getTutorial().getPlayers().keySet())
				{
					Location location = player.getLocation().clone();
					for (GameTeam team : GetTeamList())
					{
						if (team != event.getTutorial().getTeam())
						{
							location.setPitch(UtilAlg.GetPitch(UtilAlg.getTrajectory(location, _giants.get(team).getLocation())));
							location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, _giants.get(team).getLocation())));
						}
					}
					player.teleport(location);
				}
			}
		}, 7);
	}
	
	@Override
	public void addTutorials()
	{
		GetTeamList().get(0).setTutorial(new TutorialTypeWars(Manager));
		GetTeamList().get(1).setTutorial(new TutorialTypeWars(Manager));
	}
	
	@EventHandler
	public void tutorialFrames(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
			return;
		
		for (Giant giant : _giants.values())
		{
			giant.setHealth(100);
		}
			
		_activeMinions.clear();
		_deadMinions.clear();
		_finishedMinions.clear();
	}
	
	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (event.getItem() == null)
			return;
		
		if (GetState() != GameState.Live)
			return;
		
		for (MinionSize type : MinionSize.values())
		{
			if (type.getDisplayItem().getType() == event.getItem().getType() && type.getDisplayItem().getDurability() == event.getItem().getDurability())
			{
				if (type.getCost() > _moneyMap.get(event.getPlayer()))
				{
					UtilTextMiddle.display("", ChatColor.GRAY + "You dont have enough money to spawn this Minion.", event.getPlayer());
					return;
				}
				GameTeam teams = GetTeam(event.getPlayer());
				for (GameTeam team : GetTeamList())
				{
					if (teams != team)
					{
						if (getMinions(teams).size() >= 60)
						{
							UtilTextMiddle.display("", ChatColor.GRAY + "Your Team can't have more than 60 Minions", 5, 30, 5, event.getPlayer());
							return;
						}
							
						this.CreatureAllowOverride = true;
						_moneyMap.put(event.getPlayer(), _moneyMap.get(event.getPlayer()) - type.getCost());
						UtilTextMiddle.display("", ChatColor.GRAY + "You bought a Minion.", event.getPlayer());
						int rdm = UtilMath.r(_minionSpawns.get(teams).size());
						Minion minion = new Minion(Manager, _minionSpawns.get(teams).get(rdm), _minionSpawns.get(team).get(rdm), teams, event.getPlayer(), true, type.getRandomType(), rdm);
						Bukkit.getPluginManager().callEvent(new SummonMinionEvent(event.getPlayer(), minion));
						_activeMinions.add(minion);
						this.CreatureAllowOverride = false;
					}
				}
				return;
			}
		}
	}
	
	@EventHandler
	public void mobSpawn(EntitySpawnEvent event)
	{
		if (event.getEntityType() == EntityType.CREEPER)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void noItems(EntityDeathEvent event)
	{
		event.getDrops().clear();
	}
	
	@EventHandler
	public void spawnMinions(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!IsLive())
			return;
		
		if (UtilTime.elapsed(_lastSpawnedRed, _timeToSpawnRed))
		{
			if (getMinions(GetTeamList().get(0)).size() < 60)
			{
				_lastSpawnedRed = System.currentTimeMillis();
				
				this.CreatureAllowOverride = true;
				int rdm = UtilMath.r(_minionSpawns.get(GetTeamList().get(0)).size());
				Minion minion = null;
				
				if (_minionsSpawned.get(GetTeamList().get(0)) >= 100)
				{
					_minionsSpawned.put(GetTeamList().get(0), 0);
					minion = new Minion(Manager, _minionSpawns.get(GetTeamList().get(0)).get(rdm), _minionSpawns.get(GetTeamList().get(1)).get(rdm), GetTeamList().get(0), null, true, MinionSize.BOSS.getRandomType(), rdm);
					UtilTextMiddle.display("", minion.getTeam().GetColor() + "A Boss monster has spawned!");
				}
				else
				{
					minion = new Minion(Manager, _minionSpawns.get(GetTeamList().get(0)).get(rdm), _minionSpawns.get(GetTeamList().get(1)).get(rdm), GetTeamList().get(0), rdm);
				}
				_activeMinions.add(minion);

				this.CreatureAllowOverride = false;
				
				if (_timeToSpawnRed > 5000 / (GetTeamList().get(1).GetPlayers(true).size() > 0 ? GetTeamList().get(1).GetPlayers(true).size() : 1))
					_timeToSpawnRed = _timeToSpawnRed - 75;
			
			}
		}
		if (UtilTime.elapsed(_lastSpawnedBlue, _timeToSpawnBlue))
		{
			if (getMinions(GetTeamList().get(1)).size() < 60)
			{
				_lastSpawnedBlue = System.currentTimeMillis();
			
				this.CreatureAllowOverride = true;
				int rdm = UtilMath.r(_minionSpawns.get(GetTeamList().get(1)).size());
				Minion minion = null;
				if (_minionsSpawned.get(GetTeamList().get(1)) >= 100)
				{
					_minionsSpawned.put(GetTeamList().get(1), 0);
					minion = new Minion(Manager, _minionSpawns.get(GetTeamList().get(1)).get(rdm), _minionSpawns.get(GetTeamList().get(0)).get(rdm), GetTeamList().get(1), null, true, MinionSize.BOSS.getRandomType(), rdm);
					UtilTextMiddle.display("", minion.getTeam().GetColor() + "A Boss monster has spawned!");
				}
				else
				{
					minion = new Minion(Manager, _minionSpawns.get(GetTeamList().get(1)).get(rdm), _minionSpawns.get(GetTeamList().get(0)).get(rdm), GetTeamList().get(1), rdm);
				}
				_activeMinions.add(minion);
				this.CreatureAllowOverride = false;
				
				if (_timeToSpawnBlue > 5000 / (GetTeamList().get(0).GetPlayers(true).size() > 0 ? GetTeamList().get(0).GetPlayers(true).size() : 1))
					_timeToSpawnBlue = _timeToSpawnRed - 75;
			
			}
		}
	}
	
	@EventHandler
	public void updateMinions(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (GetState() != GameState.Live && GetState() != GameState.Prepare)
			return;
		
		Iterator<Minion> minionIterator = _activeMinions.iterator();
		
		while(minionIterator.hasNext())
		{
			Minion minion = minionIterator.next();
			
			if (minion.isSpawned())
			{
				if (minion.isMoving())
				{
					if (!UtilEnt.CreatureMoveFast(minion.getEntity(), minion.getTarget(), minion.getWalkSpeed()))
					{
						GameTeam enemy = null;
						for (GameTeam teams : GetTeamList())
						{
							if (teams != minion.getTeam())
								enemy = teams;
						}
						Location nextTarget = _giantAttackZones.get(enemy).get(minion.getSpawnID());
						if (!nextTarget.equals(minion.getTarget()))
						{
							minion.setTarget(nextTarget);
						}
						else
						{
							if (!_finishedMinions.contains(minion))
								_finishedMinions.add(minion);
						}
					}
				}
			}	
			Hologram hologram = minion.getHologram();
			hologram.setLocation(minion.getEntity().getLocation().add(0, minion.getTagHight() + 2.3, 0));	
		}
	}
	
	@EventHandler
	public void checkDeadMinions(UpdateEvent event)
	{
		if (GetState() != GameState.Live && GetState() != GameState.End)
			return;
					
		if (event.getType() != UpdateType.FASTER)
			return;
		
		for (Minion minion : _deadMinions)
		{
			if (!minion.getEntity().isDead())
				minion.despawn(null, false);
		}
	}
	
	@EventHandler
	public void giants(UpdateEvent event)
	{
		if (GetState() != GameState.Live)
			return;
		
		if (event.getType() != UpdateType.SLOW)
			return;
		
		for (GameTeam team : _giants.keySet())
		{
			ArrayList<Minion> minions = new ArrayList<>();
			for (Minion minion : _finishedMinions)
			{
				if (!minion.getEntity().isDead())
				{
					if (minion.getTeam() != team)
						minions.add(minion);
				}
			}
			if (minions.isEmpty())
				continue;
				
			Giant giant = _giants.get(team);
			Minion minion = minions.get(UtilMath.r(minions.size()));
			Location loc = giant.getLocation().clone();
			loc.setYaw(UtilAlg.GetYaw(new Vector(minion.getEntity().getLocation().getBlockX() - loc.getBlockX(), minion.getEntity().getLocation().getBlockY() - loc.getBlockY(), minion.getEntity().getLocation().getBlockZ() - loc.getBlockZ())));
			giant.teleport(loc);
			for (Player player : team.GetPlayers(false))
			{
				player.playSound(giant.getLocation(), Sound.ZOMBIE_WOODBREAK, 100, 1);
				player.playSound(giant.getLocation(), Sound.ZOMBIE_IDLE, 1, 1);
			}
			
			UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, minion.getEntity().getLocation(), 0, 0, 0, 1, 1, ViewDist.LONG, UtilServer.getPlayers());
			
			minion.despawn(null, true);
			if (!minion.hasLives())
				_deadMinions.add(minion);
		}
	}
	
	@EventHandler
	public void minionAttack(UpdateEvent event)
	{
		if (GetState() != GameState.Live && GetState() != GameState.Prepare)
			return;

		if (event.getType() != UpdateType.SEC)
			return;
		
		for (GameTeam team : _giants.keySet())
		{
			int damage = 0;
			for (Minion minion : _finishedMinions)
			{
				if (minion.getTeam() == team)
					continue;
				
				if (minion.getEntity().isDead())
					continue;
				
				damage++;
			}
			if (damage == 0)
				continue;
			
			for (GameTeam otherTeam : GetTeamList())
			{
				if (team != otherTeam)
				{
					_giants.get(team).getWorld().playSound(_giants.get(team).getEyeLocation(), Sound.ZOMBIE_HURT, 100, 1);
					_giants.get(team).damage(damage);
				}
			}
			if (!_giantsAttacked.containsKey(team) || UtilTime.elapsed(_giantsAttacked.get(team), 10000))
			{	
				_giantsAttacked.put(team, System.currentTimeMillis());
				for (Player player : GetPlayers(true))
				{
					if (GetTeam(player) == team)
					{
						if (IsLive())
						{
							UtilTextMiddle.display("", "Your giant is under Attack!", 0, 30, 9, player);
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void minionAnimation(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		Iterator<Minion> minionIterator = _activeMinions.iterator();
		while(minionIterator.hasNext())
		{
			Minion minion = minionIterator.next();
			minion.animation();
		}
	}
	
	public double getScore(GameTeam team)
	{
		if (_giants.get(team).isDead())
		{
			return 0;
		}
		return _giants.get(team).getHealth();
	}
	
	@EventHandler
	public void chatCheck(AsyncPlayerChatEvent event)
	{
		if (!IsLive())
			return;
		
		if (!GetPlayers(true).contains(event.getPlayer()))
			return;
		
		if (event.getMessage().split(" ").length == 1)
			event.setCancelled(true);
	}
	
	@EventHandler
	public void chatCheck(PlayerChatEvent event)
	{
		if (!IsLive())
			return;
		
		if (!GetPlayers(true).contains(event.getPlayer()))
			return;
		
		try
		{
			Minion minion = getFarestMininion(event.getPlayer(), event.getMessage());
			
			Bukkit.getPluginManager().callEvent(new TypeAttemptEvent(event.getPlayer(), event.getMessage(), minion != null));
			
			if (minion == null)
				return;
			
			MinionKillEvent minionEvent = new MinionKillEvent(event.getPlayer(), minion, KillType.TYPED);
			Bukkit.getPluginManager().callEvent(minionEvent);
			
			if (minionEvent.isCancelled())
				return;
			
			killMinion(minion, event.getPlayer());

			int spawned = _minionsSpawned.get(GetTeam(event.getPlayer()));
			_minionsSpawned.put(GetTeam(event.getPlayer()), spawned + 1);
			
			if (_playerTitles.contains(event.getPlayer()))
			{
				_playerTitles.remove(event.getPlayer());
				UtilTextMiddle.clear(event.getPlayer());
			}
			UtilTextMiddle.display("", C.cGreen + "+$" + minion.getMoney(), event.getPlayer());
			_moneyMap.put(event.getPlayer(), _moneyMap.get(event.getPlayer()) + minion.getMoney());
			return;
		}
		catch (Exception ex) {}
	}
	
	public enum KillType
	{
		TYPED, SPELL;
	}
	
	public void killMinion(Minion minion, Player player)
	{
		if (!minion.hasLives())
		{
			_activeMinions.remove(minion);
			_deadMinions.add(minion);
		}
		minion.despawn(player, true);
	}
	
	private Minion getFarestMininion(Player player, String msg)
	{
		for (Minion minion : _activeMinions)
		{
			if (msg != null && !minion.getName().equalsIgnoreCase(msg))
				continue;
			
			if (GetTeam(player) == minion.getTeam())
				continue;
			
			boolean found = true;
			
			for (Minion otherMinion : _activeMinions)
			{
				if (minion == otherMinion)
					continue;
				
				if (msg != null && !otherMinion.getName().equalsIgnoreCase(msg))
					continue;
				
				if (GetTeam(player) == otherMinion.getTeam())
					continue;
				
				if (UtilMath.offset(minion.getEntity().getLocation(), minion.getTarget()) > UtilMath.offset(otherMinion.getEntity().getLocation(), otherMinion.getTarget()))
					found = false;
			}
			
			if (found)
				return minion;
			else
				continue;
		}
		return null;
	}
	
	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;
		
		ArrayList<GameTeam> winners = new ArrayList<>();
		
		for (GameTeam team : GetTeamList())
		{
			for (GameTeam otherTeam : GetTeamList())
			{
				if (team == otherTeam)
					continue;
				
				if (getScore(team) <= 0)
				{
					_giants.get(team).damage(1);
					winners.add(otherTeam);
				}	
			}
		}
		
		if (winners.isEmpty())
			return;
		
		GameTeam winner = winners.get(UtilMath.r(winners.size()));
		AnnounceEnd(winner);
		
		Iterator<Minion> minionIterator = _activeMinions.iterator();
		
		while (minionIterator.hasNext())
		{
			Minion minion = minionIterator.next();
			minion.despawn(null, false);
			minionIterator.remove();
		}

		for (GameTeam team : GetTeamList())
		{
			if (WinnerTeam != null && team.equals(WinnerTeam))
			{
				for (Player player : team.GetPlayers(false))
					AddGems(player, 10, "Winning Team", false, false);
			}

			for (Player player : team.GetPlayers(false))
			{
				if (player.isOnline())
				{
					AddGems(player, 10, "Participation", false, false);
					AddGems(player, getPlayerKills(player), getPlayerKills(player) + " Minions killed", false, true);
					
					for (MinionSize size : MinionSize.values())
					{
						if (size == MinionSize.BOSS || size == MinionSize.FREAK || size == MinionSize.EASY)
							continue;
						
						AddGems(player, getSpawnedMinions(player, size) * size.getGemReward(), getSpawnedMinions(player, size) + " " + size.getDisplayName() + " Minions spawned", false, true);
					}
				}
			}
		}

		Scoreboard.reset();

		Scoreboard.writeNewLine();
		
		for (GameTeam team : GetTeamList())
		{
			Scoreboard.write(team.GetColor() + C.Bold + team.GetName() + " Team");
			Scoreboard.write(team.GetColor() + "Health: " + Math.round(getScore(team)));
			Scoreboard.write(team.GetColor() + "Minions: " + getMinions(team).size() + "/60");
			String wpm = String.valueOf((double) getTeamKills(team) / ((double) (System.currentTimeMillis() - GetStateTime())/60000));
			if (wpm.length() > 4)
				wpm = wpm.substring(0, 4);
			
			Scoreboard.write(team.GetColor() + "WPM: " + wpm);
			Scoreboard.writeNewLine();
		}
			
		Scoreboard.draw();
		
		//End
		Manager.GetCreature().SetDisableCustomDrops(false);
		SetState(GameState.End);
	}
	
	@EventHandler
	public void preventFire(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		Iterator<Minion> minionIterator = _activeMinions.iterator();
		while(minionIterator.hasNext())
		{
			minionIterator.next().getEntity().setFireTicks(0);
		}
	}
	
	@EventHandler
	public void updateHotbarItems(UpdateEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getType() != UpdateType.FASTEST)
			return;
		
		for (Player player : GetPlayers(true))
		{
			int e = 0;
			for (Spell spell : ((KitTypeWarsBase) GetKit(player)).getSpells())
			{
				if (spell instanceof SpellKillEverything)
				{
					if (spell.hasUsed(player))
					{				
						player.getInventory().setItem(e, new ItemStack(Material.AIR));
						continue;
					}
				}
				if (_moneyMap.get(player) >= spell.getCost())
				{
					if (spell.getCost() > 0)
						player.getInventory().setItem(e, ItemStackFactory.Instance.CreateStack(spell.getMaterial(), (byte) 0, Math.round(_moneyMap.get(player)/spell.getCost()), C.cGreen + "Activate " + spell.getName() + " Cost: " + spell.getCost()));
					else
						player.getInventory().setItem(e, ItemStackFactory.Instance.CreateStack(spell.getMaterial(), (byte) 0, 1, C.cGreen + "Activate " + spell.getName() + " Cost: " + spell.getCost()));
				}
				else
				{
					player.getInventory().setItem(e, ItemStackFactory.Instance.CreateStack(spell.getMaterial(), (byte) 0, 0, C.cRed + "Activate " + spell.getName() + " Cost: " + spell.getCost()));
				}
				e++;
			}
			
			int i = 4;
			for (MinionSize type : MinionSize.values())
			{
				if (type == MinionSize.BOSS || type == MinionSize.FREAK)
					continue;
				
				if (_moneyMap.get(player) >= type.getCost())
				{
					player.getInventory().setItem(i, ItemStackFactory.Instance.CreateStack(type.getDisplayItem().getType(), (byte) 0, Math.round(_moneyMap.get(player)/type.getCost()), (short) type.getDisplayItem().getDurability(), C.cGreen + "Spawn " + type.getDisplayName() + " Minion Cost: " + type.getCost(), new String[]{}));
				}
				else
				{
					player.getInventory().setItem(i, ItemStackFactory.Instance.CreateStack(type.getDisplayItem().getType(), (byte) 0, 0, (short) type.getDisplayItem().getDurability(), C.cRed + "Spawn " + type.getDisplayName() + " Minion Cost: " + type.getCost(), new String[]{}));
				}
				i++;
			}
		}
	}
	
	private int _animationTicks;
	
	@EventHandler
	public void lines(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;
		
		for (List<Location> locs : _lineGrowth.values())
		{
			for (Location loc : locs)
			{
				double radius = _animationTicks / 20D;
				int particleAmount = _animationTicks / 2;
				for (int e = 0; e < particleAmount; e++)
				{
					double xDiff = Math.sin(e/(double)particleAmount * 2 * Math.PI) * radius;
					double zDiff = Math.cos(e/(double)particleAmount * 2 * Math.PI) * radius;

					Location location = loc.clone().add(0.5, 0, 0.5).clone().add(xDiff, particleAmount/10, zDiff);
					UtilParticle.PlayParticle(UtilParticle.ParticleType.RED_DUST, location, 0, 0, 0, 0, 1,
							ViewDist.NORMAL, UtilServer.getPlayers());
				}
			}
		}
		for (List<Location> locs : _lineShorten.values())
		{
			for (Location loc : locs)
			{
				double radius = _animationTicks / 20D;
				int particleAmount = _animationTicks / 2;
				for (int e = 0; e < particleAmount; e++)
				{
					double xDiff = Math.sin(e/(double)particleAmount * 2 * Math.PI) * radius;
					double zDiff = Math.cos(e/(double)particleAmount * 2 * Math.PI) * radius;

					Location location = loc.clone().add(0.5, 0, 0.5).add(xDiff, particleAmount/10, zDiff);
					UtilParticle.PlayParticle(UtilParticle.ParticleType.WITCH_MAGIC, location, 0, 0, 0, 0, 1,
							ViewDist.NORMAL, UtilServer.getPlayers());
				}
			}
		}
		
		_animationTicks++;
		if (_animationTicks > 15)
			_animationTicks = 0;
		
		Iterator<Minion> minionIterator = _activeMinions.iterator();
		
		while(minionIterator.hasNext())
		{
			Minion minion = minionIterator.next();
			for (GameTeam team : _lineGrowth.keySet())
			{
				for (Location loc : _lineGrowth.get(team))
				{
					if (minion.getEntity().getLocation().getBlockX() != loc.getBlockX())
						continue;
					
					if (minion.getEntity().getLocation().getBlockZ() != loc.getBlockZ())
						continue;
					
					if (!minion.isNameChangeable())
						continue;
					
					if (team != minion.getTeam())
						continue;
					
					int oldname = minion.getName().length() + 2;
					minion.changeRandomName(oldname, oldname, false);
				}
			}
			for (GameTeam team : _lineShorten.keySet())
			{
				for (Location loc : _lineShorten.get(team))
				{
					if (minion.getEntity().getLocation().getBlockX() != loc.getBlockX())
						continue;
					
					if (minion.getEntity().getLocation().getBlockZ() != loc.getBlockZ())
						continue;
					
					if (!minion.isNameChangeable())
						continue;
					
					if (team == minion.getTeam())
						continue;
					
					int oldname = minion.getName().length() - 2;
					minion.changeRandomName(oldname, oldname, false);
				}
			}
		}
	}
	
	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (GetTeamList().isEmpty())
			return;
		
		if (!IsLive())
			return;
		
		Scoreboard.reset();

		Scoreboard.writeNewLine();
		
		for (GameTeam team : GetTeamList())
		{
			Scoreboard.write(team.GetColor() + C.Bold + team.GetName() + " Team");
			Scoreboard.write(team.GetColor() + "Health: " + Math.round(getScore(team)));
			Scoreboard.write(team.GetColor() + "Minions: " + getMinions(team).size() + "/60");
			String wpm = String.valueOf((double) getTeamKills(team) / ((double) (System.currentTimeMillis() - GetStateTime())/60000));
			if (wpm.length() > 4)
				wpm = wpm.substring(0, 4);
			
			Scoreboard.write(team.GetColor() + "WPM: " + wpm);
			Scoreboard.writeNewLine();
		}
			
		Scoreboard.draw();
	}
	
	public ArrayList<Minion> getMinions(GameTeam team)
	{
		ArrayList<Minion> minionList = new ArrayList<>();
		Iterator<Minion> minionIterator = _activeMinions.iterator();
		while(minionIterator.hasNext())
		{
			Minion minion = minionIterator.next();
			if (minion.getTeam() == team)
				minionList.add(minion);
		}
		return minionList;
	}
	
	private int _nukeFrame;
	
	@EventHandler
	public void nuke(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (_pendingNukes.isEmpty())
			return;
		
		Player player = _pendingNukes.get(0);
		GameTeam team = GetTeam(player);
		
		GameTeam otherTeam = null;
		for (GameTeam teams : GetTeamList())
		{
			if (teams != team)
			{
				otherTeam = teams;
			}
		}
		ArrayList<Location> testLocs = UtilShapes.getLinesDistancedPoints(_minionSpawns.get(team).get(0), _minionSpawns.get(otherTeam).get(0), 1);

		if (_nukeFrame >= testLocs.size())
		{
			_nukeFrame = 0;
			_pendingNukes.remove(0);
			return;
		}
		if (_nukeFrame < 25)
		{
			_giants.get(team).getWorld().playSound(_giants.get(team).getLocation(), Sound.ZOMBIE_IDLE, 1, 1);
		}
		boolean cansee = true;
		int i = 0;
		for (Location loc : _minionSpawns.get(team))
		{
			cansee = !cansee;
			ArrayList<Location> locations = UtilShapes.getLinesDistancedPoints(loc, _minionSpawns.get(otherTeam).get(i), 1);
			Location location = locations.get(_nukeFrame);
			
			if (cansee)
			{
				UtilParticle.PlayParticle(UtilParticle.ParticleType.HUGE_EXPLOSION, location, 0, 0, 0, 0, 1, ViewDist.LONG, UtilServer.getPlayers());
				for (Player players : GetPlayers(false))
					players.playSound(location, Sound.EXPLODE, 1, 1);
			}
			
			Iterator<Minion> minionIterator = _activeMinions.iterator();
			while(minionIterator.hasNext())
			{
				Minion minion = minionIterator.next();
				if (minion.getTeam() == team)
					continue;
				
				if (UtilMath.offset(location, minion.getEntity().getLocation()) > 1)
					continue;
				
				minion.despawn(player, true);
				if (!minion.hasLives())
				{
					minionIterator.remove();
					_deadMinions.add(minion);	
				}
			}
			Iterator<Minion> finishedMinionIterator = _finishedMinions.iterator();
			while(finishedMinionIterator.hasNext())
			{
				Minion minion = finishedMinionIterator.next();
				if (minion.getTeam() == team)
					continue;
				
				if (UtilMath.offset(location, minion.getEntity().getLocation()) > 3)
					continue;
				
				minion.despawn(player, true);
				if (!minion.hasLives())
				{
					finishedMinionIterator.remove();
					_deadMinions.add(minion);	
				}
			}
			i++;
		}
		_nukeFrame++;
	}
	
	public int getPlayerKills(Player player)
	{
		int kills = 0;
		for (Minion minion : _deadMinions)
		{
			if (minion.getKiller() != null)
			{
				if (minion.getKiller().getName().contentEquals(player.getName()))
				{
					kills++;
				}
			}
		}
		return kills;
	}
	
	public int getTeamKills(GameTeam team)
	{
		int kills = 0;
		for (Player player : team.GetPlayers(true))
		{
			kills = kills + getPlayerKills(player);
		}
		return kills;
	}
	
	public int getSpawnedMinions(Player player, MinionSize size)
	{
		int spawns = 0;
		for (Minion minion : _deadMinions)
		{
			if (minion.getType().getSize() != size)
				continue;
		
			if (minion.getPlayer() == null)
				continue;
				
			if (minion.getPlayer().getName().contentEquals(player.getName()))
				spawns++;
		}
		return spawns;
	}
	
	public Map<Player, Integer> getMoneyMap()
	{
		return _moneyMap;
	}
	
	public List<Minion> getActiveMinions()
	{
		return _activeMinions;
	}
	
	public List<Minion> getDeadMinions()
	{
		return _deadMinions;
	}
	
	public Map<GameTeam, List<Location>> getMinionSpawns()
	{
		return _minionSpawns;
	}
	
	public Map<GameTeam, List<Location>> getLineGrowth()
	{
		return _lineGrowth;
	}
	
	public Map<GameTeam, List<Location>> getLineShorten()
	{
		return _lineShorten;
	}
	
	public void addNuke(Player player)
	{
		_pendingNukes.add(player);
	}
}