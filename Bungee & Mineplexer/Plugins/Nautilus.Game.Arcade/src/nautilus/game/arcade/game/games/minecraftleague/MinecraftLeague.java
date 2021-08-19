package nautilus.game.arcade.game.games.minecraftleague;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.Dispenser;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.base.Objects;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.message.PrivateMessageEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.DeathMessageType;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerDeathOutEvent;
import nautilus.game.arcade.events.PlayerGameRespawnEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.RankedTeamGame;
import nautilus.game.arcade.game.games.minecraftleague.data.BlockProtection;
import nautilus.game.arcade.game.games.minecraftleague.data.MapZone;
import nautilus.game.arcade.game.games.minecraftleague.data.Spawner;
import nautilus.game.arcade.game.games.minecraftleague.data.TeamBeacon;
import nautilus.game.arcade.game.games.minecraftleague.data.TeamCrystal;
import nautilus.game.arcade.game.games.minecraftleague.data.TeamTowerBase;
import nautilus.game.arcade.game.games.minecraftleague.data.TowerAlert;
import nautilus.game.arcade.game.games.minecraftleague.data.TowerManager;
import nautilus.game.arcade.game.games.minecraftleague.data.map.ItemMapManager;
import nautilus.game.arcade.game.games.minecraftleague.kit.KitPlayer;
import nautilus.game.arcade.game.games.minecraftleague.tracker.AltarBuilderTracker;
import nautilus.game.arcade.game.games.minecraftleague.tracker.FirstStrikeTracker;
import nautilus.game.arcade.game.games.minecraftleague.tracker.HeadHunterTracker;
import nautilus.game.arcade.game.games.minecraftleague.tracker.SavingUpTracker;
import nautilus.game.arcade.game.games.minecraftleague.tracker.TowerDefenderTracker;
import nautilus.game.arcade.game.games.minecraftleague.variation.ExtraScoreboardData;
import nautilus.game.arcade.game.games.minecraftleague.variation.GameVariation;
import nautilus.game.arcade.game.games.minecraftleague.variation.VariationManager;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;

public class MinecraftLeague extends RankedTeamGame
{
	//private ConcurrentHashMap<Player, PlayerRespawnPoint> _customRespawns = new ConcurrentHashMap<Player, PlayerRespawnPoint>();
	//private ConcurrentHashMap<GameTeam, TeamMap> _maps = new ConcurrentHashMap<GameTeam, TeamMap>();
	private ConcurrentHashMap<Player, List<ItemStack>> _gear = new ConcurrentHashMap<Player, List<ItemStack>>();
	public ConcurrentHashMap<GameTeam, TeamBeacon> Beacons = new ConcurrentHashMap<GameTeam, TeamBeacon>();
	//public ConcurrentHashMap<GameTeam, Long> TeamPoison = new ConcurrentHashMap<GameTeam, Long>();
	private ConcurrentHashMap<Player, BlockProtection> _blockLock = new ConcurrentHashMap<Player, BlockProtection>();
	//private ConcurrentHashMap<Player, Long> _spawnAllow = new ConcurrentHashMap<Player, Long>();
	private List<Spawner> _spawners = new ArrayList<Spawner>();
	private List<Player> _noFall = new ArrayList<Player>();
	
	private static final String[] PERM_OP = new String[] {"SamitoD", "Relyh", "AlexTheCoder"};
	
	public ConcurrentHashMap<ExtraScoreboardData, GameVariation> ExtraSb = new ConcurrentHashMap<ExtraScoreboardData, GameVariation>();
	
	private long _liveTime = 0;
	private long _lastIncrease;
	private long _lastOreReset;
	private long _lastGrindReset;
	private boolean _yellow = false;
	
	public List<MapZone> MapZones = new ArrayList<MapZone>();
	public ItemMapManager MapManager;
	public boolean OverTime = false;
	
	private VariationManager _vman;
	//public ObjectiveManager Objective;
	private TowerManager _tower;
	public TowerAlert Alert;
	public boolean ScoreboardAutoWrite = true;
	
	//private final EntityType[] _passive = new EntityType[] {EntityType.CHICKEN, EntityType.COW, EntityType.PIG, EntityType.RABBIT, EntityType.SHEEP};
	
	public MinecraftLeague(ArcadeManager manager)
	{
		super(manager, GameType.MinecraftLeague,

		new Kit[]
		{
				new KitPlayer(manager)
		},

		new String[]
		{
				C.cWhite + "Gather resources for battle.",
				C.cWhite + "Destroy all of the enemy's Towers to",
				C.cWhite + "Defeat them and win the game!",
				" ",
				C.cWhite + "Last team with Towers alive wins",
		});
		
		this.MaxPlayers = 10;
		this.MaxPerTeam = 5;
		this.DeathOut = true;
		this.DamageSelf = false;
		this.DeathSpectateSecs = 10;
		this.WorldBoundaryKill = true;
		this.DeathDropItems = true;
		this.CreatureAllow = false;
		this.HungerSet = 20;
		
		this.BlockBreak = true;
		this.BlockPlace = true;
		this.ItemPickup = true;
		this.ItemDrop = true;
		
		this.InventoryClick = true;
		this.InventoryOpenBlock = true;
		this.InventoryOpenChest = true;
		
		this.WorldWeatherEnabled = false;
		this.WorldBlockBurn = true;
		this.WorldBlockGrow = true;
		this.WorldBoneMeal = true;
		this.WorldFireSpread = true;
		this.WorldLeavesDecay = true;
		this.WorldSoilTrample = true;
		
		this.StrictAntiHack = true;
		this.AllowParticles = false;
		this.SoupEnabled = false;
		this.GameTimeout = -1;

		_help = new String[]
		{
			"Towers have 150 health per person in-game!",
			"The better the sword you have, the more damage you deal to Towers!",
			//"Right-click a bed in order to change your personal spawn location!",
			"Your map will display the locations of your enemies in OverTime!"
		};
		
		
		registerStatTrackers(
			new AltarBuilderTracker(this),
			new FirstStrikeTracker(this),
			new HeadHunterTracker(this),
			new TowerDefenderTracker(this, 2, 8000, "TowerDefender"),
			new SavingUpTracker(this)
		);
	
		_vman = new VariationManager(this);
		//Objective = new ObjectiveManager(this);
		_tower = new TowerManager(this);
		Alert = new TowerAlert();
		Bukkit.getPluginManager().registerEvents(_tower, manager.getPlugin());

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);
	}
	
	private enum DamageAmount
	{
		NONE(1),
		WOOD(4),
		STONE(5),
		GOLD(4),
		IRON(6),
		DIAMOND(7);
		
		private double _amount;
		
		private DamageAmount(int amount)
		{
			_amount = amount;
		}
		
		public double getDamage(int sharpnessLevel, DamageType type)
		{
			if (type.getDamageReduction() == null)
				return 1;
				
			return _amount + (sharpnessLevel / 2) - type.getDamageReduction();
		}
		
		public static DamageAmount getDamageAmount(Material material)
		{
			for (DamageAmount da : DamageAmount.values())
			{
				if (da == DamageAmount.NONE)
					continue;
				
				if (material.toString().contains(da.toString() + "_"))
					return da;
			}
			
			return DamageAmount.NONE;
		}
	}
	
	private enum DamageType
	{
		NONE(null),
		PICKAXE(2),
		AXE(1),
		SWORD(0),
		SPADE(3);
		
		private Integer _reduction;
		
		private DamageType(Integer reduction)
		{
			_reduction = reduction;
		}
		
		public Integer getDamageReduction()
		{
			return _reduction;
		}
		
		public static DamageType getDamageType(Material material)
		{
			for (DamageType dt : DamageType.values())
			{
				if (dt == DamageType.NONE)
					continue;
				
				if (material.toString().contains("_" + dt.toString()))
					return dt;
			}
			
			return DamageType.NONE;
		}
	}
	
	private ItemStack getNewItemStack(ItemStack item)
	{
		ItemStack ret = item.clone();
		/*String type = ret.getType().toString();
		boolean damage = false;
		boolean itemD = false;
		if (type.contains("HELMET") || type.contains("CHESTPLATE") || type.contains("LEGGINGS") || type.contains("BOOTS"))
			damage = true;
		if (DamageType.getDamageType(ret.getType()) != DamageType.NONE)
			itemD = true;
		
		if (damage)
		{
			ret.setDurability((short) (ret.getDurability() + (ret.getDurability() * .25)));
		}
		if (itemD)
		{
			ret.setDurability((short) (ret.getDurability() + 25));
		}
		
		if (ret.getDurability() > ret.getType().getMaxDurability())
			ret = new ItemStack(Material.AIR);*/
		
		if (ret.getType() == Material.MAP)
			ret = new ItemStack(Material.AIR);
		if (UtilItem.isTool(ret))
			ret = new ItemStack(ret.getType());
		
		return ret;
	}
	
	private int getSwordLevel(ItemStack sword)
	{
		if (UtilItem.isSword(sword))
		{
			//if (UtilItem.isDiamondProduct(sword))
				//return 5;
			if (UtilItem.isIronProduct(sword))
				return 4;
			if (UtilItem.isGoldProduct(sword))
				return 3;
			if (UtilItem.isStoneProduct(sword))
				return 2;
			if (UtilItem.isWoodProduct(sword))
				return 1;
		}
		
		return 0;
	}
	
	private int getPickLevel(ItemStack sword)
	{
		if (UtilItem.isPickaxe(sword))
		{
			//if (UtilItem.isDiamondProduct(sword))
				//return 5;
			if (UtilItem.isIronProduct(sword))
				return 4;
			if (UtilItem.isGoldProduct(sword))
				return 3;
			if (UtilItem.isStoneProduct(sword))
				return 2;
			if (UtilItem.isWoodProduct(sword))
				return 1;
		}
		
		return 0;
	}
	
	private ItemStack getBestSword(Player player)
	{
		ItemStack ret = new ItemStack(Material.AIR);
		int level = 0;
		
		for (ItemStack item : UtilInv.getItems(player, false, true, false))
		{
			if (UtilItem.isSword(item))
			{
				if (getSwordLevel(item) > level)
				{
					ret = item;
					level = getSwordLevel(item);
				}
			}
		}
		
		return ret;
	}
	
	private ItemStack getBestPick(Player player)
	{
		ItemStack ret = new ItemStack(Material.AIR);
		int level = 0;
		
		for (ItemStack item : UtilInv.getItems(player, false, true, false))
		{
			if (UtilItem.isPickaxe(item))
			{
				if (getPickLevel(item) > level)
				{
					ret = item;
					level = getPickLevel(item);
				}
			}
		}
		
		return ret;
	}
	
	public List<String> getMapVariantIDS()
	{
		List<String> ids = new ArrayList<String>();
		for (String s : WorldData.GetAllCustomLocs().keySet())
		{
			if (s.contains(DataLoc.VARIANT_BASE.getKey()))
				ids.add(s);
		}
		return ids;
	}
	
	public TeamTowerBase getActiveTower(GameTeam team)
	{
		return _tower.getVulnerable(team);
	}
	
	public TowerManager getTowerManager()
	{
		return _tower;
	}
	
	/*public TeamMap getMap(GameTeam team)
	{
		return _maps.get(team);
	}*/
	
	@Override
	public void ParseData()
	{		
		_tower.parseTowers(WorldData);
		
		//_beacons.put(GetTeam(ChatColor.RED), new TeamBeacon(GetTeam(ChatColor.RED), WorldData.GetDataLocs(DataLoc.RED_BEACON.getKey()).get(0).getBlock(), redLoc));
		//_beacons.put(GetTeam(ChatColor.AQUA), new TeamBeacon(GetTeam(ChatColor.AQUA), WorldData.GetDataLocs(DataLoc.BLUE_BEACON.getKey()).get(0).getBlock(), blueLoc));
		
		for (Location loc : WorldData.GetDataLocs(DataLoc.SKELETON_SPAWNER.getKey()))
		{
			_spawners.add(new Spawner(this, loc, EntityType.SKELETON));
		}
		
		/*for (Location loc : WorldData.GetDataLocs(DataLoc.MAP_DIAMOND.getKey()))
		{
			Ore.add(new OreDeposit(loc, Material.DIAMOND_ORE, new int[] {0, 255, 255}));
		}
		for (Location loc : WorldData.GetDataLocs(DataLoc.MAP_IRON.getKey()))
		{
			Ore.add(new OreDeposit(loc, Material.IRON_ORE, new int[] {190, 190, 190}));
		}*/
		
		for (Location diamond : WorldData.GetCustomLocs(DataLoc.DIAMOND_ORE.getKey()))
		{
			diamond.getBlock().setType(Material.DIAMOND_ORE);
		}
		for (Location coal : WorldData.GetCustomLocs(DataLoc.COAL_ORE.getKey()))
		{
			coal.getBlock().setType(Material.COAL_ORE);
		}
		
		MapManager = new ItemMapManager(this, WorldData.World, WorldData.MinX, WorldData.MinZ, WorldData.MaxX, WorldData.MaxZ);
		
		_vman.selectVariation();
	}
	
	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (!ScoreboardAutoWrite)
			return;
		
		if (event.getType() == UpdateType.FAST || event.getType() == UpdateType.SEC)
			scoreboardWrite(event.getType() == UpdateType.SEC);
	}
	
	public void writeEndSb(String winner)
	{
		Scoreboard.reset();
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cDRedB + GetName());
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cGoldB + "Winner:");
		Scoreboard.write(winner);
		
		Scoreboard.draw();
	}
	
	private void scoreboardWrite(boolean sec)
	{
		if (!InProgress())
			return;
		
		if (!IsLive())
		{
			if (!sec)
				return;
			Scoreboard.reset();
			Scoreboard.writeNewLine();
			Scoreboard.write(C.cDRedB + GetName());
			Scoreboard.writeNewLine();
			
			if (_yellow)
			{
				Scoreboard.write(C.cYellow + "Loading...");
				_yellow = false;
			}
			else
			{
				Scoreboard.write("Loading...");
				_yellow = true;
			}
			
			Scoreboard.draw();
			return;
		}
		
		if (sec)
			return;
		
		GameTeam red = GetTeam(ChatColor.RED);
		GameTeam blue = GetTeam(ChatColor.AQUA);
		String reds = "";
		String blues = "";
		for (TeamTowerBase tb : _tower.getTeamTowers(red))
		{
			if (!reds.equalsIgnoreCase(""))
				reds = reds + " ";
			
			String symbol = "♛";
			if (tb instanceof TeamCrystal)
				symbol = "♚";
			
			reds = reds + tb.getHealthColor() + symbol;
		}
		for (TeamTowerBase tb : _tower.getTeamTowers(blue))
		{
			if (!blues.equalsIgnoreCase(""))
				blues = blues + " ";
			
			String symbol = "♛";
			if (tb instanceof TeamCrystal)
				symbol = "♚";
			
			blues = blues + tb.getHealthColor() + symbol;
		}
		
		_liveTime = Math.max(System.currentTimeMillis() - GetStateTime(), 0);
		
		Scoreboard.reset();
		
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cRedB + "Red Team");
		Scoreboard.write("Towers: " + reds);
		for (String s : _vman.getSelected().getTeamScoreboardAdditions(red))
		{
			Scoreboard.write(s);
		}
		
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cAquaB + "Blue Team");
		Scoreboard.write("Towers: " + blues);
		for (String s : _vman.getSelected().getTeamScoreboardAdditions(blue))
		{
			Scoreboard.write(s);
		}
		
		int i = 1;
		for (ExtraScoreboardData sbD : ExtraSb.keySet())
		{
			Scoreboard.writeNewLine();
			sbD.write();
			if (i < ExtraSb.size())
				Scoreboard.writeNewLine();
			i++;
		}
		
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cYellowB + "Time Elapsed");
		Scoreboard.write(UtilTime.MakeStr(_liveTime));
		if (OverTime)
			Scoreboard.write(C.cDRedB + "Overtime");
		
		Scoreboard.draw();
	}
	
	/*@Override
	public void RespawnPlayerTeleport(Player player)
	{
		if (_customRespawns.containsKey(player))
		{
			PlayerRespawnPoint point = _customRespawns.get(player);
			if (point.respawnPlayer())
				return;
			
			_customRespawns.remove(player);
		}
		
		player.teleport(GetTeam(player).GetSpawn());
	}*/
	
	public boolean handleCommand(Player caller)
	{
		for (GameTeam team : GetTeamList())
		{
			TeamTowerBase tower = _tower.getTeamTowers(team).getLast();
			UtilPlayer.message(caller, team.GetColor() + team.GetName());
			UtilPlayer.message(caller, C.cGray + "Health: " + tower.getHealth());
			UtilPlayer.message(caller, C.cGray + "Alive: " + tower.Alive);
		}

		return true;
	}
	
	public void restoreGear(Player player)
	{
		if (!_gear.containsKey(player))
			return;
		List<ItemStack> items = _gear.get(player);
		for (int i = 0; i < 4; i++)
		{
			ItemStack item;
			if (items.get(i) != null)
				item = items.get(i);
			else
				item = new ItemStack(Material.AIR);
			
			switch(i + 1)
			{
			case 1:
				player.getInventory().setHelmet(getNewItemStack(item));
				break;
			case 2:
				player.getInventory().setChestplate(getNewItemStack(item));
				break;
			case 3:
				player.getInventory().setLeggings(getNewItemStack(item));
				break;
			case 4:
				player.getInventory().setBoots(getNewItemStack(item));
				break;
			}
		}
		for (int i = 4; i < items.size(); i++)
		{
			if (items.get(i) == null)
				UtilInv.insert(player, new ItemStack(Material.AIR));
			else
				UtilInv.insert(player, getNewItemStack(items.get(i)));
		}
		_gear.remove(player);
	}
	
	@EventHandler
	public void onEditSettings(GameStateChangeEvent event)
	{
		if (event.GetGame() != this)
			return;
		
		if (event.GetState() == GameState.Live)
		{
			_lastIncrease = System.currentTimeMillis();
			_lastOreReset = System.currentTimeMillis();
			_lastGrindReset = System.currentTimeMillis() - UtilTime.convert(30, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
			Manager.GetExplosion().setEnabled(false);
			Manager.GetExplosion().SetTemporaryDebris(false);
			Manager.GetExplosion().SetDebris(true);
			Manager.GetDamage().SetEnabled(false);
			Manager.GetCreature().SetForce(false);
			Manager.GetCreature().SetDisableCustomDrops(true);
			Manager.getGameChatManager().TeamSpy = false;
			//Objective.setMainObjective(new GearObjective());
		}
		
		if (event.GetState() == GameState.End)
		{
			Manager.GetExplosion().setEnabled(true);
			Manager.GetExplosion().SetDebris(false);
			Manager.GetExplosion().SetTemporaryDebris(true);
			Manager.GetDamage().SetEnabled(true);
			Manager.GetCreature().SetForce(false);
			Manager.GetCreature().SetDisableCustomDrops(false);
			Manager.getGameChatManager().TeamSpy = true;
			HandlerList.unregisterAll(MapManager);
		}
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getType() == UpdateType.SEC)
		{
			/*for (MapZone od : Ore)
			{
				od.update();
			}*/
			
			for (Player player : GetTeam(ChatColor.RED).GetPlayers(true))
			{
				for (ItemStack armor : player.getInventory().getArmorContents())
				{
					if (UtilItem.isLeatherProduct(armor))
					{
						LeatherArmorMeta im = (LeatherArmorMeta)armor.getItemMeta();
						im.setColor(Color.RED);
						armor.setItemMeta(im);
					}
				}
			}
			for (Player player : GetTeam(ChatColor.AQUA).GetPlayers(true))
			{
				for (ItemStack armor : player.getInventory().getArmorContents())
				{
					if (UtilItem.isLeatherProduct(armor))
					{
						LeatherArmorMeta im = (LeatherArmorMeta)armor.getItemMeta();
						im.setColor(Color.BLUE);
						armor.setItemMeta(im);
					}
				}
			}
		}
		
		/*if (event.getType() == UpdateType.FASTER)
		{
			for (GameTeam team : _teamList)
			{
				for (Location loc : team.GetSpawns())
				{
					for (LivingEntity near : UtilEnt.getInRadius(loc, 2).keySet())
					{
						if (!(near instanceof Player))
							continue;
						Player player = (Player)near;
						if (team.HasPlayer(player))
						{
							if (_spawnAllow.containsKey(player))
								continue;
						}
						if (UtilPlayer.isSpectator(player))
							continue;
						
						Vector vec = UtilAlg.getTrajectory(loc, player.getLocation());

						Location tpLoc = loc.add(vec.clone().multiply(8));
						tpLoc.setDirection(player.getLocation().getDirection());

						//First tp out this combats hacked clients with anti-KB
						player.teleport(tpLoc);

						//Then apply velocity as normal
						UtilAction.velocity(player, vec, 1.8, false, 0, 0.4, vec.length(), false);

						player.playSound(player.getEyeLocation(), Sound.NOTE_PLING, 10F, 0.5F);
					}
				}
			}
		}*/
		
		if (event.getType() == UpdateType.FASTEST)
		{
			_tower.update();
			
			/*for (TeamMap map : _maps.values())
			{
				map.update(null);
			}*/
			
			for (Spawner spawner : _spawners)
			{
				spawner.update();
			}
			
			for (TeamBeacon beacon : Beacons.values())
			{
				beacon.update();
			}
			
			//Alert.update();
			
			/*for (GameTeam dmg : TeamPoison.keySet())
			{
				if (UtilTime.elapsed(TeamPoison.get(dmg), UtilTime.convert(1, TimeUnit.SECONDS, TimeUnit.MILLISECONDS)))
				{
					TeamPoison.put(dmg, System.currentTimeMillis());
					for (Player player : dmg.GetPlayers(true))
					{
						this.storeGear(player);
						player.damage(1);
					}
				}
			}*/
			
			if (UtilTime.elapsed(_lastOreReset, UtilTime.convert(5, TimeUnit.MINUTES, TimeUnit.MILLISECONDS)))
			{
				_lastOreReset = System.currentTimeMillis();
				for (Location loc : WorldData.GetCustomLocs(DataLoc.COAL_ORE.getKey()))
				{
					loc.getBlock().setType(Material.COAL_ORE);
				}
			}
			
			if (!OverTime)
			{
				if (UtilTime.elapsed(GetStateTime(), UtilTime.convert(12, TimeUnit.MINUTES, TimeUnit.MILLISECONDS)))
				{
					OverTime = true;
					UtilTextMiddle.display(C.cGold + "Overtime", C.cGold + "Dying will now cause your crystal to lose 50 health!");
				}
			}
			
			if (UtilTime.elapsed(_lastIncrease, UtilTime.convert(5, TimeUnit.MINUTES, TimeUnit.MILLISECONDS)))
			{
				_lastIncrease = System.currentTimeMillis();
				this.DeathSpectateSecs = Math.min(20, this.DeathSpectateSecs + 2.5);
			}
			
			if (UtilTime.elapsed(_lastGrindReset, UtilTime.convert(30, TimeUnit.SECONDS, TimeUnit.MILLISECONDS)))
			{
				_lastGrindReset = System.currentTimeMillis();
				for (Location grind : WorldData.GetCustomLocs(DataLoc.GRIND_AREA.getKey()))
				{
					int spider = 0;
					int chicken = 0;
					for (LivingEntity le : UtilEnt.getInRadius(grind, 15).keySet())
					{
						if (le.getType() == EntityType.SPIDER)
							spider++;
						if (le.getType() == EntityType.CHICKEN)
							chicken++;
					}
					while (spider < 5)
					{
						spider = 5;
						CreatureAllowOverride = true;
						Manager.GetCreature().SpawnEntity(grind, EntityType.SPIDER);
						CreatureAllowOverride = false;
					}
					while (chicken < 10)
					{
						chicken = 10;
						CreatureAllowOverride = true;
						Manager.GetCreature().SpawnEntity(grind, EntityType.CHICKEN);
						CreatureAllowOverride = false;
					}
				}
			}
			
			/*for (Player player : _spawnAllow.keySet())
			{
				if (UtilTime.elapsed(_spawnAllow.get(player), UtilTime.convert(30, TimeUnit.SECONDS, TimeUnit.MILLISECONDS)))
				{
					_spawnAllow.remove(player);
				}
			}*/
			
			for (Player player : _blockLock.keySet())
			{
				if (!player.isOnline())
					_blockLock.remove(player);
			}
			
			for (Player player : Bukkit.getOnlinePlayers())
			{
				if (UtilPlayer.isSpectator(player))
				{
					player.setFireTicks(-1);
					player.setFoodLevel(20);
				}
				if (UtilInv.contains(player, Material.LAVA_BUCKET, (byte) 0, 1))
				{
					UtilInv.insert(player, new ItemStack(Material.BUCKET, UtilInv.removeAll(player, Material.LAVA_BUCKET, (byte) 0)));
				}
				if (player.getOpenInventory().getType() == InventoryType.BEACON)
					player.closeInventory();
				if (player.getFireTicks() > 20 * 4)
					player.setFireTicks(20 * 4);
			}
		}
	}
	
	@EventHandler
	public void towerDmg(EntityDamageEvent event)
	{
		if (!InProgress())
			return;
		
		if (event.getEntity().getType() != EntityType.ENDER_CRYSTAL)
			return;
		
		for (GameTeam team : GetTeamList())
			for (TeamTowerBase tower : _tower.getTeamTowers(team))
			{
				if (tower.isEntity(event.getEntity()))
					event.setCancelled(true);
			}
	}
	
	@EventHandler
	public void towerDmg(EntityDamageByEntityEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getEntity().getType() != EntityType.ENDER_CRYSTAL)
			return;
		
		TeamTowerBase tower = null;
		for (GameTeam team : GetTeamList())
		for (TeamTowerBase tow : _tower.getTeamTowers(team))
		{
			if (tow == null)
				continue;
			if (tow.isEntity(event.getEntity()))
				tower = tow;
		}
		
		if (tower == null)
			return;
		
		event.setCancelled(true);
		
		Player player;
		
		if (event.getDamager() instanceof Projectile)
		{
			if (((Projectile)event.getDamager()).getShooter() instanceof Player)
			{
				if (event.getDamager() instanceof Arrow)
				{
					player = (Player) ((Projectile)event.getDamager()).getShooter();
					
					if (!tower.canDamage(player))
						return;
					
					if (!tower.Vulnerable)
					{
						UtilPlayer.message(player, F.main("Game", "That Tower is protected by the power of another!"));
						return;
					}
					
					if (!tower.damage(event.getDamage() / 2, player))
					{
						player.playSound(tower.getLocation(), Sound.ORB_PICKUP, 100, 0);
						Alert.alert(tower.getTeam(), tower);
					}
				}
			}
		}
		
		if (event.getDamager() instanceof Player)
		{
			player = (Player)event.getDamager();
			if (!tower.canDamage(player))
				return;
			
			if (!tower.Vulnerable)
			{
				UtilPlayer.message(player, F.main("Game", "That Tower is protected by the power of another!"));
				return;
			}
			
			if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR)
			{
				if (!tower.damage(1, player))
				{
					player.getWorld().playSound(tower.getLocation(), Sound.ZOMBIE_METAL, 1, 1.5f);
					Alert.alert(tower.getTeam(), tower);
				}
				return;
			}
			
			Material type = player.getItemInHand().getType();
			int level = player.getItemInHand().getEnchantmentLevel(Enchantment.DAMAGE_ALL);
			double damage = DamageAmount.getDamageAmount(type).getDamage(level, DamageType.getDamageType(type));
			
			if (!tower.damage(damage, player))
			{
				player.getWorld().playSound(tower.getLocation(), Sound.ZOMBIE_METAL, 1, 1.5f);
				Alert.alert(tower.getTeam(), tower);
			}
			
			if (DamageAmount.getDamageAmount(type) != DamageAmount.NONE)
			{
				player.getItemInHand().setDurability((short) (player.getItemInHand().getDurability() + 1));
				if (player.getItemInHand().getDurability() > player.getItemInHand().getType().getMaxDurability())
				{
					player.getInventory().remove(player.getItemInHand());
					player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0);
				}
				UtilInv.Update(player);
			}
		}
	}
	
	/*@EventHandler
	public void placeBed(PlayerInteractEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getClickedBlock() == null)
			return;
		
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		if (event.getClickedBlock().getType() != Material.BED_BLOCK)
			return;
		
		event.setCancelled(true);
		
		Player player = event.getPlayer();
		if (_customRespawns.containsKey(player))
			_customRespawns.get(player).overWrite(event.getClickedBlock().getLocation());
		else
			_customRespawns.put(player, new PlayerRespawnPoint(player, event.getClickedBlock().getLocation()));
	}*/
	
	private boolean isLocked(Block block)
	{
		for (BlockProtection prot : _blockLock.values())
		{
			if (prot.hasBlock(block))
				return true;
		}
		return false;
	}
	
	private Player getOwner(Block block)
	{
		for (Player player : _blockLock.keySet())
		{
			if (_blockLock.get(player).hasBlock(block))
				return player;
		}
		
		return null;
	}
	
	private boolean isLockedTo(Block block, Player to, boolean ignoreTeam)
	{
		for (BlockProtection prot : _blockLock.values())
		{
			if (prot.hasBlock(block))
				return prot.isLockedTo(to, block, ignoreTeam);
		}
		return false;
	}
	
	private boolean isOwner(Block block, Player owner)
	{
		for (BlockProtection prot : _blockLock.values())
		{
			if (prot.hasBlock(block))
				return prot.getOwner().getName().equalsIgnoreCase(owner.getName());
		}
		return false;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void lockBlock(BlockPlaceEvent event)
	{
		if (!IsLive())
			return;
		
		Block block = event.getBlock();
		if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST || block.getType() == Material.FURNACE || block.getType() == Material.BURNING_FURNACE)
			_blockLock.get(event.getPlayer()).lockBlock(block);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void unlockBlock(BlockBreakEvent event)
	{
		if (!IsLive())
			return;
		
		if (!isLocked(event.getBlock()))
			return;
		
		if (!isLockedTo(event.getBlock(), event.getPlayer(), false))
			_blockLock.get(getOwner(event.getBlock())).unlockBlock(event.getBlock());
		else
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game", "That container is locked by " + getOwner(event.getBlock()).getName() + "!"));
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void tryOpen(PlayerInteractEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getClickedBlock() == null)
			return;
		
		if (UtilPlayer.isSpectator(event.getPlayer()))
		{
			event.setCancelled(true);
			return;
		}
		
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		Block block = event.getClickedBlock();
		
		if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST || block.getType() == Material.FURNACE || block.getType() == Material.BURNING_FURNACE)
		{
			if (!isLocked(block))
			{
				if (event.getPlayer().isSneaking())
				{
					_blockLock.get(event.getPlayer()).lockBlock(block);
					return;
				}
			}
			
			if (isLockedTo(event.getClickedBlock(), event.getPlayer(), false))
			{
				event.setCancelled(true);
				UtilPlayer.message(event.getPlayer(), F.main("Game", "That container is locked by " + getOwner(event.getClickedBlock()).getName() + "!"));
				return;
			}
			if (isOwner(event.getClickedBlock(), event.getPlayer()))
			{
				if (event.getPlayer().isSneaking())
				{
					_blockLock.get(event.getPlayer()).unlockBlock(event.getClickedBlock());
					return;
				}
			}
		}
	}
	
	/*@EventHandler
	public void breakBed(BlockBreakEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getBlock().getType() != Material.BED_BLOCK)
			return;
		
		for (Player player : _customRespawns.keySet())
		{
			PlayerRespawnPoint point = _customRespawns.get(player);
			
			if (point.breakBed(event.getBlock()))
				_customRespawns.remove(player);
		}
	}*/
	
	@EventHandler
	public void onRespawn(PlayerGameRespawnEvent event)
	{
		if (event.GetGame() != this)
			return;
		
		Player player = event.GetPlayer();
		
		player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 3));
		_noFall.add(player);
		//_spawnAllow.put(player, System.currentTimeMillis());
		
		/*if (!_crystals.get(GetTeam(player)).Alive)
		{
			SetPlayerState(player, PlayerState.OUT);

			Manager.GetCondition().Factory().Blind("PermDead", player, player, 1.5, 0, false, false, false);
			Manager.GetCondition().Factory().Cloak("PermDead", player, player, 9999, false, false);

			player.setAllowFlight(true);
			player.setFlying(true);
			((CraftPlayer)player).getHandle().spectating = true;
			((CraftPlayer)player).getHandle().k = false;

			UtilAction.velocity(player, new Vector(0,1.2,0));
			
			getMap(GetTeam(player)).update(player);
			player.getInventory().clear();
			player.getInventory().setArmorContents(new ItemStack[]
			{
				new ItemStack(Material.AIR),
				new ItemStack(Material.AIR),
				new ItemStack(Material.AIR),
				new ItemStack(Material.AIR)
			});
			return;
		}
		
		if (_overTime)
			_crystals.get(GetTeam(player)).damage(20, null);*/
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent event)
	{
		if (!IsLive())
			return;
		
		ItemStack item = event.getItemDrop().getItemStack();
		
		if (item.getType() != Material.MAP)
			return;
		
		event.setCancelled(true);
		UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot drop your team map!"));
	}
	
	@EventHandler
	public void preventMapMoveInventories(InventoryClickEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getWhoClicked() instanceof Player)
		{
			Player p = (Player)event.getWhoClicked();
			if (p.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING)
				return;
			
			if (p.getOpenInventory().getTopInventory().getType() == InventoryType.CREATIVE)
				return;
		}
		
		if (event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.HOTBAR_SWAP)
		{
			ItemStack i = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
			if (i == null || i.getType() != Material.MAP)
				return;
			
			event.setCancelled(true);
			UtilPlayer.message(event.getWhoClicked(), F.main("Game", "You cannot store team maps inside containers!"));
			return;
		}
		
		Inventory inv = event.getClickedInventory();

		if (inv == null)
			return;

		for (ItemStack item : new ItemStack[]
			{
					event.getCurrentItem(), event.getCursor()
			})
		{
			if (item == null || item.getType() != Material.MAP)
				continue;

			if (inv.getHolder() instanceof Player ? !event.isShiftClick() : Objects.equal(event.getCurrentItem(), item))
				continue;

			event.setCancelled(true);

			UtilPlayer.message(event.getWhoClicked(), F.main("Game", "You cannot store team maps inside containers!"));
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onDie(PlayerDeathEvent event)
	{
		if (!IsLive())
			return;
		
		event.getEntity().setHealth(event.getEntity().getMaxHealth());
		List<ItemStack> newDrops = new ArrayList<ItemStack>();
		Integer arrows = 0;
		
		for (ItemStack item : event.getDrops())
		{
			if (item.getType() == Material.MAP)
				continue;
			
			if (_gear.get(event.getEntity()) != null)
			{
				if (item.getType() == Material.ARROW)
				{
					arrows += item.getAmount();
					continue;
				}
				if (_gear.get(event.getEntity()).contains(item))
					continue;
			}
			
			newDrops.add(item);
		}
		arrows = arrows / 2;
		
		while (arrows >= 1)
		{
			int subtract = Math.min(64, arrows);
			newDrops.add(new ItemStack(Material.ARROW, subtract));
			arrows -= subtract;
		}
		
		event.getDrops().clear();
		for (ItemStack item : newDrops)
			event.getDrops().add(item);
	}
	
	@EventHandler
	public void preventMapInItemFrame(PlayerInteractEntityEvent event)
	{
		if (!IsLive())
			return;
		
		if (!(event.getRightClicked() instanceof ItemFrame))
			return;

		ItemStack item = event.getPlayer().getItemInHand();

		if (item == null || item.getType() != Material.MAP)
			return;

		event.setCancelled(true);
	}
	
	@EventHandler
	public void craftItem(PrepareItemCraftEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getInventory().getResult().getType() == Material.EMPTY_MAP || event.getInventory().getResult().getType() == Material.MAP)
			event.getInventory().setResult(new ItemStack(Material.AIR));
		
		if (UtilItem.isArmor(event.getInventory().getResult()))
		{
			event.getInventory().setResult(UtilItem.makeUnbreakable(event.getInventory().getResult()));
		}
		if (event.getInventory().getResult().getType() == Material.ARROW)
		{
			event.getInventory().setResult(new ItemStack(Material.ARROW, ((event.getInventory().getResult().getAmount() / 4) * 6)));
		}
		if (event.getInventory().getResult().getType() == Material.FLINT_AND_STEEL)
		{
			event.getInventory().setResult(new ItemBuilder(Material.FLINT_AND_STEEL).setData((short) (Material.FLINT_AND_STEEL.getMaxDurability() - 4)).build());
		}
	}
	
	@EventHandler
	public void onSpawnerActivate(CreatureSpawnEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getSpawnReason() != SpawnReason.SPAWNER)
			return;
		
		event.setCancelled(true);
	}
	
	/*@EventHandler
	public void controlMobRate(CreatureSpawnEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getSpawnReason() != SpawnReason.NATURAL)
			return;
		
		if (UtilEnt.getInRadius(event.getLocation(), 10).size() >= 5)
		{
			event.setCancelled(true);
			return;
		}
		
		EntityType et = event.getEntityType();
		
		for (EntityType pass : _passive)
		{
			if (pass == event.getEntityType())
				et = EntityType.CHICKEN;
		}
		if (et == EntityType.ZOMBIE || et == EntityType.SKELETON || et == EntityType.CREEPER)
			et = EntityType.SPIDER;
		
		event.setCancelled(true);
		
		Manager.GetCreature().SpawnEntity(event.getLocation(), et);
		if (et == EntityType.SPIDER || et == EntityType.CHICKEN)
		{
			for (int i = 1; i <= 3; i++)
				Manager.GetCreature().SpawnEntity(event.getLocation(), EntityType.SPIDER);
		}
	}*/
	
	@EventHandler
	public void editHealth(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
			return;
		
		if (event.GetGame() != this)
			return;

		int playercount = GetTeam(ChatColor.RED).GetPlayers(false).size() + GetTeam(ChatColor.AQUA).GetPlayers(false).size();
		_tower.prepareHealth(playercount, 150);
	}
	
	@EventHandler
	public void dropBowStuff(EntityDeathEvent event)
	{
		/*if (event.getEntityType() == EntityType.SKELETON)
		{
			boolean addbow = true;
			for (ItemStack check : event.getDrops())
			{
				if (check.getType() == Material.BOW)
					addbow = false;
			}

			if (addbow)
			{
				if (new Random().nextDouble() > .75)
					event.getDrops().add(new ItemStack(Material.BOW));
			}

			event.getDrops().add(new ItemStack(Material.ARROW, 15));
		}*/
		if (event.getEntityType() == EntityType.CHICKEN)
		{
			for (ItemStack test : event.getDrops())
				if (test.getType() == Material.FEATHER)
					return;
			
			event.getDrops().add(new ItemStack(Material.FEATHER, UtilMath.random.nextInt(4)));
		}
	}
	
	@EventHandler
	public void giveGear(GameStateChangeEvent event)
	{
		if (event.GetGame() != this)
			return;
		if (!(event.GetState() == GameState.Live || event.GetState() == GameState.Prepare))
			return;
		
		if (event.GetState() == GameState.Prepare)
		{
			/*for (Player player : Bukkit.getOnlinePlayers())
				_spawnAllow.put(player, System.currentTimeMillis());*/
			return;
		}
		for (Player player : GetTeam(ChatColor.AQUA).GetPlayers(true))
		{
			player.getInventory().setHelmet(new ItemBuilder(Material.LEATHER_HELMET).setColor(Color.BLUE).setUnbreakable(true).build());
			player.getInventory().setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(Color.BLUE).setUnbreakable(true).build());
			player.getInventory().setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).setColor(Color.BLUE).setUnbreakable(true).build());
			player.getInventory().setBoots(new ItemBuilder(Material.LEATHER_BOOTS).setColor(Color.BLUE).setUnbreakable(true).build());
			player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
			player.getInventory().addItem(new ItemStack(Material.STONE_PICKAXE));
			//player.getInventory().addSkillItem(new ItemStack(Material.COOKED_BEEF, 5));
			_blockLock.put(player, new BlockProtection(this, player));
			_noFall.add(player);
		}
		for (Player player : GetTeam(ChatColor.RED).GetPlayers(true))
		{
			player.getInventory().setHelmet(new ItemBuilder(Material.LEATHER_HELMET).setColor(Color.RED).setUnbreakable(true).build());
			player.getInventory().setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(Color.RED).setUnbreakable(true).build());
			player.getInventory().setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).setColor(Color.RED).setUnbreakable(true).build());
			player.getInventory().setBoots(new ItemBuilder(Material.LEATHER_BOOTS).setColor(Color.RED).setUnbreakable(true).build());
			player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
			player.getInventory().addItem(new ItemStack(Material.STONE_PICKAXE));
			//player.getInventory().addSkillItem(new ItemStack(Material.COOKED_BEEF, 5));
			_blockLock.put(player, new BlockProtection(this, player));
			_noFall.add(player);
		}
	}
	
	public void storeGear(Player player)
	{
		List<ItemStack> gear = new ArrayList<ItemStack>();
		
		if (!UtilItem.isDiamondProduct(player.getInventory().getHelmet()))
			gear.add(player.getInventory().getHelmet());
		else
			gear.add(new ItemStack(Material.AIR));
		
		if (!UtilItem.isDiamondProduct(player.getInventory().getChestplate()))
			gear.add(player.getInventory().getChestplate());
		else
			gear.add(new ItemStack(Material.AIR));
		
		if (!UtilItem.isDiamondProduct(player.getInventory().getLeggings()))
			gear.add(player.getInventory().getLeggings());
		else
			gear.add(new ItemStack(Material.AIR));
		
		if (!UtilItem.isDiamondProduct(player.getInventory().getBoots()))
			gear.add(player.getInventory().getBoots());
		else
			gear.add(new ItemStack(Material.AIR));
		
		if (!UtilItem.isDiamondProduct(getBestSword(player)))
			gear.add(getBestSword(player));
		else
			gear.add(new ItemStack(Material.AIR));
		
		if (!UtilItem.isDiamondProduct(getBestPick(player)))
			gear.add(getBestPick(player));
		else
			gear.add(new ItemStack(Material.AIR));
		
		if (UtilInv.getAmount(player, Material.BOW) >= 1)
		{
			for (ItemStack poss : UtilInv.getItems(player))
			{
				if (poss.getType() == Material.BOW)
				{
					gear.add(poss);
					break;
				}
			}
		}
		
		if (UtilInv.getAmount(player, Material.FISHING_ROD) >= 1)
		{
			for (ItemStack poss : UtilInv.getItems(player))
			{
				if (poss.getType() == Material.FISHING_ROD)
				{
					gear.add(poss);
					break;
				}
			}
		}
		
		int arrowsToAdd = UtilInv.getAmount(player, Material.ARROW) / 2;
		while (arrowsToAdd >= 1)
		{
			int subtract = Math.min(64, arrowsToAdd);
			gear.add(new ItemStack(Material.ARROW, subtract));
			arrowsToAdd -= subtract;
		}
		
		int oresToAdd = UtilInv.getAmount(player, Material.IRON_ORE) / 2;
		while (oresToAdd >= 1)
		{
			int subtract = Math.min(64, oresToAdd);
			gear.add(new ItemStack(Material.IRON_ORE, subtract));
			oresToAdd -= subtract;
		}
		
		int ingotsToAdd = UtilInv.getAmount(player, Material.IRON_INGOT) / 2;
		while (ingotsToAdd >= 1)
		{
			int subtract = Math.min(64, ingotsToAdd);
			gear.add(new ItemStack(Material.IRON_INGOT, subtract));
			ingotsToAdd -= subtract;
		}
		/*if (UtilInv.getAmount(player, Material.ARROW) >= 1)
		{
			for (ItemStack arrow : UtilInv.getItems(player))
			{
				if (arrow.getType() == Material.ARROW)
				{
					gear.add(arrow);
				}
			}
		}*/
		
		_gear.put(player, gear);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void storeGear(EntityDamageEvent event)
	{
		if (!IsLive())
			return;
		if (!(event.getEntity() instanceof Player))
			return;
		if (UtilPlayer.isSpectator(event.getEntity()))
			return;
		if (_noFall.contains(event.getEntity()))
		{
			if (event.getCause() == DamageCause.FALL)
			{
				_noFall.remove(event.getEntity());
				event.setCancelled(true);
			}
		}
		
		Player player = (Player)event.getEntity();
		storeGear(player);
	}
	
	@EventHandler
	public void blockDeadPvt(PrivateMessageEvent event)
	{
		boolean onedead = false;
		boolean onelive = false;
		
		if (event.getSender() != null)
		{
			if (UtilPlayer.isSpectator(event.getSender()))
				onedead = true;
			else
				onelive = true;
		}
		if (event.getRecipient() != null)
		{
			if (UtilPlayer.isSpectator(event.getRecipient()))
				onedead = true;
			else
				onelive = true;
		}
		
		if (onedead)
		{
			if (onelive)
			{
				if (event.getSender() != null)
				{
					UtilPlayer.message(event.getSender(), F.main("Game", "You cannot message a player if you are not both alive/dead!"));
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void blockDeadTalk(AsyncPlayerChatEvent event)
	{
		try
		{
			if (GetTeam(event.getPlayer()) == null)
			{
				for (Player player : GetTeam(ChatColor.RED).GetPlayers(true))
				{
					event.getRecipients().remove(player);
				}
				for (Player player : GetTeam(ChatColor.AQUA).GetPlayers(true))
				{
					event.getRecipients().remove(player);
				}

				return;
			}
			if (!GetTeam(event.getPlayer()).IsAlive(event.getPlayer()))
			{
				for (Player player : GetTeam(ChatColor.RED).GetPlayers(true))
				{
					event.getRecipients().remove(player);
				}
				for (Player player : GetTeam(ChatColor.AQUA).GetPlayers(true))
				{
					event.getRecipients().remove(player);
				}
			}
		}
		catch (Exception e)
		{
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleDeath(PlayerDeathOutEvent event)
	{
		if (!IsLive())
			return;
		
		if (_tower.getAmountAlive(GetTeam(event.GetPlayer())) >= 1)
		{
			if (OverTime)
			{
				for (TeamTowerBase tb : _tower.getTeamTowers(GetTeam(event.GetPlayer())))
				{
					if (tb.Alive)
					{
						tb.damage(50, null);
						break;
					}
				}
				if (_tower.getAmountAlive(GetTeam(event.GetPlayer())) >= 1)
				{
					event.setCancelled(true);
					UtilPlayer.message(event.GetPlayer(), F.main("Game", "Your Tower lost 50 HP on your Respawn!"));
				}
			}
			else
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleDeath(CombatDeathEvent event)
	{
		if (!IsLive())
			return;
		
		event.SetBroadcastType(DeathMessageType.Detailed);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPickup(PlayerPickupItemEvent event)
	{
		if (UtilPlayer.isSpectator(event.getPlayer()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void handleMobs(EntitySpawnEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getEntity() instanceof Enderman)
			event.setCancelled(true);
		
		if (event.getEntityType() == EntityType.DROPPED_ITEM || event.getEntityType() == EntityType.ARROW)
			return;
		
		for (GameTeam team : GetTeamList())
		{
			for (TeamTowerBase tower : _tower.getTeamTowers(team))
			{
				if (event.getLocation().getWorld().getUID() != WorldData.World.getUID())
					continue;

				if (event.getLocation().distance(tower.getLocation()) <= 5)
					event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void handlePlace(BlockPlaceEvent event)
	{
		if (!IsLive())
			return;
		
		for (Location red : GetTeam(ChatColor.RED).GetSpawns())
		{
			if (UtilMath.offset(red, event.getBlock().getLocation()) < 5)
			{
				event.setCancelled(true);
				return;
			}
		}
		for (Location blue : GetTeam(ChatColor.AQUA).GetSpawns())
		{
			if (UtilMath.offset(blue, event.getBlock().getLocation()) < 5)
			{
				event.setCancelled(true);
				return;
			}
		}
		for (TeamTowerBase base : _tower.getTeamTowers(GetTeam(ChatColor.RED)))
		{
			if (UtilMath.offset(base.getLocation(), event.getBlock().getLocation()) <= 7)
			{
				event.setCancelled(true);
			}
		}
		for (TeamTowerBase base : _tower.getTeamTowers(GetTeam(ChatColor.AQUA)))
		{
			if (UtilMath.offset(base.getLocation(), event.getBlock().getLocation()) <= 7)
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void handleBreak(BlockBreakEvent event)
	{
		if (!IsLive())
			return;
		
		if (UtilPlayer.isSpectator(event.getPlayer()))
		{
			event.setCancelled(true);
			return;
		}
		
		for (Location red : GetTeam(ChatColor.RED).GetSpawns())
		{
			if (UtilMath.offset(red, event.getBlock().getLocation()) < 5)
			{
				event.setCancelled(true);
				return;
			}
		}
		for (Location blue : GetTeam(ChatColor.AQUA).GetSpawns())
		{
			if (UtilMath.offset(blue, event.getBlock().getLocation()) < 5)
			{
				event.setCancelled(true);
				return;
			}
		}
		
		for (TeamTowerBase base : _tower.getTeamTowers(GetTeam(ChatColor.RED)))
		{
			if (UtilMath.offset(base.getLocation(), event.getBlock().getLocation()) <= 7)
			{
				event.setCancelled(true);
			}
		}
		for (TeamTowerBase base : _tower.getTeamTowers(GetTeam(ChatColor.AQUA)))
		{
			if (UtilMath.offset(base.getLocation(), event.getBlock().getLocation()) <= 7)
			{
				event.setCancelled(true);
			}
		}
		
		if (event.getBlock().getType() == Material.GRAVEL)
		{
			event.setCancelled(true);
			event.getBlock().setType(Material.AIR);
			event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.FLINT));
		}
	}

	@EventHandler
	public void noMonsterLava(BlockFromToEvent event)
	{
		if (!IsLive())
			return;

		Block block = event.getBlock();
		if (block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA || block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
		{
			Block next = event.getToBlock();
			for (BlockFace bf : BlockFace.values())
			{
				if (!next.getRelative(bf).equals(block))
				{
					if (block.getType().toString().contains("LAVA"))
					{
						if (next.getRelative(bf).getType().toString().contains("WATER"))
						{
							event.setCancelled(true);
						}
					}
					if (block.getType().toString().contains("WATER"))
					{
						if (next.getRelative(bf).getType().toString().contains("LAVA"))
						{
							event.setCancelled(true);
						}
					}
				}
			}
		}
		if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onPlayerEmptyBucket(PlayerBucketEmptyEvent event)
	{
		if (!IsLive())
			return;
		
		Player player = event.getPlayer();
		if (player.getItemInHand().getType() == Material.WATER_BUCKET)
		{
			player.getItemInHand().setType(Material.BUCKET);
			Block block = event.getBlockClicked().getRelative(event.getBlockFace());
			if (block.getType().toString().contains("LAVA"))
			{
				event.setCancelled(true);
				return;
			}
			for (BlockFace bf : BlockFace.values())
			{
				if (block.getRelative(bf).getType().toString().contains("LAVA"))
				{
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onBlockDispense(BlockDispenseEvent event)
	{
		if (!IsLive())
			return;

		if (event.getItem().getType() == Material.WATER_BUCKET)
		{
			Block dispenser = event.getBlock();

			MaterialData mat = dispenser.getState().getData();
			Dispenser disp_mat = (Dispenser)mat;
			BlockFace face = disp_mat.getFacing();
			Block block = dispenser.getRelative(face);
			if (block.getType().toString().contains("LAVA"))
			{
				event.setCancelled(true);
				return;
			}
			for (BlockFace bf : BlockFace.values())
			{
				if (block.getRelative(bf).getType().toString().contains("LAVA"))
				{
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onTakeLava(PlayerBucketFillEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getItemStack().getType() == Material.LAVA_BUCKET)
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onCreateWaterSource(PlayerBucketEmptyEvent event)
	{
		if (!IsLive())
			return;
		
		Block current = event.getBlockClicked().getRelative(event.getBlockFace());
		if (current.getType() == Material.WATER || current.getType() == Material.STATIONARY_WATER)
		{
			event.setCancelled(true);
			event.getPlayer().setItemInHand(new ItemStack(Material.BUCKET));
		}
	}
	  
	@EventHandler
	public void furnaceBurn(FurnaceBurnEvent event)
	{
		if (!IsLive())
			return;
		
		Furnace furnace = (Furnace) event.getBlock().getState();
		furnace.setCookTime((short)100);
	}

	@EventHandler
	public void furnaceSmeltEvent(FurnaceSmeltEvent event)
	{
		if (!IsLive())
			return;
		
		Furnace furnace = (Furnace) event.getBlock().getState();
		furnace.setCookTime((short)100);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		if (!IsLive())
			return;
		
		Block blocktype = UtilPlayer.getTargetLocation((Player) event.getWhoClicked(), 10D).getBlock();

		if (blocktype.getType() == Material.FURNACE || blocktype.getType() == Material.BURNING_FURNACE)
		{
			if ((event.getSlot() == 0 || event.getSlot() == 1) && event.getCursor().getType() != Material.AIR)
			{
				Furnace furnace = (Furnace) blocktype.getState();
				furnace.setCookTime((short)100);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void handlePlace(PlayerBucketEmptyEvent event)
	{
		if (!IsLive())
			return;
		
		Block block = event.getBlockClicked().getRelative(event.getBlockFace());
		
		for (Location red : GetTeam(ChatColor.RED).GetSpawns())
		{
			if (UtilMath.offset(red, block.getLocation()) < 5)
			{
				event.setCancelled(true);
				return;
			}
		}
		for (Location blue : GetTeam(ChatColor.AQUA).GetSpawns())
		{
			if (UtilMath.offset(blue, block.getLocation()) < 5)
			{
				event.setCancelled(true);
				return;
			}
		}
		for (TeamTowerBase base : _tower.getTeamTowers(GetTeam(ChatColor.RED)))
		{
			if (UtilMath.offset(base.getLocation(), block.getLocation()) <= 7)
			{
				event.setCancelled(true);
			}
		}
		for (TeamTowerBase base : _tower.getTeamTowers(GetTeam(ChatColor.AQUA)))
		{
			if (UtilMath.offset(base.getLocation(), block.getLocation()) <= 7)
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void handleBreak(PlayerBucketFillEvent event)
	{
		if (!IsLive())
			return;
		
		Block block = event.getBlockClicked().getRelative(event.getBlockFace());
		
		for (Location red : GetTeam(ChatColor.RED).GetSpawns())
		{
			if (UtilMath.offset(red, block.getLocation()) < 5)
			{
				event.setCancelled(true);
				return;
			}
		}
		for (Location blue : GetTeam(ChatColor.AQUA).GetSpawns())
		{
			if (UtilMath.offset(blue, block.getLocation()) < 5)
			{
				event.setCancelled(true);
				return;
			}
		}
		for (TeamTowerBase base : _tower.getTeamTowers(GetTeam(ChatColor.RED)))
		{
			if (UtilMath.offset(base.getLocation(), block.getLocation()) <= 7)
			{
				event.setCancelled(true);
			}
		}
		for (TeamTowerBase base : _tower.getTeamTowers(GetTeam(ChatColor.AQUA)))
		{
			if (UtilMath.offset(base.getLocation(), block.getLocation()) <= 7)
			{
				event.setCancelled(true);
			}
		}
	}
}
