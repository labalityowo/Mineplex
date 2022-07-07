package nautilus.game.arcade.game.games.smash.perks.golem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.smash.TeamSuperSmash;
import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;

public class SmashGolem extends SmashUltimate
{

	private int _hitFrequency;
	private int _damageRadius;
	private int _effectRadius;

	private final Set<Player> _killed = new HashSet<>();

	public SmashGolem()
	{
		super("Earthquake", new String[] {}, Sound.IRONGOLEM_HIT, 0);
	}

	@Override
	public void setupValues()
	{
		super.setupValues();

		_hitFrequency = getPerkInt("Hit Frequency (ms)");
		_damageRadius = getPerkInt("Damage Radius");
		_effectRadius = getPerkInt("Effect Radius");
	}

	@Override
	public void cancel(Player player)
	{
		super.cancel(player);

		_killed.clear();
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
		{
			return;
		}

		List<Player> alivePlayers = Manager.GetGame().GetPlayers(true);

		for (Player player : alivePlayers)
		{
			if (!isUsingUltimate(player))
			{
				continue;
			}
			
			List<Player> team = TeamSuperSmash.getTeam(Manager, player, true);

			for (Player other : alivePlayers)
			{
				if (player.equals(other) || UtilPlayer.isSpectator(other) || team.contains(other) || _killed.contains(other))
				{
					continue;
				}

				other.playSound(other.getLocation(), Sound.MINECART_BASE, 0.2f, 0.2f);

				boolean grounded = false;
				
				for (Block block : UtilBlock.getInRadius(other.getLocation(), _damageRadius).keySet())
				{
					if (block.getType() != Material.AIR)
					{
						grounded = true;
						break;
					}
				}

				if (!grounded)
				{
					continue;
				}
				
				// Damage Event
				Manager.GetDamage().NewDamageEvent(other, player, null, DamageCause.CUSTOM, 1 + 2 * Math.random(), false, false, false, other.getName(), GetName());

				// Velocity
				if (Recharge.Instance.use(other, GetName() + " Hit", _hitFrequency, false, false))
				{
					UtilAction.velocity(other, new Vector(Math.random() - 0.5, Math.random() * 0.2, Math.random() - 0.5), Math.random() * 1 + 1, false, 0, 0.1 + Math.random() * 0.2, 2, true);
				}

				// Effect
				for (Block block : UtilBlock.getInRadius(other.getLocation(), _effectRadius).keySet())
				{
					if (Math.random() < 0.98)
					{
						continue;
					}

					if (!UtilBlock.solid(block))
					{
						continue;
					}

					if (!UtilBlock.airFoliage(block.getRelative(BlockFace.UP)))
					{
						continue;
					}

					other.playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
				}
			}
		}
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();

		if (getLastUltimate().isEmpty())
		{
			return;
		}

		_killed.add(player);
	}
}
