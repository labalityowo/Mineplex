package nautilus.game.arcade.game.games.christmasnew.section.six.attack;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.christmasnew.ChristmasNewAudio;
import nautilus.game.arcade.game.games.christmasnew.section.six.phase.BossPhase;

public class AttackSeismicWave extends BossAttack
{

	private static final long DURATION = TimeUnit.SECONDS.toMillis(6);
	private static final int MAX_RADIUS = 14;
	private static final String HIT_KEY = "Wave Hit";

	private final int _delay;
	private final int _max;

	public AttackSeismicWave(BossPhase phase, int delay, int max)
	{
		super(phase);

		_delay = delay;
		_max = max;

		setAllowsMovement(true);
	}

	@Override
	public boolean isComplete()
	{
		return UtilTime.elapsed(_start, DURATION);
	}

	@Override
	public void onRegister()
	{
		ArcadeManager manager = _phase.getHost().getArcadeManager();

		_phase.getHost().sendSantaMessage("Watch out for that magic aura. Jump over the purple waves!", ChristmasNewAudio.SANTA_PURPLE);

		manager.runSyncTimer(new BukkitRunnable()
		{
			int waves = 0;

			@Override
			public void run()
			{
				Location center = _boss.getLocation().add(0, 0.3, 0);
				center.getWorld().playSound(center, Sound.NOTE_PLING, 2, 0.3F);

				manager.runSyncTimer(new BukkitRunnable()
				{
					double radius = 0;

					@Override
					public void run()
					{
						double deltaTheta = Math.PI / (radius * 3);

						for (double theta = 0; theta < 2 * Math.PI; theta += deltaTheta)
						{
							double x = radius * Math.cos(theta);
							double z = radius * Math.sin(theta);

							center.add(x, 0, z);

							UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, center, null, 0, 1, ViewDist.NORMAL);

							center.subtract(x, 0, z);
						}

						for (Player player : _phase.getHost().GetPlayers(true))
						{
							if (!Recharge.Instance.usable(player, HIT_KEY))
							{
								continue;
							}

							double offset = UtilMath.offset(center, player.getLocation()) - radius;

							if (offset > 0.5 || offset < -0.5 || !UtilEnt.onBlock(player))
							{
								continue;
							}

							Recharge.Instance.useForce(player, HIT_KEY, 800);
							manager.GetDamage().NewDamageEvent(player, _boss, null, DamageCause.CUSTOM, 8, true, true, true, _boss.getCustomName(), "Magic");
						}

						if ((radius += 0.4) >= MAX_RADIUS)
						{
							cancel();
						}
					}
				}, 0, 2);

				if (++waves == _max)
				{
					cancel();
				}
			}
		}, 30, _delay);
	}

	@Override
	public void onUnregister()
	{

	}
}
