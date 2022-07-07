package nautilus.game.arcade.game.games.skyfall;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.mineplex.anticheat.checks.move.Glide;
import com.mineplex.anticheat.checks.move.HeadRoll;
import com.mineplex.anticheat.checks.move.Speed;

import mineplex.core.Managers;
import mineplex.core.antihack.AntiHack;
import mineplex.core.common.MinecraftVersion;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.loot.ChestLoot;
import mineplex.core.recharge.Recharge;
import mineplex.core.titles.tracks.standard.LuckyTrack;
import mineplex.core.titles.tracks.standard.UnluckyTrack;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.ChestRefillEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.skyfall.kits.KitAeronaught;
import nautilus.game.arcade.game.games.skyfall.kits.KitBooster;
import nautilus.game.arcade.game.games.skyfall.kits.KitDeadeye;
import nautilus.game.arcade.game.games.skyfall.kits.KitJouster;
import nautilus.game.arcade.game.games.skyfall.kits.KitSpeeder;
import nautilus.game.arcade.game.games.skyfall.kits.KitStunner;
import nautilus.game.arcade.game.games.skyfall.stats.AeronaughtStatTracker;
import nautilus.game.arcade.game.games.skyfall.stats.RingStatTracker;
import nautilus.game.arcade.game.modules.VersionModule;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.stats.KillsWithinTimeLimitStatTracker;
import nautilus.game.arcade.stats.WinWithoutWearingArmorStatTracker;
import net.md_5.bungee.api.ChatColor;

/**
 * GameObject of the game Skyfall
 *
 * @author xXVevzZXx
 */
public abstract class Skyfall extends Game
{
	private static final long MAP_CRUMBLE_DELAY = 1000*20; // 2 Minutes
	private static final long RING_CRUMBLE_DELAY = 1000*60*2; // 2 Minutes
	private static final long CHEST_REFILL_TIME = 1000*60*3; // 3 minutes
	private static final long CHEST_REFILL_ANNOUNCE_TIME = 1000*60*3; // 3 minutes
	private static final long ELYTRA_TAKEAWAY = 1000;
	private static final int ROT_START = 256;
	private static final int ROT_Y_OFFSET = 25;
	
	private static final long ISLAND_ROT_TIME = 1000*60*5; // 5 Minutes
	
	private static final int RING_CRUMBLE_RATE = 10;
	
	private static final float RING_BOOST_STRENGTH = 3.25F;
	private static final float BIG_RING_BOOST_STRENGTH = 4.3F;
	
	private static final long BOOSTER_COOLDOWN_TIME = 1000*20; // 20 Seconds
	
	private static final long SUPPLY_DROP_TIME = 1000*60*5; // 5 Minutes
	private static final long DEATHMATCH_START_TIME = 1000*30; // 30 Seconds
	private static final long DEATHMATCH_WAIT_TIME = 1000*10; // 10 Seconds
	
	private static final int TNT_EXPLOSION_RADIUS = 14;

	private static final long EAT_RECHARGE = 500; // 0.5 Second
	
	private int _islandCrumbleRate;
	
	private int _bigIslandBounds;
	private int _bigIslandHeight;
	
	private Island _upperIsland;
	private Island _lowerIsland;
	
	private HashMap<Island, TreeMap<Island, Integer>> _islands;
	private HashMap<Entity, Player> _tntMap;
	private ArrayList<BoosterRing> _boosterRings;
	private HashMap<UUID, Long> _disabledElytras;
	
	private HashSet<UUID> _currentlyEating;

	private boolean _crumbleAnnounced;
	
	private long _chestsRefilled;
	
	private Location _supplyDrop;
	private Location _supplyEffect;
	private boolean _supplyDropActive;
	private boolean _supplyDropOver;
	
	private ArrayList<Location> _deathMatchSpawns;
	private boolean _deathmatch;
	private boolean _deathMatchStarted;
	private boolean _teleportedDeathmatch;
	private long _deathMatchStartTime;
	
	private boolean _refillAnnounced;
	
	private boolean _supplyOpened;
	
	private double _rotY;
	
	//private int _ringCrumbleRate;
	
	public Skyfall(ArcadeManager manager, GameType type) 
	{
		super(manager, type,

				new Kit[]
						{
							new KitSpeeder(manager),
							new KitBooster(manager),
							new KitJouster(manager),
							new KitStunner(manager),
							//new KitSurefoot(manager),
							new KitAeronaught(manager),
							new KitDeadeye(manager)
						},

						new String[]
								{
				"Fly with your Elytra",
				"Try to land on Islands",
				"Get your gear from chests"
								});
		


		registerStatTrackers(new WinWithoutWearingArmorStatTracker(this),
				new KillsWithinTimeLimitStatTracker(this, 3, 60, "Bloodlust"),
				new RingStatTracker(this),
				new AeronaughtStatTracker(this));

		registerChatStats(
				Kills,
				Assists,
				BlankLine,
				DamageTaken,
				DamageDealt,
				BlankLine
		);
		
		registerModule(new VersionModule(MinecraftVersion.Version1_9));

		// Disable specific GWEN checks for this game
		AntiHack antiHack = Managers.get(AntiHack.class);
		antiHack.addIgnoredCheck(Speed.class);
		antiHack.addIgnoredCheck(Glide.class);
		antiHack.addIgnoredCheck(HeadRoll.class);

		_islands = new HashMap<>();
		_boosterRings = new ArrayList<>();
		_tntMap = new HashMap<>();
		_disabledElytras = new HashMap<>();
		_currentlyEating = new HashSet<>();
		
		PrepareFreeze = true;		
		AnnounceStay = false;
		DeathDropItems = true;
		QuitDropItems = true;
		DamageSelf = true;
		DamageTeamSelf = true;
		DamageEvP = true;
		Damage = true;
		DeathDropItems = true;
		ItemDrop = true;
		ItemPickup = true;
		InventoryClick = true;
		InventoryOpenBlock = true;
		InventoryOpenChest = true;
		DamageFall = false;
		SoupEnabled = true;

		new CompassModule()
		.setGiveCompassToAlive(true)
		.register(this);
		
		SpeedMeasurement = true;
		
		_bigIslandBounds = 25;
		_bigIslandHeight = 15;
		
		_rotY = ROT_START;
	}
	
	@EventHandler
	public void testCommands(PlayerCommandPreprocessEvent event)
	{	
		if(GetState() != GameState.Live)
			return;
		
		if (!UtilServer.isTestServer())
			return;
		
		if(event.getMessage().contains("/Rate"))
		{
			int rate = Integer.parseInt(event.getMessage().split(" ")[1]);
			_islandCrumbleRate = rate;
			UtilPlayer.message(event.getPlayer(), "Crumble rate changed to " + rate);
			event.setCancelled(true);
			return;
		}
		
		if(event.getMessage().contains("/Rot"))
		{
			UtilPlayer.message(event.getPlayer(), "Current Rot value " + _rotY);
			event.setCancelled(true);
			return;
		}
		
		if(event.getMessage().contains("/Boost"))
		{
			float rate = Float.parseFloat(event.getMessage().split(" ")[1]);
			for (BoosterRing ring : _boosterRings)
			{
				ring.setBoostStrength(rate);
			}
			UtilPlayer.message(event.getPlayer(), "Boost changed to " + rate);
			event.setCancelled(true);
			return;
		}
		
	}
	
	@EventHandler
	public void gameStart(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Live)
		{
			_chestsRefilled = System.currentTimeMillis();
			
			for (Location spawn : GetTeamList().get(0).GetSpawns())
			{
				spawn.clone().subtract(0, 1, 0).getBlock().setType(Material.AIR);
			}
			
			for (Player player : GetPlayers(true))
			{
				ItemStack stack = new ItemStack(Material.COMPASS);
				
				ItemMeta itemMeta = stack.getItemMeta();
				itemMeta.setDisplayName(C.cGreen + C.Bold + "Tracking Compass");
				stack.setItemMeta(itemMeta);
				
				player.getInventory().addItem(stack);
			}
		}
	}
	
	@EventHandler
	public void chestRefill(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!IsLive())
			return;
		
		if (!UtilTime.elapsed(_chestsRefilled, CHEST_REFILL_TIME))
			return;
		
		if (_deathmatch)
			return;
		
		_chestsRefilled = System.currentTimeMillis();
		_refillAnnounced = false;
		
		UtilServer.CallEvent(new ChestRefillEvent(_lowerIsland.getChests()));
		
		_lowerIsland.refillChests();
			
		Announce(ChatColor.AQUA + "" + ChatColor.BOLD + "Chests on the lower middle Island have been refilled!", true);
	}
	
	@EventHandler
	public void chestRefillAnnounce(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		if (!IsLive())
			return;

		if (!UtilTime.elapsed(_chestsRefilled, CHEST_REFILL_ANNOUNCE_TIME))
			return;
		
		if (_refillAnnounced)
			return;
		
		Announce(C.cGold + C.Bold + "The chests will be refilled in "
				+ UtilTime.MakeStr(CHEST_REFILL_TIME - CHEST_REFILL_ANNOUNCE_TIME), false);
		
		_refillAnnounced = true;
	}
	
	@EventHandler
	public void dontRemoveElytra(InventoryClickEvent event)
	{
		if (event.getCurrentItem() == null)
			return;
		
		if (event.getCurrentItem().getType() == Material.ELYTRA)
			event.setCancelled(true);
	}
	
	@EventHandler
	public void mapCrumble(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;
		
		if (!IsLive())
			return;
		
		if (!UtilTime.elapsed(GetStateTime(), MAP_CRUMBLE_DELAY))
			return;
		
		if (!_crumbleAnnounced)
		{
			Announce(C.cGreenB + "As time passes, the world begins to rot...", true);
			_crumbleAnnounced = true;
		}
		
		for (Island island : islandCrumble())
		{
			for (Player player : GetPlayers(true))
			{
				if (!island.isOnIsland(player))
					continue;
				
				if (UtilPlayer.isGliding(player))
					continue;
				
				Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.CUSTOM, 1, false, false, true, "Island Rot", "Island Rot");
			}
		}
		
		for (BoosterRing ring : _boosterRings)
		{
			for (Player player : GetPlayers(true))
			{
				if (!ring.isCrumbledAway())
					continue;
				
				if (UtilPlayer.isGliding(player))
					continue;
				
				if (UtilMath.offset(player.getLocation(), ring.getMiddle()) > (ring.getSize()/2))
					continue;
				
				Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.CUSTOM, 1, false, false, true, "Island Rot", "Island Rot");
			}
		}
	}
	
	@EventHandler
	public void lowerRot(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC_05)
			return;
		
		if (!IsLive())
			return;
		
		if (_rotY <= (0 - ROT_Y_OFFSET))
			return;
		
		long startTime = GetStateTime() + MAP_CRUMBLE_DELAY;
		//System.out.println("starttime " + startTime);
		double current = System.currentTimeMillis() - startTime;
		//System.out.println("current " + current);
		
		double percentage = current/((double) ISLAND_ROT_TIME);
		//System.out.println("precentage " + percentage);
		double value = ROT_START * percentage;
		//System.out.println("value " + value);
		
		_rotY = (ROT_START - value);
	}
	
	public ArrayList<Island> islandCrumble()
	{
		ArrayList<Island> islands = new ArrayList<>();
		
		
		for (Island island : _islands.get(_upperIsland).keySet())
		{
			if (island.isCrumbledAway())
				islands.add(island);
			
			if (island.getLocation().getBlockY() < (_rotY + ROT_Y_OFFSET))
				continue;
				
			island.crumble(_islandCrumbleRate, Material.COAL_BLOCK, Material.ENDER_STONE);
		}
		
		
		if (_upperIsland.isCrumbledAway())
			islands.add(_upperIsland);
		

		if (_upperIsland.getLocation().getBlockY() > (_rotY + ROT_Y_OFFSET))
		{
			if (_upperIsland.crumble(_islandCrumbleRate, Material.COAL_BLOCK, Material.ENDER_STONE))
			{
				while (!_upperIsland.getBoosterRing().isCrumbledAway())
				{
					_upperIsland.getBoosterRing().crumble(RING_CRUMBLE_RATE, Material.COAL_BLOCK, Material.ENDER_STONE);
				}
			}
		}
		
		
		for (Island island : _islands.get(_lowerIsland).keySet())
		{
			if (island.isCrumbledAway())
				islands.add(island);
			
			if (island.getLocation().getBlockY() < (_rotY + ROT_Y_OFFSET))
				continue;
			
			island.crumble(_islandCrumbleRate, Material.COAL_BLOCK, Material.ENDER_STONE);
		}
		
		
		if (_lowerIsland.isCrumbledAway())
			islands.add(_lowerIsland);
		
		if (_lowerIsland.getLocation().getBlockY() > (_rotY + ROT_Y_OFFSET))
		{
			if (_lowerIsland.crumble(_islandCrumbleRate, Material.COAL_BLOCK, Material.ENDER_STONE))
			{
				while (!_lowerIsland.getBoosterRing().isCrumbledAway())
				{
					_lowerIsland.getBoosterRing().crumble(RING_CRUMBLE_RATE, Material.COAL_BLOCK, Material.ENDER_STONE);
				}
			}
		}
		return islands;
	}
	
	@EventHandler
	public void disableAC(PlayerTeleportEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getCause() == TeleportCause.UNKNOWN)
			event.setCancelled(true);	
	}
	
	//@EventHandler
	public void deathMatch(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		if (!IsLive())
			return;
		
		if (_deathmatch)
		{
			if (!UtilTime.elapsed(_deathMatchStartTime, DEATHMATCH_START_TIME))
				return;
			
			if (_teleportedDeathmatch)
			{
				
				if (UtilTime.elapsed(_deathMatchStartTime, DEATHMATCH_START_TIME + DEATHMATCH_WAIT_TIME))
				{

					if (_deathMatchStarted)
						return;
					
					_deathMatchStarted = true;

					Announce(C.cRed + C.Bold + "Deathmatch has begun!", false);
					return;
				}	
				
				if (!_deathMatchStarted)
				{
					long time = (_deathMatchStartTime + DEATHMATCH_START_TIME + DEATHMATCH_WAIT_TIME) - System.currentTimeMillis();
					int real = Math.round(time/1000) + 1;
					
					Announce(C.cRed + C.Bold + "Deathmatch is starting in " + real + "...", false);
				}
			}
			else
			{
				_teleportedDeathmatch = true;
				deathMatch();
			}
			return;
		}
		else
		{
			if (GetPlayers(true).size() <= 4)
			{
				_deathmatch = true;
				_deathMatchStartTime = System.currentTimeMillis();

				Announce(C.cRed + C.Bold + "The Deathmatch is starting in " + DEATHMATCH_START_TIME / 1000 + " Seconds!", false);
			}
		}
	}
	
	public void deathMatch()
	{
		for (Player player : GetPlayers(true))
		{
			Location loc = UtilAlg.getLocationNearPlayers(_deathMatchSpawns, GetPlayers(true), GetPlayers(true));
			player.teleport(loc);
		}
	}
	
	@EventHandler
	public void ringCrumble(UpdateEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (BoosterRing ring : _boosterRings)
		{
			if (ring == _upperIsland.getBoosterRing() || ring == _lowerIsland.getBoosterRing())
				continue;
			
			if (!UtilTime.elapsed(GetStateTime(), RING_CRUMBLE_DELAY))
				return;
			
			if (ring.getMiddle().getBlockY() > (_upperIsland.getLocation().getBlockY() - (_upperIsland.getHeight()*2)))
			{
				if (!ring.crumble(RING_CRUMBLE_RATE, Material.COAL_BLOCK, Material.ENDER_STONE))
					break;
			}

			if (!UtilTime.elapsed(GetStateTime(), (RING_CRUMBLE_DELAY*2)))
				continue;
			
			if (ring.getMiddle().getBlockY() > (_lowerIsland.getLocation().getBlockY() - (_lowerIsland.getHeight()*2)))
			{
				if (!ring.crumble(RING_CRUMBLE_RATE, Material.COAL_BLOCK, Material.ENDER_STONE))
					break;
			}
			
			if (!UtilTime.elapsed(GetStateTime(), (RING_CRUMBLE_DELAY*3)))
				continue;
			
			if (!ring.crumble(RING_CRUMBLE_RATE, Material.COAL_BLOCK, Material.ENDER_STONE))
				break;
			
		}
	}
	
	@EventHandler
	public void playerHunger(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;
		
		for (Player player : GetPlayers(true))
		{
			if (!UtilPlayer.isGliding(player))
				continue;
			
			if (player.getFoodLevel() > 0)
				player.setFoodLevel(player.getFoodLevel() - 1);
			
		}
	}
	
	@EventHandler
	public void supplyDrop(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;
		
		if (!IsLive())
			return;
		
		if (!UtilTime.elapsed(GetStateTime(), SUPPLY_DROP_TIME))
			return;

		if (_supplyDropOver)
			return;
		
		if (!_supplyDropActive)
		{
			Announce(C.cYellow + C.Bold + "Supply Drop Incoming");
			_supplyEffect = _supplyDrop.clone();
			_supplyEffect.setY(250);
			_supplyDrop.getBlock().getRelative(BlockFace.DOWN).setType(Material.GLASS);
			_supplyDrop.getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).setType(Material.BEACON);
			for (int x = -1; x <= 1; x++)
				for (int z = -1; z <= 1; z++)
					_supplyDrop.getBlock().getRelative(x, -3, z)
							.setType(Material.IRON_BLOCK);
			
			_supplyDropActive = true;
		}

		FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).with(Type.BURST).trail(false).build();
		UtilFirework.playFirework(_supplyEffect, effect);

		_supplyEffect.setY(_supplyEffect.getY() - 2);

		if (UtilMath.offset(_supplyEffect, _supplyDrop) < 2)
		{
			effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).with(Type.BALL_LARGE).trail(true).build();
			UtilFirework.playFirework(_supplyEffect, effect);

			_supplyDrop.getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).setType(Material.GLASS);
			
			Block block = _supplyDrop.getBlock();
			block.setType(Material.CHEST);
			
			Chest chest = (Chest) block.getState();
			ChestLoot loot = LootTable.SUPPLY_DROP.getloot();
			ArrayList<Material> exclude = new ArrayList<>();
			for (int i = 0; i < UtilMath.rRange(3, 5); i++)
			{	
				int slot = UtilMath.r(26);
				ItemStack item = loot.getLoot(exclude);
				Inventory inventory = chest.getInventory();
				inventory.setItem(slot, item);
				if (item.getType() == Material.BOW)
				{
					inventory.setItem(slot + 1, new ItemStack(Material.ARROW, UtilMath.r(6) + 1));
				}
				if (UtilItem.isHelmet(item))
				{
					exclude.add(Material.DIAMOND_HELMET);
				}
				if (UtilItem.isChestplate(item))
				{
					exclude.add(Material.DIAMOND_CHESTPLATE);	
				}
				if (UtilItem.isLeggings(item))
				{
					exclude.add(Material.DIAMOND_LEGGINGS);
				}
				if (UtilItem.isBoots(item))
				{
					exclude.add(Material.DIAMOND_BOOTS);
				}
				if (UtilItem.isSword(item))
				{
					exclude.add(Material.DIAMOND_SWORD);
				}
				if (UtilItem.isAxe(item))
				{
					exclude.add(Material.DIAMOND_AXE);
				}
				if (item.getType() == Material.BOW)
					exclude.add(Material.BOW);
			}
			_supplyDropOver = true;
		}
	}
	
	@EventHandler
	public void lootChest(PlayerInteractEvent event)
	{
		if (!IsLive())
			return;
		
		if (!IsAlive(event.getPlayer()))
			return;
		
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		if (event.getClickedBlock().getType() != Material.CHEST
				&& event.getClickedBlock().getType() != Material.TRAPPED_CHEST)
			return;
			
		if (event.getClickedBlock().getLocation().getBlockX() == _supplyDrop.getBlockX()
				&& event.getClickedBlock().getLocation().getBlockY() == _supplyDrop.getBlockY()
				&& event.getClickedBlock().getLocation().getBlockZ() == _supplyDrop.getBlockZ())
		{
			if (!_supplyOpened)
			{
				AddStat(event.getPlayer(), "SupplyDropsOpened", 1, false, false);
				AddGems(event.getPlayer(), 15, "Supply Drop", false, false);
				_supplyOpened = true;
			}
			return;
		}
		
		Island island = getCurrentIsland(event.getPlayer());
		if (island == null)
		{
			_upperIsland.fillLoot(event.getClickedBlock());
			return;
		}
		
		island.fillLoot(event.getClickedBlock());
		
		if (event.getPlayer() != null && Manager != null && Manager.GetServerConfig().RewardStats)
		{
			Manager.getTrackManager().getTrack(LuckyTrack.class).handleLoot(event.getPlayer(), ((Chest) event.getClickedBlock().getState()).getBlockInventory());
			Manager.getTrackManager().getTrack(UnluckyTrack.class).handleLoot(event.getPlayer(), ((Chest) event.getClickedBlock().getState()).getBlockInventory());
		}
	}
	
	// I have no clue why, but this is fixing all food issues
	@EventHandler
	public void foodFix(PlayerInteractEvent event)
	{
		if (!IsLive())
			return;
		
		if (!UtilEvent.isAction(event, ActionType.R))
			return;
		
		Player player = event.getPlayer();
		
		if (player.getItemInHand() == null)
			return;
		
		ItemStack stack = player.getItemInHand();
		
		if (!UtilItem.isFood(stack))
			return;
		
		if (stack.getType() == Material.MUSHROOM_SOUP)
			return;
		
		if (player.getFoodLevel() >= 20)
			return;
		
		if (!Recharge.Instance.usable(player, "eating"))
			return;
		
		if (_currentlyEating.contains(player.getUniqueId()))
			return;
		
		_currentlyEating.add(player.getUniqueId());	
		//player.setWalkSpeed(0.1F);
		
		Manager.runSyncLater(new Runnable()
		{
			@Override
			public void run()
			{
				//player.setWalkSpeed(0.2F);
				
				if (stack.getAmount() <= 0)
					return;
				
				Recharge.Instance.use(player, "eating", EAT_RECHARGE, false, false);
					
				_currentlyEating.remove(player.getUniqueId());
				//stack.setAmount(stack.getAmount() - 1);
				
				int heal = 4;
				if (stack.getType() == Material.BAKED_POTATO 
						|| stack.getType() == Material.BREAD
						|| stack.getType() == Material.COOKED_FISH)
					heal = 5;
				
				if (stack.getType() == Material.COOKED_BEEF
						|| stack.getType() == Material.GRILLED_PORK
						|| stack.getType() == Material.PUMPKIN_PIE)
					heal = 8;
				
				//player.setFoodLevel(player.getFoodLevel() + heal);
				//player.getWorld().playSound(player.getEyeLocation(), Sound.BURP, 100, 1);
			}	
		}, 40);
	}
	
	@Override
	public void ParseData()
	{
		WorldData.MaxY = 1000;
		
		_supplyDrop = WorldData.GetDataLocs("PINK").get(0);
		_deathMatchSpawns = (ArrayList<Location>) WorldData.GetDataLocs("BROWN").clone();
		
		for (String name : WorldData.GetAllCustomLocs().keySet())
		{
			if (name.split(" ")[0].equalsIgnoreCase("BIG_HEIGHT"))
			{
				_bigIslandHeight = Integer.parseInt(name.split(" ")[1]);
			}
			else if (name.split(" ")[0].equalsIgnoreCase("BIG_BOUNDS"))
			{
				_bigIslandBounds = Integer.parseInt(name.split(" ")[1]);
			}
		}
		
		_upperIsland = new Island(WorldData.GetDataLocs("GREEN").get(0), LootTable.BASIC, _bigIslandBounds, _bigIslandHeight);
		_lowerIsland = new Island(WorldData.GetDataLocs("YELLOW").get(0), LootTable.BASIC, _bigIslandBounds, _bigIslandHeight);
		
		registerIslands();
		registerBoosters();
		
		((CraftWorld) WorldData.World).getHandle().spigotConfig.playerTrackingRange = 250;
	}
	
	@EventHandler
	public void ringBoost(PlayerBoostRingEvent event)
	{	
		if (IsAlive(event.getPlayer()))
			event.getRing().disable(BOOSTER_COOLDOWN_TIME, Material.STAINED_CLAY, (byte) 14, true);
	}
	
	public void registerBoosters()
	{
		ArrayList<Location> boosters = WorldData.GetDataLocs("ORANGE");
		for (Location boosterMid : boosters)
		{
			BoosterRing ring = new BoosterRing(this, boosterMid, RING_BOOST_STRENGTH);
			
			if (_upperIsland.isOnIsland(boosterMid))
			{
				_upperIsland.setBoosterRing(ring);
				ring.setBoostStrength(BIG_RING_BOOST_STRENGTH);
			}
			
			if (_lowerIsland.isOnIsland(boosterMid))
			{
				_lowerIsland.setBoosterRing(ring);
				ring.setBoostStrength(BIG_RING_BOOST_STRENGTH);
			}
			
			_boosterRings.add(ring);
		}
	}
	
	public void registerIslands()
	{
		
		HashMap<Island, Integer> upperIslandMap = new HashMap<>();
		HashMap<Island, Integer> lowerIslandMap = new HashMap<>();

		
		for (String string : WorldData.GetAllDataLocs().keySet())
		{
			if (string.equalsIgnoreCase("ORANGE") || 
					string.equalsIgnoreCase("PINK") || 
					string.equalsIgnoreCase("BROWN") || 
					string.equalsIgnoreCase("GREEN") || 
					string.equalsIgnoreCase("YELLOW"))
				continue;
			
			ArrayList<Location> islands = WorldData.GetDataLocs(string);
			
			int islandHeight = _bigIslandHeight;
			int islandBounds = _bigIslandBounds;
			String loot = "BASIC";
			
			for (String name : WorldData.GetAllCustomLocs().keySet())
			{
				if (name.split(" ")[0].equalsIgnoreCase(string))
				{
					if (name.split(" ")[1].equalsIgnoreCase("H"))
					{
						islandHeight = Integer.parseInt(name.split(" ")[2]);
					}
					if (name.split(" ")[1].equalsIgnoreCase("B"))
					{
						islandBounds = Integer.parseInt(name.split(" ")[2]);
					}
					if (name.split(" ")[1].equalsIgnoreCase("L"))
					{
						islandBounds = Integer.parseInt(name.split(" ")[2]);
					}
				}
			}
			
			for (Location islandMid : islands)
			{
				try
				{
					if (islandMid.getBlockY() >= _lowerIsland.getLocation().getBlockY())
					{
						upperIslandMap.put(new Island(islandMid, (LootTable) LootTable.class.getField(loot).get(null), islandBounds, islandHeight), islandMid.getBlockY());
					}
					else
					{
						lowerIslandMap.put(new Island(islandMid, (LootTable) LootTable.class.getField(loot).get(null), islandBounds, islandHeight), islandMid.getBlockY());
					}
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		IslandSorter upperSorter = new IslandSorter(upperIslandMap);
		IslandSorter lowerSorter = new IslandSorter(lowerIslandMap);
		
		_islands.put(_upperIsland, new TreeMap<>(upperSorter));
		_islands.get(_upperIsland).putAll(upperIslandMap);

		_islands.put(_lowerIsland, new TreeMap<>(lowerSorter));
		_islands.get(_lowerIsland).putAll(lowerIslandMap);

		int blocks = 0;

		for (Island island : _islands.get(_upperIsland).keySet())
			blocks += island.getRealBlocks().size();
		
		for (Island island : _islands.get(_lowerIsland).keySet())
			blocks += island.getRealBlocks().size();
		
		blocks += _upperIsland.getRealBlocks().size();
		blocks += _lowerIsland.getRealBlocks().size();
		
		
		int ticks = (int) (((ISLAND_ROT_TIME / 1000) *20) / 3);
		_islandCrumbleRate = blocks / ticks;
		
		if (_islandCrumbleRate < 1)
			_islandCrumbleRate = 1;
		
		_islandCrumbleRate = _islandCrumbleRate * 3;
	}
	
	public Island getCurrentIsland(Player player)
	{
		for (Island island : _islands.keySet())
		{
			if (island.isOnIsland(player))
				return island;
			
			for (Island subIsland : _islands.get(island).keySet())
			{
				if (subIsland.isOnIsland(player))
					return subIsland;
			}
		}
		return null;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void elytraDrop(PlayerDeathEvent event)
	{
		Iterator<ItemStack> itemIterator = event.getDrops().iterator();
		while (itemIterator.hasNext())
		{
			ItemStack item = itemIterator.next();
			if (item.getType() == Material.ELYTRA)
			{
				itemIterator.remove();
			}
		}
	}
	
	@EventHandler
	public void deathmatchBowShoot(EntityShootBowEvent event)
	{
		if (!_teleportedDeathmatch)
			return;

		if (_deathMatchStarted)
			return;

		event.getProjectile().remove();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void deathmatchDamage(CustomDamageEvent event)
	{
		if (!_teleportedDeathmatch)
			return;

		if (_deathMatchStarted)
			return;

		event.SetCancelled("Deathmatch");
	}

	@EventHandler
	public void deathmatchMoveCancel(PlayerMoveEvent event)
	{
		if (!_teleportedDeathmatch)
			return;

		if (_deathMatchStarted)
			return;

		if (UtilMath.offset2d(event.getFrom(), event.getTo()) == 0)
			return;

		if (!IsAlive(event.getPlayer()))
			return;

		event.setTo(event.getFrom());
	}
	
	@EventHandler
	public void supplyGlow(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;
		
		Block block = _supplyDrop.getBlock();

		if (block.getType() != Material.CHEST)
		{
			return;
		}

		if (_supplyOpened)
			return;
		
		UtilParticle.PlayParticle(ParticleType.SPELL, block.getLocation()
						.add(0.5, 0.5, 0.5), 0.3f, 0.3f, 0.3f, 0, 1, ViewDist.LONG,
				UtilServer.getPlayers());
	}
	
	@EventHandler
	public void sendWarning(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Player player : UtilServer.getPlayers())
		{
			if (!IsLive())
				UtilPlayer.removeWorldBorder(player);
			
			if (!IsAlive(player))
				UtilPlayer.removeWorldBorder(player);
				
			if (player.getInventory().getChestplate() == null)
				continue;
			
			UtilPlayer.removeWorldBorder(player);
		}
		
		if (!IsLive())
			return;
		
		for (Player player : GetPlayers(true))
		{
			if (player.getInventory().getChestplate() != null)
				continue;
			
			if (!UtilPlayer.hasWorldBorder(player))
				UtilPlayer.sendRedScreen(player, 100000);
		}
	}

	@EventHandler
	public void TNTDelay(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
			return;

		for (Player player : UtilServer.getPlayers())
			Recharge.Instance.useForce(player, "Throw TNT", 30000);
	}

	@EventHandler
	public void TNTExplosion(ExplosionPrimeEvent event)
	{		
		if (!_tntMap.containsKey(event.getEntity()))
			return;

		Player player = _tntMap.remove(event.getEntity());

		for (Player other : UtilPlayer.getNearby(event.getEntity().getLocation(), TNT_EXPLOSION_RADIUS))
		{
			Manager.GetCondition().Factory().Explosion("Throwing TNT", other, player, 50, 0.1, false, false);
			Manager.GetDamage().NewDamageEvent(other, player, null, DamageCause.ENTITY_EXPLOSION, 6, true, true, true, player.getName(), "Throwing TNT");
		}
		
		event.getEntity().getLocation().getWorld().playEffect(event.getEntity().getLocation(), Effect.EXPLOSION_LARGE, 100);
		event.getEntity().getLocation().getWorld().playSound(event.getEntity().getLocation(), Sound.EXPLODE, 100, 1);
		
		event.setCancelled(true);
	}

	@EventHandler
	public void TNTThrow(PlayerInteractEvent event)
	{
		if (!IsLive())
			return;

		if (!UtilEvent.isAction(event, ActionType.L))
			return;

		Player player = event.getPlayer();

		if (!UtilInv.IsItem(player.getItemInHand(), Material.TNT, (byte) 0))
			return;

		if (!IsAlive(player))
			return;

		event.setCancelled(true);

		if (!Recharge.Instance.use(player, "Throw TNT", 0, true, false))
		{
			UtilPlayer.message(
					event.getPlayer(),
					F.main(GetName(), "You cannot use " + F.item("Throw TNT")
							+ " yet."));
			return;
		}

		if (!Manager.GetGame().CanThrowTNT(player.getLocation()))
		{
			// Inform
			UtilPlayer.message(
					event.getPlayer(),
					F.main(GetName(), "You cannot use " + F.item("Throw TNT")
							+ " here."));
			return;
		}

		UtilInv.remove(player, Material.TNT, (byte) 0, 1);
		UtilInv.Update(player);

		TNTPrimed tnt = player.getWorld().spawn(
				player.getEyeLocation()
						.add(player.getLocation().getDirection()),
				TNTPrimed.class);

		tnt.setFuseTicks(60);

		UtilAction.velocity(tnt, player.getLocation().getDirection(), 0.5,
				false, 0, 0.1, 10, false);

		_tntMap.put(tnt, player);
	}
	
	@EventHandler
	public void removeElytraLava(CustomDamageEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.GetCause() != DamageCause.LAVA)
			return;

		Recharge.Instance.useForce(event.GetDamageePlayer(), "Elytra Removal", ELYTRA_TAKEAWAY, true);
		_disabledElytras.put(event.GetDamageePlayer().getUniqueId(), System.currentTimeMillis() + ELYTRA_TAKEAWAY);
	}
	
	@EventHandler
	public void removeElytraWater(UpdateEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Player player : GetPlayers(true))
		{
			if (player.getLocation().getBlock().getTypeId() == 8 || player.getLocation().getBlock().getRelative(BlockFace.UP).getTypeId() == 8 || 
				player.getLocation().getBlock().getTypeId() == 9 || player.getLocation().getBlock().getRelative(BlockFace.UP).getTypeId() == 9)
			{
				Recharge.Instance.useForce(player, "Elytra Removal", ELYTRA_TAKEAWAY, true);
				_disabledElytras.put(player.getUniqueId(), System.currentTimeMillis() + ELYTRA_TAKEAWAY);
			}
		}
	}
	
	@EventHandler
	public void updateElytras(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (Manager.GetGame() == null)
			return;
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (_disabledElytras.containsKey(player.getUniqueId()))
			{
				if (System.currentTimeMillis() > _disabledElytras.get(player.getUniqueId()))
				{
					player.getInventory().setChestplate(new ItemStack(Material.ELYTRA));
				}
				else
				{
					if (player.getInventory().getChestplate() != null)
					{
						UtilPlayer.message(player, F.main("Game", C.cRed + "Your Elytra is disabled!"));
					}
					player.getInventory().setChestplate(null);
				}
			}
		}
	}
	
	@EventHandler
	public void updateSpecs(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (!IsLive())
			return;
		
		for (Player player : UtilServer.getPlayers())
		{
			if (!IsAlive(player))
				player.getInventory().setChestplate(new ItemStack(Material.ELYTRA));
		}
	}

	@EventHandler
	public void craftedItems(CraftItemEvent event)
	{
		if (UtilItem.isWeapon(event.getCurrentItem()) || UtilItem.isArmor(event.getCurrentItem()))
		{
			UtilItem.makeUnbreakable(event.getCurrentItem());
		}
	}
	
	public boolean isDeathMatch()
	{
		return _deathmatch;
	}
	
	public boolean isDeathMatchStarted()
	{
		return _deathMatchStarted;
	}
	
	public boolean isTeleportedDeathmatch()
	{
		return _teleportedDeathmatch;
	}
	
	public long getDeathmatchStartTime()
	{
		return _deathMatchStartTime;
	}
	
	public long getChestsRefilled()
	{
		return _chestsRefilled;
	}
	
	public long getChestRefillTime()
	{
		return CHEST_REFILL_TIME;
	}
	
	public long getDeathmatchStartingTime()
	{
		return DEATHMATCH_START_TIME;
	}
	
	public long getDeathmatchWaitTime()
	{
		return DEATHMATCH_WAIT_TIME;
	}
	
	private class IslandSorter implements Comparator<Island>
	{
		private HashMap<Island, Integer> _map;

		public IslandSorter(HashMap<Island, Integer> map)
		{
			_map = map;
		}
		
		@Override
		public int compare(Island a, Island b)
		{
			return _map.get(a) >= _map.get(b) ? -1 : 1;
		}
		
	}

}