package mineplex.game.clans.clans.freeze;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Sets;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.recharge.Recharge;
import mineplex.core.slack.SlackAPI;
import mineplex.core.slack.SlackMessage;
import mineplex.core.slack.SlackTeam;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.event.ClansCommandExecutedEvent;
import mineplex.game.clans.clans.event.IronDoorOpenEvent;
import mineplex.game.clans.clans.freeze.commands.FreezeCommand;
import mineplex.game.clans.clans.freeze.commands.PanicCommand;
import mineplex.game.clans.clans.freeze.commands.UnfreezeCommand;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class ClansFreezeManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		FREEZE_COMMAND,
		UNFREEZE_COMMAND,
		PANIC_COMMAND,
		NOTIFY,
	}

	private static final long FREEZE_MESSAGE_INTERVAL = 10000;
	private static final Set<Material> CONTAINERS = Sets.newHashSet(Material.CHEST, Material.TRAPPED_CHEST, Material.HOPPER, Material.FURNACE, Material.BURNING_FURNACE, Material.DISPENSER, Material.DROPPER, Material.WORKBENCH, Material.BREWING_STAND);
	
	private final CoreClientManager _clientManager;
	private final Map<UUID, Float> _frozen = new HashMap<>();
	private final Map<UUID, Float> _panic = new HashMap<>();

	public ClansFreezeManager(JavaPlugin plugin, CoreClientManager clientManager)
	{
		super("Freeze", plugin);
		
		_clientManager = clientManager;
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.CMOD.setPermission(Perm.FREEZE_COMMAND, false, true);
		PermissionGroup.CMA.setPermission(Perm.FREEZE_COMMAND, false, true);
		PermissionGroup.ADMIN.setPermission(Perm.FREEZE_COMMAND, true, true);
		PermissionGroup.CMOD.setPermission(Perm.UNFREEZE_COMMAND, false, true);
		PermissionGroup.CMA.setPermission(Perm.UNFREEZE_COMMAND, false, true);
		PermissionGroup.ADMIN.setPermission(Perm.UNFREEZE_COMMAND, true, true);
		PermissionGroup.CMOD.setPermission(Perm.NOTIFY, false, true);
		PermissionGroup.CMA.setPermission(Perm.NOTIFY, false, true);
		PermissionGroup.ADMIN.setPermission(Perm.NOTIFY, true, true);
		PermissionGroup.CONTENT.setPermission(Perm.PANIC_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.PANIC_COMMAND, true, true);
	}
	
	@Override
	public void addCommands()
	{
		addCommand(new FreezeCommand(this));
		addCommand(new UnfreezeCommand(this));
		addCommand(new PanicCommand(this));
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onQuit(PlayerQuitEvent event)
	{
		Float walkSpeed = _frozen.remove(event.getPlayer().getUniqueId());
		if (walkSpeed != null)
		{
			event.getPlayer().setWalkSpeed(walkSpeed);
			event.getPlayer().removePotionEffect(PotionEffectType.JUMP);
			for (Player staff : UtilServer.GetPlayers())
			{
				if (_clientManager.Get(staff).hasPermission(Perm.NOTIFY))
				{
					UtilPlayer.message(staff, F.main(getName(), F.elem(event.getPlayer().getName()) + " has logged out while frozen!"));
				}
			}
		}
		
		walkSpeed = _panic.remove(event.getPlayer().getUniqueId());
		if (walkSpeed != null)
		{
			event.getPlayer().setWalkSpeed(walkSpeed);
			event.getPlayer().removePotionEffect(PotionEffectType.JUMP);
		}
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent event)
	{
		if ((isFrozen(event.getPlayer()) || isPanicking(event.getPlayer())) && UtilMath.offset2d(event.getFrom().getBlock().getLocation(), event.getTo().getBlock().getLocation()) >= 1)
		{
			event.setCancelled(true);
			event.getPlayer().teleport(event.getFrom().getBlock().getLocation().add(0, 0.5, 0));
		}
	}
	
	@EventHandler
	public void onDisplayFreezeMessage(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (UUID frozenUUID : _frozen.keySet())
		{
			Player frozen = Bukkit.getPlayer(frozenUUID);
			if (Recharge.Instance.use(frozen, "Freeze Message", FREEZE_MESSAGE_INTERVAL, false, false))
			{
				String border = C.cGray + C.Strike + "-----------------------------------------------------";
				String sq = "\u2589";

				UtilPlayer.message(frozen, border);
				UtilPlayer.message(frozen, C.Reset + "");
				UtilPlayer.message(frozen, C.cWhite + sq + sq + sq + sq + C.cRed + sq + C.cWhite + sq + sq + sq + sq);
				UtilPlayer.message(frozen, C.cWhite + sq + sq + sq + C.cRed + sq + C.cBlack + sq + C.cRed + sq + C.cWhite + sq + sq + sq);
				UtilPlayer.message(frozen, C.cWhite + sq + sq + C.cRed + sq + C.cGold + sq + C.cBlack + sq + C.cGold + sq + C.cRed + sq + C.cWhite + sq + sq);
				UtilPlayer.message(frozen, C.cWhite + sq + sq + C.cRed + sq + C.cGold + sq + C.cBlack + sq + C.cGold + sq + C.cRed + sq + C.cWhite + sq + sq);
				UtilPlayer.message(frozen, C.cWhite + sq + sq + C.cRed + sq + C.cGold + sq + C.cBlack + sq + C.cGold + sq + C.cRed + sq + C.cWhite + sq + sq);
				UtilPlayer.message(frozen, C.cWhite + sq + C.cRed + sq + C.cGold + sq + sq + sq + C.cGold + sq + sq + C.cRed + sq + C.cWhite + sq);
				UtilPlayer.message(frozen, C.cRed + sq + C.cGold + sq + sq + sq + C.cBlack + sq + C.cGold + sq + sq + sq + C.cRed + sq);
				UtilPlayer.message(frozen, C.cRed + sq + sq + sq + sq + sq + sq + sq + sq + sq);
				UtilPlayer.message(frozen, C.Reset + "");
				UtilPlayer.message(frozen, C.cRed + "You have been frozen by a staff member!");
				UtilPlayer.message(frozen, C.cRed + "Do not log out or you will be banned!");
				UtilPlayer.message(frozen, C.Reset + "");
				UtilPlayer.message(frozen, border);
			}
		}
	}
	
	@EventHandler
	public void handleMobs(EntityTargetLivingEntityEvent event)
	{
		if (event.getTarget() instanceof Player)
		{
			Player player = (Player) event.getTarget();
			if (isFrozen(player) || isPanicking(player))
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onDamage(CustomDamageEvent event)
	{
		Player damager = event.GetDamagerPlayer(event.GetCause() == DamageCause.PROJECTILE);
		Player damagee = event.GetDamageePlayer();
		
		if (damager != null && (isFrozen(damager) || isPanicking(damager)))
		{
			event.SetCancelled("Frozen Attacker");
			UtilPlayer.message(damager, F.main(getName(), "You cannot attack others while frozen!"));
		}
		if (damagee != null && (isFrozen(damagee) || isPanicking(damagee)))
		{
			event.SetCancelled("Frozen Damagee");
			if (damager != null)
			{
				UtilPlayer.message(damager, F.main(getName(), "You cannot attack " + F.elem(damagee.getName()) + " while they are frozen!"));
			}
		}
	}
	
	@EventHandler
	public void onUseSkill(SkillTriggerEvent event)
	{
		if (isFrozen(event.GetPlayer()) || isPanicking(event.GetPlayer()))
		{
			event.SetCancelled(true);
			UtilPlayer.message(event.GetPlayer(), F.main(getName(), "You cannot use " + F.skill(event.GetSkillName()) + " while frozen!"));
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent event)
	{
		if (isFrozen(event.getPlayer()) || isPanicking(event.getPlayer()))
		{
			event.setCancelled(true);
			UtilPlayer.message(event.getPlayer(), F.main(getName(), "You cannot break blocks while frozen!"));
		}
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent event)
	{
		if (isFrozen(event.getPlayer()) || isPanicking(event.getPlayer()))
		{
			event.setCancelled(true);
			UtilPlayer.message(event.getPlayer(), F.main(getName(), "You cannot place blocks while frozen!"));
		}
	}
	
	@EventHandler
	public void onTpHome(ClansCommandExecutedEvent event)
	{
		if (isFrozen(event.getPlayer()) || isPanicking(event.getPlayer()))
		{
			event.setCancelled(true);
			UtilPlayer.message(event.getPlayer(), F.main(getName(), "You cannot use that command while frozen!"));
		}
	}
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent event)
	{
		if (isFrozen(event.getPlayer()) || isPanicking(event.getPlayer()))
		{
			event.setCancelled(true);
			UtilPlayer.message(event.getPlayer(), F.main(getName(), "You cannot drop items while frozen!"));
		}
	}
	
	@EventHandler
	public void onPickupItem(PlayerPickupItemEvent event)
	{
		if (isFrozen(event.getPlayer()) || isPanicking(event.getPlayer()))
		{
			event.setCancelled(true);
			UtilPlayer.message(event.getPlayer(), F.main(getName(), "You cannot pick up items while frozen!"));
		}
	}
	
	@EventHandler
	public void onOpenContainer(PlayerInteractEvent event)
	{
		if ((isFrozen(event.getPlayer()) || isPanicking(event.getPlayer())) && event.getClickedBlock() != null && CONTAINERS.contains(event.getClickedBlock().getType()))
		{
			event.setCancelled(true);
			UtilPlayer.message(event.getPlayer(), F.main(getName(), "You cannot open containers while frozen!"));
		}
	}
	
	@EventHandler
	public void onOpenDoor(IronDoorOpenEvent event)
	{
		if (isFrozen(event.getPlayer()) || isPanicking(event.getPlayer()))
		{
			event.setCancelled(true);
			UtilPlayer.message(event.getPlayer(), F.main(getName(), "You cannot use doors while frozen!"));
		}
	}
	
	@EventHandler
	public void onToggleFlight(PlayerToggleFlightEvent event)
	{
		if ((isFrozen(event.getPlayer()) || isPanicking(event.getPlayer())) && event.isFlying())
		{
			event.setCancelled(true);
		}
	}
	
	/**
	 * Checks if a player is frozen
	 * @param player The player to check
	 * @return Whether the player is frozen
	 */
	public boolean isFrozen(Player player)
	{
		return _frozen.containsKey(player.getUniqueId());
	}
	
	/**
	 * Checks if a player is panicking
	 * @param player The player to check
	 * @return Whether the player is panicking
	 */
	public boolean isPanicking(Player player)
	{
		return _panic.containsKey(player.getUniqueId());
	}
	
	/**
	 * Enters a player into panic mode
	 * @param player The player who is panicking
	 */
	public void panic(Player player)
	{
		_panic.put(player.getUniqueId(), _frozen.getOrDefault(player.getUniqueId(), player.getWalkSpeed()));
		player.setFlying(false);
		player.setWalkSpeed(0);
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999, -10));
		if (!UtilServer.isTestServer())
		{
			SlackMessage message = new SlackMessage("Clans Panic System", "crossed_swords", player.getName() + " has entered panic mode on " + UtilServer.getServerName() + "!");
			SlackAPI.getInstance().sendMessage(SlackTeam.CLANS, "#urgent", message, true);
			SlackAPI.getInstance().sendMessage(SlackTeam.DEVELOPER, "#clans-commandspy", message, true);
			SlackAPI.getInstance().sendMessage(SlackTeam.SOCIAL_MEDIA, "#streams-announcements", message, true);
		}
		for (Player alert : UtilServer.GetPlayers())
		{
			if (_clientManager.Get(alert).hasPermission(Perm.NOTIFY))
			{
				UtilPlayer.message(alert, F.main(getName(), F.elem(player.getName()) + " has entered panic mode!"));
			}
			if (alert.getName().equals(player.getName()))
			{
				UtilPlayer.message(alert, F.main(getName(), "You have entered panic mode!"));
			}
		}
	}
	
	/**
	 * Removes a player from panic mode
	 * @param player The player to unpanic
	 */
	public void unpanic(Player player)
	{
		Float walkSpeed = _panic.remove(player.getUniqueId());
		if (walkSpeed != null)
		{
			if (!_frozen.containsKey(player.getUniqueId()))
			{
				player.setWalkSpeed(walkSpeed);
				player.removePotionEffect(PotionEffectType.JUMP);
			}
			if (!UtilServer.isTestServer())
			{
				SlackMessage message = new SlackMessage("Clans Panic System", "crossed_swords", player.getName() + " has exited panic mode on " + UtilServer.getServerName() + "!");
				SlackAPI.getInstance().sendMessage(SlackTeam.CLANS, "#urgent", message, true);
				SlackAPI.getInstance().sendMessage(SlackTeam.DEVELOPER, "#clans-commandspy", message, true);
				SlackAPI.getInstance().sendMessage(SlackTeam.SOCIAL_MEDIA, "#streams-announcements", message, true);
			}
			for (Player alert : UtilServer.GetPlayers())
			{
				if (_clientManager.Get(alert).hasPermission(Perm.NOTIFY))
				{
					UtilPlayer.message(alert, F.main(getName(), F.elem(player.getName()) + " has exited panic mode!"));
				}
				if (alert.getName().equals(player.getName()))
				{
					UtilPlayer.message(alert, F.main(getName(), "You have exited panic mode!"));
				}
			}
		}
	}
	
	/**
	 * Freezes a player
	 * @param player The player to freeze
	 * @param staff The staff member who froze them
	 */
	public void freeze(Player player, Player staff)
	{
		_frozen.put(player.getUniqueId(), _panic.getOrDefault(player.getUniqueId(), player.getWalkSpeed()));
		player.setFlying(false);
		player.setWalkSpeed(0);
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999, -10));
		for (Player alert : UtilServer.GetPlayers())
		{
			if (_clientManager.Get(alert).hasPermission(Perm.NOTIFY))
			{
				UtilPlayer.message(alert, F.main(getName(), F.elem(player.getName()) + " has been frozen by " + F.elem(staff.getName()) + "!"));
			}
		}
	}
	
	/**
	 * Unfreezes a player
	 * @param player The player to unfreeze
	 * @param staff The staff member who unfroze them
	 */
	public void unfreeze(Player player, Player staff)
	{
		Float walkSpeed = _frozen.remove(player.getUniqueId());
		if (walkSpeed != null)
		{
			if (!_panic.containsKey(player.getUniqueId()))
			{
				player.setWalkSpeed(walkSpeed);
				player.removePotionEffect(PotionEffectType.JUMP);
			}
			for (Player alert : UtilServer.GetPlayers())
			{
				if (_clientManager.Get(alert).hasPermission(Perm.NOTIFY))
				{
					UtilPlayer.message(alert, F.main(getName(), F.elem(player.getName()) + " has been unfrozen by " + F.elem(staff.getName()) + "!"));
					continue;
				}
				if (alert.getName().equals(player.getName()))
				{
					UtilPlayer.message(alert, F.main(getName(), "You have been unfrozen!"));
				}
			}
		}
	}
}