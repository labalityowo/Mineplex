package nautilus.game.arcade.game.modules;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.minecraft.game.core.condition.ConditionFactory;

import nautilus.game.arcade.events.PlayerGameRespawnEvent;

public class SpawnRegenerationModule extends Module
{

	@EventHandler
	public void respawn(PlayerGameRespawnEvent event)
	{
		Player player = event.GetPlayer();
		ConditionFactory factory = getGame().getArcadeManager().GetCondition().Factory();
		String reason = "Spawn Regeneration";

		factory.Regen(reason, player, player, 5, 2, false, false, false);
		factory.Protection(reason, player, player, 5, 2, false, false, false);
	}

}
