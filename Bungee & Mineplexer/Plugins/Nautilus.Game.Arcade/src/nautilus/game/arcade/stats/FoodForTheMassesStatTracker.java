package nautilus.game.arcade.stats;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.game.Game;

public class FoodForTheMassesStatTracker extends StatTracker<Game>
{
	public FoodForTheMassesStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeathEvent(CombatDeathEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.GetLog().GetKiller() == null)
			return;

		if (!event.GetLog().GetKiller().IsPlayer())
			return;

		Player killer = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());
		if (killer == null)
			return;

		if (event.GetLog().GetPlayer() == null)
			return;

		if (!event.GetLog().GetPlayer().IsPlayer())
			return;

		Player player = UtilPlayer.searchExact(event.GetLog().GetPlayer().GetName());
		if (player == null)
			return;
		
		if (killer.getItemInHand().getType() != Material.APPLE)
		{
			return;
		}

		if (event.GetLog().GetKiller().GetReason() != null && event.GetLog().GetKiller().GetReason().contains("Apple Thrower"))
		{
			addStat(killer, "FoodForTheMasses", 1, false, false);
		}
			
	}
}
