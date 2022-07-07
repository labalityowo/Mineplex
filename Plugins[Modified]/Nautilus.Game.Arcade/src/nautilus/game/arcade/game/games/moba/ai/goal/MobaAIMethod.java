package nautilus.game.arcade.game.games.moba.ai.goal;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public interface MobaAIMethod
{

	boolean updateMovement(LivingEntity entity, Location goal, float speed);

}
