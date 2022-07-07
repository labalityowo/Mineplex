package mineplex.game.clans.tutorial.tutorials.clans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;
//import mineplex.game.clans.tutorial.tutorials.clans.repository.TutorialRepository;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.hologram.HologramManager;
import mineplex.core.npc.NpcManager;
import mineplex.core.task.TaskManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.event.ClansCommandPreExecutedEvent;
import mineplex.game.clans.clans.event.ClansPlayerBuyItemEvent;
import mineplex.game.clans.clans.event.ClansPlayerSellItemEvent;
import mineplex.game.clans.clans.event.PreEnergyShopBuyEvent;
import mineplex.game.clans.clans.gui.events.ClansButtonClickEvent;
import mineplex.game.clans.economy.GoldManager;
import mineplex.game.clans.message.ClansMessageManager;
import mineplex.game.clans.spawn.Spawn;
import mineplex.game.clans.tutorial.Tutorial;
import mineplex.game.clans.tutorial.TutorialRegion;
import mineplex.game.clans.tutorial.TutorialSession;
import mineplex.game.clans.tutorial.TutorialWorldManager;
import mineplex.game.clans.tutorial.map.TutorialMapManager;
import mineplex.game.clans.tutorial.tutorials.clans.objective.AttackEnemyObjective;
import mineplex.game.clans.tutorial.tutorials.clans.objective.ClanObjective;
import mineplex.game.clans.tutorial.tutorials.clans.objective.ClassesObjective;
import mineplex.game.clans.tutorial.tutorials.clans.objective.EnergyObjective;
import mineplex.game.clans.tutorial.tutorials.clans.objective.FieldsObjective;
import mineplex.game.clans.tutorial.tutorials.clans.objective.FinalObjective;
import mineplex.game.clans.tutorial.tutorials.clans.objective.PurchaseItemsObjective;
import mineplex.game.clans.tutorial.tutorials.clans.objective.ShopsObjective;

public class ClansMainTutorial extends Tutorial
{
	// The distance from which the gate opens when a player approaches.
	private static final int GATE_OPEN_DISTANCE = 15;

	private TutorialMapManager _mapManager;
	private List<Player> _fireworks;
//	private TutorialRepository _repository;
	private TaskManager _taskManager;


	private List<Material> _items = Lists.newArrayList(
			Material.SMOOTH_BRICK,
			Material.TORCH,
			Material.IRON_DOOR,
			Material.IRON_DOOR_BLOCK
	);


	public ClansMainTutorial(JavaPlugin plugin, ClansManager clansManager, ClansMessageManager message, HologramManager hologram, NpcManager npcManager, TaskManager taskManager)
	{
		super(plugin, message, hologram, "Clans Tutorial", "main", Material.DIAMOND_SWORD, (byte) 0);

		_fireworks = new ArrayList<>();

		try
		{
			setWorldManager(new TutorialWorldManager(plugin, "main_tutorial", "schematic/ClansTutorial.schematic"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		_mapManager = new TutorialMapManager(plugin, this, getWorldManager().getTutorialWorld(), -10, 0, 117, 127);
		_taskManager = taskManager;
		
//		_repository = new TutorialRepository(ClansManager.getInstance().getClientManager());
		
		addObjective(new ClanObjective(this, plugin));
		addObjective(new AttackEnemyObjective(this, clansManager, plugin));
		addObjective(new ShopsObjective(this, npcManager, plugin));
		addObjective(new PurchaseItemsObjective(this, plugin));
		addObjective(new ClassesObjective(this, plugin));
		addObjective(new FieldsObjective(this, plugin));
		addObjective(new EnergyObjective(this, plugin));
		addObjective(new FinalObjective(this, plugin));
	}

	@Override
	protected void onFinish(Player player)
	{
		_fireworks.add(player);
		UtilTextMiddle.display(C.cYellow + "Congratulations", "You have completed the Tutorial!", 10, 60, 10, player);

		Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
		{
			_fireworks.remove(player);
			getMessage().removePlayer(player);

			player.teleport(Spawn.getNorthSpawn());
			UtilInv.Clear(player);

			ClansManager.getInstance().getItemMapManager().setMap(player);
		}, 20 * 10L);

		player.setWalkSpeed(0.2F);

		//ClansManager.getInstance().getPvpTimer().unpause(player);

		if (!_taskManager.hasCompletedTask(player, getTaskIdentifier()))
		{
			_taskManager.completedTask(data ->
			{
				GoldManager.getInstance().addGold(player, 32000);
				UtilPlayer.message(player, F.main("Clans", "You have earned " + F.elem(32000 + " Gold") + " for finishing the tutorial!"));
			}, player, getTaskIdentifier());
		}

		/*
		ClansManager.getInstance().runAsync(() ->
		{
			_repository.SetTimesPlayed(player.getUniqueId(), _repository.GetTimesPlayed(player.getUniqueId()) + 1);

			final int times = _repository.GetTimesPlayed(player.getUniqueId());

			if (_repository.GetTimesPlayed(player.getUniqueId()) == 1)
			{
				ClansManager.getInstance().runSync(() ->
				{
					if (times == 1)
					{
						GoldManager.getInstance().addGold(player, 32000);
						UtilPlayer.message(player, F.main("Clans", "You have earned " + F.elem(32000 + " Gold") + " for finishing the tutorial!"));
					}

					UtilInv.give(player, Material.COOKIE);
				});
			}
		});
		*/
	}

	@Override
	protected boolean canStart(Player player)
	{
		ClanInfo clan = ClansManager.getInstance().getClan(player);
		if (clan != null)
		{
			UtilPlayer.message(player, F.main("Clans", "You are part of a clan - please leave or disband the clan before doing the tutorial"));
			return false;
		}
		return true;
	}

	@Override
	protected void onStart(Player player)
	{
		TutorialRegion region = getRegion(player);
		
		player.teleport(getSpawn(region));
		spawnFences(region, DyeColor.BLACK); // Fields
		spawnFences(region, DyeColor.BROWN); // Shops
		spawnFences(region, DyeColor.RED); // Middle

		player.setGameMode(GameMode.SURVIVAL);
		player.getInventory().clear();
		player.getInventory().setArmorContents(new ItemStack[4]);
		player.setHealth(20);
		player.setFoodLevel(20);
		
		//ClansManager.getInstance().getPvpTimer().pause(player);

		// Spawn Holograms
		setSpawnHologram(player,
				getPoint(region, ClansMainTutorial.Point.SPAWN).add(0, 1.5, -3),
				C.cGoldB + "Welcome to the Clans Tutorial!",
				" ",
				"This will teach you the basics of Clans.",
				"It will take about 5 minutes to complete.",
				"You must complete it before playing Clans.",
				" ",
				"Starting in " + C.cGreen + "10 Seconds");

		player.setWalkSpeed(0);

		addHologram(player,
				getPoint(region, ClansMainTutorial.Point.SPAWN).add(0, 1.5, -23),
				"Jump Off!");
	}

	@Override
	protected void onQuit(Player player)
	{
		_fireworks.remove(player);
	}

	public Location getPoint(TutorialRegion region, Point point)
	{
		return region.getLocationMap().getGoldLocations(point.getDataLocColor()).get(0).clone();
	}

	public boolean isIn(Location location, TutorialRegion region, Bounds bounds)
	{
		if (region.getOrigin().getWorld() != location.getWorld())
			return false;
		
		List<Location> locs = region.getLocationMap().getGoldLocations(bounds.getDataLocColor());
		return UtilAlg.inBoundingBox(location, locs.get(0), locs.get(1));
	}

	public boolean isIn(Player player, Bounds bounds)
	{
		if(player == null || !player.isOnline()) return false;
		TutorialRegion region = getRegion(player);

		if (region != null)
		{
			if (player.getLocation() == null) return false;
			return isIn(player.getLocation(), region, bounds);
		}

		return false;
	}

	public Location getCenter(TutorialRegion region, Bounds bounds)
	{
		List<Location> locs = region.getLocationMap().getGoldLocations(bounds.getDataLocColor());
		return UtilAlg.getMidpoint(locs.get(0), locs.get(1));
	}

	public Location getSpawn(TutorialRegion region)
	{
		Location location = region.getLocationMap().getGoldLocations(Point.SPAWN.getDataLocColor()).get(0).clone();
		location.setYaw(180);
		return location;
	}

	public enum Bounds
	{
		LAND_CLAIM(DyeColor.LIGHT_BLUE),
		// Should be 16x16
		ENEMY_LAND(DyeColor.RED),
		// Should be 16x16
		SPAWN(DyeColor.GREEN),
		// Spawn Platform
		FIELDS(DyeColor.PURPLE),
		// Boulders for ores to spawn in?
		SHOPS(DyeColor.YELLOW),
		// Little shop areas similar to Shops in clans map
		ENEMY_ATTACK_AREA(DyeColor.BLACK),
		NPC_DIE_AREA(DyeColor.BLUE),
		NPC_SHOOT_AREA(DyeColor.BROWN);
		// Gray Wool - Cannon Location
		// Magenta Wool - Spawn
		// Lime Wool - Farming Shop
		// Purple Wool - Pvp Shop
		// Light Gray Wool - Energy Shop

		private DyeColor _dataLocColor;

		Bounds(DyeColor dataLocColor)
		{
			_dataLocColor = dataLocColor;
		}

		public DyeColor getDataLocColor()
		{
			return _dataLocColor;
		}
	}

	public enum Point
	{
		SPAWN(DyeColor.MAGENTA),
		FARMING_SHOP(DyeColor.LIME),
		PVP_SHOP(DyeColor.PINK),
		ENERGY_SHOP(DyeColor.SILVER),
		MINING_SHOP(DyeColor.ORANGE),
		CANNON(DyeColor.GRAY),
		NPC_1(DyeColor.CYAN),
		NPC_2(DyeColor.WHITE);

		private DyeColor _dataLocColor;

		Point(DyeColor dataLocColor)
		{
			_dataLocColor = dataLocColor;
		}

		public DyeColor getDataLocColor()
		{
			return _dataLocColor;
		}
	}

	@EventHandler
	public void dropItem(PlayerDropItemEvent event)
	{
		if (isInTutorial(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void breakBlock(BlockBreakEvent event)
	{
		if (isInTutorial(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void blockDamage(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			if (isInTutorial((Player) event.getEntity()))
				event.setCancelled(true);
		}
	}

	// Fences
	// Black = fields
	// Brown = shops
	public void spawnFence(Block startBlock)
	{
		Block block = startBlock;
		int count = 0;
		while ((block.getType() == Material.AIR || block.getType() == Material.FENCE) && count < 100)
		{
			block.setType(Material.FENCE);

			block = block.getRelative(BlockFace.UP);
			count++;
		}
	}

	public void destroyFence(Block startBlock)
	{
		Block block = startBlock;
		int count = 0;
		while (block.getType() == Material.FENCE && count < 100)
		{
			block.setType(Material.AIR);

			block = block.getRelative(BlockFace.UP);
			count++;
		}
	}

	public void spawnFences(TutorialRegion region, DyeColor dataLoc)
	{
		List<Location> locations = region.getLocationMap().getIronLocations(dataLoc);
		locations.stream().map(Location::getBlock).forEach(this::spawnFence);
	}

	public void destroyFences(TutorialRegion region, DyeColor dataLoc)
	{
		List<Location> locations = region.getLocationMap().getIronLocations(dataLoc);
		locations.stream().map(Location::getBlock).forEach(this::destroyFence);
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void teleport(ClansCommandPreExecutedEvent event)
	{
		if (event.getArguments().length < 1)
		{
			return;
		}
		
		if (!isInTutorial(event.getPlayer()))
		{
			return;
		}
		
		if (event.getArguments()[0].equalsIgnoreCase("join")
		 || event.getArguments()[0].equalsIgnoreCase("leave")
		 || event.getArguments()[0].equalsIgnoreCase("stuck")
		 || event.getArguments()[0].equalsIgnoreCase("promote")
		 || event.getArguments()[0].equalsIgnoreCase("demote")
		 || event.getArguments()[0].equalsIgnoreCase("invite")
		 || event.getArguments()[0].equalsIgnoreCase("kick")
		 || event.getArguments()[0].equalsIgnoreCase("neutral")
		 || event.getArguments()[0].equalsIgnoreCase("enemy")
		 || event.getArguments()[0].equalsIgnoreCase("ally")
		 || event.getArguments()[0].equalsIgnoreCase("trust")
		 || event.getArguments()[0].equalsIgnoreCase("unclaim")
		 || event.getArguments()[0].equalsIgnoreCase("claim")
		 || event.getArguments()[0].equalsIgnoreCase("delete")
		 || event.getArguments()[0].equalsIgnoreCase("disband")
		 || event.getArguments()[0].equalsIgnoreCase("admin")
		 || event.getArguments()[0].equalsIgnoreCase("x"))
		{
			UtilPlayer.message(event.getPlayer(), F.main("Clans", "You are not allowed to run this command during the Tutorial."));
			event.setCancelled(true);
		}
		
	}
	
	@EventHandler
	public void checkInRegion(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Player player : getPlayers())
		{
			if (player == null || !player.isOnline()) continue;
			if (player.getLocation().getWorld() != getWorldManager().getTutorialWorld())
			{
				TutorialRegion region = getRegion(player);
				player.teleport(getSpawn(region));
			}
		}
	}

	@EventHandler
	public void fireworkUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST) return;

		for (Player player : _fireworks)
		{
			if (player == null || !player.isOnline()) continue;
			UtilFirework.spawnRandomFirework(UtilAlg.getRandomLocation(player.getLocation(), 10));
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
//		ClansManager.getInstance().runAsync(() -> {
//			if (_repository.GetTimesPlayed(event.getPlayer().getUniqueId()) == 0)
//			{
//				ClansManager.getInstance().runSync(() -> start(event.getPlayer()));
//			}
//		});

		/*if (!_taskManager.hasCompletedTask(event.getPlayer(), getTaskIdentifier()))
		{
			ClanInfo clan = ClansManager.getInstance().getClan(event.getPlayer());
			if (clan == null)
			{
				start(event.getPlayer());
			}
			else
			{
				UtilPlayer.message(event.getPlayer(), F.main("Clans", "It seems you already have a clan here, so we can skip the tutorial"));
			}
		}
		else */if (!event.getPlayer().hasPlayedBefore() || !event.getPlayer().getLocation().getWorld().equals(Spawn.getSpawnWorld()))
		{
			Spawn.getInstance().teleport(event.getPlayer(), Spawn.getInstance().getSpawnLocation(), 2);
		}
	}

	@EventHandler
	public void preventMovement(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player player : getPlayers())
		{
			TutorialSession session = getTutorialSession(player);
			long time = session.getElapsedTime();
			if (time <= 10000) // 10 seconds
			{
//				player.teleport(getSpawn(session.getRegion()));

				String secondsLeft = UtilTime.convertString(10000 - time, 0, UtilTime.TimeUnit.SECONDS);
				setSpawnHologram(player,
						getPoint(session.getRegion(), ClansMainTutorial.Point.SPAWN).add(0, 1.5, -3),
						C.cGoldB + "Welcome to the Clans Tutorial!",
						" ",
						"This will teach you the basics of Clans.",
						"It will take about 5 minutes to complete.",
						"You must complete it before playing Clans.",
						" ",
						"Starting in " + C.cGreen + secondsLeft);
			}
			else if (!session.isRemovedHologram())
			{
				removeSpawnHologram(player);
				player.setWalkSpeed(0.2F);
			}
		}
	}

	public void performGateCheck(Player player, DyeColor key)
	{
		if(player == null || !player.isOnline()) return;
		Location exact = getRegion(player).getLocationMap().getIronLocations(key).get(0);
		if(exact == null) return;
		Location fence = UtilAlg.getAverageLocation(getRegion(player).getLocationMap().getIronLocations(key));

		if (exact.getBlock().getType() == Material.AIR)
		{
			// Gates are already open.
			return;
		}

		if (player.getLocation().distanceSquared(fence) <= (GATE_OPEN_DISTANCE * GATE_OPEN_DISTANCE))
		{
			// Within the correct blocks of the gates.
			destroyFences(getRegion(player), key);
		}
	}

	public TutorialMapManager getMapManager()
	{
		return _mapManager;
	}

	@EventHandler (priority = EventPriority.LOWEST)
	public void onClick(ClansButtonClickEvent event) {
		if(isInTutorial(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler (priority = EventPriority.LOWEST)
	public void onShop(ClansPlayerBuyItemEvent event)
	{
		if(isInTutorial(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler (priority = EventPriority.LOWEST)
	public void onSell(ClansPlayerSellItemEvent event)
	{
		if(isInTutorial(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void energyBuy(PreEnergyShopBuyEvent event)
	{
		if (isInTutorial(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void blockBreak(BlockDamageEvent event)
	{
		if (!isInTutorial(event.getPlayer()))
		{
			return;
		}

		if (!isInBuildArea(event.getPlayer(), event.getBlock()))
		{
			return;
		}


		if (_items.contains(event.getBlock().getType()))
		{
			event.getBlock().breakNaturally();

			event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), Effect.TILE_BREAK, event.getBlock().getTypeId(), 18);

			event.getPlayer().playSound(event.getBlock().getLocation(), Sound.LAVA_POP, 1.f, 1.f);
		}
	}




	@EventHandler (priority = EventPriority.HIGHEST)
	public void blockPlace(BlockPlaceEvent event)
	{
		if (!isInTutorial(event.getPlayer()))
		{
			return;
		}

		if (isInBuildArea(event.getPlayer(), event.getBlock()))
		{
			event.setCancelled(false);
		}
		else
		{
			UtilPlayer.message(event.getPlayer(), F.main("Clans", "You are not allowed to place blocks here."));
			event.setCancelled(true);
		}
	}

	public boolean isInBuildArea(Player player, Block block)
	{
		TutorialRegion region = getRegion(player);
		return isIn(block.getLocation(), region, ClansMainTutorial.Bounds.LAND_CLAIM);
	}
}