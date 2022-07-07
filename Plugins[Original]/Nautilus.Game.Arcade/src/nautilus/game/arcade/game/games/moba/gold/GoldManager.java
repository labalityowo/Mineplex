package nautilus.game.arcade.game.games.moba.gold;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.structure.tower.TowerDestroyEvent;
import nautilus.game.arcade.game.modules.capturepoint.CapturePoint;
import nautilus.game.arcade.game.modules.capturepoint.CapturePointCaptureEvent;

public class GoldManager implements Listener
{
	public enum Perm implements Permission
	{
		DEBUG_GOLD_COMMAND,
	}

	private static final int GOLD_PER_5 = 20;
	private static final int GOLD_PER_CAPTURE_5 = 5;
	private static final int GOLD_PER_CAPTURE_INITIAL = 25;
	private static final int GOLD_PER_FIRST_TOWER = 100;
	private static final int GOLD_PER_SECOND_TOWER = 250;


	private final Moba _host;

	private final Map<Player, Integer> _playerGold;

	public GoldManager(Moba host)
	{
		_host = host;

		_playerGold = new HashMap<>();

		host.registerDebugCommand("gold", Perm.DEBUG_GOLD_COMMAND, PermissionGroup.ADMIN, (caller, args) ->
		{
			if (args.length < 1)
			{
				caller.sendMessage(F.main("Debug", "/gold <amount>"));
				return;
			}

			try
			{
				int amount = Integer.parseInt(args[0]);

				addGold(caller, amount);
				caller.sendMessage(F.main("Debug", "Gave yourself " + F.elem(args[0]) + " gold."));
			} catch (NumberFormatException e)
			{
				caller.sendMessage(F.main("Debug", F.elem(args[0]) + " is not a number."));
			}
		});
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_playerGold.remove(event.getPlayer());
	}

	@EventHandler
	public void passiveGain(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC_05 || !_host.IsLive())
		{
			return;
		}

		// Every player passive
		for (Player player : _host.GetPlayers(true))
		{
			addGold(player, GOLD_PER_5);
		}

		// Capture points
		for (CapturePoint point : _host.getCapturePointManager().getCapturePoints())
		{
			GameTeam owner = point.getOwner();

			if (owner == null)
			{
				continue;
			}

			for (Player player : owner.GetPlayers(true))
			{
				addGold(player, GOLD_PER_CAPTURE_5);
			}
		}
	}

	@EventHandler
	public void towerDestroy(TowerDestroyEvent event)
	{
		for (Player player : _host.GetPlayers(true))
		{
			// Don't give the gold to the owners
			if (_host.GetTeam(player).equals(event.getTower().getOwner()))
			{
				continue;
			}

			addGold(player, event.getTower().isFirstTower() ? GOLD_PER_FIRST_TOWER : GOLD_PER_SECOND_TOWER, "Destroying a tower");
		}
	}

	@EventHandler
	public void pointCapture(CapturePointCaptureEvent event)
	{
		GameTeam team = event.getPoint().getOwner();

		for (Player player : team.GetPlayers(true))
		{
			addGold(player, GOLD_PER_CAPTURE_INITIAL, "Capturing a beacon");
		}
	}

	public int getGold(Player player)
	{
		return _playerGold.getOrDefault(player, 0);
	}

	public void addGold(Player player, int amount)
	{
		addGold(player, amount, null);
	}

	public void addGold(Player player, int amount, String reason)
	{
		_playerGold.putIfAbsent(player, 0);
		_playerGold.put(player, _playerGold.get(player) + amount);
		_host.AddStat(player, "GoldEarned", amount, false, false);

		if (amount > 20 && reason != null)
		{
			_host.AddGems(player, (double) amount / 3D, reason, true, true);
			player.sendMessage(F.main("Game", C.cGold + "+" + amount + " gold (" + reason + ")" + C.cGray + "."));
		}
	}

	public void removeGold(Player player, int amount)
	{
		_playerGold.putIfAbsent(player, 0);
		_playerGold.put(player, _playerGold.get(player) - amount);
	}

	public boolean hasGold(Player player, int amount)
	{
		return _playerGold.getOrDefault(player, 0) >= amount;
	}

}
