package mineplex.game.clans.gameplay;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.material.Dye;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.weight.Weight;
import mineplex.core.common.weight.WeightSet;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.event.ClansWaterPlaceEvent;
import mineplex.game.clans.clans.event.IronDoorOpenEvent;
import mineplex.game.clans.core.repository.ClanTerritory;
import mineplex.minecraft.game.classcombat.Class.ClientClass;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.event.BlockTossEvent;
import mineplex.minecraft.game.classcombat.Skill.event.BlockTossLandEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.minecraft.game.core.damage.DamageManager;

public class Gameplay extends MiniPlugin
{
	private static final int MAX_BUILD_HEIGHT = 120;
	private static final int MIN_CHEST_HEIGHT = 30;
	private static final Material[] REDSTONE_TYPES = new Material[] {Material.ACTIVATOR_RAIL, Material.REDSTONE, Material.REDSTONE_BLOCK, Material.REDSTONE_COMPARATOR, Material.REDSTONE_WIRE, Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON, Material.DIODE, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.DETECTOR_RAIL};
	private static final byte[] PLACED_LOG_DATA_VALUES = {12,13,14,15};
	private ClansManager _clansManager;
	private BlockRestore _blockRestore;
	private DamageManager _damageManager;
	private WeightSet<Boolean> _foodDecrease; // Weighted probability sets for
												// food decrease event outcomes
	
	public Gameplay(JavaPlugin plugin, ClansManager clansManager, BlockRestore blockRestore, DamageManager damageManager)
	{
		super("PvP Gameplay", plugin);
		
		_clansManager = clansManager;
		_blockRestore = blockRestore;
		_damageManager = damageManager;
		_foodDecrease = new WeightSet<Boolean>(new Weight<Boolean>(10, true), new Weight<Boolean>(90, false));
		
		// Register the custom recipes and mobs
		Bukkit.getPluginManager().registerEvents(new CustomRecipes(), plugin);
		Bukkit.getPluginManager().registerEvents(new CustomCreatures(), plugin);
		Bukkit.getPluginManager().registerEvents(new DurabilityManager(), plugin);
	}
	
//	@EventHandler(priority = EventPriority.LOWEST)
//	public void spawnDamage(CustomDamageEvent event)
//	{
//		if (_clansManager.getClanUtility().getClaim(event.GetDamageeEntity().getLocation()) != null && _clansManager.getClanUtility().getClaim(event.GetDamageeEntity().getLocation()).isSafe(event.GetDamageeEntity().getLocation()))
//		{
//			event.SetCancelled("Safe Zone");
//		}
//	}
	
	@EventHandler
	public void stopBlockTossExploit(BlockTossLandEvent event)
	{
		int id = event.Entity.getBlockId();
		if (_clansManager.getClanUtility().isClaimed(event.getLocation())
				&& (Material.getMaterial(id).name().endsWith("_PLATE")
				 || id == Material.STONE_BUTTON.getId()
				 || id == Material.WOOD_BUTTON.getId()
				 || id == Material.LEVER.getId()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerFishing(PlayerFishEvent event)
	{
		State state = event.getState();
		if (state == State.CAUGHT_ENTITY || state == State.CAUGHT_FISH)
		{
			event.setCancelled(true);
			notify(event.getPlayer(), "Fishing is disabled!");
		}
	}
	
	@EventHandler
	public void onBlockToss(BlockTossEvent event)
	{
		Location location = event.getLocation();
		if (_clansManager.getClanUtility().isClaimed(location))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void itemFrameRotate(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked() instanceof ItemFrame)
		{
			if (_clansManager.getClanUtility().isClaimed(event.getRightClicked().getLocation()) && _clansManager.getClanUtility().getClaim(event.getRightClicked().getLocation()).isSafe(event.getRightClicked().getLocation()))
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onBowShoot(EntityShootBowEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}
		
		if (event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();
		
			ClientClass playerClass = _clansManager.getClassManager().Get(player);
			
			if (!playerClass.IsGameClass(ClassType.Assassin, ClassType.Ranger))
			{
				notify(player, "You cannot use bows without the proper class!");
				event.setCancelled(true);
			}
		}
	}
	
	/**
	 * Decreases the hunger rate decrease speed by arbitrarily canceling a
	 * portion of decrease events.
	 * 
	 * @param event
	 */
	@EventHandler(ignoreCancelled = true)
	public void foodChangeLevel(FoodLevelChangeEvent event)
	{
		Player player = (Player) event.getEntity();
		
		if (event.getFoodLevel() < player.getFoodLevel()) // Hunger is
															// decreasing for
															// player
		{
			event.setCancelled(_foodDecrease.generateRandom());
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void ObsidianCancel(BlockPlaceEvent event)
	{
		if (event.getBlock().getType() == Material.OBSIDIAN && event.getPlayer().getGameMode() != GameMode.CREATIVE)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot place " + F.item("Obsidian") + "."));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void BedrockCancel(BlockPlaceEvent event)
	{
		if (event.getBlock().getType() == Material.BEDROCK && event.getPlayer().getGameMode() != GameMode.CREATIVE)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot place " + F.item("Bedrock") + "."));
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void BarrierCancel(BlockPlaceEvent event)
	{
		if (event.getBlock().getType() == Material.BARRIER && event.getPlayer().getGameMode() != GameMode.CREATIVE)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot place " + F.item("Barrier Blocks") + "."));
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void HopperCancel(BlockPlaceEvent event)
	{
		if (event.getBlock().getType() == Material.HOPPER && event.getPlayer().getGameMode() != GameMode.CREATIVE)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot place " + F.item("Hoppers") + "."));
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void CommandPlace(BlockPlaceEvent event)
	{
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
		{
			return;
		}
		if (event.getBlock().getType() == Material.COMMAND || event.getBlock().getType() == Material.NOTE_BLOCK || event.getBlock().getType() == Material.REDSTONE_LAMP_ON)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot place " + F.item("Proximity Devices") + "."));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void RedstoneCancel(BlockPlaceEvent event)
	{
		if (Arrays.asList(REDSTONE_TYPES).contains(event.getBlock().getType()) && event.getPlayer().getGameMode() != GameMode.CREATIVE)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot place redstone based items."));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void MaxHeight(BlockPlaceEvent event)
	{
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
		{
			return;
		}
		if (event.getBlock().getLocation().getBlockY() > MAX_BUILD_HEIGHT)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot place blocks this high."));
			event.setCancelled(true);
		}
		else if(event.getBlock().getLocation().getBlockY() == (MAX_BUILD_HEIGHT - 1) && event.getBlock().getType().name().contains("DOOR") && event.getBlock().getType() != Material.TRAP_DOOR && event.getBlock().getType() != Material.IRON_TRAPDOOR)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot place blocks this high."));
			event.setCancelled(true);
		}
		
		if (event.getBlock().getLocation().getBlockY() < MIN_CHEST_HEIGHT && (event.getBlock().getType() == Material.CHEST || event.getBlock().getType() == Material.TRAPPED_CHEST || event.getBlock().getType() == Material.DISPENSER || event.getBlock().getType() == Material.DROPPER || event.getBlock().getType() == Material.FURNACE || event.getBlock().getType() == Material.BURNING_FURNACE))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void GrowTree(StructureGrowEvent event)
	{
		event.getBlocks().stream().filter(blockState -> blockState.getLocation().getBlockY() > MAX_BUILD_HEIGHT).forEach(blockState -> blockState.setType(Material.AIR)	);
	}

	/**
	 * Disable all Piston related events in Clans
	 * 
	 * @param event
	 */
	@EventHandler
	public void onPistonExtend(BlockPistonExtendEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void disableEnderChest(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
		{
			return;
		}
		
		if (event.getClickedBlock() == null)
		{
			return;
		}
		
		if (_clansManager.getWorldEvent().isInEvent(event.getClickedBlock().getLocation(), false))
		{
			return;
		}
		
		if (event.getClickedBlock().getType().equals(Material.ENDER_CHEST))
		{
			UtilPlayer.message(event.getPlayer(), F.main("Clans", "You are not permitted to use Ender Chests."));
			event.setCancelled(true);
			return;
		}
	}
	
	/**
	 * Disable all Piston related events in Clans
	 * 
	 * @param event
	 */
	@EventHandler
	public void onPistonRetract(BlockPistonRetractEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void DispenseLiquidCancel(BlockDispenseEvent event)
	{
		Material material = event.getItem().getType();
		
		if (material == Material.LAVA_BUCKET || material == Material.LAVA || material == Material.WATER_BUCKET || material == Material.WATER)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void WebBreak(BlockDamageEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}
		if (_clansManager.getWorldEvent().isInEvent(event.getBlock().getLocation(), false))
		{
			return;
		}
		
		if (event.getBlock().getType() == Material.WEB)
		{
			event.setInstaBreak(true);
		}
	}
	
	@EventHandler
	public void LapisPlace(BlockPlaceEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}
		
		if (event.getBlock().getType() != Material.LAPIS_BLOCK)
		{
			return;
		}
		
		event.setCancelled(true);
		
		ClansWaterPlaceEvent placeEvent = new ClansWaterPlaceEvent(event.getPlayer(), event.getBlock());
		
		UtilServer.CallEvent(placeEvent);
		
		if (placeEvent.isCancelled())
		{
			return;
		}
		
		UtilInv.remove(event.getPlayer(), Material.LAPIS_BLOCK, (byte) 0, 1);
		
		final Block block = event.getBlock();
		
		_plugin.getServer().getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable()
		{
			public void run()
			{
				block.setType(Material.WATER);
				block.setData((byte) 0);
				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, 8);
				block.getWorld().playSound(block.getLocation(), Sound.SPLASH, 2f, 1f);
			}
		}, 0);
	}
	
	@EventHandler
	public void EnderChestBreak(BlockBreakEvent event)
	{
		if (event.isCancelled()) return;
		
		if (event.getBlock().getType() != Material.ENDER_CHEST) return;
		
		event.setCancelled(true);
		
		event.getBlock().setTypeId(0);
		event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().add(0.5, 0.5, 0.5), ItemStackFactory.Instance.CreateStack(Material.ENDER_CHEST));
	}
	
	@EventHandler
	public void disableEnderPearls(PlayerInteractEvent event)
	{
		if (!isRightClick(event.getAction())) return;
		
		if (event.hasItem() && event.getItem().getType() == Material.ENDER_PEARL)
		{
			event.setCancelled(true);
			UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot use ender pearls!"));
		}
	}
	
	@EventHandler
	public void disableFlintNSteel(PlayerInteractEvent event)
	{
		if (!isRightClick(event.getAction())) return;
		
		if (event.hasItem() && event.getItem().getType() == Material.FLINT_AND_STEEL)
		{
			event.setCancelled(true);
			UtilPlayer.message(event.getPlayer(), F.main("Game", "You cannot use flint & steel!"));
		}
	}
	
	private static boolean isRightClick(Action action)
	{
		return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
	}
	
	@EventHandler
	public void IronDoor(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}
		
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
		{
			return;
		}
		
		if (event.getClickedBlock().getTypeId() != 71)
		{
			return;
		}

		Block block = event.getClickedBlock();
		
		// Knock
		if (event.isCancelled())
		{
			if (!Recharge.Instance.use(event.getPlayer(), "Door Knock", 500, false, false))
			{
				return;
			}
			
			block.getWorld().playEffect(block.getLocation(), Effect.ZOMBIE_CHEW_WOODEN_DOOR, 0);
		}
		
		// Open
		else
		{
			IronDoorOpenEvent customEvent = UtilServer.CallEvent(new IronDoorOpenEvent(event.getPlayer(), block));
			
			if (customEvent.isCancelled())
			{
				return;
			}
			
			if (block.getData() >= 8) block = block.getRelative(BlockFace.DOWN);
			
			if (block.getData() < 4)
				block.setData((byte) (block.getData() + 4), true);
			else
				block.setData((byte) (block.getData() - 4), true);
				
			// Effect
			block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
		}
	}
	
	@EventHandler
	public void BrewingDisable(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK)) return;
		
		if (event.getClickedBlock().getTypeId() != 117) return;
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void AnvilDisable(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
			return;
		else if (event.getClickedBlock().getType() != Material.ANVIL)
			return;
		else if (player.isSneaking() && player.getItemInHand().getType() != Material.AIR) return;
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void BonemealCancel(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R)) return;
		
		Player player = event.getPlayer();
		
		if (player.getItemInHand() == null) return;
		
		if (player.getItemInHand().getType() != Material.INK_SACK) return;
		
		if (player.getItemInHand().getData() == null) return;
		
		if (player.getItemInHand().getData().getData() != 15) return;
		
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void disableSaplings(BlockPlaceEvent event)
	{
		if (!event.getItemInHand().getType().equals(Material.SAPLING))
		{
			return;
		}
		
		Block block = event.getBlock();
		ClanInfo clan = _clansManager.getClanUtility().getClanByPlayer(event.getPlayer());
		if (clan != null)
		{
			ClanTerritory claim = _clansManager.getClanUtility().getClaim(block.getLocation());
			if (claim != null)
			{
				if (claim.Owner.equals(clan.getName()))
				{
					if (!Recharge.Instance.use(event.getPlayer(), "Place Sapling", 20 * 60 * 1000, true, false))
					{
						event.setCancelled(true);
						return;
					}
				}
			}
		}
		UtilPlayer.message(event.getPlayer(), F.main("Clans", "You can only place saplings in your own territory!"));
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void disableString(BlockPlaceEvent event)
	{
		if (!event.getItemInHand().getType().equals(Material.STRING))
		{
			return;
		}
		
		Block block = event.getBlock();
		ClanInfo clan = _clansManager.getClanUtility().getClanByPlayer(event.getPlayer());
		if (clan != null)
		{
			ClanTerritory claim = _clansManager.getClanUtility().getClaim(block.getLocation());
			if (claim != null)
			{
				if (claim.Owner.equals(clan.getName()))
				{
					return;
				}
			}
		}
		UtilPlayer.message(event.getPlayer(), F.main("Clans", "You can only place string in your own territory!"));
		event.setCancelled(true);
	}
	
	@EventHandler
	public void blockDispenser(BlockDispenseEvent event)
	{
		// Block bonemeal
		if (event.getItem().getType() == Material.INK_SACK && ((Dye) event.getItem().getData()).getColor() == DyeColor.WHITE)
		{
			event.setCancelled(true);
		}
		
		// Block flint-and-steal
		if (event.getItem().getType() == Material.FLINT_AND_STEEL)
		{
			event.setCancelled(true);
		}
		
		if (event.getItem().getType() == Material.FIREWORK_CHARGE)
		{
			event.setCancelled(true);
		}
		
		if (event.getItem().getType() == Material.SAPLING)
		{
			event.setCancelled(true);
		}
		
		if (event.getItem().getType() == Material.TNT)
		{
			event.setCancelled(true);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onLogPlace(BlockPlaceEvent event)
	{
		if (UtilItem.isLog(event.getBlock().getType()))
		{
			byte data = event.getBlock().getData();
			final byte newData;
			if (data % 4 == 0)
			{
				newData = 12;
			}
			else if ((data - 1) % 4 == 0)
			{
				newData = 13;
			}
			else if ((data - 2) % 4 == 0)
			{
				newData = 14;
			}
			else if ((data - 3) % 4 == 0)
			{
				newData = 15;
			}
			else
			{
				newData = data;
			}
			
			UtilServer.runSyncLater(() ->
			{
				event.getBlock().setData(newData);
			}, 1);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST)
	public void replantTree(BlockBreakEvent event)
	{
		final Block block = event.getBlock();

		if (_clansManager.getClanUtility().getClaim(block.getLocation()) != null)
		{
			return;
		}

		if (!UtilItem.isLog(block.getType()))
		{
			return;
		}

		if (UtilItem.isLog(block.getRelative(BlockFace.DOWN).getType()))
		{
			return;
		}

		if (UtilItem.isLeaf(block.getRelative(BlockFace.DOWN).getType()))
		{
			return;
		}

		if (block.getRelative(BlockFace.DOWN).getType() != Material.DIRT && block.getRelative(BlockFace.DOWN).getType() != Material.GRASS)
		{
			return;
		}

		final byte data = block.getData();
		
		if (ArrayUtils.contains(PLACED_LOG_DATA_VALUES, data))
		{
			return;
		}

		UtilServer.runSyncLater(() ->
		{
			if (block.getType() != Material.AIR)
			{
				return;
			}
			Material mat = block.getRelative(BlockFace.DOWN).getType();
			if (mat == Material.DIRT || mat == Material.GRASS)
			{
				block.setType(Material.SAPLING);
				block.setData(data);
			}
		}, 20 * 10);
	}

	@EventHandler
	public void killRain(WeatherChangeEvent event)
	{
		event.setCancelled(event.toWeatherState());
	}
	
	@EventHandler
	public void WildfireSpread(BlockBurnEvent event)
	{
		if (event.isCancelled()) return;
		
		event.setCancelled(true);
		
		for (int x = -1; x <= 1; x++)
		{
			for (int y = -1; y <= 1; y++)
			{
				for (int z = -1; z <= 1; z++)
				{
					// Self
					if (x == 0 && y == 0 && z == 0)
					{
						event.getBlock().setType(Material.FIRE);
						
						if (event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.GRASS) event.getBlock().getRelative(BlockFace.DOWN).setType(Material.DIRT);
						
						return;
					}
					
					Block block = event.getBlock().getRelative(x, y, z);
					
					if (block.getRelative(BlockFace.DOWN).getType() == Material.GRASS) block.getRelative(BlockFace.DOWN).setType(Material.DIRT);
					
					// Surroundings
					if (!((x == 0 && y == 0) || (x == 0 && z == 0) || (y == 0 && z == 0))) continue;
					
					if (block.getTypeId() == 0) block.setType(Material.FIRE);
				}
			}
		}
	}
	
	@EventHandler
	public void WildfireDirt(BlockIgniteEvent event)
	{
		if (event.isCancelled()) return;
		
		if (event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.GRASS) event.getBlock().getRelative(BlockFace.DOWN).setType(Material.DIRT);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void WildfireCancel(BlockIgniteEvent event)
	{
		if (event.isCancelled()) return;
		
		if (event.getBlock().getBiome() == Biome.JUNGLE || event.getBlock().getBiome() == Biome.JUNGLE_HILLS) if (event.getCause() == IgniteCause.SPREAD) event.setCancelled(true);
	}
	
	// @EventHandler
	// public void onBlockPlace(BlockPlaceEvent event)
	// {
	// Location location = event.getBlock().getLocation();
	//
	// if (_clansManager.getClanUtility().isNearAdminClaim(location))
	// {
	// event.setCancelled(true);
	// UtilPlayer.message(event.getPlayer(), F.main("Admin", "You cannot place
	// blocks near admin territories!"));
	// }
	// }
	//
	// @EventHandler
	// public void onBlockBreak(BlockBreakEvent event)
	// {
	// Location location = event.getBlock().getLocation();
	//
	// if (_clansManager.getClanUtility().isNearAdminClaim(location))
	// {
	// event.setCancelled(true);
	// UtilPlayer.message(event.getPlayer(), F.main("Admin", "You cannot break
	// blocks near admin territories!"));
	// }
	// }
	
	/*
	 * @EventHandler (priority = EventPriority.HIGHEST) public void
	 * MoneyLossSteal(CombatDeathEvent event) { if
	 * (!(event.GetEvent().getEntity() instanceof Player)) return; Player player
	 * = (Player)event.GetEvent().getEntity(); int balance =
	 * Clients().Get(player).Game().GetEconomyBalance(); int lose = (int) (0.04
	 * * balance); //Balance
	 * Clients().Get(player).Game().SetEconomyBalance(balance - lose); CombatLog
	 * log = event.GetLog(); if (log.GetKiller() != null) { //Inform
	 * UtilPlayer.message(UtilPlayer.searchExact(log.GetKiller().getName()),
	 * F.main("Death", "You stole " + F.count((lose) + " Coins") + " from " +
	 * F.name(player.getName()) + ".")); //Inform UtilPlayer.message(player,
	 * F.main("Death", "You lost " + F.count((lose) + " Coins") + " to " +
	 * F.name(log.GetKiller().getName()) + ".")); } else { //Inform
	 * UtilPlayer.message(player, F.main("Death", "You lost " + F.count((lose) +
	 * " Coins") + " for dying.")); } }
	 */
	
	@EventHandler
	public void SpawnDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled()) return;
		
		if (event.GetCause() != DamageCause.FALL) return;
		
		if (!_clansManager.getClanUtility().isSpecial(event.GetDamageeEntity().getLocation(), "Spawn")) return;
		
		event.SetCancelled("Spawn Fall");
	}
	
	public DamageManager getDamageManager()
	{
		return _damageManager;
	}
	
	public static void notify(Player player, String message)
	{
		UtilPlayer.message(player, F.main("Clans", message));
	}
}
