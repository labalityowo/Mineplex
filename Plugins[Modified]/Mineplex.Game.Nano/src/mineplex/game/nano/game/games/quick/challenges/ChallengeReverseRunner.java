package mineplex.game.nano.game.games.quick.challenges;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class ChallengeReverseRunner extends Challenge
{

	public ChallengeReverseRunner(Quick game)
	{
		super(game, ChallengeType.REVERSE_RUNNER);

		_timeout = TimeUnit.MINUTES.toMillis(1);

		_winConditions.setLastThree(true);
	}

	@Override
	public void challengeSelect()
	{

	}

	@Override
	public void disable()
	{

	}

	@EventHandler
	public void updateBlocks(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER || !UtilTime.elapsed(_startTime, 2000))
		{
			return;
		}

		for (Player player : _game.getAlivePlayers())
		{
			if (!isParticipating(player))
			{
				continue;
			}

			Location location = player.getLocation().add(0, 10, 0).getBlock().getLocation().add(0.5, 0, 0.5);

			if (!inArena(location))
			{
				failPlayer(player, true);
				continue;
			}

			FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(location, Material.STAINED_CLAY, (byte) 14);
			fallingBlock.setHurtEntities(true);
			fallingBlock.setDropItem(false);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void damage(CustomDamageEvent event)
	{
		if (event.GetCause() == DamageCause.SUFFOCATION)
		{
			event.AddMod("Falling Block", 4);
		}
	}
}
