package mineplex.core.antihack.animations;

import com.google.common.util.concurrent.AtomicDouble;
import mineplex.core.antihack.AntiHack;
import mineplex.core.antihack.guardians.AntiHackGuardian;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import net.minecraft.server.v1_8_R3.MathHelper;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class BanwaveAnimationSpin implements BanwaveAnimation
{
	@Override
	public void run(Player player, Runnable after)
	{
		float oldWalkSpeed = player.getWalkSpeed();
		player.setWalkSpeed(0);
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999, -10));

		double radius = 4;
		double heightAdj = 8;

		double baseDeg = 18;

		Location center = player.getLocation().add(0, heightAdj, 0);
		AntiHackGuardian north = new AntiHackGuardian(center.clone().add(0, 0, -radius), 0, 0, 0, 0, 0, 0, false);
		AntiHackGuardian east = new AntiHackGuardian(center.clone().add(radius, 0, 0), 0, 0, 0, 0, 0, 0, false);
		AntiHackGuardian south = new AntiHackGuardian(center.clone().add(0, 0, radius), 0, 0, 0, 0, 0, 0, false);
		AntiHackGuardian west = new AntiHackGuardian(center.clone().add(-radius, 0, 0), 0, 0, 0, 0, 0, 0, false);

		UtilEnt.CreatureLook(east.getEntity(), player);
		UtilEnt.CreatureLook(west.getEntity(), player);
		UtilEnt.CreatureLook(south.getEntity(), player);
		UtilEnt.CreatureLook(north.getEntity(), player);

		Function<Double, Double> magic = seconds -> Math.pow(2, seconds - 5);

		UtilServer.runSyncLater(() ->
		{
			north.shoot(player);
			east.shoot(player);
			south.shoot(player);
			west.shoot(player);

			// We get 5 seconds, or 100 ticks
			AtomicInteger timer = new AtomicInteger(5);

			AtomicDouble cNorth = new AtomicDouble(270);
			AtomicDouble cEast = new AtomicDouble(0);
			AtomicDouble cSouth = new AtomicDouble(90);
			AtomicDouble cWest = new AtomicDouble(180);

			UtilServer.runSyncTimer(new BukkitRunnable()
			{
				public void run()
				{
					timer.getAndIncrement();
					if (timer.get() > 100)
					{
						cancel();

						player.removePotionEffect(PotionEffectType.JUMP);
						player.setWalkSpeed(oldWalkSpeed);
						Location location = player.getLocation();

						UtilParticle.PlayParticle(UtilParticle.ParticleType.HUGE_EXPLOSION, player.getLocation(), 3f, 3f, 3f, 0, 32, UtilParticle.ViewDist.MAX, UtilServer.getPlayers());

						after.run();

						north.shoot(null);
						south.shoot(null);
						east.shoot(null);
						west.shoot(null);
						UtilEnt.CreatureLook(north.getEntity(), location);
						UtilEnt.CreatureLook(south.getEntity(), location);
						UtilEnt.CreatureLook(east.getEntity(), location);
						UtilEnt.CreatureLook(west.getEntity(), location);
						UtilServer.runSyncLater(() ->
						{
							north.remove();
							south.remove();
							east.remove();
							west.remove();
						}, 40L);
						return;
					}

					double seconds = timer.get() / 20.0;

					double rate = magic.apply(seconds) * 3 * baseDeg;

					player.getLocation(center);
					center.add(0, heightAdj, 0);

					{
						cNorth.addAndGet(rate);
						north.move(center.getX() + radius * MathHelper.cos((float) Math.toRadians(cNorth.get())), center.getY(), center.getZ() + radius * MathHelper.sin((float) Math.toRadians(cNorth.get())));
					}
					{
						cSouth.addAndGet(rate);
						south.move(center.getX() + radius * MathHelper.cos((float) Math.toRadians(cSouth.get())), center.getY(), center.getZ() + radius * MathHelper.sin((float) Math.toRadians(cSouth.get())));
					}
					{
						cEast.addAndGet(rate);
						east.move(center.getX() + radius * MathHelper.cos((float) Math.toRadians(cEast.get())), center.getY(), center.getZ() + radius * MathHelper.sin((float) Math.toRadians(cEast.get())));
					}
					{
						cWest.addAndGet(rate);
						west.move(center.getX() + radius * MathHelper.cos((float) Math.toRadians(cWest.get())), center.getY(), center.getZ() + radius * MathHelper.sin((float) Math.toRadians(cWest.get())));
					}
				}
			}, 5L, 1L);
		}, 20);
	}
}
