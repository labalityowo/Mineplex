package mineplex.minecraft.game.core.boss.broodmother.attacks;

import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilShapes;
import mineplex.minecraft.game.core.boss.broodmother.SpiderCreature;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class SpiderCeilingCling extends SpiderEggAbility
{
	private double _startedHealth;
	private boolean _isClinging;
	private int _tick;
	private Location _clingTo;
	private double _lastY;
	private int _eggsToLay = UtilMath.r(10) + 10;
	private boolean _upsideDown;

	public SpiderCeilingCling(SpiderCreature creature)
	{
		super(creature);

		_startedHealth = creature.getHealth();

		for (int i = 0; i < 10; i++)
		{
			Location loc = getLocation().add(UtilMath.rr(10, true), 12, UtilMath.rr(10, true));

			if (UtilAlg.HasSight(getLocation(), loc))
			{
				while (loc.getBlock().getRelative(BlockFace.UP).getType() == Material.AIR)
				{
					loc.add(0, 1, 0);
				}

				if (UtilAlg.HasSight(getLocation(), loc))
				{
					_clingTo = loc;
					break;
				}
			}
		}
	}

	@Override
	public boolean canMove()
	{
		return !_isClinging;
	}

	@Override
	public int getCooldown()
	{
		return 90;
	}

	public Player getTarget()
	{
		List<Player> targets = UtilPlayer.getNearby(getLocation(), 35, true);

		Collections.shuffle(targets);

		for (Player player : targets)
		{
			if (UtilAlg.HasSight(getLocation(), player))
			{
				return player;
			}
		}

		return null;
	}

	@Override
	public boolean hasFinished()
	{
		return _clingTo == null;
	}

	@Override
	public boolean inProgress()
	{
		return true;
	}

	@EventHandler
	public void onKnockback(CustomDamageEvent event)
	{
		if (!event.GetDamageeEntity().equals(getEntity()))
		{
			return;
		}

		if (!_isClinging)
		{
			return;
		}

		event.SetKnockback(false);
	}

	@Override
	public void setFinished()
	{
		if (_upsideDown)
		{
			getBoss().setUseName(true);
			getBoss().setShowHealthName(true);
			getBoss().setName(null);
		}

		setEggsFinished();
	}

	@Override
	public void tick()
	{
		if (!_isClinging && _upsideDown)
		{
			if (Math.abs(getLocation().getY() - _lastY) < 0.05)
			{
				_isClinging = true;
			}

			_lastY = getLocation().getY();
		}

		if (_isClinging && _upsideDown)
		{
			getEntity().setVelocity(new Vector(0, 1, 0));

			if (getBoss().getEvent().getCreatures().size() <= 3
					&& (_tick > 30 * 20 || _startedHealth - getBoss().getHealth() > 150 || getEggs() == 0))
			{
				_isClinging = false;
				_clingTo = null;
			}
		}

		if (!_isClinging && _clingTo != null)
		{
			int i = 0;

			for (Location loc : UtilShapes.getLinesDistancedPoints(getLocation(), _clingTo, 0.3))
			{
				if (i++ % 3 == _tick % 3)
				{
					UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, loc, 0F, 0F, 0F, 0, 1, ViewDist.NORMAL,
							UtilServer.getPlayers());
				}
			}

			if (_eggsToLay > 0)
			{
				if (_tick % 3 == 0)
				{
					Vector vec = new Vector(UtilMath.rr(1, true), UtilMath.rr(0.5, false), UtilMath.rr(1, true)).normalize()
							.multiply(0.7).setY(0.4 + (UtilMath.rr(0.4, false)));

					UtilEnt.CreatureLook(getEntity(), getLocation().add(vec));

					shootEgg(vec);

					_eggsToLay--;
				}
			}

			if (_eggsToLay < 3)
			{
				if (!_upsideDown)
				{
					_upsideDown = true;

					getBoss().setUseName(false);
					getBoss().setShowHealthName(false);
					getBoss().setName("Dinnerbone");
				}

				getEntity().setVelocity(UtilAlg.getTrajectory(getLocation(), _clingTo).multiply(0.4));
			}
		}

		_tick++;
	}

	@EventHandler
	public void onDamage(CustomDamageEvent event)
	{
		if (!event.GetDamageeEntity().equals(getEntity()))
		{
			return;
		}

		if (event.GetCause() != DamageCause.FALL)
		{
			return;
		}

		event.SetCancelled("Cancelled");
	}
}