package mineplex.game.nano.game.games.quick.challenges;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class ChallengePunchAPig extends Challenge
{

	private final Map<Player, Integer> _punched;

	public ChallengePunchAPig(Quick game)
	{
		super(game, ChallengeType.PUNCH_A_PIG);

		_punched = new HashMap<>();

		_timeout = TimeUnit.SECONDS.toMillis(9);
	}

	@Override
	public void challengeSelect()
	{

	}

	@Override
	public void disable()
	{
		_punched.clear();
	}

	@EventHandler
	public void updatePigs(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		int max = Math.min(10, _players.size());
		int size = _game.getArenaSize() - 1;

		_game.getWorldComponent().setCreatureAllowOverride(true);

		for (int i = 0; i < max; i++)
		{
			Location location = UtilAlg.getRandomLocation(_game.getCenter(), size, 0, size);
			location.setYaw(UtilMath.r(360));

			location.getWorld().spawn(location, Pig.class);
		}

		_game.getWorldComponent().setCreatureAllowOverride(false);
	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		LivingEntity damagee = event.GetDamageeEntity();
		Player damager = event.GetDamagerPlayer(false);

		if (damagee instanceof Pig && damager != null)
		{
			int punched = _punched.getOrDefault(damager, 0) + 1;

			if (punched == 5)
			{
				completePlayer(damager, true);
			}
			else
			{
				_punched.put(damager, punched);
				UtilTextMiddle.display(null, C.cGreen + punched, 0, 20, 10, damager);
			}

			event.AddMod(damager.getName(), 20);
		}
	}

	@EventHandler
	public void entityDeath(EntityDeathEvent event)
	{
		event.setDroppedExp(0);
		event.getDrops().clear();
	}
}
