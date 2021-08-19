package nautilus.game.arcade.game.games.moba.kit.common;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.particles.effects.LineParticle;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class DashSkill extends HeroSkill
{

	// Teleport Only
	protected boolean _teleport = false;
	protected boolean _horizontial = false;
	protected double _range = 10;
	protected ParticleType _particleType = ParticleType.FIREWORKS_SPARK;

	// Velocity
	protected long _velocityTime;
	protected double _velocityMagnitude = 1;
	protected boolean _velocityStopOnEnd = false;

	// Collisions
	protected boolean _collide = true;
	protected double _collideRange = 2;
	protected boolean _collideTeammates = false;
	protected boolean _collidePlayers = true;
	protected boolean _collideEntities = true;
	protected boolean _collideOnce = true;

	private final Map<Player, Long> _startTime;

	public DashSkill(String name, String[] perkDesc, ItemStack itemStack, int slot)
	{
		super(name, perkDesc, itemStack, slot, ActionType.ANY);

		_startTime = new HashMap<>();
		setSneakActivate(true);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();

		useSkill(player);
	}

	@EventHandler
	public void toggleSneak(PlayerToggleSneakEvent event)
	{
		if (!isSkillSneak(event))
		{
			return;
		}

		Player player = event.getPlayer();

		useSkill(player);
	}

	@Override
	public void useSkill(Player player)
	{
		super.useSkill(player);

		preDash(player);

		if (_teleport)
		{
			Vector direction = player.getLocation().getDirection();

			if (_horizontial)
			{
				direction.setY(0);
			}

			LineParticle particle = new LineParticle(player.getEyeLocation(), direction, 0.8, _range, _particleType, UtilServer.getPlayers());

			while (!particle.update())
			{
				dashTick(player);
				checkCollisions(player, particle.getLastLocation());
			}

			player.teleport(particle.getDestination());
			postDash(player);
		}
		// Otherwise we set their velocity.
		else if (_velocityTime > 0)
		{
			_startTime.put(player, System.currentTimeMillis());
		}
		else
		{
			setVelocity(player);
		}
	}

	@EventHandler
	public void updateVelocity(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<Player> iterator =  _startTime.keySet().iterator();

		while (iterator.hasNext())
		{
			Player player = iterator.next();
			long start = _startTime.get(player);

			if (UtilTime.elapsed(start, _velocityTime))
			{
				if (_velocityStopOnEnd)
				{
					UtilAction.zeroVelocity(player);
				}

				iterator.remove();
				postDash(player);
			}
			else
			{
				checkCollisions(player, player.getLocation());
				setVelocity(player);
			}
		}
	}

	private void setVelocity(Player player)
	{
		Vector direction = player.getLocation().getDirection().multiply(_velocityMagnitude);

		if (_horizontial)
		{
			direction.setY(0);
		}

		dashTick(player);
		UtilAction.velocity(player, direction);
	}

	private void checkCollisions(Player player, Location location)
	{
		// No colliding
		if (!_collide || !_collideEntities && !_collidePlayers)
		{
			return;
		}

		// All Living entities within the range
		for (Entry<LivingEntity, Double> entry : UtilEnt.getInRadius(location, _collideRange).entrySet())
		{
			LivingEntity entity = entry.getKey();
			double scale = entry.getValue();

			// If player hit themselves
			if (player.equals(entity))
			{
				continue;
			}

			if (_collideOnce && !Recharge.Instance.use(player, GetName() + entity.getUniqueId(), 500, false, false))
			{
				continue;
			}

			// If not allowing collisions with players
			if (!_collidePlayers)
			{
				continue;
			}

			boolean sameTeam = isTeamDamage(entity, player);

			// If their teams are the same and we don't allow collisions with teammates, ignore
			if (sameTeam && !_collideTeammates)
			{
				continue;
			}

			collideEntity(entity, player, scale, sameTeam);
		}
	}

	public void preDash(Player player)
	{
	}

	public void dashTick(Player player)
	{
	}

	public void collideEntity(LivingEntity entity, Player damager, double scale, boolean sameTeam)
	{
	}

	public void postDash(Player player)
	{
	}
}

