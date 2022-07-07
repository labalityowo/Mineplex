package nautilus.game.arcade.game.games.christmasnew.section.six.attack;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.particleeffects.Effect;
import mineplex.core.particleeffects.ObjectiveEffect;
import mineplex.minecraft.game.core.damage.DamageManager;

import nautilus.game.arcade.game.games.christmasnew.ChristmasNewAudio;
import nautilus.game.arcade.game.games.christmasnew.section.six.phase.BossPhase;

public class AttackFlame extends BossAttack
{

	private static final long DURATION = TimeUnit.SECONDS.toMillis(4);

	private final double _chance;

	public AttackFlame(BossPhase phase, double chance)
	{
		super(phase);

		_chance = chance;
	}

	@Override
	public boolean isComplete()
	{
		return UtilTime.elapsed(_start, DURATION);
	}

	@Override
	public void onRegister()
	{
		_phase.getHost().sendSantaMessage("Move, quick! Dodge those flame fairies.", ChristmasNewAudio.SANTA_FAIRIES);

		DamageManager manager = _phase.getHost().getArcadeManager().GetDamage();
		Location location = _boss.getEyeLocation();
		List<Player> nearby = UtilPlayer.getNearby(location, 50);

		for (Player player : nearby)
		{
			if (nearby.size() < 3 || Math.random() < _chance)
			{
				Effect effect = new ObjectiveEffect(location, location.getDirection(), player.getEyeLocation(), 0.15F, 0.4F, ParticleType.FLAME);
				effect.setCallback(data ->
				{
					Location playerLocation = player.getLocation().add(0, 1, 0);

					if (UtilMath.offset2dSquared(data.getEffectLocation().getLocation(), playerLocation) < 16)
					{
						manager.NewDamageEvent(player, _boss, null, DamageCause.CUSTOM, 5, true, true, true, _boss.getCustomName(), "Flame Fairy");
						UtilParticle.PlayParticleToAll(ParticleType.LAVA, playerLocation, 0.8F, 0.4F, 0.8F, 0.1F, 15, ViewDist.NORMAL);
						player.getWorld().playSound(playerLocation, Sound.EXPLODE, 1, 1.2F);
					}
				});
				effect.start();
			}
		}
	}

	@Override
	public void onUnregister()
	{

	}
}
