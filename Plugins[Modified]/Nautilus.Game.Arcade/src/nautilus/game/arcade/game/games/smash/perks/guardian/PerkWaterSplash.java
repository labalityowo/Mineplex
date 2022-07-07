package nautilus.game.arcade.game.games.smash.perks.guardian;

import mineplex.core.common.util.*;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.smash.TeamSuperSmash;
import nautilus.game.arcade.kit.Perk;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.*;

public class PerkWaterSplash extends Perk
{

	private int _cooldown;
	private float _velocityY;
	private int _radius;
	private int _minAirTime;
	private int _secondBoostTime;
	private float _secondBoostVelocity;
	private int _damage;
	
	private Map<UUID, Long> _active = new HashMap<>();
	private Set<UUID> _usedSecondBoost = new HashSet<>();
	
	public PerkWaterSplash()
	{
		super("Water Splash", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Sword to use " + C.cGreen + "Water Splash", C.cYellow + "Hold Block" + C.cGray + " to " + C.cGreen + "Bounce higher with Water Splash" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_velocityY = getPerkFloat("Y Velocity");
		_radius = getPerkInt("Radius");
		_minAirTime = getPerkInt("Min Air Time (ms)");
		_secondBoostTime = getPerkInt("Second Boost Time (ms)");
		_secondBoostVelocity = getPerkFloat("Second Boost Velocity");
		_damage = getPerkInt("Damage");
	}

	@EventHandler
	public void activate(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		if (UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!UtilItem.isSword(player.getItemInHand()))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}
		
		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}
		
		UtilAction.velocity(player, new Vector(0, _velocityY, 0));
		_active.put(player.getUniqueId(), System.currentTimeMillis());
		
		List<Player> team = TeamSuperSmash.getTeam(Manager, player, true);
		for (Player other : UtilPlayer.getNearby(player.getLocation(), _radius))
		{
			if (team.contains(other))
			{
				continue;
			}
			
			UtilAction.velocity(other, UtilAlg.getTrajectory(other, player).setY(0.5));
		}
	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		if (_active.containsKey(event.GetDamageeEntity().getUniqueId()))	
		{
			event.SetKnockback(false);
		}
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		Iterator<UUID> iterator = _active.keySet().iterator();
		
		while (iterator.hasNext())
		{
			UUID uuid = iterator.next();	
			Player player = UtilPlayer.searchExact(uuid);
		
			if (player == null)
			{
				continue;
			}
			
			if (UtilPlayer.isSpectator(player))
			{
				iterator.remove();
				_usedSecondBoost.remove(uuid);
				continue;
			}
			
			UtilParticle.PlayParticleToAll(ParticleType.DRIP_WATER, player.getLocation(), 0.5F, 0.5F, 0.5F, 0.01F, 10, ViewDist.LONG);
			
			if (UtilEnt.isGrounded(player) && player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.FENCE && UtilTime.elapsed(_active.get(uuid), _minAirTime))
			{
				iterator.remove();
				_usedSecondBoost.remove(uuid);
				
				UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, player.getEyeLocation(), 0, 0, 0, 0.5F, 50, ViewDist.LONG);
				
				for (Block block : UtilBlock.getInRadius(player.getLocation(), _radius).keySet())
				{
					if (Math.random() < 0.9 || UtilBlock.airFoliage(block) || !UtilBlock.airFoliage(block.getRelative(BlockFace.UP)))
					{
						continue;
					}
					
					block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
				}
				
				player.getWorld().playSound(player.getLocation(), Sound.SPLASH2, 2, 0);
				
				Map<Player, Double> nearby = UtilPlayer.getInRadius(player.getLocation(), _radius);
				
				List<Player> team = TeamSuperSmash.getTeam(Manager, player, true);
				for (Player other : nearby.keySet())
				{
					if (team.contains(other))
					{
						continue;
					}
					
					double power = nearby.get(other);
										
					Manager.GetDamage().NewDamageEvent(other, player, null, DamageCause.CUSTOM, _damage * power, true, true, false, player.getName(), GetName());
					Manager.GetCondition().Factory().Falling(GetName(), other, player, _damage, false, true);
				}
			}
			else if (UtilTime.elapsed(_active.get(uuid), _secondBoostTime) && !_usedSecondBoost.contains(uuid) && player.isBlocking())
			{
				_usedSecondBoost.add(uuid);
				
				Vector direction = player.getLocation().getDirection().multiply(_secondBoostVelocity);
				
				UtilAction.velocity(player, direction);
			}
		}
	}
	
}
