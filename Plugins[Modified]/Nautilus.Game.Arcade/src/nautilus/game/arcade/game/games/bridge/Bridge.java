package nautilus.game.arcade.game.games.bridge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.explosion.ExplosionEvent;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.bridge.animation.BridgeAnimation;
import nautilus.game.arcade.game.games.bridge.animation.BridgeAnimationType;
import nautilus.game.arcade.game.games.bridge.animation.custom.CustomBridgeAnimation;
import nautilus.game.arcade.game.games.bridge.animation.custom.RadiusCustomBridgeAnimation;
import nautilus.game.arcade.game.games.bridge.animation.custom.RandomCustomBridgeAnimation;
import nautilus.game.arcade.game.games.bridge.kits.KitApple;
import nautilus.game.arcade.game.games.bridge.kits.KitArcher;
import nautilus.game.arcade.game.games.bridge.kits.KitBerserker;
import nautilus.game.arcade.game.games.bridge.kits.KitBomber;
import nautilus.game.arcade.game.games.bridge.kits.KitBrawler;
import nautilus.game.arcade.game.games.bridge.kits.KitDestructor;
import nautilus.game.arcade.game.games.bridge.kits.KitMiner;
import nautilus.game.arcade.game.games.bridge.mission.KillLastTracker;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.game.modules.rejoin.RejoinModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.perks.PerkBomber;
import nautilus.game.arcade.ore.OreHider;
import nautilus.game.arcade.ore.OreObsfucation;
import nautilus.game.arcade.stats.BridgesSniperStatTracker;
import nautilus.game.arcade.stats.DeathBomberStatTracker;
import nautilus.game.arcade.stats.FoodForTheMassesStatTracker;
import nautilus.game.arcade.stats.KillFastStatTracker;
import nautilus.game.arcade.stats.TntMinerStatTracker;

public class Bridge extends TeamGame implements OreObsfucation
{
	public enum Perm implements Permission
	{
		DEBUG_BRIDGE_COMMAND,
		DEBUG_BRIDGEINFO_COMMAND,
	}

	/**
	 * When a block is broken of one of these materials, the item drop will be locked to the player that broke the block for 8 seconds. After that, anyone can pick up the item.
	 */
	private static final Material[] PLAYER_DROP_DELAY_MATERIALS = new Material[]{Material.LOG, Material.LOG_2, Material.IRON_ORE, Material.DIAMOND_ORE, Material.COAL_ORE, Material.GOLD_ORE, Material.WORKBENCH, Material.FURNACE};

	/**
	 * The number of milliseconds from the game start time til the bridges should be built.
	 */
	private static final long BRIDGE_TIME = TimeUnit.MINUTES.toMillis(10);

	private static final String CUSTOM_BRIDGE_KEY = "TYPE";

	//Bridge
	private long _bridgeTime = BRIDGE_TIME;
	private boolean _bridgesDown = false;
	private BridgeAnimation _animation;
	private CustomBridgeAnimation[] _customAnimations;
	private final RejoinModule _rejoinModule;

	private HashSet<BridgePart> _bridgeParts = new HashSet<BridgePart>();

	//Animals
	private long _lastAnimal = System.currentTimeMillis();
	private HashMap<GameTeam, HashSet<Entity>> _animalSet = new HashMap<GameTeam, HashSet<Entity>>();

	//Mushroom
	private long _lastMushroom = System.currentTimeMillis();

	//Chest Loot
	private ArrayList<ItemStack> _chestLoot = new ArrayList<ItemStack>();

	//Ore
	private OreHider _ore;
	private double _oreDensity = 2.2;

	//Map Flags
	private int _buildHeight = -1;

	//Player Respawn
	private Set<String> _usedLife = new HashSet<>();

	//Tourney Mode
	private boolean _tournament;
	private HashMap<GameTeam, Integer> _tournamentKills = new HashMap<GameTeam, Integer>();
	private long _tournamentKillMessageTimer = 0;

	private final Map<GameTeam, Location> _averageSpawns = new HashMap<>();

	@SuppressWarnings("unchecked")
	public Bridge(ArcadeManager manager)
	{
		this(manager, GameType.Bridge);

		registerStatTrackers(
				new FoodForTheMassesStatTracker(this),
				new BridgesSniperStatTracker(this),
				new TntMinerStatTracker(this),
				new KillFastStatTracker(this, 4, 10, "Rampage"),
				new DeathBomberStatTracker(this, 5)
		);

		registerMissions(
				new KillLastTracker(this)
		);

		registerChatStats(
				Kills,
				Deaths,
				KDRatio,
				BlankLine,
				Assists,
				DamageTaken,
				DamageDealt
		);
	}

	public Bridge(ArcadeManager manager, GameType type)
	{
		this(manager, new Kit[]
				{
						new KitApple(manager),
						new KitBerserker(manager),
						new KitBrawler(manager),
						new KitArcher(manager),
						new KitMiner(manager),
						new KitBomber(manager),
						new KitDestructor(manager),
				}, type);
	}

	public Bridge(ArcadeManager manager, Kit[] kitarray, GameType type)
	{
		super(manager, type, kitarray,

				new String[]{
						"Gather resources and prepare for combat.",
						"After 10 minutes, The Bridges will emerge.",
						"Special loot is located in the center.",
						"The last team alive wins!"
				});

		_ore = new OreHider();

		// Flags
		GameTimeout = Manager.IsTournamentServer() ? TimeUnit.MINUTES.toMillis(90) : TimeUnit.MINUTES.toMillis(60);

		Manager.GetExplosion().SetLiquidDamage(false);

		StrictAntiHack = true;

		DamageSelf = true;

		ItemDrop = true;
		ItemPickup = true;

		InventoryClick = true;

		AnnounceStay = false;

		PrivateBlocks = true;
		BlockBreak = true;
		BlockPlace = true;

		InventoryOpenBlock = true;
		InventoryOpenChest = true;

		WorldTimeSet = 2000;

		WorldWaterDamage = 0;
		WorldBoundaryKill = true;

		DeathDropItems = true;

		GemMultiplier = 2.5;

		PrepareFreeze = false;

		//Tournament
		if (Manager.IsTournamentServer())
		{
			QuitOut = false;

			_gameDesc = new String[]
					{
							"Gather resources and prepare for combat.",
							"After 10 minutes, The Bridges will emerge.",
							"Special loot is located in the center.",
							"Killing yourself counts as -1 team kill.",
							"Team with the most kills wins!"
					};

			_tournament = true;
		}

		_customAnimations = new CustomBridgeAnimation[]{
				new RandomCustomBridgeAnimation(this),
				new RadiusCustomBridgeAnimation(this)
		};

		new CompassModule()
				.setGiveCompassToAlive(true)
				.register(this);

		_rejoinModule = new RejoinModule(manager)
				.setSaveInventory(true);
		_rejoinModule.register(this);

		// So that we can be 110% sure
		for (Kit kit : GetKits())
		{
			if (kit instanceof KitDestructor)
			{
				((KitDestructor) kit).SetEnabled(false);
				break;
			}
		}

		registerDebugCommand("bridge", Perm.DEBUG_BRIDGE_COMMAND, PermissionGroup.ADMIN, (caller, args) ->
		{
			caller.sendMessage(F.main("Debug", "Spawning the bridges."));
			_bridgeTime = 3000;
		});

		registerDebugCommand("bridgeinfo", Perm.DEBUG_BRIDGEINFO_COMMAND, PermissionGroup.ADMIN, (caller, args) ->
		{
			if (_animation == null || !(_animation instanceof CustomBridgeAnimation))
			{
				caller.sendMessage(F.main("Debug", "The bridge animation for this map isn't a custom one."));
				return;
			}

			caller.sendMessage(F.main("Debug", "Bridge Info:"));
			caller.sendMessage(_animation.toString());
		});
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		Manager.runSyncLater(() -> _animation.onParse(), 10);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		if (!WorldData.GetCustomLocs("WATER_DAMAGE").isEmpty())
		{
			WorldWaterDamage = 4;
			String name = WorldData.MapName;

			if (name.equals("Volcanic Islands"))
			{
				UtilTextMiddle.display(C.cRed + "Warning", "Water is Boiling Hot", 10, 60, 20);
			}
			else if (name.equals("Icelands"))
			{
				UtilTextMiddle.display(C.cRed + "Warning", "Water is Freezing Cold", 10, 60, 20);
			}
			else
			{
				UtilTextMiddle.display(C.cRed + "Warning", "Water is Deadly", 10, 60, 20);
			}
		}
	}

	@Override
	public void ParseData()
	{
		// Now we need to decide on what bridge animation.
		typeLoop:
		for (BridgeAnimationType type : BridgeAnimationType.values())
		{
			for (String colours : type.getColoursUsed())
			{
				if (WorldData.GetDataLocs(colours).isEmpty())
				{
					continue typeLoop;
				}
			}

			_animation = type.createInstance(this);
			break;
		}

		// If none of the premade ones are usable then we need a custom one!
		if (_animation == null)
		{
			locationLoop:
			for (String key : WorldData.GetAllCustomLocs().keySet())
			{
				if (!key.startsWith(CUSTOM_BRIDGE_KEY))
				{
					continue;
				}

				String[] split = key.split(" ");

				if (split.length < 2)
				{
					continue;
				}

				String subKey = split[1];

				for (CustomBridgeAnimation animation : _customAnimations)
				{
					if (animation.getTypeKey().equalsIgnoreCase(subKey))
					{
						_animation = animation;
						break locationLoop;
					}
				}
			}
		}

		ParseChests();

		ParseOre(WorldData.GetCustomLocs("73")); // Red
		ParseOre(WorldData.GetCustomLocs("14")); // Yellow
		ParseOre(WorldData.GetCustomLocs("129")); // Green
		ParseOre(WorldData.GetCustomLocs("56")); // Blue

		//Mass Teams
		if (!WorldData.GetCustomLocs("152").isEmpty())
		{
			ParseOre(WorldData.GetCustomLocs("152"));
		}
		if (!WorldData.GetCustomLocs("41").isEmpty())
		{
			ParseOre(WorldData.GetCustomLocs("41"));
		}
		if (!WorldData.GetCustomLocs("133").isEmpty())
		{
			ParseOre(WorldData.GetCustomLocs("133"));
		}
		if (!WorldData.GetCustomLocs("57").isEmpty())
		{
			ParseOre(WorldData.GetCustomLocs("57"));
		}

		if (!WorldData.GetCustomLocs("100").isEmpty())
		{
			ParseOre(WorldData.GetCustomLocs("100"));
		}
		if (!WorldData.GetCustomLocs("86").isEmpty())
		{
			ParseOre(WorldData.GetCustomLocs("86"));
		}
		if (!WorldData.GetCustomLocs("103").isEmpty())
		{
			ParseOre(WorldData.GetCustomLocs("103"));
		}
		if (!WorldData.GetCustomLocs("22").isEmpty())
		{
			ParseOre(WorldData.GetCustomLocs("22"));
		}
	}

	protected void ParseChests()
	{
		for (Location loc : WorldData.GetCustomLocs("54"))
		{
			if (loc.getBlock().getType() != Material.CHEST)
			{
				loc.getBlock().setType(Material.CHEST);
			}

			Chest chest = (Chest) loc.getBlock().getState();

			chest.getBlockInventory().clear();

			int count = 2 + UtilMath.r(5);
			for (int i = 0; i < count; i++)
			{
				chest.getBlockInventory().addItem(GetChestItem());
			}
		}
	}

	protected ItemStack GetChestItem()
	{
		if (_chestLoot.isEmpty())
		{
			for (int i = 0; i < 1; i++)
				_chestLoot.add(new ItemStack(Material.DIAMOND_HELMET));
			for (int i = 0; i < 1; i++)
				_chestLoot.add(new ItemStack(Material.DIAMOND_CHESTPLATE));
			for (int i = 0; i < 1; i++)
				_chestLoot.add(new ItemStack(Material.DIAMOND_LEGGINGS));
			for (int i = 0; i < 1; i++)
				_chestLoot.add(new ItemStack(Material.DIAMOND_BOOTS));
			for (int i = 0; i < 1; i++)
				_chestLoot.add(new ItemStack(Material.DIAMOND_SWORD));
			for (int i = 0; i < 1; i++)
				_chestLoot.add(new ItemStack(Material.DIAMOND_AXE));
			for (int i = 0; i < 1; i++)
				_chestLoot.add(new ItemStack(Material.DIAMOND_PICKAXE));

			for (int i = 0; i < 6; i++)
				_chestLoot.add(new ItemStack(Material.IRON_HELMET));
			for (int i = 0; i < 6; i++)
				_chestLoot.add(new ItemStack(Material.IRON_CHESTPLATE));
			for (int i = 0; i < 6; i++)
				_chestLoot.add(new ItemStack(Material.IRON_LEGGINGS));
			for (int i = 0; i < 6; i++)
				_chestLoot.add(new ItemStack(Material.IRON_BOOTS));
			for (int i = 0; i < 6; i++)
				_chestLoot.add(new ItemStack(Material.IRON_SWORD));
			for (int i = 0; i < 6; i++)
				_chestLoot.add(new ItemStack(Material.IRON_AXE));
			for (int i = 0; i < 6; i++)
				_chestLoot.add(new ItemStack(Material.IRON_PICKAXE));

			for (int i = 0; i < 18; i++)
				_chestLoot.add(new ItemStack(Material.BOW));
			for (int i = 0; i < 24; i++)
				_chestLoot.add(new ItemStack(Material.ARROW, 8));

			for (int i = 0; i < 48; i++)
				_chestLoot.add(new ItemStack(Material.MUSHROOM_SOUP));
			for (int i = 0; i < 24; i++)
				_chestLoot.add(new ItemStack(Material.COOKED_CHICKEN, 2));
		}

		ItemStack stack = _chestLoot.get(UtilMath.r(_chestLoot.size()));

		int amount = 1;

		if (stack.getType().getMaxStackSize() > 1)
		{
			amount = stack.getAmount() + UtilMath.r(stack.getAmount());
		}

		return ItemStackFactory.Instance.CreateStack(stack.getTypeId(), amount);
	}

	@EventHandler
	public void ChestDeny(PlayerInteractEvent event)
	{
		if (_bridgesDown)
		{
			return;
		}

		if (event.getClickedBlock() == null)
		{
			return;
		}

		if (event.getClickedBlock().getType() != Material.CHEST)
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
		{
			return;
		}

		for (Location loc : WorldData.GetCustomLocs("54"))
		{
			if (loc.getBlock().equals(event.getClickedBlock()))
			{
				event.setCancelled(true);
				return;
			}
		}
	}

	public void ParseOre(ArrayList<Location> teamOre)
	{
		int coal = (int) ((teamOre.size() / 32d) * _oreDensity);
		int iron = (int) ((teamOre.size() / 24d) * _oreDensity);
		int gold = (int) ((teamOre.size() / 64d) * _oreDensity);
		int diamond = 1 + (int) ((teamOre.size() / 128d) * _oreDensity);

		int gravel = (int) ((teamOre.size() / 64d) * _oreDensity);

		int lowY = 256;
		int highY = 0;

		for (Location loc : teamOre)
		{
			if (loc.getBlockY() < lowY)
			{
				lowY = loc.getBlockY();
			}

			if (loc.getBlockY() > highY)
			{
				highY = loc.getBlockY();
			}

			loc.getBlock().setTypeId(1);
		}

		int varY = highY - lowY;

		//Gravel
		for (int i = 0; i < gravel && !teamOre.isEmpty(); i++)
		{
			int attempts = 20;
			int id = 0;

			while (attempts > 0)
			{
				id = UtilMath.r(teamOre.size());

				double height = (double) (teamOre.get(id).getBlockY() - lowY) / (double) varY;

				if (height > 0.8)
				{
					break;
				}

				else if (height > 0.6 && Math.random() > 0.4)
				{
					break;
				}

				else if (height > 0.4 && Math.random() > 0.6)
				{
					break;
				}

				else if (height > 0.2 && Math.random() > 0.8)
				{
					break;
				}
			}

			CreateOre(teamOre.remove(id), Material.GRAVEL, 6);
		}

		//Coal
		for (int i = 0; i < coal && !teamOre.isEmpty(); i++)
		{
			int attempts = 20;
			int id = 0;

			while (attempts > 0)
			{
				id = UtilMath.r(teamOre.size());

				double height = (double) (teamOre.get(id).getBlockY() - lowY) / (double) varY;

				if (height > 0.8)
				{
					break;
				}

				else if (height > 0.6 && Math.random() > 0.4)
				{
					break;
				}

				else if (height > 0.4 && Math.random() > 0.6)
				{
					break;
				}

				else if (height > 0.2 && Math.random() > 0.8)
				{
					break;
				}
			}

			CreateOre(teamOre.remove(id), Material.COAL_ORE, 6);
		}

		//Iron
		for (int i = 0; i < iron && !teamOre.isEmpty(); i++)
		{
			int id = UtilMath.r(teamOre.size());

			CreateOre(teamOre.remove(id), Material.IRON_ORE, 3);
		}

		//Gold
		for (int i = 0; i < gold && !teamOre.isEmpty(); i++)
		{
			int attempts = 20;
			int id = 0;

			while (attempts > 0)
			{
				id = UtilMath.r(teamOre.size());

				double height = (double) (teamOre.get(id).getBlockY() - lowY)
						/ (double) varY;

				if (height > 0.8 && Math.random() > 0.8)
				{
					break;
				}

				else if (height > 0.6 && Math.random() > 0.7)
				{
					break;
				}

				else if (height > 0.4 && Math.random() > 0.6)
				{
					break;
				}

				else if (height > 0.2 && Math.random() > 0.4)
				{
					break;
				}

				else if (Math.random() > 0.2)
				{
					break;
				}
			}

			CreateOre(teamOre.remove(id), Material.GOLD_ORE, 3);
		}

		//Diamond
		for (int i = 0; i < diamond && !teamOre.isEmpty(); i++)
		{
			int attempts = 20;
			int id = 0;

			while (attempts > 0)
			{
				id = UtilMath.r(teamOre.size());

				double height = (double) (teamOre.get(id).getBlockY() - lowY)
						/ (double) varY;

				if (height > 0.8)
				{
					continue;
				}

				else if (height > 0.6 && Math.random() > 0.9)
				{
					break;
				}

				else if (height > 0.4 && Math.random() > 0.7)
				{
					break;
				}

				else if (height > 0.2 && Math.random() > 0.5)
				{
					break;
				}

				else
				{
					break;
				}
			}

			CreateOre(teamOre.remove(id), Material.DIAMOND_ORE, 2);
		}
	}

	public void CreateOre(Location loc, Material type, int amount)
	{
		double bonus = Math.random() + 1;

		amount = (int) (amount * bonus);

		int attempts = 100;
		while (amount > 0 && attempts > 0)
		{
			attempts--;

			BlockFace faceXZ = BlockFace.SELF;
			BlockFace faceY = BlockFace.SELF;

			if (Math.random() > 0.20)
			{
				int rFace = UtilMath.r(6);

				if (rFace == 0)
				{
					faceY = BlockFace.UP;
				}
				else if (rFace == 1)
				{
					faceY = BlockFace.DOWN;
				}
				else if (rFace == 2)
				{
					faceXZ = BlockFace.NORTH;
				}
				else if (rFace == 3)
				{
					faceXZ = BlockFace.SOUTH;
				}
				else if (rFace == 4)
				{
					faceXZ = BlockFace.EAST;
				}
				else
				{
					faceXZ = BlockFace.WEST;
				}
			}
			else
			{
				//Height
				int rFace = UtilMath.r(3);

				if (rFace == 0)
				{
					faceY = BlockFace.SELF;
				}
				else if (rFace == 1)
				{
					faceY = BlockFace.UP;
				}
				else
				{
					faceY = BlockFace.DOWN;
				}

				//Flat
				if (faceY == BlockFace.SELF)
				{
					rFace = UtilMath.r(4);

					if (rFace == 0)
					{
						faceXZ = BlockFace.NORTH_EAST;
					}
					else if (rFace == 1)
					{
						faceXZ = BlockFace.NORTH_WEST;
					}
					else if (rFace == 2)
					{
						faceXZ = BlockFace.SOUTH_EAST;
					}
					else
					{
						faceXZ = BlockFace.SOUTH_WEST;
					}
				}
				else
				{
					rFace = UtilMath.r(4);

					if (rFace == 0)
					{
						faceXZ = BlockFace.NORTH;
					}
					else if (rFace == 1)
					{
						faceXZ = BlockFace.SOUTH;
					}
					else if (rFace == 2)
					{
						faceXZ = BlockFace.EAST;
					}
					else
					{
						faceXZ = BlockFace.WEST;
					}
				}
			}

			if (loc.getBlock().getRelative(faceY).getRelative(faceXZ).getType() != Material.STONE)
			{
				continue;
			}

			loc = loc.getBlock().getRelative(faceY).getRelative(faceXZ).getLocation();

			_ore.AddOre(loc, type);

			amount--;
		}
	}

	@EventHandler
	public void BridgeBuild(UpdateEvent event)
	{
		if (!IsLive() || !UtilTime.elapsed(GetStateTime(), _bridgeTime))
		{
			return;
		}

		if (_animation != null)
		{
			_animation.onUpdate(event.getType());
		}

		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		if (!_bridgesDown)
		{
			_bridgesDown = true;
			Manager.GetExplosion().SetLiquidDamage(true);
			_rejoinModule.disableRejoining();
			Announce(C.cRedB + "ALERT: " + C.Reset + C.Bold + "THE BRIDGES ARE SPAWNING!");
			UtilTextMiddle.display(C.cRedB + "ALERT", "The BRIDGES ARE SPAWNING!");

			for (Kit kit : GetKits())
			{
				if (kit instanceof KitDestructor)
				{
					((KitDestructor) kit).SetEnabled(true);
				}
			}
		}
	}

	@EventHandler
	public void BridgeUpdate(UpdateEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_bridgeParts.removeIf(BridgePart::Update);
	}

	@EventHandler
	public void BridgeForm(EntityChangeBlockEvent event)
	{
		for (BridgePart part : _bridgeParts)
			if (part.Entity.equals(event.getEntity()))
			{
				event.setCancelled(true);
			}
	}

	@EventHandler
	public void BridgeItem(ItemSpawnEvent event)
	{
		for (BridgePart part : _bridgeParts)
			if (part.ItemSpawn(event.getEntity()))
			{
				event.setCancelled(true);
			}
	}

	@EventHandler
	public void IceForm(BlockFormEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void AnimalSpawn(UpdateEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		if (!UtilTime.elapsed(_lastAnimal, 30000))
		{
			return;
		}

		_lastAnimal = System.currentTimeMillis();

		for (GameTeam team : GetTeamList())
		{
			if (_animalSet.get(team) == null)
			{
				_animalSet.put(team, new HashSet<Entity>());
			}

			// Clean
			Iterator<Entity> entIterator = _animalSet.get(team).iterator();

			while (entIterator.hasNext())
			{
				Entity ent = entIterator.next();

				if (ent.isDead() || !ent.isValid())
				{
					entIterator.remove();
				}
			}

			// Too Many
			if (_animalSet.get(team).size() > 4)
			{
				continue;
			}

			// Spawn
			double rand = Math.random();

			Entity ent;

			CreatureAllowOverride = true;
			if (rand > 0.66)
			{
				ent = team.GetSpawn().getWorld().spawn(team.GetSpawn(), Cow.class);
			}
			else if (rand > 0.33)
			{
				ent = team.GetSpawn().getWorld().spawn(team.GetSpawn(), Pig.class);
			}
			else
			{
				ent = team.GetSpawn().getWorld().spawn(team.GetSpawn(), Chicken.class);
			}
			CreatureAllowOverride = false;

			_animalSet.get(team).add(ent);
		}
	}

	@EventHandler
	public void MushroomSpawn(UpdateEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		if (!UtilTime.elapsed(_lastMushroom, 20000))
		{
			return;
		}

		_lastMushroom = System.currentTimeMillis();

		for (GameTeam team : GetTeamList())
		{
			Block block = team.GetSpawn().getBlock();

			while (!UtilBlock.airFoliage(block))
			{
				block = block.getRelative(BlockFace.UP);

				if (block.getY() >= 256)
				{
					break;
				}
			}

			while (UtilBlock.airFoliage(block))
			{
				block = block.getRelative(BlockFace.DOWN);

				if (block.getY() <= 0)
				{
					break;
				}
			}

			if (block.getType() != Material.SNOW)
			{
				block = block.getRelative(BlockFace.UP);
			}

			if (Math.random() > 0.5)
			{
				block.setTypeId(39);
			}
			else
			{
				block.setTypeId(40);
			}
		}
	}

	@EventHandler
	public void handleExplosion(ExplosionEvent event)
	{
		// Reveal ore that are inside the explosion
		_ore.Explosion(event);

		NautHashMap<Block, HashSet<ItemStack>> lootDrops = new NautHashMap<Block, HashSet<ItemStack>>();

		// Handle block ownership for explosion
		if (event.getOwner() != null)
		{
			for (Block cur : new HashSet<Block>(event.GetBlocks()))
			{
				// These are the only blocks that will drop from the explosion so they are the only ones
				// we need to worry about for keeping owner data of
				if (cur.getType() == Material.IRON_ORE ||
						cur.getType() == Material.COAL_ORE ||
						cur.getType() == Material.GOLD_ORE ||
						cur.getType() == Material.DIAMOND_ORE)
				{
					event.GetBlocks().remove(cur);
					lootDrops.put(cur, new HashSet<ItemStack>());

					lootDrops.get(cur).addAll(cur.getDrops());
				}
			}
		}

		Manager.runSyncLater(new Runnable() // Run after the explosion has already happened
		{
			@Override
			public void run()
			{
				for (Block block : new HashSet<Block>(lootDrops.keySet()))
				{
					block.setType(Material.AIR);
					Location drop = block.getLocation().clone().add(.5, .5, .5);

					for (ItemStack stack : lootDrops.remove(block))
					{
						Item item = block.getWorld().dropItem(drop, stack);
						item.setMetadata("owner", new FixedMetadataValue(Manager.getPlugin(), event.getOwner().getUniqueId()));
					}
				}
			}
		}, 1);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void BlockPlace(BlockPlaceEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (!IsAlive(event.getPlayer()))
		{
			return;
		}

		//No water basing
		if (event.getBlock().getRelative(BlockFace.UP).isLiquid() ||
				event.getBlockReplacedState().getTypeId() == 8 ||
				event.getBlockReplacedState().getTypeId() == 9 ||
				event.getBlockReplacedState().getTypeId() == 10 ||
				event.getBlockReplacedState().getTypeId() == 11)
		{
			if (event.getItemInHand() != null &&
					event.getItemInHand().getType() == Material.WOOD_DOOR ||
					event.getItemInHand().getTypeId() == 193 || //1.8 doors
					event.getItemInHand().getTypeId() == 194 || //1.8 doors
					event.getItemInHand().getTypeId() == 195 || //1.8 doors
					event.getItemInHand().getTypeId() == 196 || //1.8 doors
					event.getItemInHand().getTypeId() == 197 || //1.8 doors
					event.getItemInHand().getTypeId() == 330 || //1.8 doors
					event.getItemInHand().getTypeId() == 427 || //1.8 doors
					event.getItemInHand().getTypeId() == 428 || //1.8 doors
					event.getItemInHand().getTypeId() == 429 || //1.8 doors
					event.getItemInHand().getTypeId() == 430 || //1.8 doors
					event.getItemInHand().getTypeId() == 431 || //1.8 doors
					event.getItemInHand().getType() == Material.TRAP_DOOR ||
					event.getItemInHand().getTypeId() == 167 || //Iron trap door
					event.getItemInHand().getType() == Material.LADDER ||
					event.getItemInHand().getType() == Material.STONE_PLATE ||
					event.getItemInHand().getType() == Material.WOOD_PLATE ||
					event.getItemInHand().getType() == Material.IRON_PLATE ||
					event.getItemInHand().getType() == Material.GOLD_PLATE ||
					event.getItemInHand().getType() == Material.WOOD_BUTTON ||
					event.getItemInHand().getType() == Material.STONE_BUTTON ||
					event.getItemInHand().getType() == Material.SIGN ||
					event.getItemInHand().getType() == Material.FENCE ||
					event.getItemInHand().getType() == Material.FENCE_GATE ||
					event.getItemInHand().getTypeId() == 183 || //1.8 Fences stuff
					event.getItemInHand().getTypeId() == 184 ||
					event.getItemInHand().getTypeId() == 185 ||
					event.getItemInHand().getTypeId() == 186 ||
					event.getItemInHand().getTypeId() == 187 ||
					event.getItemInHand().getTypeId() == 188 ||
					event.getItemInHand().getTypeId() == 189 ||
					event.getItemInHand().getTypeId() == 190 ||
					event.getItemInHand().getTypeId() == 191 ||
					event.getItemInHand().getTypeId() == 192 ||
					event.getItemInHand().getType() == Material.NETHER_FENCE ||
					event.getItemInHand().getType() == Material.STEP ||
					event.getItemInHand().getType() == Material.WOOD_STEP ||
					event.getItemInHand().getTypeId() == 182) //Red sandstone slab
			{
				event.getPlayer().getWorld().playEffect(event.getBlock().getLocation(), Effect.STEP_SOUND, event.getItemInHand().getTypeId());
				event.setCancelled(true);
				return;
			}
		}

		//Too High
		if (event.getBlock().getLocation().getBlockY() > GetHeightLimit())
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game",
					"Cannot place blocks this high up."));
			event.setCancelled(true);
			return;
		}

		if (_bridgesDown)
		{
			return;
		}

		//In Liquid
		if (event.getBlock().getRelative(BlockFace.UP).isLiquid() ||
				event.getBlockReplacedState().getTypeId() == 8 ||
				event.getBlockReplacedState().getTypeId() == 9 ||
				event.getBlockReplacedState().getTypeId() == 10 ||
				event.getBlockReplacedState().getTypeId() == 11)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game",
					"Cannot place blocks in liquids until Bridge is down."));

			UtilAction.velocity(event.getPlayer(), new Vector(0, -0.5, 0));

			event.setCancelled(true);
			return;
		}

		//Above Water/Void
		for (int i = 1; i <= event.getBlock().getLocation().getY(); i++)
		{
			Block below = event.getBlock().getRelative(BlockFace.DOWN, i);

			if (below.isLiquid())
			{
				UtilPlayer
						.message(
								event.getPlayer(),
								F.main("Game",
										"Cannot place blocks above water until Bridge is down."));
				event.setCancelled(true);
				return;
			}

			if (event.getBlock().getLocation().getY() - i <= 0)
			{
				UtilPlayer
						.message(
								event.getPlayer(),
								F.main("Game",
										"Cannot place blocks above void until Bridge is down."));
				event.setCancelled(true);
				return;
			}

			if (below.getTypeId() != 0)
			{
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void dropItem(BlockBreakEvent event)
	{
		_ore.BlockBreak(event);

		if (!IsLive())
		{
			return;
		}

		if (!IsAlive(event.getPlayer()))
		{
			return;
		}

		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
		{
			return;
		}

		List<ItemStack> drops = new ArrayList<ItemStack>();

		for (Material mat : PLAYER_DROP_DELAY_MATERIALS)
		{
			if (event.getBlock().getType() == mat)
			{
				drops.addAll(event.getBlock().getDrops(event.getPlayer().getItemInHand()));
				break;
			}
		}

		if (drops.isEmpty())
		{
			return;
		}

		event.setCancelled(true);
		event.getBlock().setType(Material.AIR);

		Location loc = event.getBlock().getLocation().clone().add(.5, .5, .5);

		for (ItemStack item : drops)
		{
			Item drop = loc.getWorld().dropItem(loc, item);
			drop.setMetadata("owner", new FixedMetadataValue(Manager.getPlugin(), event.getPlayer().getUniqueId()));
		}
	}

	@EventHandler
	public void diamondOreExplode(PerkBomber.BomberExplodeDiamondBlock event)
	{
		event.setSpawnDrop(false);
		event.getBlock().setType(Material.AIR);

		Item item = event.getBlock().getWorld().dropItem(event.getBlock().getLocation().add(.5, .5, .5), new ItemStack(Material.DIAMOND));
		item.setMetadata("owner", new FixedMetadataValue(Manager.getPlugin(), event.getPlayer().getUniqueId()));
	}

	@EventHandler
	public void itemPickup(PlayerPickupItemEvent event)
	{
		Item item = event.getItem();

		if (item.hasMetadata("owner"))
		{
			FixedMetadataValue ownerData = (FixedMetadataValue) item.getMetadata("owner").get(0);
			UUID ownerUUID = (UUID) ownerData.value();

			// 8 Seconds
			if (item.getTicksLived() <= 160 && !event.getPlayer().getUniqueId().equals(ownerUUID))
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void ChestProtect(EntityExplodeEvent event)
	{
		Iterator<Block> blockIterator = event.blockList().iterator();

		while (blockIterator.hasNext())
		{
			Block block = blockIterator.next();

			if (block.getType() == Material.CHEST
					|| block.getType() == Material.FURNACE
					|| block.getType() == Material.BURNING_FURNACE
					|| block.getType() == Material.WORKBENCH)
			{
				blockIterator.remove();
			}
		}
	}


	@EventHandler(priority = EventPriority.LOW)
	public void BucketEmpty(PlayerBucketEmptyEvent event)
	{
		if (event.getBucket() != Material.WATER_BUCKET)
		{
			return;
		}

		if (WorldWaterDamage > 0)
		{
			UtilPlayer.message(
					event.getPlayer(),
					F.main("Game", "Cannot use " + F.elem("Water Bucket") + " on this map."));
			event.setCancelled(true);
		}

		else if (!_bridgesDown)
		{
			UtilPlayer.message(
					event.getPlayer(),
					F.main("Game", "Cannot use " + F.elem("Water Bucket") + " befores Bridges drop."));
			event.setCancelled(true);
		}
	}

	public int GetHeightLimit()
	{
		if (_buildHeight == -1)
		{
			_buildHeight = 0;
			int amount = 0;

			for (GameTeam team : GetTeamList())
				for (Location loc : team.GetSpawns())
				{
					_buildHeight += loc.getBlockY();
					amount++;
				}


			_buildHeight = _buildHeight / amount;
		}

		return _buildHeight + 24;
	}

	@Override
	public OreHider GetOreHider()
	{
		return _ore;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void CraftingDeny(PrepareItemCraftEvent event)
	{
		if (event.getRecipe().getResult() == null)
		{
			return;
		}

		Material type = event.getRecipe().getResult().getType();

		if (type != Material.GOLDEN_APPLE &&
				type != Material.GOLDEN_CARROT &&
				type != Material.FLINT_AND_STEEL && type != Material.HOPPER)
		{
			return;
		}

		if (!(event.getInventory() instanceof CraftingInventory))
		{
			return;
		}

		CraftingInventory inv = (CraftingInventory) event.getInventory();
		inv.setResult(null);
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		//Wipe Last
		Scoreboard.reset();

		//Display Players Alive
		if (!_tournament)
		{
			for (GameTeam team : this.GetTeamList())
			{
				//Display Individual Players
				if (this.GetPlayers(true).size() < 8)
				{
					if (!team.IsTeamAlive())
					{
						continue;
					}

					Scoreboard.writeNewLine();

					for (Player player : team.GetPlayers(true))
					{
						Scoreboard.write(team.GetColor() + player.getName());
					}
				}

				//Display Players Alive
				else
				{
					Scoreboard.writeNewLine();
					Scoreboard.write(team.GetColor() + team.GetName() + " Team");
					Scoreboard.write(team.GetColor() + "" + team.GetPlayers(true).size() + " Alive");
				}
			}
		}
		//Display Kills + Players
		else
		{
			for (GameTeam team : this.GetTeamList())
			{
				int kills = 0;
				if (_tournamentKills.containsKey(team))
				{
					kills = _tournamentKills.get(team);
				}

				Scoreboard.writeNewLine();

				Scoreboard.write(team.GetColor() + " " + team.GetPlayers(true).size() + " Players");
				Scoreboard.write(team.GetColor() + " " + kills + " Kills");
			}
		}

		Scoreboard.writeNewLine();


		long time = _bridgeTime
				- (System.currentTimeMillis() - this.GetStateTime());

		if (time > 0)
		{
			Scoreboard.write(C.cYellow + C.Bold + "Bridges In");
			Scoreboard.write(UtilTime.MakeStr(time, 0));
		}
		else
		{
			Scoreboard.write(C.cYellow + C.Bold + "Time Left");
			Scoreboard.write(UtilTime.MakeStr(GameTimeout - (System.currentTimeMillis() - GetStateTime()), 0));
		}

		Scoreboard.draw();
	}

	@EventHandler
	public void RecordKill(CombatDeathEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)
		{
			return;
		}

		if (!(event.GetEvent().getEntity() instanceof Player))
		{
			return;
		}

		Player killed = (Player) event.GetEvent().getEntity();

		GameTeam killedTeam = GetTeam(killed);
		if (killedTeam == null)
		{
			return;
		}

		if (event.GetLog().GetKiller() != null)
		{
			Player killer = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());

			if (killer != null && !killer.equals(killed))
			{
				GameTeam killerTeam = GetTeam(killer);
				if (killerTeam == null)
				{
					return;
				}

				if (killerTeam.equals(killedTeam))
				{
					return;
				}

				if (!_tournamentKills.containsKey(killerTeam))
				{
					_tournamentKills.put(killerTeam, 1);
				}
				else
				{
					_tournamentKills.put(killerTeam, _tournamentKills.get(killerTeam) + 1);
				}
			}
			//self kill
			else if (_bridgesDown)
			{
				if (!_tournamentKills.containsKey(killedTeam))
				{
					_tournamentKills.put(killedTeam, -1);
				}
				else
				{
					_tournamentKills.put(killedTeam, _tournamentKills.get(killedTeam) - 1);
				}
			}
		}
		//self kill
		else if (_bridgesDown)
		{
			if (!_tournamentKills.containsKey(killedTeam))
			{
				_tournamentKills.put(killedTeam, -1);
			}
			else
			{
				_tournamentKills.put(killedTeam, _tournamentKills.get(killedTeam) - 1);
			}
		}
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
		{
			return;
		}

		ArrayList<GameTeam> teamsAlive = new ArrayList<GameTeam>();

		for (GameTeam team : this.GetTeamList())
			if (team.GetPlayers(true).size() > 0)
			{
				teamsAlive.add(team);
			}

//		if (!QuitOut)
//		{
//			//Offline Player Team
//			for (GameTeam team : RejoinTeam.values())
//				teamsAlive.add(team);
//		}

		if (teamsAlive.size() <= 1)
		{
			//Announce Winner
			if (!_tournament)
			{
				if (teamsAlive.size() > 0)
				{
					AnnounceEnd(teamsAlive.get(0));
				}
			}
			else
			{
				GameTeam bestTeam = null;
				int bestKills = 0;

				for (GameTeam team : GetTeamList())
				{
					if (_tournamentKills.containsKey(team))
					{
						int kills = _tournamentKills.get(team);

						if (bestTeam == null || bestKills < kills)
						{
							bestTeam = team;
							bestKills = kills;
						}
					}
				}

				if (bestTeam != null)
				{
					AnnounceEnd(bestTeam);
				}
			}


			for (GameTeam team : GetTeamList())
			{
				if (WinnerTeam != null && team.equals(WinnerTeam))
				{
					for (Player player : team.GetPlayers(false))
						AddGems(player, 10, "Winning Team", false, false);
				}

				for (Player player : team.GetPlayers(false))
					if (player.isOnline())
					{
						AddGems(player, 10, "Participation", false, false);
					}
			}

			//End
			SetState(GameState.End);
		}
	}

	@Override
	public void HandleTimeout()
	{
		if (!_tournament)
		{
			SetState(GameState.End);
			return;
		}

		ArrayList<GameTeam> bestTeams = new ArrayList<GameTeam>();
		int bestKills = 0;

		for (GameTeam team : GetTeamList())
		{
			if (_tournamentKills.containsKey(team))
			{
				int kills = _tournamentKills.get(team);

				if (bestTeams == null || kills > bestKills)
				{
					bestTeams.clear();
					bestTeams.add(team);
					bestKills = kills;
				}

				else if (kills == bestKills)
				{
					bestTeams.add(team);
				}
			}
		}

		//Many Teams Alive
		if (bestTeams.size() != 1)
		{
			if (UtilTime.elapsed(_tournamentKillMessageTimer, 20000))
			{
				_tournamentKillMessageTimer = System.currentTimeMillis();

				Announce(C.cRed + C.Bold + "ALERT: " + C.Reset + C.Bold + "FIRST TEAM TO HAVE MOST KILLS WINS!");
			}
		}

		//Team Won
		else
		{
			AnnounceEnd(bestTeams.get(0));

			for (GameTeam team : GetTeamList())
			{
				if (WinnerTeam != null && team.equals(WinnerTeam))
				{
					for (Player player : team.GetPlayers(false))
						AddGems(player, 10, "Winning Team", false, false);
				}

				for (Player player : team.GetPlayers(false))
					if (player.isOnline())
					{
						AddGems(player, 10, "Participation", false, false);
					}
			}

			//End
			SetState(GameState.End);
		}
	}

	public boolean isBridgesDown()
	{
		return _bridgesDown;
	}

	@EventHandler
	public void CheatChestBreak(BlockBreakEvent event)
	{
		if (_bridgesDown)
		{
			return;
		}

		if (event.getBlock().getType() != Material.CHEST)
		{
			return;
		}

		if (!IsAlive(event.getPlayer()))
		{
			return;
		}

		for (Location loc : WorldData.GetCustomLocs("54"))
		{
			if (loc.getBlock().equals(event.getBlock()))
			{
				cheaterKill(event.getPlayer());
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void CheatChestBreak(BlockPlaceEvent event)
	{
		if (_bridgesDown)
		{
			return;
		}

		if (event.getBlock().getType() != Material.CHEST)
		{
			return;
		}

		if (!IsAlive(event.getPlayer()))
		{
			return;
		}

		for (Location loc : WorldData.GetCustomLocs("54"))
		{
			if (UtilMath.offset(loc, event.getBlock().getLocation()) < 2)
			{
				cheaterKill(event.getPlayer());
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void CheatChestInteract(PlayerInteractEvent event)
	{
		if (_bridgesDown)
		{
			return;
		}

		if (event.getClickedBlock() == null)
		{
			return;
		}

		if (event.getClickedBlock().getType() != Material.CHEST)
		{
			return;
		}

		if (!IsAlive(event.getPlayer()))
		{
			return;
		}

		for (Location loc : WorldData.GetCustomLocs("54"))
		{
			if (loc.getBlock().equals(event.getClickedBlock()))
			{
				cheaterKill(event.getPlayer());
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void PreBridgeDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		if (_bridgesDown || event.GetProjectile() != null)
		{
			return;
		}

		GameTeam damageeTeam = GetTeam(event.GetDamageePlayer());
		GameTeam damagerTeam = GetTeam(event.GetDamagerPlayer(false));

		if (damageeTeam == null || damagerTeam == null)
		{
			return;
		}

		if (damageeTeam.equals(damagerTeam))
		{
			return;
		}

		Player damagee = event.GetDamageePlayer();
		Player damager = event.GetDamagerPlayer(false);

		//Damagee is closer to Damagers Island
		if (UtilMath.offset(damagee.getLocation(), UtilWorld.averageLocation(damageeTeam.GetSpawns())) >
				UtilMath.offset(damagee.getLocation(), UtilWorld.averageLocation(damagerTeam.GetSpawns())))
		{
			cheaterKill(damagee);
		}

		//Damagee is closer to Damagees Island
		if (UtilMath.offset(damager.getLocation(), UtilWorld.averageLocation(damagerTeam.GetSpawns())) >
				UtilMath.offset(damager.getLocation(), UtilWorld.averageLocation(damageeTeam.GetSpawns())))
		{
			cheaterKill(damager);
		}
	}

	@EventHandler
	public void preventMinecarts(PlayerInteractEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (!IsAlive(event.getPlayer()))
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		if (event.getItem() == null)
		{
			return;
		}

		if (event.getItem().getType().toString().toLowerCase().contains("minecart"))
		{
			event.setCancelled(true);
			UtilInv.Update(event.getPlayer());
		}
	}

	@EventHandler
	public void preventMinecarts(EntitySpawnEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (event.getEntityType().toString().toLowerCase().contains("minecart"))
		{
			event.getEntity().remove();
		}
	}

	public void cheaterKill(Player player)
	{
		Announce(C.Bold + player.getName() + " was killed for cheating!");
		_usedLife.add(player.getName());
		player.damage(9999);
	}

	@EventHandler
	public void liquidFlow(BlockFromToEvent event)
	{
		if (!_bridgesDown)
		{
			if (!event.getToBlock().getRelative(BlockFace.UP).equals(event.getBlock()))
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void revivePlayer(CustomDamageEvent event)
	{
		if (!IsLive() || event.isCancelled() || _bridgesDown || !(event.GetDamageeEntity() instanceof Player))
		{
			return;
		}

		Player player = event.GetDamageePlayer();

		if (player.getHealth() - event.GetDamage() > 0)
		{
			return;
		}

		if (!_usedLife.contains(player.getName()))
		{
			_usedLife.add(player.getName());

			UtilPlayer.message(player, F.main("Game", "You used your " + F.elem(C.cAqua + "Early Game Revive") + "."));
			UtilServer.broadcast(F.main("Game", GetTeam(player).GetColor() + player.getName()) + C.cGray + " fell into the void.");

			player.setFallDistance(0);
			GetTeam(player).SpawnTeleport(player);
			player.setHealth(20);
			event.SetCancelled("Early Game Revive");
		}
	}

	@EventHandler
	public void vehicleDeny(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		if (UtilGear.isMat(event.getPlayer().getItemInHand(), Material.BOAT))
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game",
					"You cannot place boats."));

			event.setCancelled(true);
		}
	}

	@Override
	public double GetKillsGems(Player killer, Player killed, boolean assist)
	{
		if (assist)
		{
			return 3;
		}
		else
		{
			return 12;
		}
	}

	@EventHandler
	public void disableIceForm(BlockFormEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void disableWaterPickup(PlayerBucketFillEvent event)
	{
		if (_bridgesDown)
		{
			return;
		}
		UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot pickup liquids before the bridges have fallen."));
		event.setCancelled(true);
	}

	@EventHandler
	public void disableDoors(CraftItemEvent event)
	{
		if (_bridgesDown)
		{
			return;
		}

		Material type = event.getRecipe().getResult().getType();

		if (type == Material.WOOD_DOOR || type == Material.IRON_DOOR)
		{
			event.setResult(null);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void disableDoors(PlayerPickupItemEvent event)
	{
		if (_bridgesDown)
		{
			return;
		}

		Material type = event.getItem().getItemStack().getType();

		if (type == Material.WOOD_DOOR || type == Material.IRON_DOOR)
		{
			event.getItem().remove();
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void updateBorder(UpdateEvent event)
	{
		if (!IsLive() || event.getType() != UpdateType.FAST || _bridgesDown)
		{
			return;
		}

		for (GameTeam team : GetTeamList())
		{
			List<Location> border = WorldData.GetCustomLocs("BORDER " + team.getDisplayName());

			if (border.size() < 2)
			{
				continue;
			}

			Location one = border.get(0);
			Location two = border.get(1);
			Location average = _averageSpawns.computeIfAbsent(team, k -> UtilAlg.getAverageLocation(team.GetSpawns()));

			one.setY(0);
			two.setY(256);

			for (Player player : team.GetPlayers(true))
			{
				if (player.getGameMode() != GameMode.SURVIVAL)
				{
					continue;
				}

				Location location = player.getLocation();

				if (!UtilAlg.inBoundingBox(location, one, two))
				{
					player.setVelocity(UtilAlg.getTrajectory(location, average).multiply(2).add(new Vector(0, 0.5, 0)));
					player.sendMessage(C.cRedB + "STAY ON YOUR ISLAND!");
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 0.5F);
				}
			}
		}
	}

	@EventHandler
	public void blockOutsideBorder(BlockBreakEvent event)
	{
		Player player = event.getPlayer();

		if (_bridgesDown || player.getGameMode() != GameMode.SURVIVAL)
		{
			return;
		}

		GameTeam team = GetTeam(player);
		Block block = event.getBlock();

		if (team == null)
		{
			return;
		}

		List<Location> border = WorldData.GetCustomLocs("BORDER " + team.getDisplayName());

		if (border.size() < 2)
		{
			return;
		}

		Location one = border.get(0);
		Location two = border.get(1);

		if (!UtilAlg.inBoundingBox(block.getLocation(), one, two))
		{
			event.setCancelled(true);
			player.sendMessage(F.main("Game", "You cannot break blocks outside the border."));
		}
	}

	public void setBridgeTime(long time)
	{
		_bridgeTime = time;
	}

	public boolean hasUsedRevive(Player player)
	{
		return _usedLife.contains(player.getName());
	}

	public double getOreDensity()
	{
		return _oreDensity;
	}

	public ArrayList<ItemStack> getChestLoot()
	{
		return _chestLoot;
	}

	public HashSet<BridgePart> getBridgeParts()
	{
		return _bridgeParts;
	}

	public boolean bridgesDown()
	{
		return _bridgesDown;
	}

}
