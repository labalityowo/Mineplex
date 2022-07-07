package nautilus.game.arcade.game.modules;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

/**
 * A legacy module that awards players enchanting levels when they kill or assist on killing another player.
 */
public class EXPForKillsModule extends Module
{

	private final int _levelsForKill, _levelsForAssist;

	public EXPForKillsModule()
	{
		this(2, 1);
	}

	public EXPForKillsModule(int levelsForKill, int levelsForAssist)
	{
		_levelsForKill = levelsForKill;
		_levelsForAssist = levelsForAssist;
	}

	@EventHandler
	public void onCombatDeathEventLevels(CombatDeathEvent event)
	{
		if (!(event.GetEvent().getEntity() instanceof Player))
		{
			return;
		}

		Player killed = (Player) event.GetEvent().getEntity();

		if (event.GetLog().GetKiller() != null)
		{
			Player killer = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());

			if (killer != null && !killer.equals(killed))
			{
				// Kill
				killer.giveExpLevels(_levelsForKill);
				killer.playSound(killer.getLocation(), Sound.LEVEL_UP, 1f, 1f);
			}
		}

		for (CombatComponent log : event.GetLog().GetAttackers())
		{
			if (event.GetLog().GetKiller() != null && log.equals(event.GetLog().GetKiller()))
			{
				continue;
			}

			Player assist = UtilPlayer.searchExact(log.GetName());

			// Assist
			if (assist != null)
			{
				assist.giveExpLevels(_levelsForAssist);
				assist.playSound(assist.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
			}
		}
	}
}
