package mineplex.hub.hubgame.common.damage;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.Color;
import mineplex.core.common.jsonchat.HoverEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.hub.hubgame.CycledGame.GameState;
import mineplex.hub.hubgame.HubGame;
import mineplex.hub.hubgame.HubGameManager;
import mineplex.hub.hubgame.common.HubGameComponent;
import mineplex.hub.hubgame.event.HubGamePlayerDeathEvent;
import mineplex.hub.hubgame.event.HubGameStateChangeEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class PVPTrackerComponent extends HubGameComponent<HubGame>
{

	private static final int MATCH_HISTORY_SIZE = 5;
	private static final String STATS_COMMAND = "/duelstats";
	private static final String HEART = C.cRed + "❤" + C.Reset;
	private static final DecimalFormat FORMAT = new DecimalFormat("0.#");

	private final List<Match> _matches;

	public PVPTrackerComponent(HubGame game)
	{
		super(game);

		_matches = new ArrayList<>();
	}

	private Match getLatestMatch(Player player)
	{
		Match latest = null;

		for (Match match : _matches)
		{
			if (match.PlayerA.Player.equals(player) || match.PlayerB.Player.equals(player))
			{
				latest = match;
			}
		}

		return latest;
	}


	private PVPStats getLatestStats(Player player)
	{
		PVPStats latest = null;

		for (Match match : _matches)
		{
			if (match.PlayerA.Player.equals(player))
			{
				latest = match.PlayerA;
			}
			else if (match.PlayerB.Player.equals(player))
			{
				latest = match.PlayerB;
			}
		}

		return latest;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerDeath(HubGamePlayerDeathEvent event)
	{
		Player player = event.getPlayer();
		Player killer = player.getKiller();

		if (killer == null || !_game.isAlive(killer))
		{
			return;
		}

		PVPStats playerStats = getLatestStats(player);
		PVPStats killerStats = getLatestStats(killer);

		playerStats.EndHealth = 0;
		killerStats.EndHealth = killer.getHealth() / 2D;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void damage(CustomDamageEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		Player damagee = event.GetDamageePlayer();
		Player damager = event.GetDamagerPlayer(true);

		if (damagee == null)
		{
			return;
		}

		PVPStats damageeStats = getLatestStats(damagee);

		if (!_game.isAlive(damagee))
		{
			return;
		}

		damageeStats.CurrentCombo = 0;
		damageeStats.DamageTaken += event.GetDamage();

		if (damager == null)
		{
			return;
		}

		PVPStats damagerStats = getLatestStats(damager);

		damagerStats.DamageDealt += event.GetDamage();

		if (event.GetCause() == DamageCause.ENTITY_ATTACK)
		{
			UtilTextBottom.display(C.cGreen + FORMAT.format(damagee.getHealth() / 2D) + HEART, damager);
			damagerStats.CurrentCombo++;

			if (damagerStats.CurrentCombo > damagerStats.MaxCombo)
			{
				damagerStats.MaxCombo = damagerStats.CurrentCombo;
			}

		}
		if (event.GetCause() == DamageCause.PROJECTILE)
		{
			damagerStats.ArrowsHit++;

			damager.sendMessage(F.main(_game.getManager().getName(), F.name(damagee.getName()) + " is at " + C.cRedB + FORMAT.format(damagee.getHealth() / 2D) + HEART + C.mBody + "."));
		}
	}

	@EventHandler
	public void appleEat(PlayerItemConsumeEvent event)
	{
		Player player = event.getPlayer();

		if (event.getItem().getType() != Material.GOLDEN_APPLE || !_game.isAlive(player))
		{
			return;
		}

		getLatestStats(player).ApplesUsed++;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void blockPlace(BlockPlaceEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		Player player = event.getPlayer();

		if (!_game.isAlive(player))
		{
			return;
		}

		getLatestStats(player).BlocksPlaced++;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void blockBreak(BlockBreakEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		Player player = event.getPlayer();

		if (!_game.isAlive(player))
		{
			return;
		}

		getLatestStats(player).BlocksBroken++;
	}

	@EventHandler
	public void shootBow(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.getEntity();

		if (!_game.isAlive(player))
		{
			return;
		}

		getLatestStats(player).ArrowsShot++;
	}

	@EventHandler
	public void start(HubGameStateChangeEvent event)
	{
		if (event.getState() != GameState.Prepare || !event.getGame().equals(_game))
		{
			return;
		}

		if (_matches.size() >= MATCH_HISTORY_SIZE)
		{
			_matches.remove(0);
		}

		List<Player> alive = _game.getAlivePlayers();
		Player a = alive.get(0);
		Player b = alive.get(1);

		_matches.add(new Match(new PVPStats(a, b), new PVPStats(b, a)));
	}

	@EventHandler
	public void end(HubGameStateChangeEvent event)
	{
		if (event.getState() != GameState.Waiting || !event.getGame().equals(_game))
		{
			return;
		}

		UtilServer.runSyncLater(() ->
		{
			Match match = _matches.get(_matches.size() - 1);
			match.Complete = true;

			for (Player player : match.getPlayers())
			{
				player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 0.5F);
				player.sendMessage("");
				new JsonMessage("   Click to view the match summary for this match!  ")
						.color(Color.GREEN)
						.bold()
						.click(ClickEvent.RUN_COMMAND, STATS_COMMAND)
						.hover(HoverEvent.SHOW_TEXT, C.cYellow + "Click here to view the match summary.")
						.sendToPlayer(player);
				player.sendMessage("");
			}
		}, 10);
	}

	@EventHandler
	public void command(PlayerCommandPreprocessEvent event)
	{
		if (!event.getMessage().equals(STATS_COMMAND))
		{
			return;
		}

		Player player = event.getPlayer();
		Match match = getLatestMatch(player);

		if (match == null)
		{
			return;
		}

		event.setCancelled(true);

		if (!match.Complete)
		{
			player.sendMessage(F.main(_game.getManager().getName(), "Sorry but it seems that we cannot display the stats for that match anymore."));
			return;
		}

		sendMatch(player, match);
	}

	public void sendMatch(Player player, Match match)
	{
		boolean playerA = match.PlayerA.Player.equals(player);
		boolean none = !playerA && !match.PlayerB.equals(player);
		PVPStats stats = playerA || none ? match.PlayerA : match.PlayerB;
		PVPStats other = playerA ? match.PlayerB : match.PlayerA;

		player.sendMessage(HubGameManager.getHeaderFooter());
		player.sendMessage(UtilText.centerChat(C.cGreenB + player.getName() + C.cWhite + " v " + C.cRedB + stats.Opponent.getName(), LineFormat.CHAT));
		player.sendMessage("");

		sendStat(player, "Health", stats.EndHealth, other.EndHealth, HEART);
		sendStat(player, "Damage Dealt", stats.DamageDealt, other.DamageDealt, HEART);
		sendStat(player, "Damage Taken", stats.DamageTaken, other.DamageTaken, HEART);
		sendStat(player, "Apples Used", stats.ApplesUsed, other.ApplesUsed, "");
		sendStat(player, "Blocks Placed", stats.BlocksPlaced, other.BlocksPlaced, "");
		sendStat(player, "Blocks Broken", stats.BlocksBroken, other.BlocksBroken, "");

		new JsonMessage(getStatString(
				"Bow Accuracy",
				((double) stats.ArrowsHit / Math.max(1, stats.ArrowsShot) * 100D),
				((double) other.ArrowsHit / Math.max(1, other.ArrowsShot) * 100D),
				"%", false))
				.hover(HoverEvent.SHOW_TEXT,
						getStatString("Arrows Shot", stats.ArrowsShot, other.ArrowsShot, "", true) + "\n" +
								getStatString("Arrows Hit", stats.ArrowsHit, other.ArrowsHit, "", true)
				)
				.sendToPlayer(player);

		sendStat(player, "Combo", stats.MaxCombo, other.MaxCombo, "");

		player.sendMessage("");
		player.sendMessage(C.cDGreen + "Hover over values to find out more information!");
		player.sendMessage(HubGameManager.getHeaderFooter());
	}

	private void sendStat(Player player, String statName, double statA, double statB, String suffix)
	{
		sendStat(player, statName, statA, statB, suffix, false);
	}

	private void sendStat(Player player, String statName, double statA, double statB, String suffix, boolean hoverText)
	{
		new JsonMessage(getStatString(statName, statA, statB, suffix, hoverText))
				.sendToPlayer(player);
	}

	private String getStatString(String statName, double statA, double statB, String suffix, boolean hoverText)
	{
		boolean aHigher = statA > statB;
		boolean equal = statA == statB;
		String statString =
				C.cGreen + (aHigher || equal ? C.Bold : "") + FORMAT.format(statA) + C.cWhite + suffix +
						"  v  " +
						C.cRed + (!aHigher || equal ? C.Bold : "") + FORMAT.format(statB) + C.cWhite + suffix;

		String output = C.cWhiteB + statName + "    " + statString;
		return hoverText ? output : UtilText.centerChat(output, LineFormat.CHAT);
	}

	public List<Match> getMatches()
	{
		return _matches;
	}

	public class Match
	{

		PVPStats PlayerA;
		PVPStats PlayerB;
		long Start;
		boolean Complete;

		Match(PVPStats playerA, PVPStats playerB)
		{
			PlayerA = playerA;
			PlayerB = playerB;
			Start = System.currentTimeMillis();
		}

		public Player[] getPlayers()
		{
			return new Player[]{PlayerA.Player, PlayerB.Player};
		}

		public long getStart()
		{
			return Start;
		}
	}

	private class PVPStats
	{

		Player Player;
		Player Opponent;
		double EndHealth;
		double DamageDealt;
		double DamageTaken;
		int ApplesUsed;
		int BlocksPlaced;
		int BlocksBroken;
		int ArrowsShot;
		int ArrowsHit;
		int CurrentCombo;
		int MaxCombo;

		PVPStats(Player player, Player opponent)
		{
			Player = player;
			Opponent = opponent;
		}
	}

}
