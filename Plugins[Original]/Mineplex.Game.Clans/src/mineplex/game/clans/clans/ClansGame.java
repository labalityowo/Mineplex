package mineplex.game.clans.clans;

import java.sql.Timestamp;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansUtility.ClanRelation;
import mineplex.game.clans.clans.event.ClansWaterPlaceEvent;
import mineplex.game.clans.clans.siege.weapon.projectile.event.CraterExplodeEvent;
import mineplex.game.clans.core.repository.ClanTerritory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class ClansGame extends MiniPlugin
{
	public enum Perm implements Permission
	{
		DUPE_ALERT,
	}

	private static final int CLICKS_TO_OPEN_TRAPPED = 10;
	private ClansManager _clans;
	
	public ClansGame(JavaPlugin plugin, ClansManager clans)
	{
		super("Clans Game", plugin);
		
		_clans = clans;
		setupSafes();
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.TRAINEE.setPermission(Perm.DUPE_ALERT, true, true);
	}
	
	private void setupSafes()
	{
		Iterator<Recipe> it = Bukkit.getServer().recipeIterator();
		while (it.hasNext())
		{
			Recipe recipe = it.next();
			if (recipe != null && recipe.getResult().getType() == Material.TRAPPED_CHEST)
			{
				it.remove();
			}
		}
		
		ShapedRecipe safe = new ShapedRecipe(new ItemBuilder(Material.TRAPPED_CHEST).setAmount(1).setTitle(C.cDGray + "Safe").build())
							.shape(new String[] {"III", "ICI", "III"})
							.setIngredient('I', Material.IRON_INGOT)
							.setIngredient('C', Material.CHEST);
		
		Bukkit.addRecipe(safe);
	}
	
	public static boolean isDupedFromClassShop(ItemStack item)
	{
		if (item == null || item.getType() == Material.AIR)
		{
			return false;
		}
		if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName())
		{
			return false;
		}
		String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).toUpperCase();
		if (name.contains("APPLY DEFAULT BUILD") || name.contains("APPLY BUILD") || name.contains("EDIT BUILD") || name.contains("DELETE BUILD"))
		{
			return true;
		}
		
		return false;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void openClanShop(PlayerInteractEvent event)
	{
		boolean hasItem = event.getPlayer().getItemInHand().getType() != Material.AIR;
		
		if (UtilEvent.isAction(event, ActionType.R_BLOCK) && event.getClickedBlock().getType() == Material.ENCHANTMENT_TABLE)
		{
			if (!event.getPlayer().isSneaking() || !hasItem)
			{
				if (Recharge.Instance.use(event.getPlayer(), "Open Skill Table GUI", 1000, false, false))
				{
					runSyncLater(() ->
					{
						if (event.getPlayer().isDead() || !event.getPlayer().isOnline() || !event.getPlayer().isValid())
						{
							return;
						}
						if (UtilMath.offsetSquared(event.getClickedBlock().getLocation(), event.getPlayer().getLocation()) > 36)
						{
							return;
						}
						_clans.getClassShop().attemptShopOpen(event.getPlayer());
					}, 10);
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEnchant(EnchantItemEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void BlockBurn(BlockBurnEvent event)
	{
		if (_clans.getClanUtility().isBorderlands(event.getBlock().getLocation())) event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void BlockSpread(BlockIgniteEvent event)
	{
		if (event.getCause() == IgniteCause.SPREAD) if (_clans.getClanUtility().isBorderlands(event.getBlock().getLocation())) event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void BlockPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();

		if (isDupedFromClassShop(event.getItemInHand()))
		{
			event.setCancelled(true);
			for (Player p : Bukkit.getOnlinePlayers())
			{
				if (ClansManager.getInstance().getClientManager().Get(p).hasPermission(Perm.DUPE_ALERT))
				{
					UtilPlayer.message(p, F.elem("[" + C.cRedB + "!" + C.cGray + "] ") + player.getName() + " just tried to use a duped item/block!");
				}
			}
			final int slot = player.getInventory().getHeldItemSlot();
			ClansManager.getInstance().runSyncLater(() ->
			{
				player.getInventory().setItem(slot, new ItemStack(Material.AIR));
				player.updateInventory();
			}, 1L);

			return;
		}

		if (_clans.getClanUtility().getAccess(player, event.getBlock().getLocation()) == ClanRelation.SELF)
		{
			if (_clans.getClanUtility().getOwner(event.getBlock().getLocation()) != null)
			{
				if (event.getBlock().getType() == Material.HOPPER && (event.getBlock().getRelative(BlockFace.UP).getType() == Material.CHEST || event.getBlock().getRelative(BlockFace.UP).getType() == Material.TRAPPED_CHEST))
				{
					if (_clans.getClanUtility().getRole(player) == ClanRole.RECRUIT)
					{
						UtilPlayer.message(event.getPlayer(), F.main("Clans", "Recruits cannot siphon items out of chests with a hopper!"));
						event.setCancelled(true);
					}
				}
			}

			return;
		}

		final Block block = event.getBlock();

		if (block.getType() != Material.LADDER)
		{
			return;
		}

		if (_clans.getWorldEvent().isInEvent(block.getLocation().add(0.5, 0.5, 0.5), false) || block.equals(player.getLocation().getBlock()))
		{
			player.sendMessage("Cancelled dodgy ladder placement");
			event.setCancelled(true);
		}
		else
		{
			_clans.runSyncLater(() -> _clans.getBlockRestore().add(block, 65, block.getData(), 0, (byte) 0, 30000), 0);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void teleport(PlayerTeleportEvent event)
	{
		if (event.getCause() == TeleportCause.UNKNOWN)
		{
			UtilPlayer.closeInventoryIfOpen(event.getPlayer());
		}
	}

	@EventHandler
	public void onFishHookEvent(PlayerFishEvent event)
	{
		if (event.getCaught() instanceof Player)
		{
			Player defender = (Player) event.getCaught();
			Player attacker = event.getPlayer();
			
			if (!_clans.getClanUtility().canHurt(defender, attacker))
			{
				event.setCancelled(true);
				ClanRelation rel = _clans.getRelation(defender, attacker);
				UtilPlayer.message(attacker, F.main("Clans", "You cannot harm " + _clans.getClanUtility().mRel(rel, defender.getName(), false) + "."));
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerFillBucket(PlayerBucketFillEvent event)
	{
		event.setCancelled(true);
		UtilPlayer.message(event.getPlayer(), F.main("Clans", "You cannot fill buckets!"));
		_clans.runSyncLater(() ->
		{
			event.getPlayer().updateInventory();
			Block block = event.getBlockClicked().getRelative(event.getBlockFace());
			event.getPlayer().sendBlockChange(block.getLocation(), block.getType(), block.getData());
		}, 1L);
	}
	
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
	{
		Material bucket = event.getBucket();
		
		if (bucket == Material.LAVA_BUCKET || bucket == Material.WATER_BUCKET)
		{
			event.setCancelled(true);
			UtilPlayer.message(event.getPlayer(), F.main("Clans", "You cannot empty buckets!"));
		}
	}
	
	@EventHandler
	public final void onFoodLevelChangeEvent(FoodLevelChangeEvent event)
	{
		((Player) event.getEntity()).setSaturation(3.8F); // While not entirely
															// accurate, this is
															// a pretty good
															// guess at original
															// food level
															// changes
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void BlockBreak(BlockBreakEvent event)
	{
		if (event.getPlayer().getWorld().getEnvironment() != Environment.NORMAL || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		
		String mimic = _clans.Get(event.getPlayer()).getMimic();
		
		if (mimic.length() != 0)
		{
			if (_clans.getClanUtility().searchClanPlayer(event.getPlayer(), mimic, false) != null)
				mimic = C.cGray + " You are currently mimicing " + C.cGold + mimic;
			else
				mimic = "";
		}
		
		// Borderlands
		if (_clans.getClanUtility().isBorderlands(event.getBlock().getLocation()) && event.getPlayer().getGameMode() != GameMode.CREATIVE)
		{
			// Disallow
			event.setCancelled(true);
			
			// Inform
			UtilPlayer.message(event.getPlayer(), F.main("Clans", "You can not break blocks in " + F.elem("Borderlands") + "."));
			return;
		}
		
		if (_clans.getClanBlocks().canBreak(event.getBlock().getTypeId())) return;
		
		if (_clans.getClanUtility().getAccess(event.getPlayer(), event.getBlock().getLocation()) == ClanRelation.SELF)
		{
			// Disallow Shops
			if (event.getBlock().getType() == Material.ENDER_CHEST || event.getBlock().getType() == Material.ENCHANTMENT_TABLE) if (_clans.getClanUtility().isSafe(event.getBlock().getLocation()))
			{
				// Disallow
				event.setCancelled(true);
				
				// Inform
				UtilPlayer.message(event.getPlayer(), F.main("Clans", "You can not break blocks in " + _clans.getClanUtility().getOwnerStringRel(event.getBlock().getLocation(), event.getPlayer()) + C.mBody + "'s Territory."));
			}
			
			// Disallow Recruit Chest
			if (_clans.getClanUtility().isClaimed(event.getBlock().getLocation()))
			{
				if (event.getBlock().getType() == Material.CHEST || event.getBlock().getType() == Material.TRAPPED_CHEST)
				{
					if (_clans.getClanUtility().getRole(event.getPlayer()) == ClanRole.RECRUIT)
					{
						// Disallow
						event.setCancelled(true);
						
						// Inform
						UtilPlayer.message(event.getPlayer(), F.main("Clans", "Clan Recruits cannot break " + F.elem(ItemStackFactory.Instance.GetName(event.getBlock(), true)) + "."));
					}
				}
			}
			
			// Allow
			return;
		}
		
		if (_clans.getClan(event.getPlayer()) != null && _clans.getClanUtility().isClaimed(event.getBlock().getLocation()))
		{
			if (_clans.getWarManager().isBeingBesiegedBy(_clans.getClanUtility().getOwner(event.getBlock().getLocation()), _clans.getClan(event.getPlayer())))
			{
				if (event.getBlock().getType() == Material.CHEST || event.getBlock().getType() == Material.DISPENSER || event.getBlock().getType() == Material.DROPPER || event.getBlock().getType() == Material.FURNACE || event.getBlock().getType() == Material.BURNING_FURNACE)
				{
					return;
				}
				if (event.getBlock().getType() == Material.TRAPPED_CHEST)
				{
					if (Recharge.Instance.use(event.getPlayer(), "Chest Crack", 1000, true, false))
					{
						int clicksRemain = CLICKS_TO_OPEN_TRAPPED;
						if (event.getBlock().hasMetadata("CLICKS - " + event.getPlayer().getName()))
						{
							clicksRemain = event.getBlock().getMetadata("CLICKS - " + event.getPlayer().getName()).get(0).asInt();
							event.getBlock().removeMetadata("CLICKS - " + event.getPlayer().getName(), _clans.getPlugin());
						}

						clicksRemain--;
						if (clicksRemain > 0)
						{
							UtilPlayer.message(event.getPlayer(), F.main("Clans", "You must try to break that chest " + clicksRemain + " more times before it will yield!"));
							event.getBlock().setMetadata("CLICKS - " + event.getPlayer().getName(), new FixedMetadataValue(_clans.getPlugin(), clicksRemain));
						}
						else
						{
							return;
						}
					}

					event.setCancelled(true);
					return;
				}
			}
		}
		
		// Disallow
		event.setCancelled(true);
		
		// Inform
		UtilPlayer.message(event.getPlayer(), F.main("Clans", "You can not break blocks in " + _clans.getClanUtility().getOwnerStringRel(event.getBlock().getLocation(), event.getPlayer()) + C.mBody + "'s Territory." + mimic));
	}
	
	@EventHandler
	public void respawn(PlayerRespawnEvent event)
	{
		_clans.getItemMapManager().setMap(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled()) return;
		
		Player damagee = event.GetDamageePlayer();
		if (damagee == null) return;
		
		Player damager = event.GetDamagerPlayer(true);
		if (damager == null) return;
		
		if (!_clans.getClanUtility().canHurt(damagee, damager))
		{
			// Cancel
			event.SetCancelled("Clans Ally");

			// Inform
			if (damager != null)
			{
				ClanRelation rel = _clans.getRelation(damagee, damager);
				UtilPlayer.message(damager, F.main("Clans", "You cannot harm " + _clans.getClanUtility().mRel(rel, damagee.getName(), false) + "."));
			}
		}
	}
	
	// Block Interact and Placement
	public void Interact(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		
		if (player.getWorld().getEnvironment() != Environment.NORMAL) return;
		
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
		
		// Block Interaction
		Location loc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
		if (UtilBlock.usable(event.getClickedBlock())) loc = event.getClickedBlock().getLocation();
		
		if (_clans.getClan(player) != null && _clans.getClanUtility().isClaimed(loc))
		{
			if (_clans.getWarManager().isBeingBesiegedBy(_clans.getClanUtility().getOwner(loc), _clans.getClan(player)))
			{
				if (loc.getBlock().getType() == Material.CHEST || loc.getBlock().getType() == Material.DISPENSER || loc.getBlock().getType() == Material.DROPPER || loc.getBlock().getType() == Material.FURNACE || loc.getBlock().getType() == Material.BURNING_FURNACE)
				{
					return;
				}
				if (loc.getBlock().getType() == Material.TRAPPED_CHEST)
				{
					if (Recharge.Instance.use(player, "Chest Crack", 1000, true, false))
					{
						int clicksRemain = CLICKS_TO_OPEN_TRAPPED;
						if (loc.getBlock().hasMetadata("CLICKS - " + player.getName()))
						{
							clicksRemain = loc.getBlock().getMetadata("CLICKS - " + player.getName()).get(0).asInt();
							loc.getBlock().removeMetadata("CLICKS - " + player.getName(), _clans.getPlugin());
						}
						
						clicksRemain--;
						if (clicksRemain > 0)
						{
							UtilPlayer.message(player, F.main("Clans", "You must try to open that chest " + clicksRemain + " more times before it will yield!"));
							loc.getBlock().setMetadata("CLICKS - " + player.getName(), new FixedMetadataValue(_clans.getPlugin(), clicksRemain));
						}
						else
						{
							return;
						}
					}
					
					event.setCancelled(true);
					return;
				}
			}
		}
		
		// Borderlands
		if (player.getGameMode() != GameMode.CREATIVE && player.getItemInHand() != null && _clans.getClanBlocks().denyUsePlace(player.getItemInHand().getTypeId()) && _clans.getClanUtility().isBorderlands(event.getClickedBlock().getLocation()))
		{
			// Disallow
			event.setCancelled(true);
			
			// Inform
			UtilPlayer.message(player, F.main("Clans", "You cannot place blocks in " + F.elem("Borderlands") + "."));
			return;
		}

		// Banners/String/Heads
		if (player.getGameMode() != GameMode.CREATIVE && player.getItemInHand() != null)
		{
			if (player.getItemInHand().getType() == Material.BANNER || player.getItemInHand().getType() == Material.STRING
					|| player.getItemInHand().getType() == Material.SKULL_ITEM)
			{
				Location destLocation = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
				ClanTerritory territory = _clans.getClanUtility().getClaim(destLocation);
				if (territory != null)
				{
					if (territory.Owner.equals("Shops") || territory.Owner.equals("Fields") || territory.Owner.equals("Spawn") || territory.Owner.equals("Borderlands")) {
						// Disallow
						event.setCancelled(true);

						// Inform
						UtilPlayer.message(player, F.main("Clans", "You cannot place that in " + F.elem(_clans.getClanUtility().getOwnerStringRel(destLocation, player)) + "."));
						return;
					}
				}
			}
		}

		ClanRelation access = _clans.getClanUtility().getAccess(player, loc);
		ClanInfo clan = _clans.getClan(player);
		ClanInfo mimicClan = _clans.getClanAdmin().getMimic(player, false);
		ClanInfo blockClan = _clans.getClanUtility().getClaim(loc) == null ? null : _clans.getClan(_clans.getClanUtility().getClaim(loc).Owner);
		if (blockClan != null && blockClan.equals(mimicClan)) access = ClanRelation.SELF;
		
		// Doors, chests, & furnaces
		if (blockClan != null && (!blockClan.equals(clan) && !blockClan.equals(mimicClan)) && (event.getAction() == Action.RIGHT_CLICK_BLOCK && (loc.getBlock().getType().name().contains("DOOR") || loc.getBlock().getType().name().contains("GATE") || UtilItem.doesHaveGUI(loc.getBlock().getType()))))
		{
			UtilPlayer.message(player, F.main("Clans", "You are not allowed to use that here."));
			event.setCancelled(true);
			return;
		}
		
		// Hoe Return
		if (access != ClanRelation.SELF && !UtilBlock.usable(event.getClickedBlock()))
		{
			if (UtilGear.isHoe(player.getItemInHand()))
			{
				event.setCancelled(true);
				return;
			}
		}
		
		// Full Access
		if (access == ClanRelation.SELF)
		{
			// Recruits cannot open Chests // IN OWN CLAIMED LAND
			if (event.getClickedBlock().getTypeId() == 54 && _clans.getClanUtility().getOwner(loc) != null)
			{
				if (_clans.getClanUtility().getRole(player) == ClanRole.RECRUIT)
				{
					// Disallow
					event.setCancelled(true);
					
					// Inform
					UtilPlayer.message(player, F.main("Clans", "Clan Recruits cannot access " + F.elem(ItemStackFactory.Instance.GetName(event.getClickedBlock(), true)) + "."));
				}
			}
			
			// Wilderness Adjacent
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !UtilBlock.usable(event.getClickedBlock()) && player.getItemInHand() != null && _clans.getClanBlocks().denyUsePlace(player.getItemInHand().getTypeId()) && !_clans.getClanUtility().isClaimed(loc))
			{
				
				String enemy = null;
				boolean self = false;
				
				for (int x = -1; x <= 1; x++)
					for (int z = -1; z <= 1; z++)
					{
						if (self) continue;
						
						if (x != 0 && z != 0 || x == 0 && z == 0) continue;
						
						Location sideLoc = new Location(loc.getWorld(), loc.getX() + x, loc.getY(), loc.getZ() + z);
						
						if (_clans.getClanUtility().isSelf(player, sideLoc)) self = true;
						
						if (enemy != null) continue;
						
						if (_clans.getClanUtility().getAccess(player, sideLoc) != ClanRelation.SELF) enemy = _clans.getClanUtility().getOwnerStringRel(new Location(loc.getWorld(), loc.getX() + x, loc.getY(), loc.getZ() + z), player);
					}
					
				if (enemy != null && !self)
				{
					// Disallow
					event.setCancelled(true);
					
					// Inform
					UtilPlayer.message(player, F.main("Clans", "You cannot place blocks next to " + enemy + "."));
					
					return;
				}
			}
			
			return;
		}
		
		String mimic = _clans.Get(player).getMimic();
		
		if (mimic.length() != 0)
		{
			if (_clans.getClanUtility().searchClanPlayer(player, mimic, false) != null)
				mimic = C.cGray + " You are currently mimicing " + C.cGold + mimic;
			else
				mimic = "";
		}
		
		// Deny Interaction
		if (_clans.getClanBlocks().denyInteract(event.getClickedBlock().getTypeId()))
		{
			// Block Action
			if (access == ClanRelation.NEUTRAL)
			{
				Location home = event.getClickedBlock() == null ? null : _clans.getClanUtility().getClanByClanName(_clans.getClanUtility().getClaim(event.getClickedBlock().getLocation()).Owner).getHome();
				
				// Allow Field Chest
				if (event.getClickedBlock().getTypeId() == 54)
				{
					if (_clans.getClanUtility().isSpecial(event.getClickedBlock().getLocation(), "Fields"))
					{
						return;
					}
				}
				
				// Allow Clan's Home
				if (home != null && event.getClickedBlock().getLocation().distance(home) <= 2 && event.getClickedBlock().getType().equals(Material.BED_BLOCK))
				{
					return;
				}
				
				// Disallow
				event.setCancelled(true);
				
				// Inform
				UtilPlayer.message(player, F.main("Clans", "You cannot use items in " + _clans.getClanUtility().getOwnerStringRel(event.getClickedBlock().getLocation(), player)  + C.mBody + "'s Territory." + mimic));
				
				return;
			}
			// Block is not Trust Allowed
			else if (!_clans.getClanBlocks().allowInteract(event.getClickedBlock().getTypeId()) || access != ClanRelation.ALLY_TRUST)
			{
				Location home = event.getClickedBlock() == null ? null : _clans.getClanUtility().getClanByClanName(_clans.getClanUtility().getClaim(event.getClickedBlock().getLocation()).Owner).getHome();
				
				// Allow Clan's Home
				if (home != null && event.getClickedBlock().getLocation().distance(home) <= 2 && event.getClickedBlock().getType().equals(Material.BED_BLOCK))
				{
					return;
				}
				
				// Disallow
				event.setCancelled(true);
				
				// Inform
				UtilPlayer.message(player, F.main("Clans", "You cannot use items in " + _clans.getClanUtility().getOwnerStringRel(event.getClickedBlock().getLocation(), player)  + C.mBody +  "'s Territory." + mimic));
				
				return;
			}
		}
		
		// Block Placement
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			if (player.getItemInHand().getType() != Material.AIR)
			{
				if (player.getGameMode() != GameMode.CREATIVE && _clans.getClanBlocks().denyUsePlace(player.getItemInHand().getTypeId()))
				{
					// Disallow
					event.setCancelled(true);
					
					// Inform
					UtilPlayer.message(player, F.main("Clans", "You cannot place blocks in " + _clans.getClanUtility().getOwnerStringRel(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation(), player)  + C.mBody +  "'s Territory." + mimic));
					
					return;
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void doorPlaced(BlockPlaceEvent event)
	{
		if (event.getBlockPlaced().getType().equals(Material.ACACIA_DOOR)
	     || event.getBlockPlaced().getType().equals(Material.WOODEN_DOOR)
	     || event.getBlockPlaced().getType().equals(Material.BIRCH_DOOR)
	     || event.getBlockPlaced().getType().equals(Material.DARK_OAK_DOOR)
	     || event.getBlockPlaced().getType().equals(Material.JUNGLE_DOOR)
	     || event.getBlockPlaced().getType().equals(Material.SPRUCE_DOOR)
	     || event.getBlockPlaced().getType().equals(Material.WOOD_DOOR)
	     || event.getBlockPlaced().getType().equals(Material.IRON_DOOR_BLOCK))
		{
			ClanTerritory claim = _clans.getClanUtility().getClaim(event.getBlockPlaced().getLocation());
			
			if (claim != null && (claim.Owner.equals("Spawn") || claim.Owner.equals("Shops") || claim.Owner.equals("Fields") || _clans.getClanUtility().getAccess(event.getPlayer(), event.getBlockPlaced().getLocation()) != ClanRelation.SELF))
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void Piston(BlockPistonExtendEvent event)
	{
		ClanInfo pistonClan = _clans.getClanUtility().getOwner(event.getBlock().getLocation());
		
		Block push = event.getBlock();
		for (int i = 0; i < 13; i++)
		{
			push = push.getRelative(event.getDirection());
			
			Block front = push.getRelative(event.getDirection()).getRelative(event.getDirection());
			
			if (push.getType() == Material.AIR) return;
			
			ClanInfo pushClan = _clans.getClanUtility().getOwner(front.getLocation());
			
			if (pushClan == null) continue;
			
			if (pushClan.isAdmin()) continue;
			
			if (pistonClan == null)
			{
				push.getWorld().playEffect(push.getLocation(), Effect.STEP_SOUND, push.getTypeId());
				event.setCancelled(true);
				return;
			}
			
			if (pistonClan.equals(pushClan)) continue;
			
			push.getWorld().playEffect(push.getLocation(), Effect.STEP_SOUND, push.getTypeId());
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void Quit(PlayerQuitEvent event)
	{
		ClanInfo clan = _clans.getClanUtility().getClanByPlayer(event.getPlayer());
		if (clan == null) return;

		clan.setLastOnline(new Timestamp(System.currentTimeMillis()));
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void Explosion(EntityExplodeEvent event)
	{
		try
		{
			if (event.getEntityType() != EntityType.PRIMED_TNT && event.getEntityType() != EntityType.MINECART_TNT) return;
		}
		catch (Exception e)
		{
			return;
		}
		
		ClanInfo clan = _clans.getClanUtility().getOwner(event.getEntity().getLocation());
		if (clan == null) return;
		
		clan.inform(C.cRed + "Your Territory is under attack!", null);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void Explosion(CraterExplodeEvent event)
	{
		for (Block block : event.getBlocks())
		{
			ClanInfo clan = _clans.getClanUtility().getOwner(block.getLocation());
			if (clan == null)
			{
				return;
			}
			
			if (Recharge.Instance.use(event.getShooter(), "TNT TERRITORY ALERT", 1000, false, false))
			{
				clan.inform(C.cRed + "Your Territory is under attack!", null);
			}
		}
	}
	
	@EventHandler
	public void onClanHomeInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.L_BLOCK) && !UtilEvent.isAction(event, ActionType.R_BLOCK))
		{
			return;
		}
		
		ClanInfo clan = _clans.getClan(event.getPlayer());
		
		Block block = event.getClickedBlock();
		
		if (UtilEvent.isAction(event, ActionType.R_BLOCK) && block.getType() == Material.BED_BLOCK)
		{
			event.setCancelled(true);
			return;
		}
		
		Player player = event.getPlayer();
		
		if (clan == null)
		{
			for (String otherClanName : _clans.getClanNameSet())
			{
				boolean done = false;
				ClanInfo otherClan = _clans.getClan(otherClanName);
				
				if (otherClan.getHome() == null)
				{
					return;
				}
				
				if (!(UtilMath.offset(block.getLocation(), otherClan.getHome()) < 3))
				{
					return;
				}
				
				for (int x = -2; x < 2; x++)
				{
					for (int z = -2; z < 2; z++)
					{
						Block otherBlock = event.getClickedBlock().getLocation().add(x, 0, z).getBlock();
						if (otherBlock.getType().equals(Material.BED_BLOCK))
						{
							if (UtilEvent.isAction(event, ActionType.R_BLOCK))
							{
								event.setCancelled(true);
								done = true;
								break;
							}
						}
					}
					
					if (done)
					{
						break;
					}
				}
				
				if (done)
				{
					break;
				}
			}
			
			return;
		}
		
		if (clan.getHome() == null)
		{
			return;
		}
		
		if (!(UtilMath.offset(block.getLocation(), clan.getHome()) < 3))
		{
			return;
		}
		
		for (int x = -2; x < 2; x++)
		{
			for (int z = -2; z < 2; z++)
			{
				Block otherBlock = event.getClickedBlock().getLocation().clone().add(x, 0, z).getBlock();
				if (otherBlock.getType().equals(Material.BED_BLOCK))
				{
					if (UtilEvent.isAction(event, ActionType.R_BLOCK))
					{
						event.setCancelled(true);
						break;
					}
					
					if (clan.getMembers().get(player.getUniqueId()).getRole() == ClanRole.LEADER || clan.getMembers().get(player.getUniqueId()).getRole() == ClanRole.ADMIN)
					{
						break;
					}
					else
					{
						UtilPlayer.message(player, F.main("Clans", "Only the Clan Leader and Admins can manage the Clan Home."));
						break;
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlaceBed(BlockPlaceEvent event)
	{
		if (event.getItemInHand().getType().equals(Material.BED) || event.getItemInHand().getType().equals(Material.BED_BLOCK))
		{
			UtilPlayer.message(event.getPlayer(), F.main("Clans", "You cannot manually place beds, to set your clan home, type " + F.elem("/c sethome") + "."));
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onClanHomeDestroyed(BlockBreakEvent event)
	{
		ClanInfo userClan = _clans.getClan(event.getPlayer());
		
		Block block = event.getBlock();
		Player player = event.getPlayer();
		
		ClanInfo blockClan = null;
		ClanTerritory claim = _clans.getClanUtility().getClaim(block.getLocation());
		
		if (claim != null)
		{
			if (_clans.getBlacklist().allowed(claim.Owner))
			{
				blockClan = _clans.getClan(claim.Owner);
			}
		}
		
		if (blockClan == null)
		{
			return;
		}
		
		if (blockClan.getHome() == null)
		{
			return;
		}
		
		if (UtilMath.offset(block.getLocation(), blockClan.getHome()) > 2)
		{
			return;
		}
		
		if (!block.getType().equals(Material.BED_BLOCK))
		{
			return;
		}
		
		if (!blockClan.equals(userClan))
		{
			if (userClan != null && blockClan.isAlly(userClan))
			{
				UtilPlayer.message(player, F.main("Clans", "You cannot remove an Ally's Clan Home."));
			}
			else
			{
				UtilPlayer.message(player, F.main("Clans", "You have destroyed " + F.elem(blockClan.getName()) + "'s Clan Home!"));
				
				UtilBlock.deleteBed(blockClan.getHome());
				
				blockClan.setHome(null);
			}
			
			return;
		}
		
		if (userClan.getMembers().get(player.getUniqueId()).getRole() == ClanRole.LEADER || userClan.getMembers().get(player.getUniqueId()).getRole() == ClanRole.ADMIN)
		{
			event.setCancelled(true);
			
			UtilBlock.deleteBed(blockClan.getHome());
			
			blockClan.setHome(null);
			UtilPlayer.message(player, F.main("Clans", "You have removed your Clan Home."));
		}
		else
		{
			event.setCancelled(true);
			UtilPlayer.message(player, F.main("Clans", "Only the Clan Leader and Admins can remove the Clan Home."));
		}
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.FAST)
		{
			for (Player player : Bukkit.getOnlinePlayers())
			{
				if (player.getGameMode() == GameMode.CREATIVE)
				{
					return;
				}
				
				UtilInv.removeAll(player, Material.BED, (byte) 0);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPickupSafe(PlayerPickupItemEvent event)
	{
		Item item = event.getItem();
		ItemStack stack = item.getItemStack();
		if (stack.getType() == Material.TRAPPED_CHEST)
		{
			event.setCancelled(true);
			item.remove();
			UtilInv.insert(event.getPlayer(), new ItemBuilder(Material.TRAPPED_CHEST).setAmount(stack.getAmount()).setTitle(C.cDGray + "Safe").build());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPickupSafe(InventoryPickupItemEvent event)
	{
		Item item = event.getItem();
		ItemStack stack = item.getItemStack();
		if (stack.getType() == Material.TRAPPED_CHEST)
		{
			event.setCancelled(true);
			item.remove();
			event.getInventory().addItem(new ItemBuilder(Material.TRAPPED_CHEST).setAmount(stack.getAmount()).setTitle(C.cDGray + "Safe").build());
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onBlockDispense(BlockDispenseEvent event)
	{
		if (event.getItem().getType() == Material.WATER_BUCKET || event.getItem().getType() == Material.LAVA_BUCKET || event.getItem().getType() == Material.BUCKET)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void noMonsterLava(BlockFromToEvent event)
	{
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
	}
	
	@EventHandler
	public void onUseWater(ClansWaterPlaceEvent event)
	{
		for (BlockFace bf : BlockFace.values())
		{
			if (event.getBlock().getRelative(bf).getType().toString().contains("LAVA"))
			{
				event.setCancelled(true);
			}
		}
	}
}