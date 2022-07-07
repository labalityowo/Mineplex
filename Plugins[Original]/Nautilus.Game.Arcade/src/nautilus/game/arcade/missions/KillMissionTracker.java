package nautilus.game.arcade.missions;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.mission.MissionTrackerType;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

import nautilus.game.arcade.game.Game;

public class KillMissionTracker extends GameMissionTracker<Game>
{

	public KillMissionTracker(Game game)
	{
		super(MissionTrackerType.GAME_KILL, game);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerDeath(CombatDeathEvent event)
	{
		Player player = event.GetEvent().getEntity();
		Player killerPlayer;
		String source = null;
		CombatComponent killer = event.GetLog().GetKiller();

		if (killer != null)
		{
			killerPlayer = UtilPlayer.searchExact(killer.getUniqueIdOfEntity());
			source = killer.GetLastDamageSource();
		}
		else
		{
			killerPlayer = player.getKiller();

			if (killerPlayer == null)
			{
				return;
			}
			else
			{
				for (CombatComponent attacker : event.GetLog().GetAttackers())
				{
					if (attacker.IsPlayer() && attacker.getUniqueIdOfEntity().equals(killerPlayer.getUniqueId()))
					{
						killerPlayer = UtilPlayer.searchExact(attacker.getUniqueIdOfEntity());
						source = attacker.GetLastDamageSource();
						break;
					}
				}
			}
		}

		if (killerPlayer == null || source == null || player.equals(killerPlayer))
		{
			return;
		}

		_manager.incrementProgress(killerPlayer, 1, _trackerType, getGameType(), ChatColor.stripColor(source));
	}
}
