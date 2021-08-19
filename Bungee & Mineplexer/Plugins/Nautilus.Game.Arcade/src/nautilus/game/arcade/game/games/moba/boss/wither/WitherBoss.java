package nautilus.game.arcade.game.games.moba.boss.wither;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.common.util.UtilTime;
import mineplex.core.disguise.disguises.DisguiseWither;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.ai.MobaAI;
import nautilus.game.arcade.game.games.moba.ai.goal.MobaAIMethod;
import nautilus.game.arcade.game.games.moba.ai.goal.MobaDirectAIMethod;
import nautilus.game.arcade.game.games.moba.boss.MobaBoss;
import nautilus.game.arcade.game.games.moba.boss.wither.attack.BossAttackEarthquake;
import nautilus.game.arcade.game.games.moba.structure.tower.Tower;
import nautilus.game.arcade.game.games.moba.structure.tower.TowerDestroyEvent;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.concurrent.TimeUnit;

public class WitherBoss extends MobaBoss
{

	private static final String NAME = "Wither Boss";
	private static final float SPEED_TARGET = 4F;
	private static final float SPEED_HOME = 6F;
	private static final int INITIAL_HEALTH = 275;
	private static final MobaAIMethod AI_METHOD = new MobaDirectAIMethod();
	private static final int MIN_INFORM_TIME = (int) TimeUnit.SECONDS.toMillis(30);

	private final GameTeam _team;
	private MobaAI _ai;
	private MobaAI _aiOvertime;
	private DisguiseWither _disguise;
	private boolean _damageable;
	private long _lastInform;

	public WitherBoss(Moba host, Location location, GameTeam team)
	{
		super(host, location);

		_location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, host.GetSpectatorLocation())));
		_team = team;

		addAttack(new BossAttackEarthquake(this));
	}

	@Override
	public LivingEntity spawnEntity()
	{
		ArmorStand stand = _location.getWorld().spawn(_location, ArmorStand.class);

		stand.setMaxHealth(INITIAL_HEALTH);
		stand.setHealth(INITIAL_HEALTH);
		stand.setGravity(false);

		MobaUtil.setTeamEntity(stand, _team);
		UtilEnt.setBoundingBox(stand, 3, 5);

		_disguise = new DisguiseWither(stand);
		_disguise.setName(_team.GetColor() + _team.GetName() + "\'s Wither");
		_disguise.setCustomNameVisible(true);
		_host.getArcadeManager().GetDisguise().disguise(_disguise);

		return stand;
	}

	@Override
	public MobaAI getAi()
	{
		if (_ai == null)
		{
			_ai = new MobaAI(_host, _team, _entity, _location, SPEED_TARGET, SPEED_HOME, AI_METHOD);
		}
		else if (_host.getOvertimeManager().isOvertime())
		{
			if (_aiOvertime == null)
			{
				_aiOvertime = new WitherBossOvertimeAI(_host, _team, _entity, _location, SPEED_TARGET, SPEED_HOME, AI_METHOD);
			}

			return _aiOvertime;
		}

		return _ai;
	}

	@Override
	@EventHandler
	public void updateMovement(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !_host.IsLive())
		{
			return;
		}

		getAi().updateTarget();
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void preDamage(CustomDamageEvent event)
	{
		if (!event.GetDamageeEntity().equals(_entity) || MobaUtil.isInBoundary(_team, _entity, _location, getAi().getBoundaries(), event.GetDamagerPlayer(true)))
		{
			return;
		}

		event.SetCancelled("Outside of area");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void damage(CustomDamageEvent event)
	{
		// Not a Wither Boss
		if (event.isCancelled() || !(event.GetDamageeEntity().equals(_entity)))
		{
			return;
		}

		event.SetCancelled("Wither Boss");

		// Fire doesn't damage the wither
		if (event.GetCause() == DamageCause.FIRE || event.GetCause() == DamageCause.FIRE_TICK)
		{
			return;
		}

		LivingEntity damagee = event.GetDamageeEntity();
		Player damager = event.GetDamagerPlayer(true);

		if (damager != null)
		{
			GameTeam team = _host.GetTeam(damager);

			if (team == null || _team.equals(team))
			{
				return;
			}
		}

		// If not damageable
		if (!_damageable)
		{
			if (damager != null)
			{
				damager.sendMessage(F.main("Game", "You must destroy both towers before attacking the Wither!"));
				damager.playSound(damager.getLocation(), Sound.NOTE_BASS, 1, 0.8F);
			}

			return;
		}

		// Inform the team
		if (UtilTime.elapsed(_lastInform, MIN_INFORM_TIME))
		{
			_lastInform = System.currentTimeMillis();

			for (Player player : _team.GetPlayers(true))
			{
				player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1, 0.5F);
				player.sendMessage(F.main("Game", "Your Wither is under attack."));
			}
		}

		double newHealth = damagee.getHealth() - event.GetDamage();

		// Don't allow the wither to move because of damage
		damagee.playEffect(EntityEffect.HURT);

		if (newHealth > 0)
		{
			damagee.setHealth(newHealth);
			updateDisplay();
		}
		else
		{
			UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, _entity.getLocation().add(0, 1.5, 0), 0F, 0F, 0F, 0.1F, 1, ViewDist.LONG);
			_entity.remove();
		}
	}

	@EventHandler
	public void towerDestroy(TowerDestroyEvent event)
	{
		Tower tower = event.getTower();

		if (!_team.equals(tower.getOwner()))
		{
			return;
		}

		if (tower.isFirstTower())
		{
			_entity.setHealth(_entity.getHealth() - 50);
		}
		else
		{
			_entity.setHealth(_entity.getHealth() - 100);
			_damageable = true;
			updateDisplay();
		}
	}

	@EventHandler
	public void updateTeamDisplay(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || _entity == null)
		{
			return;
		}

		double percent = getHealthPercentage();

		for (Player player : _team.GetPlayers(true))
		{
			UtilTextTop.displayTextBar(player, percent, _team.GetColor() + "Your Wither");
		}
	}

	public double getHealthPercentage()
	{
		return _entity.getHealth() / _entity.getMaxHealth();
	}

	private void updateDisplay()
	{
		_disguise.setName(MobaUtil.getHealthBar(_entity, 40));
	}

	public GameTeam getTeam()
	{
		return _team;
	}

	public void setDamageable(boolean damageable)
	{
		_damageable = damageable;
	}
}
