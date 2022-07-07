package nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.Game;

public class RecoveryMasterStatTracker extends StatTracker<Game>
{
	private final Map<UUID, Double> _damageTaken = new HashMap<>();

	public RecoveryMasterStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.GetLog().GetPlayer() == null)
			return;

		if (!event.GetLog().GetPlayer().IsPlayer())
			return;

		Player player = UtilPlayer.searchExact(event.GetLog().GetPlayer().GetName());
		if (player == null)
			return;

		_damageTaken.remove(player.getUniqueId());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;
		
		if (event.GetCause() == DamageCause.VOID)
		{
			return;
		}

		Player damagee = event.GetDamageePlayer();
		if (damagee == null)
			return;

		Double damage = _damageTaken.get(damagee.getUniqueId());

		damage = (damage == null ? 0 : damage) + event.GetDamage();

		_damageTaken.put(damagee.getUniqueId(), damage);

		if (damage >= 200)
			addStat(damagee, "RecoveryMaster", 1, true, false);
	}
}
