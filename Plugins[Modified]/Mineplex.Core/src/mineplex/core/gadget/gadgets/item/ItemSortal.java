package mineplex.core.gadget.gadgets.item;

import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.gadget.util.CostConstants;

public class ItemSortal extends ItemGadget
{

	private static final Material[] SWORD_TYPES =
			{
					Material.GOLD_SWORD,
					Material.IRON_SWORD,
					Material.DIAMOND_SWORD
			};
	private static final double DIST_FACTOR_TICK = 0.5;
	private static final double DELTA_THETA = Math.PI / 10;
	private static final int EXPLOSION_RADIUS = 7;
	private static final int PARTICLE_RATE = 5;
	private static final int MAX_SWORD_TICKS = 150;

	private final Set<Sortal> _sortals;

	public ItemSortal(GadgetManager manager)
	{
		super(manager, "Gate of Babylon", new String[]
				{
						C.cGray + "A relic of ancient times,",
						C.cGray + "said to contain every weapon that has",
						C.cGray + "ever existed."
				}, CostConstants.POWERPLAY_BONUS, Material.BONE, (byte) 0, TimeUnit.SECONDS.toMillis(2), null);

		_sortals = new HashSet<>();

		Free = false;
		setPPCYearMonth(YearMonth.of(2018, Month.AUGUST));
	}

	@Override
	public void ActivateCustom(Player player)
	{
		createAndFire(player, player.getLocation().add(0, 3, 0), 20);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		super.disableCustom(player, message);

		_sortals.forEach(sortal ->
		{
			if (sortal.Wielder.equals(player))
			{
				sortal.remove();
			}
		});
	}

	@EventHandler
	public void adminCommand(PlayerCommandPreprocessEvent event)
	{
		Player player = event.getPlayer();

		if (!event.getMessage().equalsIgnoreCase("/gil") || !player.isOp())
		{
			return;
		}

		event.setCancelled(true);

		Location center = player.getLocation().add(0, 0.5, 0);

		Manager.runSyncTimer(new BukkitRunnable()
		{
			int i = 0;

			@Override
			public void run()
			{
				if (i++ >= 25)
				{
					cancel();
					return;
				}

				Vector direction = UtilAlg.getTrajectory(UtilMath.r(360), -UtilMath.r(60) - 20);
				Location location = center.clone().add(direction.multiply(18));
				location.setDirection(direction.multiply(-1));

				createAndFire(player, location, 80 - (i * 2) + UtilMath.r(25));
			}
		}, 20, 2);
	}

	private void createAndFire(Player wielder, Location location, int delay)
	{
		Sortal sortal = new Sortal(wielder, location);
		_sortals.add(sortal);

		Manager.runSyncTimer(new BukkitRunnable()
		{
			int ticks = 0;

			@Override
			public void run()
			{
				if (ticks % PARTICLE_RATE == 0)
				{
					sortal.drawParticles();
				}

				if (ticks > delay)
				{
					sortal.throwSword();
				}

				ticks++;

				if (sortal.shouldClean())
				{
					_sortals.remove(sortal);
					sortal.remove();
					cancel();
				}
			}
		}, 0, 1);
	}

	private class Sortal
	{

		final Player Wielder;
		final Location Point;
		final Vector Direction;
		final ArmorStand Sword;
		final DustSpellColor PortalColour = new DustSpellColor(240 + UtilMath.rRange(-5, 5), 205 + UtilMath.rRange(-20, 20), UtilMath.r(5));
		final DustSpellColor SwordColour;
		final List<Location> PortalPoints, SwordPathPoints;

		double DistFactor;

		Sortal(Player wielder, Location point)
		{
			Wielder = wielder;
			Point = point;
			Direction = point.getDirection();

			Location armourStand = point.clone()
					.subtract(0, 0.7, 0)
					.add(UtilAlg.getLeft(Direction).multiply(0.35));

			Sword = point.getWorld().spawn(armourStand, ArmorStand.class);
			Sword.setGravity(false);
			Sword.setArms(true);
			Sword.setVisible(false);
			Sword.setRightArmPose(new EulerAngle(Math.toRadians(Point.getPitch() - 4), 0, 0));

			if (UtilPlayer.isSlimSkin(wielder.getUniqueId()))
			{
				SwordColour = new DustSpellColor(240 + UtilMath.r(15), UtilMath.r(20), UtilMath.r(20));
			}
			else
			{
				SwordColour = new DustSpellColor(0, 30 + UtilMath.r(50), 255);
			}

			PortalPoints = new ArrayList<>();
			SwordPathPoints = new ArrayList<>();
			createPortalPoints();
		}

		void createPortalPoints()
		{
			Vector pointVector = Point.toVector();
			double yaw = Math.toRadians(Point.getYaw()), pitch = Math.toRadians(Point.getPitch());

			for (double radius = 0.2; radius < 1.1; radius += 0.2)
			{
				for (double theta = 0; theta < 2 * Math.PI; theta += DELTA_THETA)
				{
					Location location = Point.clone().add(Math.cos(theta) * radius, Math.sin(theta) * radius, 0);
					Vector vector = location.toVector().subtract(pointVector);

					UtilAlg.rotateAroundXAxis(vector, pitch);
					UtilAlg.rotateAroundYAxis(vector, yaw);

					PortalPoints.add(Point.clone().add(vector));
				}
			}

			UtilParticle.PlayParticleToAll(ParticleType.FLAME, Point, null, 0.1F, 20, ViewDist.LONG);
			Point.getWorld().playSound(Point, Sound.PORTAL, 1, 0.5F);
		}

		void drawParticles()
		{
			PortalPoints.forEach(location -> new ColoredParticle(ParticleType.RED_DUST, PortalColour, location).display(ViewDist.LONG));
			SwordPathPoints.forEach(location -> new ColoredParticle(ParticleType.RED_DUST, SwordColour, location).display(ViewDist.LONG));
		}

		void throwSword()
		{
			if (shouldClean())
			{
				return;
			}

			if (DistFactor == 0)
			{
				Material material = UtilMath.randomElement(SWORD_TYPES);

				if (material != null)
				{
					Sword.setItemInHand(new ItemStack(material));
				}

				Point.getWorld().playSound(Point, Sound.ZOMBIE_REMEDY, 1, 0.5F);
			}

			Vector direction = Direction.clone().multiply(DistFactor += DIST_FACTOR_TICK);
			Location newLocation = Sword.getLocation().add(Direction.clone().multiply(DIST_FACTOR_TICK));

			if (newLocation.getBlock().getType() != Material.AIR)
			{
				explode(false);
				return;
			}

			for (Sortal sortal : _sortals)
			{
				if (sortal.Wielder.equals(Wielder) || sortal.shouldClean() || !hasCollided(sortal))
				{
					continue;
				}

				explode(true);
				sortal.explode(true);
				return;
			}

			UtilEnt.setPosition(Sword, newLocation);
			SwordPathPoints.add(Point.clone().add(direction));
		}

		void explode(boolean swordCollision)
		{
			Location location = Sword.getLocation();

			if (swordCollision)
			{
				UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, location.add(0, 1, 0), null, 0.1F, 10, ViewDist.LONG);
				Point.getWorld().playSound(Point, Sound.ANVIL_LAND, 1, 0.5F);
			}
			else
			{
				UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, location.add(0, 1, 0), null, 0, 1, ViewDist.LONG);
				location.getWorld().playSound(location, Sound.EXPLODE, 1.3F, 0.5F);

				UtilPlayer.getInRadius(location, EXPLOSION_RADIUS).forEach((player, scale) ->
				{
					if (!Manager.selectEntity(ItemSortal.this, player))
					{
						return;
					}

					UtilAction.velocity(player, UtilAlg.getTrajectory(location, player.getLocation())
							.setY(0.8)
							.multiply(scale * 2));
				});
			}

			remove();
		}

		void remove()
		{
			Sword.remove();
			PortalPoints.clear();
			SwordPathPoints.clear();
		}

		boolean shouldClean()
		{
			return !Sword.isValid() || Sword.getTicksLived() > MAX_SWORD_TICKS;
		}

		boolean hasCollided(Sortal sortal)
		{
			return UtilMath.offsetSquared(Sword, sortal.Sword) < 3;
		}
	}
}
