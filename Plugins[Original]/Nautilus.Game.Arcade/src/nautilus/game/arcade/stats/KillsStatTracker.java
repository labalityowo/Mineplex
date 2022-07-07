package nautilus.game.arcade.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.titles.tracks.standard.WarriorTrack;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.game.Game;

public class KillsStatTracker extends StatTracker<Game>
{
	public KillsStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.GetLog().GetKiller() == null)
			return;

		if (!event.GetLog().GetKiller().IsPlayer())
			return;

		Player player = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());
		if (player == null)
			return;

		addStat(player, "Kills", 1, false, false);

		if (getGame().getArcadeManager().GetServerConfig().RewardStats)
		{
			getGame().getArcadeManager().getTrackManager().getTrack(WarriorTrack.class).earnedKill(player, 1);
		}

//		if (getGame().GetKit(player) != null)
//			addStat(player, getGame().GetKit(player).getName() + " Kills", 1, false, false);
	}
}
