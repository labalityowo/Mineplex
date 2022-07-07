package mineplex.minecraft.game.core.boss.broodmother;

import java.util.HashMap;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.boss.EventCreature;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

public class SpiderMinionCreature extends EventCreature<CaveSpider>
{
	private long _lastAttack;
	private Location _moveTo;

	public SpiderMinionCreature(SpiderBoss boss, Location location, double maxHealth)
	{
		super(boss, location, "Spider Minion", true, maxHealth, CaveSpider.class);

		spawnEntity();
	}

	@EventHandler
	@Override
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (getEntity() == null || getEntity().isDead() || !getEntity().isValid())
		{
			remove();
		}
	}

	@Override
	protected void spawnCustom()
	{
		UtilEnt.vegetate(getEntity(), true);

		getEntity().setVelocity(new Vector(UtilMath.rr(0.5, true), 0.4, UtilMath.rr(0.4, true)));
	}

	@Override
	public void dieCustom()
	{
	}

	@EventHandler
	public void onTick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		if (UtilMath.r(150) == 0)
		{
			getEntity().getWorld().playSound(getEntity().getLocation(), Sound.SPIDER_IDLE, 2.5F, 1);
		}

		Location loc = getEntity().getLocation();

		Player target = null;

		HashMap<Player, Double> players = UtilPlayer.getInRadius(loc, 30);

		for (Player player : players.keySet())
		{
			if (player.hasLineOfSight(getEntity()) && !UtilPlayer.isSpectator(player)
					&& player.getGameMode() != GameMode.CREATIVE && (target == null || players.get(player) < players.get(target)))
			{
				target = player;
			}
		}

		if (target == null)
		{
			if (UtilTime.elapsed(_lastAttack, 6000))
			{
				_lastAttack = System.currentTimeMillis();
				_moveTo = loc.clone().add(UtilMath.rr(3, true), 0, UtilMath.rr(3, true));
			}

			if (_moveTo != null && _moveTo.distance(loc) > 0.2)
			{
				_moveTo.setY(loc.getY());
				UtilEnt.CreatureMoveFast(getEntity(), _moveTo, 1.2F);
			}

			return;
		}

		UtilEnt.CreatureMoveFast(getEntity(), target.getLocation(), 1.6F);

		if (UtilTime.elapsed(_lastAttack, 3000) && target.getLocation().distance(loc) < 2)
		{
			_lastAttack = System.currentTimeMillis();
			Vector vec = UtilAlg.getTrajectory2d(getEntity(), target);
			vec.multiply(0.4).setY(0.3);

			getEntity().setVelocity(vec);

			getEvent().getCondition().Factory().Blind("Spider Minion Bite", target, getEntity(), 1.5, 0, false, true, true);
			getEvent().getDamageManager().NewDamageEvent(target, getEntity(), null, DamageCause.ENTITY_ATTACK,
					2 * getDifficulty(), true, false, false, "Spider Minion Attack", "Spider Minion Attack");
		}
	}

	@EventHandler
	public void noFallDamage(CustomDamageEvent event)
	{
		if (getEntity() == null)
			return;

		if (!event.GetDamageeEntity().equals(getEntity()))
			return;

		DamageCause cause = event.GetCause();

		if (cause == DamageCause.FALL)
			event.SetCancelled("Cancel");
	}
}