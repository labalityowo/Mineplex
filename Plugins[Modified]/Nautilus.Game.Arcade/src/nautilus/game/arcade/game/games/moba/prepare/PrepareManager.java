package nautilus.game.arcade.game.games.moba.prepare;

import mineplex.core.common.entity.ClientArmorStand;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.events.GamePrepareCountdownCommence;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.MobaPlayer;
import nautilus.game.arcade.game.games.moba.MobaRole;
import nautilus.game.arcade.game.games.moba.kit.HeroKit;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.kit.RoleSelectEvent;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.concurrent.TimeUnit;

public class PrepareManager implements Listener
{

	private static final long PREPARE_TIME = TimeUnit.MINUTES.toMillis(1);
	private static final long POST_SELECTION_PREPARE_TIME = TimeUnit.SECONDS.toMillis(5);
	private static final int MAX_DISTANCE_WITHOUT_SELECTION_SQUARED = 400;

	private final Moba _host;

	private boolean _postPrepareStage;

	public PrepareManager(Moba host)
	{
		_host = host;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void updatePrepare(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || _host.GetState() != GameState.Prepare || _postPrepareStage)
		{
			return;
		}

		if (!UtilTime.elapsed(_host.GetStateTime(), PREPARE_TIME))
		{
			for (Player player : _host.GetPlayers(true))
			{
				Kit kit = _host.GetKit(player);

				if (!(kit instanceof HeroKit))
				{
					return;
				}
			}
		}

		_postPrepareStage = true;

		_host.AnnounceGame();
		_host.StartPrepareCountdown();

		//Event
		GamePrepareCountdownCommence countdownEvent = new GamePrepareCountdownCommence(_host);
		UtilServer.CallEvent(countdownEvent);

		// If players took too long, just give them a random free role and kit.
		for (Player player : _host.GetPlayers(true))
		{
			Kit kit = _host.GetKit(player);

			if (kit instanceof HeroKit)
			{
				continue;
			}

			HeroKit heroKit = _host.getFirstKit(player);

			_host.SetKit(player, heroKit, true);
		}

		for (GameTeam team : _host.GetTeamList())
		{
			team.SpawnTeleport();
		}

		_host.SetStateTime(System.currentTimeMillis());
		_host.getArcadeManager().GetChat().setChatSilence(-1, false);
		_host.PrepareTime = POST_SELECTION_PREPARE_TIME;
		_host.PrepareFreeze = true;
	}

	@EventHandler
	public void updateNearSpawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW || _host.GetState() != GameState.Prepare)
		{
			return;
		}

		for (MobaPlayer mobaPlayer : _host.getMobaData())
		{
			Player player = mobaPlayer.getPlayer();
			GameTeam team = _host.GetTeam(player);

			if (UtilMath.offsetSquared(player.getLocation(), team.GetSpawns().get(0)) > MAX_DISTANCE_WITHOUT_SELECTION_SQUARED && (mobaPlayer.getRole() == null || mobaPlayer.getKit() == null))
			{
				player.sendMessage(F.main("Game", "You haven't finished selecting your hero."));
				team.SpawnTeleport(player);
			}
		}
	}

	@EventHandler
	public void roleSelect(RoleSelectEvent event)
	{
		Player player = event.getPlayer();
		MobaRole role = event.getRole();
		ClientArmorStand stand = event.getStand();

		if (!_host.isRoleFree(player, role))
		{
			player.sendMessage(F.main("Game", "Another player has already chosen this role."));
			event.setCancelled(true);
			return;
		}

		// Show that the kit is claimed.
		stand.setCustomName(C.cGreenB + role.getName() + C.cGray + " - " + player.getName());

		// Store the role of the player
		_host.getMobaData(player).setRole(role);

		// Update the scoreboard
		_host.getScoreboardModule().refreshAsSubject(player);
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		for (MobaPlayer mobaPlayer : _host.getMobaData())
		{
			HeroKit kit = mobaPlayer.getKit();
			Perk perk = kit.GetPerks()[kit.GetPerks().length - 1];

			// Put Ultimates on cooldown
			if (perk instanceof HeroSkill)
			{
				((HeroSkill) perk).useSkill(mobaPlayer.getPlayer());
			}
		}

		_host.getArcadeManager().GetChat().setChatSilence(0, true);
	}
}
