package nautilus.game.arcade.game.games.battleroyale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.minestrike.GunModule;
import nautilus.game.arcade.game.games.minestrike.items.guns.GunStats;
import nautilus.game.arcade.game.modules.StrikeGamesModule;
import nautilus.game.arcade.game.modules.chest.ChestLootModule;
import nautilus.game.arcade.game.modules.chest.ChestLootPool;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;

public abstract class BattleRoyale extends Game
{

	private static final long PREPARE_TIME = TimeUnit.SECONDS.toMillis(30);
	private static final int MIN_CORD = 100;
	private static final int MAX_CORD = 1000;
	private static final int SPAWN_Y = 130;
	private static final int WORLD_SIZE_BUFFER = 300;
	private static final int MIN_DISTANCE_APART_FOR_SPAWNS_SQUARED = 100;
	private static final long MIN_DRAGON_TIME = TimeUnit.SECONDS.toMillis(5);
	private static final long MAX_DRAGON_TIME = TimeUnit.SECONDS.toMillis(60);
	private static final long BORDER_TIME = TimeUnit.MINUTES.toSeconds(20);
	protected static final long SUPPLY_DROP_TIME = TimeUnit.MINUTES.toMillis(5);
	private static final int MAX_DROPS_PER_GAME = 3;

	private static final ItemStack SMALL_BACKPACK = new ItemBuilder(Material.CHEST)
			.setTitle(C.cGreen + "Small Backpack")
			.addLore("Clicking this will unlock a new row in your inventory!")
			.build();
	private static final ItemStack LARGE_BACKPACK = new ItemBuilder(Material.ENDER_CHEST)
			.setTitle(C.cGreen + "Large Backpack")
			.addLore("Clicking this will unlock two new rows in your inventory!")
			.build();

	private static final ItemStack SMALL_HEALTH_POT = new ItemBuilder(Material.POTION)
			.setTitle(C.cGreen + "Small Health Pot")
			.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 0))
			.build();

	private static final ItemStack LARGE_HEALTH_POT = new ItemBuilder(Material.POTION)
			.setTitle(C.cGreen + "Large Health Pot")
			.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 1))
			.build();

	protected final Map<Player, BattleRoyalePlayer> _playerData = new HashMap<>(70);

	protected GunModule _gunModule;

	protected WorldBorder _border;
	private boolean _colouredMessage;

	protected BattleRoyaleSupplyDrop _supplyDrop;
	protected long _lastSupplyDrop;
	private int _totalDrops;

	public BattleRoyale(ArcadeManager manager, GameType gameType, Kit[] kits, String[] gameDesc)
	{
		super(manager, gameType, kits, gameDesc);

		PrepareTime = PREPARE_TIME;
		PrepareFreeze = false;
		SpawnTeleport = false;
		Damage = false;
		DamageTeamSelf = true;
		DeathDropItems = true;
		QuitDropItems = true;
		HungerSet = 20;
		DeathTeleport = false;
		WorldChunkUnload = true;
		GameTimeout = TimeUnit.MINUTES.toMillis(40);

		ItemDrop = true;
		ItemPickup = true;

		StrictAntiHack = true;
		InventoryClick = true;
		InventoryOpenBlock = true;
		InventoryOpenChest = true;

		BlockBreakAllow.add(Material.GLASS.getId());
		BlockBreakAllow.add(Material.STAINED_GLASS.getId());
		BlockBreakAllow.add(Material.THIN_GLASS.getId());
		BlockBreakAllow.add(Material.STAINED_GLASS_PANE.getId());
		BlockBreakAllow.add(Material.LEAVES.getId());

		_gunModule = new GunModule(this);
		_gunModule.EnableCleaning = false;
		_gunModule.EnableDrop = false;
		_gunModule.EnablePickup = false;
		_gunModule.BlockRegeneration = false;
		_gunModule.EnableNormalArmor = true;

		new StrikeGamesModule(_gunModule)
				.register(this);

		new CompassModule()
				.register(this);

		manager.GetCreature().SetDisableCustomDrops(true);
	}

	@Override
	public void ParseData()
	{
		List<Location> chestSpawns = new ArrayList<>(500);
		chestSpawns.addAll(WorldData.GetDataLocs("ORANGE"));
		chestSpawns.addAll(WorldData.GetDataLocs("GREEN"));
		chestSpawns.addAll(WorldData.GetDataLocs("YELLOW"));
		chestSpawns.addAll(WorldData.GetDataLocs("BLUE"));

		new ChestLootModule()
				.destroyAfterOpened(20)
				.spawnNearbyDataPoints()
				.registerChestType("Standard", chestSpawns,

						// Guns
						new ChestLootPool()
								.addItem(buildFromGun(GunStats.GLOCK_18))
								.addItem(buildFromGun(GunStats.CZ75))
								.addItem(buildFromGun(GunStats.DEAGLE))
								.addItem(buildFromGun(GunStats.P250))
								.addItem(buildFromGun(GunStats.P2000))
								.addItem(buildFromGun(GunStats.P90), 50)
								.addItem(buildFromGun(GunStats.PPBIZON), 50)
								.addItem(buildFromGun(GunStats.GALIL), 20)
								.addItem(buildFromGun(GunStats.FAMAS), 20)
								.addItem(buildFromGun(GunStats.AK47), 20)
								.addItem(buildFromGun(GunStats.M4A4), 20)
								.addItem(buildFromGun(GunStats.SG553), 20)
								.addItem(buildFromGun(GunStats.AUG), 20)
								.addItem(buildFromGun(GunStats.SSG08), 20)
								.addItem(buildFromGun(GunStats.NOVA), 20)
								.addItem(buildFromGun(GunStats.XM1014), 20)
								.setProbability(0.5)
						,

						// Grenades
						new ChestLootPool()
								.addItem(buildGrenade(Material.CARROT_ITEM, "Flash Bang"))
								.addItem(buildGrenade(Material.APPLE, "High Explosive"))
								.addItem(buildGrenade(Material.POTATO_ITEM, "Smoke"))
								.addItem(buildGrenade(Material.PORK, "Incendiary"), 50)
								.addItem(buildGrenade(Material.GRILLED_PORK, "Molotov"), 50)
								.setProbability(0.2)
						,

						// Weapons
						new ChestLootPool()
								.addItem(new ItemStack(Material.IRON_SWORD))
								.addItem(new ItemStack(Material.IRON_AXE))
								.addItem(new ItemStack(Material.BOW))
								.setProbability(0.1)
						,

						// Ammo
						new ChestLootPool()
								.addItem(new ItemStack(Material.ARROW), 1, 8)
								.setProbability(0.2)
								.setAmountsPerChest(1, 3)
						,

						// Medical
						new ChestLootPool()
								.addItem(SMALL_HEALTH_POT)
								.addItem(LARGE_HEALTH_POT)
								.setProbability(0.1)
								.setAmountsPerChest(1, 2)
						,

						// Armour
						new ChestLootPool()
								.addItem(new ItemBuilder(Material.LEATHER_HELMET)
										.setTitle(C.cRed + "Red Baseball Cap")
										.setColor(Color.RED)
										.build())
								.addItem(new ItemBuilder(Material.LEATHER_HELMET)
										.setTitle(C.cAqua + "Blue Baseball Cap")
										.setColor(Color.BLUE)
										.build())
								.addItem(new ItemBuilder(Material.CHAINMAIL_HELMET)
										.setTitle(C.cDGreen + "Tactical Helmet")
										.build())
								.addItem(new ItemBuilder(Material.IRON_HELMET)
										.setTitle(C.cDGreen + "Motorcycle Helmet")
										.build())
								.addItem(new ItemBuilder(Material.LEATHER_CHESTPLATE)
										.setTitle(C.cDGreen + "Wooden Body Armour")
										.build())
								.addItem(new ItemBuilder(Material.CHAINMAIL_CHESTPLATE)
										.setTitle(C.cDGreen + "Plated Body Armour")
										.build())
								.addItem(new ItemBuilder(Material.IRON_CHESTPLATE)
										.setTitle(C.cDGreen + "Laminated Tactical Body Armour")
										.build())
								.addItem(new ItemStack(Material.LEATHER_LEGGINGS))
								.addItem(new ItemStack(Material.LEATHER_BOOTS))
						,

						// Food
						new ChestLootPool()
								.addItem(new ItemStack(Material.MELON), 1, 3)
								.addItem(new ItemStack(Material.BREAD), 1, 2, 50)
								.addItem(new ItemStack(Material.COOKED_FISH), 50)
								.addItem(new ItemStack(Material.COOKED_BEEF), 50)
								.addItem(new ItemStack(Material.COOKED_CHICKEN), 50)
								.addItem(new ItemStack(Material.COOKED_MUTTON), 50)
								.addItem(new ItemStack(Material.COOKIE), 50)
						,

						// Misc
						new ChestLootPool()
								.addItem(SMALL_BACKPACK, 50)
								.addItem(LARGE_BACKPACK, 20)
								.setProbability(0.2)
				)
				.registerChestType("Supply Drop", new ArrayList<>(0),

						// Guns
						new ChestLootPool()
								.addItem(buildFromGun(GunStats.AUG))
								.addItem(buildFromGun(GunStats.AK47))
								.addItem(buildFromGun(GunStats.M4A4))
								.addItem(buildFromGun(GunStats.XM1014))
								.addItem(buildFromGun(GunStats.AWP))
								.setAmountsPerChest(1, 2)
						,
						// Backpack
						new ChestLootPool()
								.addItem(SMALL_BACKPACK)
								.addItem(LARGE_BACKPACK)
						,

						// Armour
						new ChestLootPool()
								.addItem(new ItemBuilder(Material.IRON_HELMET)
										.setTitle(C.cDGreen + "Motorcycle Helmet")
										.build())
								.addItem(new ItemBuilder(Material.IRON_CHESTPLATE)
										.setTitle(C.cDGreen + "Laminated Tactical Body Armour")
										.build())
						,

						// Grenades
						new ChestLootPool()
								.addItem(buildGrenade(Material.CARROT_ITEM, "Flash Bang"))
								.addItem(buildGrenade(Material.APPLE, "High Explosive"))
								.addItem(buildGrenade(Material.POTATO_ITEM, "Smoke"))
								.addItem(buildGrenade(Material.PORK, "Incendiary"), 50)
								.addItem(buildGrenade(Material.GRILLED_PORK, "Molotov"), 50)
								.setAmountsPerChest(1, 2)
						,

						// Medical
						new ChestLootPool()
								.addItem(SMALL_HEALTH_POT)
								.addItem(LARGE_HEALTH_POT)
								.setAmountsPerChest(1, 2)

						)
				.register(this);

		WorldData.MinX = -MAX_CORD;
		WorldData.MinZ = -MAX_CORD;
		WorldData.MaxX = MAX_CORD;
		WorldData.MaxZ = MAX_CORD;

		_border = WorldData.World.getWorldBorder();
	}

	private ItemStack buildFromGun(GunStats gunStats)
	{
		return new ItemBuilder(gunStats.getSkin())
				.setTitle(C.cWhiteB + gunStats.getName())
				.build();
	}

	private ItemStack buildGrenade(Material material, String name)
	{
		return new ItemBuilder(material)
				.setTitle(C.cDGreenB + name)
				.build();
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		_border.setCenter(getRandomCenter());
		_border.setSize(MAX_CORD * 2);

		List<Player> toTeleport = GetPlayers(true);
		AtomicInteger index = new AtomicInteger();

		Manager.runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (index.get() >= toTeleport.size())
				{
					cancel();
					return;
				}

				Player player = toTeleport.get(index.getAndIncrement());

				if (player == null || !player.isOnline())
				{
					return;
				}

				Location spawn = null;
				int attempts = 0;
				int initialXZ = 0;

				while (spawn == null && attempts++ < 20)
				{
					if (attempts > 10)
					{
						initialXZ += 20;
					}

					spawn = getPlayerSpawn(initialXZ);
				}

				// Couldn't create a spawn, this should never happen and is pretty much impossible
				if (spawn == null)
				{
					cancel();
					SetState(GameState.Dead);
					return;
				}

				Location goal = spawn.clone();

				goal.setX(-spawn.getX());
				goal.setZ(-spawn.getZ());

				BattleRoyalePlayer royalePlayer = new BattleRoyalePlayer(Manager, player, spawn, goal);
				_playerData.put(player, royalePlayer);
			}
		}, 40, 2);
	}

	private Location getPlayerSpawn(int initialXZ)
	{
		// Calculate where a player should spawn
		int max = MAX_CORD - WORLD_SIZE_BUFFER;
		int x = initialXZ;
		int z = initialXZ;
		boolean varyX = UtilMath.random.nextBoolean();
		boolean sign = UtilMath.random.nextBoolean();

		if (varyX)
		{
			x += UtilMath.rRange(-max, max);
			z += sign ? max : -max;
		}
		else
		{
			x += sign ? max : -max;
			z += UtilMath.rRange(-max, max);
		}

		Location location = new Location(WorldData.World, x, SPAWN_Y, z);

		// Check to make sure no players are nearby
		for (BattleRoyalePlayer other : _playerData.values())
		{
			if (UtilMath.offsetSquared(location, other.getLocation()) < MIN_DISTANCE_APART_FOR_SPAWNS_SQUARED)
			{
				return null;
			}
		}

		location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, GetSpectatorLocation())));
		return location;
	}

	private Location getRandomCenter()
	{
		int attempts = 0;

		while (attempts++ < 20)
		{
			Location location = UtilAlg.getRandomLocation(GetSpectatorLocation(), WORLD_SIZE_BUFFER, 0, WORLD_SIZE_BUFFER);
			Block block = location.getBlock();

			while (!UtilBlock.solid(block))
			{
				block = block.getRelative(BlockFace.DOWN);
			}

			if (block.isLiquid())
			{
				continue;
			}

			return location;
		}

		return SpectatorSpawn;
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		_lastSupplyDrop = System.currentTimeMillis();

		CreatureAllowOverride = true;

		ItemStack locked = new ItemBuilder(Material.STAINED_GLASS_PANE, (byte) 15)
				.setTitle(C.cGray + "Locked")
				.build();

		_playerData.forEach((player, battleRoyalePlayer) ->
		{
			battleRoyalePlayer.removeCage();
			battleRoyalePlayer.spawnDragon();

			for (int i = 18; i < player.getInventory().getSize(); i++)
			{
				player.getInventory().setItem(i, locked);
			}
		});

		CreatureAllowOverride = false;
	}

	@EventHandler
	public void updateDragons(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !IsLive())
		{
			return;
		}

		_colouredMessage = !_colouredMessage;

		Iterator<Player> iterator = _playerData.keySet().iterator();

		while (iterator.hasNext())
		{
			Player player = iterator.next();
			BattleRoyalePlayer royalePlayer = _playerData.get(player);

			if (royalePlayer == null || !player.isOnline())
			{
				iterator.remove();
				continue;
			}

			EnderDragon dragon = royalePlayer.getDragon();
			Chicken chicken = royalePlayer.getChicken();

			if (dragon == null || !dragon.isValid() || chicken == null || !chicken.isValid())
			{
				continue;
			}

			UtilTextBottom.display((_colouredMessage ? C.cGreenB : C.cWhiteB) + "PRESS YOUR SNEAK KEY TO DISMOUNT YOUR DRAGON", player);
			if (dragon.getPassenger() == null || chicken.getPassenger() == null)
			{
				if (!UtilTime.elapsed(GetStateTime(), MIN_DRAGON_TIME))
				{
					player.sendMessage(F.main("Game", "Did you accidentally press sneak? It's too soon to jump! Don't worry I'll put you back on your dragon."));
					dragon.setPassenger(chicken);
					chicken.setPassenger(player);
					continue;
				}

				dismountDragon(player, royalePlayer);
			}
		}

		if (!Damage && UtilTime.elapsed(GetStateTime(), MAX_DRAGON_TIME))
		{
			_playerData.forEach(this::dismountDragon);

			Announce(C.cRedB + "Grace Period Over!", false);

			for (Player player : Bukkit.getOnlinePlayers())
			{
				player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
			}

			Damage = true;
			HungerSet = -1;
			_border.setSize(MIN_CORD, BORDER_TIME);
		}
	}

	private void dismountDragon(Player player, BattleRoyalePlayer royalePlayer)
	{
		if (!royalePlayer.getDragon().isValid())
		{
			return;
		}

		// Recharge this so that players won't take fall damage for the next 10 seconds
		Recharge.Instance.useForce(player, "Fall Damage", TimeUnit.SECONDS.toMillis(10));
		player.playSound(player.getLocation(), Sound.BLAZE_DEATH, 1, 0.6F);
		UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, player.getLocation(), 5, 5, 5, 0.01F, 100, ViewDist.NORMAL);
		royalePlayer.getDragon().remove();
		royalePlayer.getChicken().remove();
	}

	@EventHandler
	public void fallDamage(CustomDamageEvent event)
	{
		Player player = event.GetDamageePlayer();

		if (player == null || event.GetCause() != DamageCause.FALL || Recharge.Instance.usable(player, "Fall Damage"))
		{
			return;
		}

		event.SetCancelled("Dragon Fall");
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void preventDragonExplosion(EntityExplodeEvent event)
	{
		if (event.getEntity() instanceof EnderDragon)
		{
			event.blockList().clear();
		}
	}

	@Override
	public void disable()
	{
		super.disable();

		_playerData.clear();
		if (_supplyDrop != null)
		{
			_supplyDrop.cleanup();
		}
	}

	@EventHandler
	public void updateSupplyDrop(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !IsLive())
		{
			return;
		}

		if (_totalDrops < MAX_DROPS_PER_GAME && UtilTime.elapsed(_lastSupplyDrop, SUPPLY_DROP_TIME))
		{
			_lastSupplyDrop = System.currentTimeMillis();

			List<Location> locations = WorldData.GetDataLocs("RED");
			Location location = null;
			int attempts = 0;

			while (location == null || attempts++ < 20)
			{
				location = UtilAlg.Random(locations);

				if (UtilWorld.inWorldBorder(location))
				{
					break;
				}
			}

			_supplyDrop = new BattleRoyaleSupplyDrop(this, location);
			_totalDrops++;
			Announce(C.cGoldB + "A New Supply Drop Will Spawn At " + C.cYellow + UtilWorld.locToStrClean(_supplyDrop.getDropLocation()) + C.cGold + "!");
		}
		else if (_supplyDrop != null && _supplyDrop.isOpened())
		{
			_supplyDrop = null;
		}
	}

	@EventHandler
	public void playerDeath(CombatDeathEvent event)
	{
		Player player = (Player) event.GetEvent().getEntity();
		CombatComponent killer = event.GetLog().GetKiller();

		if (killer.IsPlayer())
		{
			Player killerPlayer = UtilPlayer.searchExact(killer.getUniqueIdOfEntity());

			if (killerPlayer != null)
			{
				BattleRoyalePlayer royalePlayer = _playerData.get(killerPlayer);

				if (royalePlayer != null)
				{
					royalePlayer.incrementKills();
					UtilTextBottom.display(C.cRedB + royalePlayer.getKills() + " Kill" + (royalePlayer.getKills() == 1 ? "" : "s"));
				}
			}
		}

		List<CombatComponent> attackers = event.GetLog().GetAttackers();

		for (CombatComponent attacker : attackers)
		{
			if (!attacker.IsPlayer() || killer.equals(attacker))
			{
				continue;
			}

			Player attackerPlayer = UtilPlayer.searchExact(attacker.getUniqueIdOfEntity());

			if (attackerPlayer == null)
			{
				continue;
			}

			BattleRoyalePlayer royalePlayer = _playerData.get(attackerPlayer);

			if (royalePlayer != null)
			{
				attackerPlayer.sendMessage(F.main("Game", "You assisted in killing " + F.name(player.getName()) + "."));
				royalePlayer.incrementAssists();
			}
		}
	}

	@EventHandler
	public void preventLockedInventoryClick(InventoryClickEvent event)
	{
		Player player = (Player) event.getWhoClicked();
		ItemStack itemStack = event.getCurrentItem();

		if (event.getClickedInventory() == null || itemStack == null)
		{
			return;
		}

		if (itemStack.getType() == Material.STAINED_GLASS_PANE)
		{
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 0.6F);
		}
	}

	@EventHandler
	public void clickBackpack(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		ItemStack itemStack = event.getItem();

		if (itemStack == null)
		{
			return;
		}
		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		int slots = 0;

		if (itemStack.isSimilar(SMALL_BACKPACK))
		{
			slots = 9;
		}
		else if (itemStack.isSimilar(LARGE_BACKPACK))
		{
			slots = 18;
		}

		if (slots == 0)
		{
			return;
		}

		ItemStack[] items = player.getInventory().getContents();
		int removed = 0;

		for (int i = 0; i < items.length && removed < slots; i++)
		{
			ItemStack inventoryItem = items[i];

			if (inventoryItem != null && inventoryItem.getType() == Material.STAINED_GLASS_PANE)
			{
				player.getInventory().setItem(i, null);
				removed++;
			}
		}
		
		if (itemStack.getAmount() > 1)
		{
			itemStack.setAmount(itemStack.getAmount() - 1);
		}
		else
		{
			player.getInventory().setItemInHand(null);
		}
		player.updateInventory();
		player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
		player.sendMessage(F.main("Game", "You unlocked an additional " + F.elem(removed) + " slots in your inventory."));
	}

	@EventHandler
	public void noHungerRegeneration(EntityRegainHealthEvent event)
	{
		if (event.getRegainReason() == RegainReason.SATIATED)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void inventoryOpen(InventoryOpenEvent event)
	{
		Inventory inventory = event.getInventory();

		if (inventory instanceof EnchantingInventory || inventory instanceof AnvilInventory || inventory instanceof FurnaceInventory)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void damageToLevel(CustomDamageEvent event)
	{
		event.SetDamageToLevel(false);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_playerData.remove(event.getPlayer());
	}

	@EventHandler
	public void removeEmptyPotions(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			player.getInventory().remove(Material.GLASS_BOTTLE);
		}
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		event.getDrops().removeIf(itemStack -> itemStack.getType() == Material.STAINED_GLASS_PANE);

		Player player = event.getEntity();
		awardTimeGems(player);
	}
	
	@EventHandler
	public void onIceMelt(BlockFadeEvent event)
	{
		if (!event.getBlock().getWorld().equals(WorldData.World))
		{
			return;
		}
		if (event.getBlock().getType() == Material.ICE)
		{
			event.setCancelled(true);
		}
	}

	protected void awardTimeGems(Player player)
	{
		long timeAlive = Math.min(System.currentTimeMillis() - GetStateTime(), TimeUnit.MINUTES.toMillis(30));

		// i.e 1 gem per 10 seconds alive
		AddGems(player, timeAlive / TimeUnit.SECONDS.toMillis(10), "Surviving " + UtilTime.MakeStr(timeAlive), false, false);
	}

	@Override
	public double GetKillsGems(Player killer, Player killed, boolean assist)
	{
		if (assist)
		{
			return 50;
		}

		return _border.getSize() == MIN_CORD ? 200 : 100;
	}
}