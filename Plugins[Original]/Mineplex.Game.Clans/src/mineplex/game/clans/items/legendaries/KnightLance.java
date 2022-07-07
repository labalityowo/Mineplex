package mineplex.game.clans.items.legendaries;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.recharge.Recharge;
import mineplex.game.clans.clans.ClansManager;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class KnightLance extends LegendaryItem
{
	private static final double CHARGE_VELOCITY = 1.5d;
	private static final double MAX_BUILDUP_TICKS = 20 * 3;
	
	private boolean _charging;
	private double _buildup;
	
	private transient final Set<LivingEntity> _hit = new HashSet<>(); //Avoid creating a new list every tick

	public KnightLance()
	{
		super("Knight's Greatlance", new String[]
		{
			C.cWhite + "Relic of a bygone age.",
			C.cWhite + "Emblazoned with cryptic runes, this",
			C.cWhite + "Lance bears the marks of its ancient master.",
			C.cWhite + "You feel him with you always:",
			C.cWhite + "Heed his warnings and stave off the darkness.",
			" ",
			C.cWhite + "Deals " + C.cYellow + "8 Damage" + C.cWhite + " with attack",
			C.cYellow + "Right-Click" + C.cWhite + " to use " + C.cGreen + "Charge",
		}, Material.RECORD_12);
	}

	@Override
	public void update(Player wielder)
	{
		boolean holding = isHoldingRightClick();
		
		if (holding && !_charging)
		{
			if (canPropel(wielder))
			{
				_buildup = 0;
				_charging = true;
			}
		}
		else if (!holding && _charging)
		{
			_charging = false;
			_buildup = 0;
		}
		else if (_charging && !canPropel(wielder))
		{
			_charging = false;
			_buildup = 0;
		}
		
		if (_charging)
		{
			for (Entity near : wielder.getNearbyEntities(0.6, 1, 0.6))
			{
				if (near instanceof LivingEntity)
				{
					LivingEntity entity = (LivingEntity) near;
					if (!canHit(wielder, entity))
					{
						continue;
					}
					if (entity instanceof Horse)
					{
						_hit.add((LivingEntity)((Horse)entity).getPassenger());
						((Horse)entity).eject();
					}
					else
					{
						_hit.add(entity);
					}
				}
			}
			if (!_hit.isEmpty())
			{
				_charging = false;
				double damagePercentage = getBuildup() / MAX_BUILDUP_TICKS;
				_buildup = 0;
				
				wielder.getWorld().playSound(wielder.getLocation(), Sound.ZOMBIE_METAL, 1.5f, 0.5f);
				for (LivingEntity hit : _hit)
				{
					//Damage Event
					ClansManager.getInstance().getDamageManager().NewDamageEvent(hit, wielder, null, 
							DamageCause.CUSTOM, 2 + (10 * damagePercentage), false, true, false,
							wielder.getName(), "Knight's Greatlance Charge");	

					//Velocity
					UtilAction.velocity(hit,
							UtilAlg.getTrajectory2d(wielder.getLocation().toVector(), hit.getLocation().toVector()), 
							2.6, true, 0, 0.2, 1.4, true);
					
					//Condition
					ClansManager.getInstance().getCondition().Factory().Falling("Knight's Greatlance Charge", hit, wielder, 10, false, true);
				}
				_hit.clear();
				Recharge.Instance.useForce(wielder, "Knight Lance Charge Attack", 5000);
				Recharge.Instance.recharge(wielder, "Knight Lance Charge CD Inform");
				return;
			}
			
			propelPlayer(wielder);
			UtilTextBottom.displayProgress(getBuildup() / MAX_BUILDUP_TICKS, wielder);
			_buildup++;
		}
	}
	
	@Override
	public void onUnequip(Player wielder)
	{
		_charging = false;
		_buildup = 0;
	}

	@Override
	public void onAttack(CustomDamageEvent event, Player wielder)
	{
		event.AddMod("Knight's Greatlance", 7);
	}

	private void propelPlayer(Player player)
	{
		Vector direction = player.getLocation().getDirection().normalize();
		direction.setY(0);
		direction.multiply(CHARGE_VELOCITY);

		player.setVelocity(direction);
		
		UtilParticle.PlayParticle(ParticleType.CRIT, player.getLocation(), 
				(float)(Math.random() - 0.5), 0.2f + (float)(Math.random() * 1), (float)(Math.random() - 0.5), 0, 3,
				ViewDist.LONG, UtilServer.getPlayers());
	}

	private boolean canPropel(Player player)
	{
		return  UtilEnt.isGrounded(player) &&
				!UtilEnt.isInWater(player) &&
				player.getVehicle() == null &&
				!ClansManager.getInstance().getClanUtility().isSafe(player) &&
				Recharge.Instance.usable(player, "Knight Lance Charge Attack", Recharge.Instance.use(player, "Knight Lance Charge CD Inform", 1500, false, false));
	}
	
	private boolean canHit(Player player, LivingEntity entity)
	{
		if (UtilEnt.hasFlag(entity, "LegendaryAbility.IgnoreMe"))
		{
			return false;
		}
		if (ClansManager.getInstance().getClanUtility().isSafe(entity.getLocation()))
		{
			return false;
		}
		if (entity instanceof Horse)
		{
			Entity passenger = ((Horse)entity).getPassenger();
			if (passenger == null)
			{
				return false;
			}
			else
			{
				if (passenger instanceof LivingEntity)
				{
					if (!canHit(player, (LivingEntity)passenger))
					{
						return false;
					}
				}
				else
				{
					return false;
				}
			}
		}
		if (entity instanceof Player)
		{
			Player target = (Player) entity;
			if (ClansManager.getInstance().hasTimer(target))
			{
				return false;
			}
			if (ClansManager.getInstance().isInClan(player) && ClansManager.getInstance().getClan(player).isMember(target))
			{
				return false;
			}
			if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR)
			{
				return false;
			}
			if (ClansManager.getInstance().getIncognitoManager().Get(target).Hidden)
			{
				return false;
			}
			if (ClansManager.getInstance().isInClan(player) && ClansManager.getInstance().getClan(player).isAlly(ClansManager.getInstance().getClan(target)))
			{
				return false;
			}
		}
		
		return true;
	}
	
	private double getBuildup()
	{
		return UtilMath.clamp(_buildup, 0d, MAX_BUILDUP_TICKS);
	}
}