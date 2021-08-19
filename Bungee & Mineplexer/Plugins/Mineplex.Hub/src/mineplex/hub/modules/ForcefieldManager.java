package mineplex.hub.modules;

import java.util.HashMap;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.preferences.Preference;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.hub.HubManager;
import mineplex.hub.commands.ForcefieldRadius;

public class ForcefieldManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		BYPASS_FORCEFIELD,
		FORCEFIELD_RADIUS_COMMAND,
	}

	public HubManager Manager;

	private HashMap<Player, Integer> _radius = new HashMap<Player, Integer>();

	public ForcefieldManager(HubManager manager)
	{
		super("Forcefield", manager.getPlugin());

		Manager = manager;
		
		generatePermissions();
	}

	@Override
	public void addCommands()
	{
		addCommand(new ForcefieldRadius(this));
	}

	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.BYPASS_FORCEFIELD, true, true);
		PermissionGroup.EVENTMOD.setPermission(Perm.BYPASS_FORCEFIELD, false, true);
		PermissionGroup.LT.setPermission(Perm.FORCEFIELD_RADIUS_COMMAND, true, true);
	}

	@EventHandler
	public void ForcefieldUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (Manager.getPreferences().get(player).isActive(Preference.FORCE_FIELD) && Manager.GetClients().Get(player).hasPermission(Preference.FORCE_FIELD))
			{
				for (Player other : UtilServer.getPlayers())
				{
					if (player.equals(other))
						continue;

					int range = 5;
					if (_radius.containsKey(player))
						range = _radius.get(player);

					if (UtilMath.offset(other, player) > range)
						continue;

					if (Manager.GetClients().Get(other).hasPermission(Perm.BYPASS_FORCEFIELD))
						continue;

					if (Recharge.Instance.use(other, "Forcefield Bump", 500, false, false))
					{
						Entity bottom = other;
						while (bottom.getVehicle() != null)
							bottom = bottom.getVehicle();
						
						UtilAction.velocity(bottom, UtilAlg.getTrajectory2d(player, bottom), 1.6, true, 0.8, 0, 10, true);
						other.getWorld().playSound(other.getLocation(), Sound.CHICKEN_EGG_POP, 2f, 0.5f);
					}
				}
			}
		}
	}

	public void ForcefieldRadius(Player caller, String[] args) 
	{
		try
		{
			int range = Integer.parseInt(args[0]);

			_radius.put(caller, range);

			UtilPlayer.message(caller, F.main("Forcefield", "Radius set to " + F.elem(range + "") + "."));
		}
		catch (Exception e)
		{
			UtilPlayer.message(caller, F.main("Forcefield", "Invalid Input. Correct input is " + F.elem("/radius #") + "."));
		}
	}

	@EventHandler
	public void ForcefieldReset(PlayerQuitEvent event)
	{
		_radius.remove(event.getPlayer());
	}
}