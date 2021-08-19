package nautilus.game.arcade.game.games.christmasnew.section.six.phase;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.ChristmasNewAudio;
import nautilus.game.arcade.game.games.christmasnew.section.Section;
import nautilus.game.arcade.game.games.christmasnew.section.six.attack.AttackArmouredMobs;
import nautilus.game.arcade.game.games.christmasnew.section.six.attack.AttackFire;
import nautilus.game.arcade.game.games.christmasnew.section.six.attack.AttackFlame;
import nautilus.game.arcade.game.games.christmasnew.section.six.attack.AttackSeismicWave;
import nautilus.game.arcade.game.games.christmasnew.section.six.attack.AttackThrowTNT;
import nautilus.game.arcade.game.games.christmasnew.section.six.attack.BossAttack;

public class Phase2 extends BossPhase
{

	private static final int HEALTH = 500;

	private final Location _podium;
	private final AttackThrowTNT _tntAttack;

	private boolean _completing;

	public Phase2(ChristmasNew host, Section section)
	{
		super(host, section);

		_podium = _worldData.GetCustomLocs("BOSS SPAWN 2").get(0);

		_tntAttack = new AttackThrowTNT(this, 4);

		addAttacks(
				new AttackFire(this, 3, 5),
				new AttackArmouredMobs(this, 3),
				new AttackSeismicWave(this, 40, 2),
				new AttackFlame(this, 0.4)
		);
	}

	@Override
	public boolean isComplete()
	{
		return false;
	}

	@Override
	public void onAttack(BossAttack attack)
	{
		boolean onPodium = UtilMath.offsetSquared(_boss.getLocation(), _podium) < 4;

		if (attack != null && attack.isAllowingMovement())
		{
			if (onPodium)
			{
				teleport(getBossSpawn());
			}

			UtilEnt.CreatureMove(_boss, UtilAlg.getRandomLocation(getBossSpawn(), 25, 0, 15), 1);

			_tntAttack.start();

			_host.getArcadeManager().runSyncTimer(new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if (_tntAttack.isComplete())
					{
						_tntAttack.stop();
					}
				}
			}, 10, 10);
		}
		else if (!onPodium)
		{
			teleport(_podium);
			UtilEnt.CreatureMove(_boss, _boss.getLocation().add(0, 0.2, 0), 1);
		}
	}

	@Override
	public void onRegister()
	{
		_podium.getBlock().getRelative(BlockFace.DOWN).setType(Material.BARRIER);
		_boss.setMaxHealth(HEALTH);
		_boss.setHealth(HEALTH);

		onAttack(null);

		_host.getArcadeManager().runSyncLater(() -> _host.sendBossMessage("Now you've really done it!", ChristmasNewAudio.PK_ANGRY), 180);
		_host.getArcadeManager().runSyncLater(() -> _host.sendSantaMessage("Great! He’s now vulnerable to your attacks.", ChristmasNewAudio.SANTA_VULNERABLE), 280);

		Map<Player, Location> lastLocation = new HashMap<>();

		for (Player player : _host.GetPlayers(true))
		{
			Location location = player.getLocation();
			Vector direction = UtilAlg.getTrajectory(player, _boss);
			location.setYaw(UtilAlg.GetYaw(direction));
			location.setPitch(UtilAlg.GetPitch(direction));

			lastLocation.put(player, location);
		}

		_host.getArcadeManager().runSyncTimer(new BukkitRunnable()
		{
			int iterations = 0;

			@Override
			public void run()
			{
				if (iterations % 3 == 0)
				{
					_boss.getWorld().strikeLightningEffect(_boss.getLocation());
				}

				lastLocation.forEach((player, location) ->
				{
					location = location.clone();
					location.setYaw(location.getYaw() + UtilMath.rRange(-5, 5));
					location.setPitch(location.getPitch() + UtilMath.rRange(-5, 5));
					player.teleport(location);
				});

				if (++iterations == 12)
				{
					cancel();
				}
			}
		}, 1, 2);
	}

	@Override
	public void onUnregister()
	{

	}

	private void teleport(Location location)
	{
		UtilParticle.PlayParticleToAll(ParticleType.CLOUD, _boss.getLocation().add(0, 1, 0), 1, 1, 1, 0.1F, 50, ViewDist.LONG);
		_boss.teleport(location);
		UtilParticle.PlayParticleToAll(ParticleType.CLOUD, _boss.getLocation().add(0, 1, 0), 1, 1, 1, 0.1F, 50, ViewDist.LONG);
	}


	@EventHandler
	public void updateObjective(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		_section.setObjective("Defeat the Pumpkin King", _boss.getHealth() / _boss.getMaxHealth());
	}

	@EventHandler
	public void projectileHit(ProjectileHitEvent event)
	{
		Projectile projectile = event.getEntity();
		ProjectileSource source = projectile.getShooter();

		if (source == null || !(source instanceof Player))
		{
			return;
		}

		if (projectile instanceof Arrow && UtilMath.offset2dSquared(_boss, projectile) < 25)
		{
			Player player = (Player) projectile.getShooter();

			_host.getArcadeManager().GetDamage().NewDamageEvent(_boss, player, projectile, DamageCause.CUSTOM, 4, false, true, true, player.getName(), "Archery");
		}
	}

	@EventHandler
	public void bossDamage(CustomDamageEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		LivingEntity damagee = event.GetDamageeEntity();

		if (!damagee.equals(_boss))
		{
			return;
		}

		event.SetKnockback(false);

		Player damager = event.GetDamagerPlayer(true);

		if (damager != null)
		{
			if (event.GetProjectile() == null)
			{
				damager.playSound(damagee.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 0.8F);
				UtilAction.velocity(damager, UtilAlg.getTrajectory(damagee, damager), 0.7, false, 0, 0.4, 0.5, true);
			}
			else if (Math.random() < 0.4 && Recharge.Instance.use(damager, "Fire Attack", 1000, false, false))
			{
				AttackFire attackFire = new AttackFire(this, 3, damager.getLocation());
				attackFire.start();

				_host.getArcadeManager().runSyncLater(attackFire::stop, 60);
			}
		}
	}

	@EventHandler
	public void updateEnd(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TWOSEC || !_boss.isDead())
		{
			return;
		}

		if (!_completing)
		{
			clearAttacks();
			_completing = true;
			_host.sendBossMessage("No!!!!!! I can't believe you defeated me!", ChristmasNewAudio.PK_DEFEATED);
			_host.endGame(true, "The Pumpkin King was Defeated!");
		}
	}
}
