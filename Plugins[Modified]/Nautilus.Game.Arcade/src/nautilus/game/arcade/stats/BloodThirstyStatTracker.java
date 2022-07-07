package nautilus.game.arcade.stats;

import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class BloodThirstyStatTracker extends StatTracker<Game>
{

	private final Map<UUID, Integer> _kills = new HashMap<>();
	private final String _stat;
	private final int _amount;
	private final Predicate<Player> _validKiller;
	private final Predicate<Player> _validKilled;

	public BloodThirstyStatTracker(Game game, String stat, int amount, Predicate<Player> validKiller, Predicate<Player> validKilled)
	{
		super(game);

		_stat = stat;
		_amount = amount;
		_validKiller = validKiller;
		_validKilled = validKilled;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onCombatDeath(CombatDeathEvent event)
	{
		CombatComponent player = event.GetLog().GetPlayer();
		CombatComponent killer = event.GetLog().GetKiller();

		if (!getGame().IsLive() || player == null || killer == null || !player.IsPlayer() || !killer.IsPlayer())
		{
			return;
		}

		Player killerPlayer = UtilPlayer.searchExact(killer.GetName());

		if (killerPlayer == null)
		{
			return;
		}

		Player killedPlayer = UtilPlayer.searchExact(player.GetName());

		if (killedPlayer == null)
		{
			return;
		}

		if (_validKiller != null && !_validKiller.test(killerPlayer) || _validKilled != null && !_validKilled.test(killedPlayer))
		{
			return;
		}

		int newKills = _kills.getOrDefault(killerPlayer.getUniqueId(), 0) + 1;

		_kills.put(killerPlayer.getUniqueId(), newKills);

		if (newKills >= _amount)
		{
			addStat(killerPlayer, _stat, 1, true, false);
		}
	}
}
