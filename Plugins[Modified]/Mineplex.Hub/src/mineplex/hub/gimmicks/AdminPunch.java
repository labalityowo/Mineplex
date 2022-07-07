package mineplex.hub.gimmicks;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;

@ReflectivelyCreateMiniPlugin
public class AdminPunch extends MiniPlugin
{

	public enum Perm implements Permission
	{
		PUNCH_COMMAND
	}

	private static final Vector UP = new Vector(0, 8, 0);

	private final Set<Player> _active;

	private AdminPunch()
	{
		super("Rocket Punch");

		_active = new HashSet<>();

		generatePermissions();

		addCommand(new CommandBase<AdminPunch>(this, Perm.PUNCH_COMMAND, "mystery")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				toggleState(caller);
			}
		});
	}

	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.PUNCH_COMMAND, true, true);
	}

	private void toggleState(Player player)
	{
		if (_active.remove(player))
		{
			player.sendMessage(F.main(_moduleName, "Disabled " + F.name(_moduleName) + "!"));
		}
		else
		{
			_active.add(player);
			player.sendMessage(F.main(_moduleName, "Enabled " + F.name(_moduleName) + "!"));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void entityDamage(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.L))
		{
			return;
		}

		Player player = event.getPlayer();

		for (Player other : UtilPlayer.getNearby(player.getLocation(), 4))
		{
			if (player.equals(other))
			{
				continue;
			}

			punch(player, other);
		}
	}

	private void punch(Player damager, Player damagee)
	{
		if (!_active.contains(damager) ||
				!Recharge.Instance.use(damager, _moduleName, 100, false, false) ||
				!Recharge.Instance.use(damager, _moduleName + damagee.getName(), 1000, false, false))
		{
			return;
		}

		Location location =  damagee.getLocation().add(0, 1, 0);

		UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, location, 0, 0, 0, 0.1F, 1, ViewDist.LONG);
		UtilParticle.PlayParticleToAll(ParticleType.LAVA, location, 5, 0.5F, 5, 0.1F, 20, ViewDist.LONG);
		location.getWorld().playSound(location, Sound.EXPLODE, 3, 0.5F);

		UtilAction.velocity(damagee, UP);
		Bukkit.broadcastMessage(F.main(_moduleName, F.name(damager.getName()) + " punched " + F.name(damagee.getName()) + " into the air!"));
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_active.remove(event.getPlayer());
	}
}
