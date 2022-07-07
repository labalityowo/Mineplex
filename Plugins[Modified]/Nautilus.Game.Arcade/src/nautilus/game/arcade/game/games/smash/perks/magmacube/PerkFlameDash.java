package nautilus.game.arcade.game.games.smash.perks.magmacube;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.data.FireflyData;

public class PerkFlameDash extends Perk
{
	
	private int _cooldown;
	private int _time;
	private int _damageRadius;
	private int _knockbackMagnitude;
	
	private Set<FireflyData> _data = new HashSet<>();

	public PerkFlameDash()
	{
		super("Flame Dash", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Spade to use " + C.cGreen + "Flame Dash" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_time = getPerkInt("Time (ms)");
		_damageRadius = getPerkInt("Damage Radius");
		_knockbackMagnitude = getPerkInt("Knockback Magnitude");
	}

	@EventHandler
	public void Skill(PlayerInteractEvent event)
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

		if (!UtilItem.isSpade(player.getItemInHand()) || !Recharge.Instance.use(player, GetName() + " Double Activation", 100, false, false))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (!Recharge.Instance.usable(player, GetName()))
		{
			boolean done = false;
			for (FireflyData data : _data)
			{
				if (data.Player.equals(player))
				{
					data.Time = 0;
					done = true;
				}
			}

			if (done)
			{
				UtilPlayer.message(player, F.main("Skill", "You ended " + F.skill(GetName()) + "."));
				UpdateMovement();
			}
			else
			{
				Recharge.Instance.use(player, GetName(), _cooldown, true, true);
			}

			return;
		}

		for (Player other : UtilServer.getPlayersCollection())
		{
			if (other.getSpectatorTarget() != null && other.getSpectatorTarget().equals(player))
			{
				other.setSpectatorTarget(null);
			}
		}

		Recharge.Instance.recharge(player, GetName());
		Recharge.Instance.use(player, GetName(), _cooldown, true, true);

		_data.add(new FireflyData(player));

		Manager.GetCondition().Factory().Cloak(GetName(), player, player, 2.5, false, false);

		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		UpdateMovement();
	}

	private void UpdateMovement()
	{
		Iterator<FireflyData> dataIterator = _data.iterator();

		while (dataIterator.hasNext())
		{
			FireflyData data = dataIterator.next();

			// Move
			if (!UtilTime.elapsed(data.Time, _time))
			{
				Vector vel = data.Location.getDirection();
				vel.setY(0);
				vel.normalize();
				vel.setY(0.05);

				UtilAction.velocity(data.Player, vel);

				// Sound
				data.Player.getWorld().playSound(data.Player.getLocation(), Sound.FIZZ, 0.6f, 1.2f);

				// Particles
				UtilParticle.PlayParticle(ParticleType.FLAME, data.Player.getLocation().add(0, 0.4, 0), 0.2f, 0.2f, 0.2f, 0f, 3, ViewDist.LONGER, UtilServer.getPlayers());
			}
			// End
			else
			{
				for (Player other : UtilPlayer.getNearby(data.Player.getLocation(), _damageRadius))
				{
					if (other.equals(data.Player))
					{
						continue;
					}
					
					if (UtilPlayer.isSpectator(other))
					{
						continue;
					}
					
					double dist = UtilMath.offset(data.Player.getLocation(), data.Location) / 2;

					// Damage Event
					Manager.GetDamage().NewDamageEvent(other, data.Player, null, DamageCause.CUSTOM, 2 + dist, true, true, false, data.Player.getName(), GetName());

					UtilPlayer.message(other, F.main("Game", F.elem(Manager.GetColor(data.Player) + data.Player.getName()) + " hit you with " + F.elem(GetName()) + "."));
				}

				// End Invisible
				Manager.GetCondition().EndCondition(data.Player, null, GetName());

				// Sound
				data.Player.getWorld().playSound(data.Player.getLocation(), Sound.EXPLODE, 1f, 1.2f);

				// Particles
				UtilParticle.PlayParticle(ParticleType.FLAME, data.Player.getLocation(), 0.1f, 0.1f, 0.1f, 0.3f, 100, ViewDist.MAX, UtilServer.getPlayers());
				UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, data.Player.getLocation().add(0, 0.4, 0), 0.2f, 0.2f, 0.2f, 0f, 1, ViewDist.MAX, UtilServer.getPlayers());

				dataIterator.remove();
			}
		}
	}

	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}
		
		event.AddKnockback(GetName(), _knockbackMagnitude);
	}
}
