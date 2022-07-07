package nautilus.game.arcade.game.games.smash.perks.chicken;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.smash.TeamSuperSmash;
import nautilus.game.arcade.game.games.smash.perks.SmashPerk;
import nautilus.game.arcade.kit.perks.data.ChickenMissileData;

public class PerkChickenRocket extends SmashPerk
{

	private int _cooldown;
	private int _minTime;
	private int _maxTime;
	private int _hitBoxRadius;
	private int _damageRadius;
	private int _damage;

	private Set<ChickenMissileData> _data = new HashSet<>();

	public PerkChickenRocket()
	{
		super("Chicken Missile", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + "Chicken Missile", C.cGreen + "Chicken Missile" + C.cGray
				+ " instantly recharges if you hit a player." });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_minTime = getPerkInt("Min Time (ms)");
		_maxTime = getPerkTime("Max Time (ms)");
		_hitBoxRadius = getPerkInt("Hit Box Radius");
		_damageRadius = getPerkInt("Damage Radius");
		_damage = getPerkInt("Damage");
	}

	@EventHandler
	public void Missile(PlayerInteractEvent event)
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

		if (!UtilItem.isAxe(player.getItemInHand()))
		{
			return;
		}

		if (isSuperActive(player))
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

		Manager.GetGame().CreatureAllowOverride = true;
		Chicken ent = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), Chicken.class);
		ent.getLocation().setPitch(0);
		ent.getLocation().setYaw(player.getLocation().getYaw());
		ent.setBaby();
		ent.setAgeLock(true);
		UtilEnt.vegetate(ent);
		Manager.GetGame().CreatureAllowOverride = false;

		_data.add(new ChickenMissileData(player, ent));

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<ChickenMissileData> dataIterator = _data.iterator();

		while (dataIterator.hasNext())
		{
			ChickenMissileData data = dataIterator.next();

			data.Chicken.setVelocity(data.Direction);
			data.Chicken.getWorld().playSound(data.Chicken.getLocation(), Sound.CHICKEN_HURT, 0.3f, 1.5f);

			if (!UtilTime.elapsed(data.Time, _minTime))
			{
				continue;
			}

			boolean detonate = false;

			if (UtilTime.elapsed(data.Time, _maxTime))
			{
				detonate = true;
			}
			else
			{
				List<Player> team = TeamSuperSmash.getTeam(Manager, data.Player, true);
				// Hit Entity
				for (Entity ent : UtilEnt.getInRadius(data.Chicken.getLocation(), _hitBoxRadius).keySet())
				{
					if (ent instanceof Arrow)
					{
						if (ent.isOnGround())
						{
							continue;
						}
					}

					if (ent.equals(data.Player) || ent.equals(data.Chicken))
					{
						continue;
					}

					if (ent instanceof Player)
					{
						if (UtilPlayer.isSpectator(ent))
						{
							continue;
						}
						
						if(team.contains(ent))
						{
							continue;
						}
					}

					// Recharge
					Recharge.Instance.useForce(data.Player, GetName(), -1);

					detonate = true;
					break;
				}

				// Hit Block
				if (!detonate && data.HasHitBlock())
				{
					detonate = true;
				}
			}

			if (detonate)
			{
				List<Player> team = TeamSuperSmash.getTeam(Manager, data.Player, true);
				// Damage
				for (LivingEntity ent : UtilEnt.getInRadius(data.Chicken.getLocation(), _damageRadius).keySet())
				{
					if (ent.equals(data.Player))
					{
						continue;
					}

					if (ent instanceof Player)
					{
						if (UtilPlayer.isSpectator(ent))
						{
							continue;
						}
						if (team.contains(ent))
						{
							continue;
						}
					}

					// Damage Event
					Manager.GetDamage().NewDamageEvent(ent, data.Player, null, DamageCause.PROJECTILE, _damage, false, true, false, data.Player.getName(), GetName());

					UtilAction.velocity(ent, UtilAlg.getTrajectory2d(data.Chicken, ent), 1.6, true, 0.8, 0, 10, true);
				}

				// Effect
				UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, data.Chicken.getLocation(), 0, 0, 0, 0, 1, ViewDist.MAX, UtilServer.getPlayers());
				data.Chicken.getWorld().playSound(data.Chicken.getLocation(), Sound.EXPLODE, 2f, 1.2f);

				// Firework
				UtilFirework.playFirework(data.Chicken.getLocation().add(0, 0.6, 0), Type.BALL, Color.WHITE, false, false);

				data.Chicken.remove();
				dataIterator.remove();
			}
		}
	}
}
