package nautilus.game.arcade.managers;

import net.minecraft.server.v1_8_R3.EntityPlayer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.Managers;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.antihack.AntiHack;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.preferences.Preference;
import mineplex.core.recharge.Recharge;
import mineplex.core.teleport.event.MineplexTeleportEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerDeathOutEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.GameTeam.PlayerState;
import nautilus.game.arcade.kit.perks.event.PerkDestructorBlockEvent;
import nautilus.game.arcade.world.WorldData;

public class GameFlagManager implements Listener
{
	public enum Perm implements Permission
	{
		BYPASS_TELEPORT_KICK,
	}

	final ArcadeManager Manager;
	private SecondaryDamageManager Secondary;

	public GameFlagManager(ArcadeManager manager)
	{
		Manager = manager;

		Manager.getPluginManager().registerEvents(this, Manager.getPlugin());

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.DEV.setPermission(Perm.BYPASS_TELEPORT_KICK, true, true);
		if (UtilServer.isTestServer())
		{
			PermissionGroup.QA.setPermission(Perm.BYPASS_TELEPORT_KICK, true, true);
			PermissionGroup.ADMIN.setPermission(Perm.BYPASS_TELEPORT_KICK, true, true);
		}
	}

	@EventHandler
	public void triggerSecondary(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;

		if (Secondary == null)
		{
			if (!Manager.GetDamage().IsEnabled())
			{
				Secondary = new SecondaryDamageManager(Manager);
				Bukkit.getPluginManager().registerEvents(Secondary, Manager.getPlugin());
			}
		}
		else
		{
			if (Manager.GetDamage().IsEnabled())
			{
				if (Secondary != null)
				{
					HandlerList.unregisterAll(Secondary);
					Secondary = null;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void DamageEvent(CustomDamageEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)
		{
			event.SetCancelled("Game Null");
			return;
		}

		LivingEntity damagee = event.GetDamageeEntity();
		LivingEntity damager = event.GetDamagerEntity(true);

		if (damagee != null && damagee.getWorld().getName().equals("world"))
		{
			event.SetCancelled("In Lobby");

			if (event.GetCause() == DamageCause.VOID)
				damagee.teleport(Manager.GetLobby().getSpawn());

			return;
		}

		//Damagee Spec
		if (damagee != null && Manager.isSpectator(damagee))
		{
			event.SetCancelled("Damagee Spectator");

			if (damagee.getFireTicks() > 0)
			{
				damagee.setFireTicks(0);
			}

			return;
		}

		//Damager Spec
		if (damager != null && Manager.isSpectator(damager))
		{
			event.SetCancelled("Damager Spectator");
			return;
		}

		if (!game.Damage)
		{
			event.SetCancelled("Damage Disabled");
			return;
		}

		if (game.GetState() != GameState.Live)
		{
			event.SetCancelled("Game not Live");
			return;
		}

		if (damagee != null && damagee instanceof Player && !game.IsAlive((Player) damagee))
		{
			event.SetCancelled("Damagee Not Playing");
			return;
		}

		if (damager != null && damager instanceof Player && !game.IsAlive((Player) damager))
		{
			event.SetCancelled("Damager Not Playing");
			return;
		}

		if (event.GetCause() == DamageCause.FALL && !game.DamageFall)
		{
			event.SetCancelled("Fall Damage Disabled");
			return;
		}

		//Entity vs Entity
		if (damagee != null && damager != null)
		{
			//PvP
			if (damagee instanceof Player && damager instanceof Player)
			{
				if (!Manager.canHurt((Player) damagee, (Player) damager))
				{
					event.SetCancelled("PvP Disabled");
					return;
				}
			}
			//PvE
			else if (damager instanceof Player)
			{
				if (!game.DamagePvE)
				{
					event.SetCancelled("PvE Disabled");
					return;
				}
			}
			//EvP
			else if (damagee instanceof Player)
			{
				if (!game.DamageEvP)
				{
					event.SetCancelled("EvP Disabled");
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void DamageExplosion(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.ENTITY_EXPLOSION && event.GetCause() != DamageCause.BLOCK_EXPLOSION)
			return;

		Player damagee = event.GetDamageePlayer();
		if (damagee == null) return;

		Player damager = event.GetDamagerPlayer(true);
		if (damager == null) return;

		if (Manager.canHurt(damagee, damager))
			return;

		event.SetCancelled("Allied Explosion");
	}


	@EventHandler(priority = EventPriority.LOWEST)
	public void ItemPickupEvent(PlayerPickupItemEvent event)
	{
		Player player = event.getPlayer();

		Game game = Manager.GetGame();

		if (game == null || !game.IsAlive(player) || game.GetState() != GameState.Live)
		{
			event.setCancelled(true);
			return;
		}


		if (game.ItemPickup)
		{
			if (game.ItemPickupDeny.contains(event.getItem().getItemStack().getTypeId()))
			{
				event.setCancelled(true);
			}
		}
		else
		{
			if (!game.ItemPickupAllow.contains(event.getItem().getItemStack().getTypeId()))
			{
				event.setCancelled(true);
			}
		}

	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void ItemDropEvent(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();

		Game game = Manager.GetGame();
		if (game == null || !game.IsAlive(player) || game.GetState() != GameState.Live)
		{
			//Only allow ops in creative
			if (!player.isOp() || player.getGameMode() != GameMode.CREATIVE)
			{
				event.setCancelled(true);
			}

			return;
		}

		if (game.ItemDrop)
		{
			if (game.ItemDropDeny.contains(event.getItemDrop().getItemStack().getTypeId()))
			{
				event.setCancelled(true);
			}
		}
		else
		{
			if (!game.ItemDropAllow.contains(event.getItemDrop().getItemStack().getTypeId()))
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void InventoryOpen(InventoryOpenEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)
			return;

		if (!game.InProgress())
			return;

		//normal inventories
		if (!game.InventoryOpenBlock)
		{
			if (event.getInventory().getType() == InventoryType.ANVIL ||
					event.getInventory().getType() == InventoryType.BEACON ||
					event.getInventory().getType() == InventoryType.BREWING ||
					event.getInventory().getType() == InventoryType.DISPENSER ||
					event.getInventory().getType() == InventoryType.DROPPER ||
					event.getInventory().getType() == InventoryType.ENCHANTING ||
					event.getInventory().getType() == InventoryType.FURNACE ||
					event.getInventory().getType() == InventoryType.HOPPER ||
					event.getInventory().getType() == InventoryType.MERCHANT ||
					event.getInventory().getType() == InventoryType.ENDER_CHEST ||
					event.getInventory().getType() == InventoryType.WORKBENCH ||
					event.getInventory().getType() == InventoryType.CHEST)
			{
				if (event.getInventory().getType() == InventoryType.CHEST)
				{
					if (event.getInventory().getHolder() != null && event.getInventory().getHolder() instanceof Chest || event.getInventory().getHolder() instanceof DoubleChest)
					{
						event.setCancelled(true);
						event.getPlayer().closeInventory();
					}
				}
				else
				{
					event.setCancelled(true);
					event.getPlayer().closeInventory();
				}
			}
		}

		//deal with chests
		if (!game.InventoryOpenChest)
		{
			if (event.getInventory().getType() == InventoryType.CHEST)
			{
				if (event.getInventory().getHolder() instanceof Chest || event.getInventory().getHolder() instanceof DoubleChest)
				{
					event.setCancelled(true);
					event.getPlayer().closeInventory();
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void InventoryClick(InventoryClickEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)
			return;

		if (!game.InProgress())
			return;

		if (game.InventoryClick)
			return;

		Player player = UtilPlayer.searchExact(event.getWhoClicked().getName());
		if (player != null && !game.IsAlive(player))
			return;

		if (!game.IsAlive(player))
			return;

		if (event.getInventory().getType() == InventoryType.CRAFTING)
		{
			event.setCancelled(true);
			event.getWhoClicked().closeInventory();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void BlockPlaceEvent(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();

		Game game = Manager.GetGame();
		if (game == null)
		{
			//Only allow ops in creative
			if (!player.isOp() || player.getGameMode() != GameMode.CREATIVE)
				event.setCancelled(true);
		}
		else
		{
			if (!game.IsAlive(player))
			{
				//Only allow ops in creative
				if (!player.isOp() || player.getGameMode() != GameMode.CREATIVE)
					event.setCancelled(true);
			}
			// Event Server Allowance
			else if (game.BlockPlaceCreative && player.getGameMode() == GameMode.CREATIVE)
			{
				return;
			}
			else
			{
				if (game.BlockPlace)
				{
					if (game.BlockPlaceDeny.contains(event.getBlock().getTypeId()))
					{
						event.setCancelled(true);
					}
				}
				else
				{
					if (!game.BlockPlaceAllow.contains(event.getBlock().getTypeId()))
					{
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void BlockBreakEvent(org.bukkit.event.block.BlockBreakEvent event)
	{
		Player player = event.getPlayer();

		Game game = Manager.GetGame();
		if (game == null)
		{
			//Only allow ops in creative
			if (!player.isOp() || player.getGameMode() != GameMode.CREATIVE)
				event.setCancelled(true);
		}
		else if (game.GetState() == GameState.Live)
		{
			if (!game.IsAlive(player))
			{
				event.setCancelled(true);
			}
			// Event Server Allowance
			else if (game.BlockBreakCreative && player.getGameMode() == GameMode.CREATIVE)
			{
				return;
			}
			else
			{
				if (game.BlockBreak)
				{
					if (game.BlockBreakDeny.contains(event.getBlock().getTypeId()))
					{
						event.setCancelled(true);
					}

				}
				else
				{
					if (!game.BlockBreakAllow.contains(event.getBlock().getTypeId()))
					{
						event.setCancelled(true);
					}
				}
			}
		}
		else
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void PrivateBlockPlace(BlockPlaceEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null) return;

		if (!game.PrivateBlocks)
			return;

		if (event.isCancelled())
			return;

		if (!UtilBlock.usable(event.getBlockPlaced()))
			return;

		if (event.getBlockPlaced().getType() != Material.CHEST &&
				event.getBlockPlaced().getType() != Material.FURNACE &&
				event.getBlockPlaced().getType() != Material.BURNING_FURNACE &&
				event.getBlockPlaced().getType() != Material.WORKBENCH)
			return;

		String privateKey = event.getPlayer().getName();

		//Add Empty
		if (!game.PrivateBlockCount.containsKey(privateKey))
			game.PrivateBlockCount.put(privateKey, 0);

		if (game.PrivateBlockCount.get(privateKey) == 4)
			return;

		game.PrivateBlockMap.put(event.getBlockPlaced().getLocation(), event.getPlayer());
		game.PrivateBlockCount.put(event.getPlayer().getName(), game.PrivateBlockCount.get(event.getPlayer().getName()) + 1);

		if (game.PrivateBlockCount.get(privateKey) == 4)
		{
			event.getPlayer().sendMessage(F.main(game.GetName(), "Protected block limit reached."));
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void PrivateBlockPlaceCancel(BlockPlaceEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null) return;

		if (!game.PrivateBlocks)
			return;

		if (event.isCancelled())
			return;

		Block block = event.getBlockPlaced();

		if (block.getType() != Material.CHEST)
			return;

		Player player = event.getPlayer();

		BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

		for (BlockFace face : faces)
		{
			Block other = block.getRelative(face);

			if (other.getType() != Material.CHEST)
				continue;

			if (!game.PrivateBlockMap.containsKey(other.getLocation()))
				continue;

			Player owner = game.PrivateBlockMap.get(other.getLocation());

			if (player.equals(owner))
				continue;

			//Allow Enemy Raiding
			GameTeam ownerTeam = game.GetTeam(owner);
			GameTeam playerTeam = game.GetTeam(player);

			if (ownerTeam != null && playerTeam != null && !ownerTeam.equals(playerTeam))
				continue;

			//Disallow
			UtilPlayer.message(event.getPlayer(), F.main("Game",
					"You cannot combine " +
							F.elem(C.cPurple + ItemStackFactory.Instance.GetName(event.getBlock(), false)) +
							" with " + F.elem(Manager.GetColor(owner) + owner.getName() + ".")));

			event.setCancelled(true);
			return;
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void PrivateBlockBreak(org.bukkit.event.block.BlockBreakEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null) return;

		if (!game.PrivateBlocks)
			return;

		if (event.isCancelled())
			return;

		if (!game.PrivateBlockMap.containsKey(event.getBlock().getLocation()))
			return;

		Player owner = game.PrivateBlockMap.get(event.getBlock().getLocation());
		Player player = event.getPlayer();

		//Same Team (or no team)
		if (owner.equals(player))
		{
			game.PrivateBlockMap.remove(event.getBlock().getLocation());
		}
		else
		{
			//Allow Enemy Raiding
			GameTeam ownerTeam = game.GetTeam(owner);
			GameTeam playerTeam = game.GetTeam(player);

			if (ownerTeam != null && playerTeam != null && !ownerTeam.equals(playerTeam))
				return;

			//Disallow
			UtilPlayer.message(event.getPlayer(), F.main("Game",
					F.elem(C.cPurple + ItemStackFactory.Instance.GetName(event.getBlock(), false)) +
							" belongs to " + F.elem(Manager.GetColor(owner) + owner.getName() + ".")));

			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void PrivateBlockUse(PlayerInteractEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null) return;

		if (!game.PrivateBlocks)
			return;

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (!UtilBlock.usable(event.getClickedBlock()))
			return;

		if (event.getClickedBlock().getType() != Material.CHEST &&
				event.getClickedBlock().getType() != Material.FURNACE &&
				event.getClickedBlock().getType() != Material.BURNING_FURNACE)
			return;

		if (!game.PrivateBlockMap.containsKey(event.getClickedBlock().getLocation()))
			return;

		Player owner = game.PrivateBlockMap.get(event.getClickedBlock().getLocation());

		if (!game.IsAlive(owner))
			return;

		Player player = event.getPlayer();

		if (owner.equals(player))
		{
			return;
		}
		else
		{
			//Allow Enemy Raiding
			GameTeam ownerTeam = game.GetTeam(owner);
			GameTeam playerTeam = game.GetTeam(player);

			if (ownerTeam != null && playerTeam != null && !ownerTeam.equals(playerTeam))
				return;

			//Disallow
			UtilPlayer.message(event.getPlayer(), F.main("Game",
					F.elem(C.cPurple + ItemStackFactory.Instance.GetName(event.getClickedBlock(), false)) +
							" belongs to " + F.elem(Manager.GetColor(owner) + owner.getName() + ".")));

			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void PrivateBlockCrumble(PerkDestructorBlockEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null) return;

		if (!game.PrivateBlocks)
			return;

		if (event.isCancelled())
			return;

		if (!game.PrivateBlockMap.containsKey(event.getBlock().getLocation()))
			return;

		Player owner = game.PrivateBlockMap.get(event.getBlock().getLocation());
		Player player = event.getPlayer();

		//Same Team (or no team)
		if (owner.equals(player))
		{
			game.PrivateBlockMap.remove(event.getBlock().getLocation());
		}
		else
		{
			//Allow Enemy Raiding
			GameTeam ownerTeam = game.GetTeam(owner);
			GameTeam playerTeam = game.GetTeam(player);

			if (ownerTeam != null && playerTeam != null && !ownerTeam.equals(playerTeam))
				return;

			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void PlayerDeath(PlayerDeathEvent event)
	{
		Game game = Manager.GetGame();

		if (game == null)
		{
			return;
		}

		Player player = event.getEntity();
		Location location = player.getLocation();

		// Remove all conditions
		Manager.GetCondition().EndCondition(player, null, null);
		for (PotionEffect potion : player.getActivePotionEffects())
		{
			player.removePotionEffect(potion.getType());
		}

		// Drop Items
		if (game.DeathDropItems)
		{
			event.getDrops().forEach(itemStack -> player.getWorld().dropItem(location, itemStack));
		}

		event.getDrops().clear();

		if (!game.IsLive())
		{
			return;
		}

		// Death Out
		if (game.DeathOut)
		{
			//Event
			PlayerDeathOutEvent outEvent = new PlayerDeathOutEvent(game, player);
			UtilServer.CallEvent(outEvent);

			if (!outEvent.isCancelled())
			{
				game.SetPlayerState(player, PlayerState.OUT);
				Manager.addSpectator(player, game.DeathTeleport);
				return;
			}
		}

		double respawnTime = game.DeathSpectateSecs;
		GameTeam team = game.GetTeam(player);

		if (team.GetRespawnTime() > respawnTime)
		{
			respawnTime = team.GetRespawnTime();
		}

		// Respawn Now
		if (respawnTime <= 0)
		{
			game.RespawnPlayer(player);

			Manager.runSyncLater(() ->
			{
				player.setFallDistance(0);
				player.setFireTicks(0);
				UtilAction.zeroVelocity(player);
			}, 0);
		}
		// Respawn Later
		else
		{
			UtilInv.Clear(player);
			Manager.GetCondition().Factory().Cloak("Ghost", player, player, respawnTime, false, false);
			player.setAllowFlight(true);
			player.setFlying(true);

			EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
			entityPlayer.spectating = true;
			entityPlayer.setGhost(true);
			entityPlayer.k = false;

			if (!team.IsAlive(player))
			{
				Manager.addSpectator(player, game.DeathTeleport);
				return;
			}

			if (respawnTime > 3)
			{
				UtilTextMiddle.display(null, "Respawning in " + UtilMath.trim(1, respawnTime) + " seconds...", 0, 30, 10, player);
			}

			long start = System.currentTimeMillis();
			long length = (long) (respawnTime * 1000);

			Manager.runSyncTimer(new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if (!game.IsLive())
					{
						cancel();
						return;
					}

					long left = start + length - System.currentTimeMillis();
					double percentage = left / (double) length;

					if (left <= 0)
					{
						cancel();

						if (!game.IsAlive(player))
						{
							Manager.addSpectator(player, game.DeathTeleport);
						}

						game.RespawnPlayer(player);
						UtilTextBottom.display(C.cGreenB + "Respawned", player);
					}
					else
					{
						UtilTextBottom.displayProgress("Respawning", percentage, UtilTime.MakeStr(left), player);
					}
				}
			}, 0, 1);
		}
	}

	@EventHandler
	public void PlayerQuit(PlayerQuitEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null) return;

		//Drop Items
		if (game.QuitDropItems)
			if (game.IsLive())
				if (game.IsAlive(event.getPlayer()))
					UtilInv.drop(event.getPlayer(), true);

		//Remove Kit
		game.getTeamModule().getPreferences().remove(event.getPlayer());
		game.GetPlayerKits().remove(event.getPlayer());
		game.GetPlayerGems().remove(event.getPlayer());

		if (!game.QuitOut)
			return;

		GameTeam team = game.GetTeam(event.getPlayer());

		if (team != null)
		{
			if (game.InProgress())
				game.SetPlayerState(event.getPlayer(), PlayerState.OUT);
			else
				team.RemovePlayer(event.getPlayer());
		}
	}

	@EventHandler
	public void PlayerMoveCancel(PlayerMoveEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null || game.GetState() != GameState.Prepare)
			return;

		if (!game.PrepareFreeze)
			return;

		if (!game.IsAlive(event.getPlayer()))
			return;

		if (UtilMath.offset2d(event.getFrom(), event.getTo()) <= 0)
			return;

		event.getFrom().setPitch(event.getTo().getPitch());
		event.getFrom().setYaw(event.getTo().getYaw());

		event.setTo(event.getFrom());
	}

	@EventHandler
	public void PlayerHealthFoodUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		Game game = Manager.GetGame();

		//Not Playing
		for (Player player : UtilServer.getPlayers())
		{
			if (game == null || game.inLobby() || (!game.IsAlive(player) && game.shouldHeal(player)))
			{
				player.setMaxHealth(20);
				player.setHealth(20);
				player.setFoodLevel(20);
			}
		}

		if (game == null || !game.InProgress())
		{
			return;
		}

		if (game.HungerSet != -1)
		{
			for (Player player : game.GetPlayers(true))
			{
				if (Manager.GetCondition().HasCondition(player, ConditionType.ARCADE_HUNGER_DISABLE, null))
					continue;

				player.setFoodLevel(game.HungerSet);
				player.setSaturation(1F);
			}
		}

		if (game.HealthSet != -1)
		{
			for (Player player : game.GetPlayers(true))
			{
				player.setHealth(Math.min(game.HealthSet, player.getMaxHealth()));
			}
		}

		if (game.NightVision)
		{
			for (Player player : UtilServer.getPlayersCollection())
			{
				if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION))
				{
					continue;
				}

				Manager.GetCondition().Factory().NightVision("Night Vision", player, player, Integer.MAX_VALUE, 0, false, false, false);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerBoundaryCheck(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		Game game = Manager.GetGame();

		if (game == null || !game.IsLive())
		{
			return;
		}

		WorldData worldData = game.WorldData;

		for (Player player : game.WorldData.World.getPlayers())
		{
			if (game.isInsideMap(player))
			{
				continue;
			}

			if (UtilPlayer.isSpectator(player))
			{
				player.teleport(game.GetSpectatorLocation());
				continue;
			}

			//Riding a Projectile, edgecase
			if (player.getVehicle() instanceof Projectile)
			{
				player.getVehicle().remove();
				player.leaveVehicle();
				((CraftPlayer) player).getHandle().spectating = false;
			}

			Location location = player.getLocation();

			if (game.WorldBoundary && !game.WorldBoundaryKill)
			{
				UtilPlayer.message(player, C.cRed + C.Bold + "WARNING: " + C.cWhite + C.Bold + "RETURN TO PLAYABLE AREA!");

				if (location.getY() > game.WorldData.MaxY)
				{
					UtilAction.velocity(player, UtilAlg.getTrajectory2d(location, game.GetSpectatorLocation()), 1, true, 0, 0, 10, true);
				}
				else
				{
					UtilAction.velocity(player, UtilAlg.getTrajectory2d(location, game.GetSpectatorLocation()), 1, true, 0.4, 0, 10, true);
				}

				Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.VOID, 4, false, false, false, "Border", "Border Damage");

				if (!Manager.GetDamage().IsEnabled())
				{
					Bukkit.getPluginManager().callEvent(new EntityDamageEvent(player, DamageCause.VOID, 4));
				}

				player.getWorld().playSound(location, Sound.NOTE_BASS, 2f, 1f);
			}
			else if ((game.WorldBoundary && game.WorldBoundaryKill) || location.getY() < worldData.MinY)
			{
				if (!Manager.GetDamage().IsEnabled())
				{
					Bukkit.getPluginManager().callEvent(new EntityDamageEvent(player, DamageCause.VOID, 9001));
				}
				else
				{
					Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.VOID, 9001, false, true, false, "Border", "Border Damage");
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void WorldCreature(CreatureSpawnEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null) return;

		if (!game.CreatureAllow && !game.CreatureAllowOverride)
		{
			if (game.WorldData != null)
			{
				if (game.WorldData.World != null)
				{
					if (event.getLocation().getWorld().equals(game.WorldData.World))
					{
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void StaffDisqualify(MineplexTeleportEvent event)
	{
		if (event.isCancelled())
			return;

		if (Manager.GetClients().Get(event.getPlayer()).hasPermission(Perm.BYPASS_TELEPORT_KICK))
			return;

		Game game = Manager.GetGame();
		if (game == null) return;

		if (!(game.IsLive() || game.GetState() == GameState.Prepare))
			return;

		if (!game.TeleportsDisqualify)
			return;

		if (!game.IsAlive(event.getPlayer()))
			return;

		//Remove Kit
		game.disqualify(event.getPlayer());

		event.setCancelled(true);
	}

	@EventHandler
	public void WorldTime(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Game game = Manager.GetGame();
		if (game == null) return;

		if (game.WorldTimeSet != -1)
		{
			if (game.WorldData != null)
			{
				if (game.WorldData.World != null)
				{
					game.WorldData.World.setTime(game.WorldTimeSet);
				}
			}
		}
	}

	@EventHandler
	public void customWorldTime(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
		{
			return;
		}

		WorldData worldData = event.GetGame().WorldData;

		worldData.GetAllCustomLocs().forEach((key, value) ->
		{
			if (key.startsWith("TIME_SET"))
			{
				String[] split = key.split(" ");

				if (split.length < 2)
				{
					return;
				}

				try
				{
					event.GetGame().WorldTimeSet = Integer.parseInt(split[1]);
				}
				catch (NumberFormatException e)
				{
					event.GetGame().Announce(F.main("Game", "Warning! Invalid TIME_SET data point."));
				}
			}
		});
	}

	@EventHandler
	public void WorldWeather(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		Game game = Manager.GetGame();
		if (game == null) return;

		if (!game.WorldWeatherEnabled)
		{
			if (game.WorldData != null)
			{
				if (game.WorldData.World != null)
				{
					game.WorldData.World.setStorm(false);
					game.WorldData.World.setThundering(false);
				}
			}
		}
	}

	@EventHandler
	public void WorldWaterDamage(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		Game game = Manager.GetGame();
		if (game == null || !game.IsLive())
		{
			return;
		}

		if (game.WorldWaterDamage <= 0)
		{
			if (!game.WorldData.GetCustomLocs("WATER_DAMAGE").isEmpty())
			{
				game.WorldWaterDamage = 4;
			}

			return;
		}

		for (Player player : game.GetPlayers(true))
		{
			if (!Recharge.Instance.use(player, "Water Damage", 500, false, false))
			{
				continue;
			}

			Block block = player.getLocation().getBlock();
			Material lower = block.getType();
			Material upper = block.getRelative(BlockFace.UP).getType();

			if (lower == Material.WATER || lower == Material.STATIONARY_WATER || upper == Material.WATER || upper == Material.STATIONARY_WATER)
			{
				//Damage Event
				Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.DROWNING, game.WorldWaterDamage, true, false, false, "Water", "Water Damage");
				player.getWorld().playSound(player.getLocation(), Sound.SPLASH, 0.8f, 1f + (float) Math.random() / 2);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void WorldSoilTrample(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.PHYSICAL)
			return;

		Game game = Manager.GetGame();
		if (game == null) return;

		if (game.WorldSoilTrample)
			return;

		if (event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.SOIL)
			return;

		event.setCancelled(true);
	}

	@EventHandler
	public void WorldBlockBurn(BlockBurnEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null) return;

		if (game.WorldBlockBurn)
			return;

		event.setCancelled(true);
	}

	@EventHandler
	public void WorldBlockBurn(BlockGrowEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null) return;

		if (game.WorldBlockGrow)
			return;

		event.setCancelled(true);
	}

	@EventHandler
	public void WorldFireSpread(BlockIgniteEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null) return;

		if (game.WorldFireSpread)
			return;

		if (event.getCause() == IgniteCause.FLINT_AND_STEEL && game.AllowFlintAndSteel)
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void WorldLeavesDecay(LeavesDecayEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null) return;

		if (game.WorldLeavesDecay)
			return;

		event.setCancelled(true);
	}

	@EventHandler
	public void SpectatorMessage(UpdateEvent event)
	{
		if (Manager.IsTournamentServer())
			return;

		if (Manager.GetGame() == null)
			return;

		if (!Manager.GetGame().AnnounceStay)
			return;

		if (!Manager.GetGame().IsLive())
			return;

		if (event.getType() != UpdateType.SEC)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (Manager.IsAlive(player))
				continue;

			if (Recharge.Instance.use(player, "Dont Quit Message", 300000, false, false) && !Manager.getPreferences().get(player).isActive(Preference.AUTO_JOIN_NEXT_GAME))
			{
				UtilPlayer.message(player, " ");
				UtilPlayer.message(player, C.cWhite + C.Bold + "You are out of the game, but " + C.cGold + C.Bold + "DON'T QUIT" + C.cWhite + C.Bold + "!");
				UtilPlayer.message(player, C.cWhite + C.Bold + "The next game will be starting soon...");
			}
		}
	}

	@EventHandler
	public void AntiHackStrict(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Prepare || event.GetState() == GameState.Live)
			Managers.get(AntiHack.class).setStrict(event.GetGame().StrictAntiHack);
		else
			Managers.get(AntiHack.class).setStrict(true);
	}

	@EventHandler
	public void PlayerKillCommandCancel(PlayerCommandPreprocessEvent event)
	{
		if (Manager.GetGame() == null || !event.getMessage().toLowerCase().equalsIgnoreCase("/kill"))
		{
			return;
		}

		event.setCancelled(true);

		if (Manager.GetGame().DisableKillCommand)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Game", "Suicide is disabled."));
		}
		else
		{
			Manager.GetDamage().NewDamageEvent(event.getPlayer(), null, null, DamageCause.CUSTOM, 500, false, true, true, Manager.GetGame().GetName(), "Trying to Escape");
		}
	}

	@EventHandler
	public void resourceInform(PlayerJoinEvent event)
	{
		if (Manager.GetGame() == null)
			return;

		if (Manager.GetGame().GetType().getResourcePackUrls(Manager.GetGame()) == null || Manager.GetGame().GetType().getResourcePackUrls(Manager.GetGame()).length == 0)
			return;

		UtilTextMiddle.display(C.cGold + C.Bold + Manager.GetGame().GetType().getName(), "Make sure you accept the Resource Pack", 20, 120, 20, event.getPlayer());
	}
}