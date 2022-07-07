package nautilus.game.arcade.game.games.christmasnew.section.six.attack;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.christmasnew.section.six.phase.BossPhase;

public class AttackFire extends BossAttack
{

	private static final long DURATION = TimeUnit.SECONDS.toMillis(5);
	private static final int TIME_BEFORE_STRIKE = 1500;
	private static final int TICKS_BEFORE_STRIKE = TIME_BEFORE_STRIKE / 50;
	private static final int TIME_FIRE = 2500;

	private final int _max;
	private final int _radius;
	private final Location _location;

	public AttackFire(BossPhase phase, int max, int radius)
	{
		this(phase, max, radius, null);
	}

	public AttackFire(BossPhase phase, int radius, Location location)
	{
		this(phase, 1, radius, location);
	}

	private AttackFire(BossPhase phase, int max, int radius, Location location)
	{
		super(phase);

		_max = max;
		_radius = radius;
		_location = location;
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
		BlockRestore restore = manager.GetBlockRestore();

		_phase.getHost().getArcadeManager().runSyncTimer(new BukkitRunnable()
		{
			int strikes = 0;

			@Override
			public void run()
			{
				Location location;

				if (_location == null)
				{
					location = UtilAlg.getRandomLocation(_phase.getBossSpawn(), 25, 0, 15);
				}
				else
				{
					location = _location;
				}

				Set<Block> fireBlocks = new HashSet<>();

				for (Block block : UtilBlock.getInRadius(location, _radius).keySet())
				{
					if (block.getType() == Material.AIR || block.getType() == Material.FIRE || block.getRelative(BlockFace.UP).getType() != Material.AIR)
					{
						continue;
					}

					restore.add(block, Material.WOOL.getId(), (byte) 14, TIME_BEFORE_STRIKE);
					fireBlocks.add(block.getRelative(BlockFace.UP));
				}

				manager.runSyncLater(() ->
				{
					UtilParticle.PlayParticleToAll(ParticleType.LAVA, location.clone().add(0, 1.5, 0), _radius, 0.3F, _radius, 0.1F, 15, ViewDist.LONG);
					location.getWorld().playSound(location, Sound.EXPLODE, 2, 1);

					for (Block block : fireBlocks)
					{
						restore.add(block, Material.FIRE.getId(), (byte) 0, TIME_FIRE + UtilMath.r(500));
					}

					UtilPlayer.getInRadius(location, _radius + 3).forEach((player, scale) ->
					{
						manager.GetDamage().NewDamageEvent(player, _boss, null, DamageCause.CUSTOM, 20 * (scale + 0.3), false, true, true, _boss.getCustomName(), "Fire");
					});

				}, TICKS_BEFORE_STRIKE);

				if (++strikes > _max)
				{
					cancel();
				}
			}
		}, 1, 10);
	}

	@Override
	public void onUnregister()
	{

	}
}
