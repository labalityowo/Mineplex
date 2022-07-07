package mineplex.game.nano.game.components.damage;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.nano.GameManager;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.components.ComponentHook;
import mineplex.game.nano.game.components.team.GameTeam;
import mineplex.game.nano.game.event.GameStateChangeEvent;
import mineplex.minecraft.game.core.combat.CombatLog;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

@ReflectivelyCreateMiniPlugin
public class GameDamageManager extends GameManager implements ComponentHook<GameDamageComponent>
{

	private GameDamageComponent _hook;

	private GameDamageManager()
	{
		super("Damage Hook");
	}

	@Override
	public void setHook(GameDamageComponent hook)
	{
		_hook = hook;
	}

	@Override
	public GameDamageComponent getHook()
	{
		return _hook;
	}

	@EventHandler
	public void gameDeath(GameStateChangeEvent event)
	{
		if (event.getState() == GameState.Dead)
		{
			setHook(null);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void damage(CustomDamageEvent event)
	{
		if (_hook == null)
		{
			event.SetCancelled("No Damage Hook");
			return;
		}
		else if (!_hook.getGame().isLive())
		{
			event.SetCancelled("Game Not Live");
		}

		if (!_hook.isDamageEnabled())
		{
			event.SetCancelled("Damage Disabled");
		}
		else if (event.GetCause() == DamageCause.FALL && !_hook.isFallEnabled())
		{
			event.SetCancelled("Fall Disabled");
		}

		Player damageePlayer = event.GetDamageePlayer();
		Player damagerPlayer = event.GetDamagerPlayer(true);

		if (UtilPlayer.isSpectator(damageePlayer) || UtilPlayer.isSpectator(damagerPlayer))
		{
			event.SetCancelled("Spectator");
		}

		if (damageePlayer != null)
		{
			if (UtilPlayer.isSpectator(damageePlayer))
			{
				damageePlayer.setFireTicks(0);
			}

			if (damagerPlayer != null && !_manager.canHurt(damageePlayer, damagerPlayer))
			{
				event.SetCancelled("PVP Disabled");
			}
		}
	}

	@EventHandler
	public void combatDeath(CombatDeathEvent event)
	{
		if (_hook == null)
		{
			return;
		}

		Player player = event.GetEvent().getEntity();
		GameTeam team = _hook.getGame().getTeam(player);
		CombatLog log = event.GetLog();

		if (team != null)
		{
			log.SetKilledColor(team.getChatColour().toString());
		}

		if (log.GetKiller() != null)
		{
			Player killer = UtilPlayer.searchExact(log.GetKiller().getUniqueIdOfEntity());

			if (killer != null)
			{
				GameTeam killerTeam = _hook.getGame().getTeam(killer);

				if (killerTeam != null)
				{
					log.SetKillerColor(killerTeam.getChatColour().toString());
				}
		 	}
		}
	}
}
