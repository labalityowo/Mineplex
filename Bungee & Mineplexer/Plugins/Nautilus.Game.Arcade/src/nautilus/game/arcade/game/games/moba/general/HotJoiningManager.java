package nautilus.game.arcade.game.games.moba.general;

import mineplex.core.common.util.F;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.progression.MobaExperienceCalculateEvent;
import nautilus.game.arcade.game.games.moba.structure.tower.Tower;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HotJoiningManager implements Listener
{

	private static final int HOT_JOIN_EXP_REWARD = 100;

	private final Moba _host;
	private final List<Player> _pending;
	private final List<Player> _hotJoined;
	private final List<UUID> _played;

	public HotJoiningManager(Moba host)
	{
		_host = host;
		_pending = new ArrayList<>();
		_hotJoined = new ArrayList<>();
		_played = new ArrayList<>(8);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerLogin(PlayerLoginEvent event)
	{
		if (!_host.IsLive() || !_host.getArcadeManager().IsRewardStats())
		{
			return;
		}

		Player player = event.getPlayer();
		List<Player> players = _host.GetPlayers(true);
		GameTeam team = _host.getTeamSelector().getTeamToJoin(_host.GetTeamList(), 1, (int) Math.ceil(players.size() / 2D), players.size());

		if (team == null || team.GetSize() >= 4)
		{
			return;
		}

		_pending.add(player);
		team.AddPlayer(player, true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		if (!_pending.remove(player))
		{
			return;
		}

		GameTeam team = _host.GetTeam(player);

		if (_host.getArcadeManager().isVanished(player))
		{
			if (team != null)
			{
				team.RemovePlayer(player);
			}
			return;
		}

		for (Tower tower : _host.getTowerManager().getTowers())
		{
			// If the team's second tower is dead
			if (tower.getOwner().equals(team) && !tower.isFirstTower() && tower.isDead())
			{
				player.sendMessage(F.main("Game", "Sorry but you can only join a game in progress if they have at least " + F.elem(1) + " tower alive."));
				return;
			}
		}

		boolean played = _played.contains(player.getUniqueId());

		team.SpawnTeleport(player);

		if (!played)
		{
			_hotJoined.add(player);
		}

		_host.setupPlayerData(player);

		_host.getArcadeManager().runSyncLater(() ->
		{
			Kit kit = _host.getFirstKit(player);

			if (!played)
			{
				player.sendMessage(F.main("Game", "Thanks for choosing to join a game in progress! If you stay until the end of the game you will were an additional " + F.elem(HOT_JOIN_EXP_REWARD) + " " + F.greenElem("Heroes of GWEN Role") + " experience."));
			}
			_host.SetKit(player, kit, true);

			Perk perk = kit.GetPerks()[kit.GetPerks().length - 1];

			// Put Ultimates on cooldown
			if (perk instanceof HeroSkill)
			{
				((HeroSkill) perk).useSkill(player);
			}
		}, 1);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		_pending.remove(player);
		_hotJoined.remove(player);
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		_host.GetPlayers(true).forEach(player -> _played.add(player.getUniqueId()));
	}

	@EventHandler
	public void expCalculate(MobaExperienceCalculateEvent event)
	{
		if (_hotJoined.contains(event.getPlayer()))
		{
			event.getExpEarned().getAndAdd(HOT_JOIN_EXP_REWARD);
		}
	}
}
