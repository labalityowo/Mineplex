package nautilus.game.arcade.game.team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.command.ICommand;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.HoverEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.modules.Module;

public class TeamRequestsModule extends Module
{

	public enum Perm implements Permission
	{
		TEAM_COMMAND
	}

	private final Map<UUID, UUID> _teamRequests;
	private ICommand _teamCommand;

	public TeamRequestsModule()
	{
		_teamRequests = new HashMap<>();
	}

	@Override
	protected void setup()
	{
		_teamCommand = new CommandBase<ArcadeManager>(getGame().getArcadeManager(), Perm.TEAM_COMMAND, "team")
		{

			@Override
			public void Execute(Player caller, String[] args)
			{
				if (args.length == 0)
				{
					caller.sendMessage(F.main("Game", "/team <player>"));
					return;
				}

				if (getGame().GetState() != GameState.Recruit)
				{
					caller.sendMessage(F.main("Game", "You cannot send team requests at this time!"));
					return;
				}

				//Observer
				if (getGame().getArcadeManager().IsObserver(caller))
				{
					caller.sendMessage(F.main("Game", "Spectators cannot partake in games."));
					return;
				}

				Player target = UtilPlayer.searchOnline(caller, args[0], true);

				if (target == null)
				{
					return;
				}

				if (caller.equals(target))
				{
					caller.sendMessage(F.main("Game", "You can't team with yourself!"));
					return;
				}

				selectTeamMate(caller, target);
			}
		};

		PermissionGroup.PLAYER.setPermission(Perm.TEAM_COMMAND, true, true);
		getGame().getArcadeManager().addCommand(_teamCommand);
		getGame().getTeamModule().setPrioritisePreferences(true);
	}

	@Override
	public void cleanup()
	{
		_teamRequests.clear();
		getGame().getArcadeManager().removeCommand(_teamCommand);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void teamSelectInteract(PlayerInteractEntityEvent event)
	{
		if (getGame().GetState() != GameState.Recruit || !(event.getRightClicked() instanceof Player))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (itemStack != null && itemStack.getType() != Material.AIR)
		{
			return;
		}
		else if (getGame().getArcadeManager().IsObserver(player))
		{
			UtilPlayer.message(player, F.main("Game", "Spectators cannot partake in games."));
			return;
		}

		selectTeamMate(player, (Player) event.getRightClicked());
	}

	private void selectTeamMate(Player player, Player ally)
	{
		Map<Player, GameTeam> preferences = getGame().getTeamModule().getPreferences();
		GameTeam playerTeam = preferences.get(player);
		GameTeam allyTeam = preferences.get(ally);

		if (playerTeam != null && playerTeam.equals(allyTeam))
		{
			return;
		}

		//Accept Invite
		if (player.getUniqueId().equals(_teamRequests.get(ally.getUniqueId())))
		{
			//Remove Prefs
			_teamRequests.remove(player.getUniqueId());
			_teamRequests.remove(ally.getUniqueId());

			//Inform
			player.sendMessage(F.main("Game", "You accepted " + F.name(ally.getName()) + "'s " + F.elem("Team Request") + "!"));
			ally.sendMessage(F.main("Game", F.name(player.getName()) + " accepted your " + F.elem("Team Request") + "!"));

			// This ensures there is always a free team
			getGame().getTeamModule().clearQueue(playerTeam);
			getGame().getTeamModule().clearQueue(allyTeam);

			GameTeam team = getEmptyTeam();

			preferences.put(player, team);
			preferences.put(ally, team);
		}
		//Send Invite
		else
		{
			//Already on Team with Target
			if (playerTeam != null && playerTeam.HasPlayer(ally))
			{
				return;
			}

			//Inform Player
			player.sendMessage(F.main("Game", "You sent a " + F.elem("Team Request") + " to " + F.name(ally.getName()) + "!"));

			//Inform Target
			if (Recharge.Instance.use(player, "Team Req " + ally.getName(), 2000, false, false))
			{
				ally.sendMessage(F.main("Game", F.name(player.getName()) + " sent you a " + F.elem("Team Request") + "!"));
				new JsonMessage(F.main("Game", F.elem(C.Bold + "CLICK HERE") + " to join their team!"))
						.hover(HoverEvent.SHOW_TEXT, C.cYellow + "Click to join " + player.getName() + "'s team")
						.click(ClickEvent.RUN_COMMAND, "/team " + player.getName())
						.sendToPlayer(ally);
			}

			//Add Pref
			_teamRequests.put(player.getUniqueId(), ally.getUniqueId());
		}
	}

	@EventHandler (priority = EventPriority.LOWEST)
	public void teamQuit(PlayerQuitEvent event)
	{
		if (getGame().GetState() != GameState.Recruit)
		{
			return;
		}

		Player player = event.getPlayer();
		GameTeam team = getGame().GetTeam(player);

		if (team != null)
		{
			team.DisbandTeam();
		}

		_teamRequests.entrySet().removeIf(entry -> entry.getKey().equals(player.getUniqueId()) || entry.getValue().equals(player.getUniqueId()));
	}

	private GameTeam getEmptyTeam()
	{
		return getGame().GetTeamList().stream()
				.filter(team -> getGame().getTeamModule().getPlayersQueued(team) == 0)
				.findFirst()
				.orElse(null);
	}
}
