package nautilus.game.arcade.stats;

import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.game.games.bridge.Bridge;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class BridgesSniperStatTracker extends StatTracker<Bridge>
{
	public BridgesSniperStatTracker(Bridge game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (!getGame().IsLive())
			return;

		if (getGame().isBridgesDown())
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

		if (player == killer)
			return;
		
		if (event.GetLog().GetKiller().GetBestWeapon().contains("Archery"))
		{
			addStat(killer, "Sniper", 1, true, false);
		}
	}
}
