package nautilus.game.arcade.game.games.moba.boss.pumpkin;

import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.ai.MobaAI;
import nautilus.game.arcade.game.games.moba.ai.goal.MobaAIMethod;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class PumpkinBossAI extends MobaAI
{

	private static final float SPEED_TARGET = 5F;
	private static final float SPEED_HOME = 3F;

	public PumpkinBossAI(Moba host, LivingEntity entity, Location home, MobaAIMethod aiMethod)
	{
		super(host, null, entity, home, SPEED_TARGET, SPEED_HOME, aiMethod);
	}

	@Override
	public String getBoundaryKey()
	{
		return "GRAY";
	}
}
