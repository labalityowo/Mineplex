package nautilus.game.arcade.game.games.skywars;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.explosion.ExplosionEvent;
import mineplex.core.itemstack.EnchantedBookBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.skywars.kits.KitAir;
import nautilus.game.arcade.game.games.skywars.kits.KitEarth;
import nautilus.game.arcade.game.games.skywars.kits.KitFire;
import nautilus.game.arcade.game.games.skywars.kits.KitIce;
import nautilus.game.arcade.game.games.skywars.kits.KitMetal;
import nautilus.game.arcade.game.games.skywars.module.ZombieGuardianModule;
import nautilus.game.arcade.game.modules.EXPForKillsModule;
import nautilus.game.arcade.game.modules.EnderPearlModule;
import nautilus.game.arcade.game.modules.MapCrumbleModule;
import nautilus.game.arcade.game.modules.ThrowableTNTModule;
import nautilus.game.arcade.game.modules.chest.ChestLootModule;
import nautilus.game.arcade.game.modules.chest.ChestLootPool;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.game.modules.generator.Generator;
import nautilus.game.arcade.game.modules.generator.GeneratorModule;
import nautilus.game.arcade.game.modules.generator.GeneratorType;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.ore.OreHider;

@SuppressWarnings("deprecation")
public abstract class Skywars extends Game
{
	// ./parse 19 30 56

	private static final long CRUMBLE_TIME = TimeUnit.SECONDS.toMillis(100);

	private GeneratorModule _generatorModule;
	private final OreHider _oreHider;

	public Skywars(ArcadeManager manager, GameType type, String[] description)
	{
		this(manager, new Kit[]
				{
						new KitIce(manager),
						new KitFire(manager),
						new KitAir(manager),
						new KitMetal(manager),
						new KitEarth(manager),
				}, type, description);
	}

	public Skywars(ArcadeManager manager, Kit[] kits, GameType type, String[] description)
	{
		super(manager, type, kits, description);

		PrepareFreeze = true;

		AnnounceStay = false;

		HideTeamSheep = true;

		GameTimeout = TimeUnit.MINUTES.toMillis(20);

		DeathDropItems = true;

		QuitDropItems = true;

		WorldTimeSet = 0;
		WorldBoundaryKill = true;

		DamageSelf = true;
		DamageTeamSelf = true;
		DamageEvP = true;
		Damage = true;

		DeathDropItems = true;

		ItemDrop = true;
		ItemPickup = true;

		BlockBreak = true;
		BlockPlace = true;

		InventoryClick = true;
		InventoryOpenBlock = true;
		InventoryOpenChest = true;

		PlaySoundGameStart = true;
		PrepareTime = TimeUnit.SECONDS.toMillis(10);

		StrictAntiHack = true;

		_oreHider = new OreHider();

		new CompassModule()
				.setGiveCompassToAlive(true)
				.register(this);

		new EnderPearlModule()
				.register(this);

		new EXPForKillsModule()
				.register(this);

		manager.GetCreature().SetDisableCustomDrops(true);
	}

	@Override
	public void ParseData()
	{
		new MapCrumbleModule()
				.setEnableAfter(CRUMBLE_TIME, () ->
				{
					Announce(C.cGreenB + "The world begins to crumble...", false);

					for (Player player : UtilServer.getPlayersCollection())
					{
						player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
					}
				})
				.register(this);

		new ZombieGuardianModule()
				.addSpawns(WorldData.GetDataLocs("RED"))
				.register(this);

		ThrowableTNTModule tntModule = new ThrowableTNTModule()
				.setThrowAndDrop(true)
				.setThrowStrength(1.6);
		tntModule.register(this);

		ItemStack tntItem = tntModule.getTntItem().clone();
		tntItem.setAmount(2);

		_generatorModule = new GeneratorModule()
				.addGenerator(new Generator
						(
								new GeneratorType(tntItem, TimeUnit.SECONDS.toMillis(30), "Throwable TNT", ChatColor.RED, Color.RED, true),
								WorldData.GetDataLocs("LIME").get(0)
						));
		_generatorModule.register(this);

		for (Location oreLoc : WorldData.GetCustomLocs("56"))
		{
			oreLoc.getBlock().setType(Material.STONE);
		}

		// Remove Sponge (Holds up Sand)
		for (Location loc : WorldData.GetCustomLocs("19"))
		{
			MapUtil.QuickChangeBlockAt(loc, Material.AIR);
		}

		setupLoot();
	}

	private void setupLoot()
	{
		new ChestLootModule()
				.setPreGenerateLoot(true)
				.registerChestType("Island", WorldData.GetDataLocs("BROWN"),

						new ChestLootPool()
								.addItem(new ItemStack(Material.GOLD_HELMET))
								.addItem(new ItemStack(Material.GOLD_CHESTPLATE))
								.addItem(new ItemStack(Material.GOLD_LEGGINGS))
								.addItem(new ItemStack(Material.GOLD_BOOTS))
								.setAmountsPerChest(2, 3)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.STONE_SWORD))
								.addEnchantment(Enchantment.DAMAGE_ALL, 1)
								.setEnchantmentRarity(0.5)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.STONE_AXE))
								.addItem(new ItemStack(Material.STONE_SPADE))
								.setProbability(0.8)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.BOW))
								.setProbability(0.15)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.COOKED_BEEF), 1, 3)
								.addItem(new ItemStack(Material.COOKED_CHICKEN), 1, 3)
								.addItem(new ItemStack(Material.COOKED_FISH), 1, 3)
								.setAmountsPerChest(1, 2)
								.setProbability(0.8)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.WOOD), 16, 32)
								.addItem(new ItemStack(Material.COBBLESTONE), 16, 32)
								.setProbability(0.9)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.SNOW_BALL), 2, 5)
								.addItem(new ItemStack(Material.EGG), 2, 5)
								.setProbability(0.4)

				)
				.registerChestType("Connector", WorldData.GetDataLocs("GRAY"),

						new ChestLootPool()
								.addItem(new ItemStack(Material.CHAINMAIL_HELMET))
								.addItem(new ItemStack(Material.CHAINMAIL_CHESTPLATE))
								.addItem(new ItemStack(Material.CHAINMAIL_LEGGINGS))
								.addItem(new ItemStack(Material.CHAINMAIL_BOOTS))
								.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
								.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 1)
								.setEnchantmentRarity(0.6)
								.setAmountsPerChest(1, 2)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.STONE_SWORD))
								.addEnchantment(Enchantment.DAMAGE_ALL, 1)
								.setProbability(0.8)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.IRON_AXE))
								.addItem(new ItemStack(Material.IRON_PICKAXE))
								.addItem(new ItemStack(Material.FISHING_ROD))
								.setProbability(0.7)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.BOW))
								.setProbability(0.25)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.COOKED_BEEF), 1, 3)
								.addItem(new ItemStack(Material.COOKED_CHICKEN), 1, 3)
								.addItem(new ItemStack(Material.COOKED_FISH), 1, 3)
								.setAmountsPerChest(1, 2)
								.setProbability(0.8)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.WOOD), 16, 32)
								.addItem(new ItemStack(Material.COBBLESTONE), 16, 32)
								.addItem(new ItemStack(Material.GLASS), 16, 32)
								.setProbability(0.9)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.ARROW), 4, 8, 150)
								.addItem(new ItemStack(Material.SNOW_BALL), 2, 5)
								.addItem(new ItemStack(Material.EGG), 2, 5)
								.addItem(new ItemStack(Material.LAVA_BUCKET), 80)
								.addItem(new ItemStack(Material.WATER_BUCKET))
								.addItem(new ItemStack(Material.ENDER_PEARL), 1, 2)
								.setAmountsPerChest(1, 2)
						,

						new ChestLootPool()
								.addItem(createEnchantedBook(Enchantment.DAMAGE_ALL))
								.addItem(createEnchantedBook(Enchantment.KNOCKBACK))
								.addItem(createEnchantedBook(Enchantment.ARROW_DAMAGE))
								.addItem(createEnchantedBook(Enchantment.ARROW_KNOCKBACK))
								.addItem(createEnchantedBook(Enchantment.PROTECTION_ENVIRONMENTAL))
								.setProbability(0.25)

				)
				.registerChestType("Middle", WorldData.GetDataLocs("BLACK"),

						new ChestLootPool()
								.addItem(new ItemStack(Material.IRON_HELMET))
								.addItem(new ItemStack(Material.IRON_CHESTPLATE))
								.addItem(new ItemStack(Material.IRON_LEGGINGS))
								.addItem(new ItemStack(Material.IRON_BOOTS))
								.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
								.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 2)
								.setAmountsPerChest(1, 2)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.IRON_SWORD))
								.addEnchantment(Enchantment.DAMAGE_ALL, 1)
								.addEnchantment(Enchantment.KNOCKBACK, 1)
								.setProbability(0.4)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.DIAMOND_SWORD), 30)
								.addItem(new ItemStack(Material.DIAMOND), 1, 3)
								.setProbability(0.4)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.IRON_AXE))
								.addItem(new ItemStack(Material.IRON_PICKAXE))
								.addItem(new ItemStack(Material.FISHING_ROD))
								.setProbability(0.8)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.BOW))
								.setProbability(0.35)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.COOKED_BEEF), 1, 3)
								.addItem(new ItemStack(Material.COOKED_CHICKEN), 1, 3)
								.addItem(new ItemStack(Material.COOKED_FISH), 1, 3)
								.setAmountsPerChest(1, 2)
								.setProbability(0.8)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.WOOD), 16, 32)
								.addItem(new ItemStack(Material.COBBLESTONE), 16, 32)
								.addItem(new ItemStack(Material.GLASS), 16, 32)
								.setProbability(0.9)
						,

						new ChestLootPool()
								.addItem(new ItemStack(Material.EXP_BOTTLE), 5, 10, 130)
								.addItem(new ItemStack(Material.ARROW), 6, 10, 160)
								.addItem(new ItemStack(Material.SNOW_BALL), 2, 5)
								.addItem(new ItemStack(Material.EGG), 2, 5)
								.addItem(new ItemStack(Material.LAVA_BUCKET), 60)
								.addItem(new ItemStack(Material.WATER_BUCKET), 130)
								.addItem(new ItemStack(Material.ENDER_PEARL), 1, 2)
								.addItem(new ItemStack(Material.MUSHROOM_SOUP), 130)
								.setAmountsPerChest(2, 3)
						,

						new ChestLootPool()
								.addItem(createEnchantedBook(Enchantment.DAMAGE_ALL))
								.addItem(createEnchantedBook(Enchantment.KNOCKBACK))
								.addItem(createEnchantedBook(Enchantment.ARROW_DAMAGE))
								.addItem(createEnchantedBook(Enchantment.ARROW_KNOCKBACK))
								.addItem(createEnchantedBook(Enchantment.PROTECTION_ENVIRONMENTAL))
								.setProbability(0.5)

				)
				.register(this);
	}

	private ItemStack createEnchantedBook(Enchantment enchantment)
	{
		return new EnchantedBookBuilder(1)
				.setLevel(enchantment, 1)
				.build();
	}

	void writeScoreboard()
	{
		if (IsLive())
		{
			long timeUtilTNT = _generatorModule.getGenerators().get(0).getTimeUtilSpawn();

			Scoreboard.writeNewLine();

			Scoreboard.write(C.cGreenB + "Throwable TNT");

			if (timeUtilTNT > 0)
			{
				Scoreboard.write(UtilTime.MakeStr(timeUtilTNT));
			}
			else
			{
				Scoreboard.write("Lootable");
			}
		}
	}

	@EventHandler
	public void informLeapCooldown(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		String message = F.main("Game", "Your " + F.skill("Leap") + " is on a " + F.time("20 second") + " cooldown.");

		for (Player player : GetPlayers(true))
		{
			Kit kit = GetKit(player);

			if (kit instanceof KitAir)
			{
				player.sendMessage(message);
				Recharge.Instance.use(player, "Leap", 20000, true, false);
			}
		}
	}

	@EventHandler
	public void onGameStateChangeCreateOres(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		// Store which chests are closest to which spawn
		NautHashMap<Location, ArrayList<Location>> islandOres = new NautHashMap<>();

		// Allocate chests to their nearest spawn point
		for (Location oreLoc : WorldData.GetCustomLocs("56"))
		{
			for (GameTeam team : GetTeamList()) // This handles Team Skywars
			{
				// Gets the spawn point closest to the current
				Location closestOre = UtilAlg.findClosest(oreLoc, team.GetSpawns());

				if (UtilMath.offset2d(oreLoc, closestOre) > 8)
					continue;

				// Ensure the list exists
				if (!islandOres.containsKey(closestOre))
					islandOres.put(closestOre, new ArrayList<Location>());

				// Add this chest location to the spawn
				islandOres.get(closestOre).add(oreLoc);
			}
		}

		// Vein Counts
		int diamondVeins = 3;
		int ironVeins = 7;
		int gravelVeins = 4;

		// Create Ores
		for (ArrayList<Location> ores : islandOres.values())
		{
			for (int i = 0; i < diamondVeins; i++)
				createVein(ores, Material.DIAMOND_ORE, 2, true);

			for (int i = 0; i < ironVeins; i++)
				createVein(ores, Material.IRON_ORE, 3 + UtilMath.r(3), true);

			for (int i = 0; i < gravelVeins; i++)
				createVein(ores, Material.GRAVEL, 3 + UtilMath.r(3), false);
		}
	}

	private void createVein(ArrayList<Location> ores, Material type, int veinSize, boolean allowAboveAir)
	{
		if (ores.isEmpty())
			return;

		// First
		Location ore = UtilAlg.Random(ores);

		// Create Vein
		for (int i = 0; i < veinSize && !ores.isEmpty(); i++)
		{
			if (allowAboveAir || ore.getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR)
				_oreHider.AddOre(ore, type);

			ores.remove(ore);

			ore = UtilAlg.findClosest(ore, ores);
		}
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

	@EventHandler(priority = EventPriority.HIGH)
	public void blockPlace(BlockPlaceEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		Player player = event.getPlayer();
		Block block = event.getBlock();
		Material material = event.getBlock().getType();

		if (block.getLocation().getY() >= WorldData.MaxY - 3)
		{
			player.sendMessage(F.main("Game", "You cannot build this high."));
			event.setCancelled(true);
		}
		else if (material == Material.CHEST || material == Material.PISTON_BASE || material == Material.PISTON_STICKY_BASE || material == Material.HOPPER)
		{
			player.sendMessage(F.main("Game", "You cannot place this block."));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void blockBreakBonusDrops(BlockBreakEvent event)
	{
		event.setExpToDrop(0);

		Block block = event.getBlock();
		ItemStack toDrop = null;

		switch (block.getType())
		{
			case WEB:
				toDrop = new ItemStack(Material.STRING, 1 + UtilMath.r(2));
				break;
			case GRAVEL:
				toDrop = new ItemStack(Material.FLINT, 1 + UtilMath.r(3));
				break;
			case IRON_ORE:
				toDrop = new ItemStack(Material.IRON_INGOT);
				break;
		}

		if (toDrop != null)
		{
			event.setCancelled(true);
			block.setType(Material.AIR);
			block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), toDrop);
		}
	}

	@EventHandler
	public void projectileDamage(CustomDamageEvent event)
	{
		event.SetDamageToLevel(false);

		if (!IsLive() || event.GetProjectile() == null)
		{
			return;
		}

		boolean egg = event.GetProjectile() instanceof Egg;
		boolean snowball = event.GetProjectile() instanceof Snowball;

		if (egg || snowball)
		{
			event.AddMod(event.GetDamagerPlayer(true).getName(), (egg ? "Egg" : "Snowball"), 0.5, true);
		}
	}

	@EventHandler
	public void explosion(ExplosionEvent event)
	{
		_oreHider.Explosion(event);
		event.GetBlocks().removeIf(block -> block.getType() == Material.CHEST || block.getType() == Material.ANVIL);
	}

	@EventHandler
	public void blockBreakOreReveal(BlockBreakEvent event)
	{
		_oreHider.BlockBreak(event);
	}

	@EventHandler
	public void playerEnchant(EnchantItemEvent event)
	{
		Player player = event.getEnchanter();

		if (event.getEnchantsToAdd().containsKey(Enchantment.FIRE_ASPECT))
		{
			player.sendMessage(F.main("Game", "You cannot enchant with " + F.name("Fire Aspect") + "."));
			event.setCancelled(true);
		}
	}

	@Override
	public double GetKillsGems(Player killer, Player killed, boolean assist)
	{
		return assist ? 3 : 12;
	}

}
