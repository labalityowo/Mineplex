package nautilus.game.arcade.game.games.wizards.spells;

import java.util.ArrayList;
import java.util.Random;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilShapes;
import nautilus.game.arcade.game.games.wizards.Spell;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SpellManaBolt extends Spell implements SpellClick
{

	public void castSpell(final Player player)
	{
		final Location missileLocation = player.getEyeLocation();
		final Location shotFrom = missileLocation.clone();
		final Vector direction = missileLocation.getDirection().normalize().multiply(0.3);
		final int maxRange = 20 + (10 * getSpellLevel(player));
		final int maxDings = maxRange * 3;
		final int damage = 4 + (getSpellLevel(player) * 2);

		new BukkitRunnable()
		{
			private int dingsDone;
			private Location previousLocation = missileLocation;

			private void burst()
			{
				for (Entity cur : missileLocation.getWorld().getEntities())
				{

					if (cur == player || !(cur instanceof LivingEntity) || (cur instanceof Player && UtilPlayer.isSpectator(cur)))
						continue;

					LivingEntity entity = (LivingEntity) cur;

					Location eLoc = entity.getLocation();

					// If they are less than 0.5 blocks away
					if (eLoc.clone().add(0, missileLocation.getY() - eLoc.getY(), 0).distance(missileLocation) <= 0.7)
					{
						// If it is in their body height
						if (Math.abs((eLoc.getY() + (entity.getEyeHeight() / 1.5)) - missileLocation.getY()) <= entity
								.getEyeHeight() / 2)
						{

							if (entity != player && (!(entity instanceof Player) || Wizards.IsAlive(entity)))
							{
								Wizards.Manager.GetDamage().NewDamageEvent(entity, player, null, DamageCause.MAGIC, damage, true,
										true, false, "Mana Bolt", "Mana Bolt");
							}
						}
					}
				}

				playParticle(missileLocation, previousLocation);

				for (int i = 0; i < 120; i++)
				{
					Vector vector = new Vector(new Random().nextFloat() - 0.5F, new Random().nextFloat() - 0.5F,
							new Random().nextFloat() - 0.5F);

					if (vector.length() >= 1)
					{
						i--;
						continue;
					}

					Location loc = missileLocation.clone();

					loc.add(vector.multiply(2));

					UtilParticle.PlayParticle(ParticleType.RED_DUST, loc, -1, 1, 1, 1, 0,
							ViewDist.LONG, UtilServer.getPlayers());
				}

				missileLocation.getWorld().playSound(missileLocation, Sound.BAT_TAKEOFF, 1.2F, 1);
				cancel();
			}

			public void run()
			{
				if (dingsDone >= maxDings || !player.isOnline() || !Wizards.Manager.IsAlive(player))
				{
					burst();
				}
				else
				{
					for (int i = 0; i < 2; i++)
					{
						Player closestPlayer = null;
						double dist = 0;

						// This lot of code makes the magic missile change direction towards the closest player in its path
						// Not entirely accurate, it doesn't go only for the people it can hit.
						// This makes magic missile pretty cool in my opinion
						for (Player closest : Wizards.GetPlayers(true))
						{

							Location loc = closest.getLocation();

							if (closest != player)
							{
								double dist1 = loc.distance(shotFrom);
								// If the player is a valid target
								if (dist1 < maxRange + 10)
								{
									double dist2 = missileLocation.distance(loc);
									// If the player is closer to the magic missile than the other dist
									if (closestPlayer == null || dist2 < dist)
									{
										double dist3 = missileLocation.clone().add(direction).distance(loc);

										if (dist3 < dist2)
										{
											// If the magic missile grows closer when it moves
											closestPlayer = closest;
											dist = dist2;
										}
									}
								}
							}
						}

						if (closestPlayer != null)
						{
							Vector newDirection = closestPlayer.getLocation().add(0, 1, 0).toVector()
									.subtract(missileLocation.toVector());

							direction.add(newDirection.normalize().multiply(0.01)).normalize().multiply(0.3);
						}

						missileLocation.add(direction);

						for (Entity cur : missileLocation.getWorld().getEntities())
						{

							if (cur == player || !(cur instanceof LivingEntity)
									|| (cur instanceof Player && UtilPlayer.isSpectator(cur)))
								continue;

							LivingEntity ent = (LivingEntity) cur;

							Location eLoc = ent.getLocation();

							// If they are less than 0.5 blocks away
							if (eLoc.clone().add(0, missileLocation.getY() - eLoc.getY(), 0).distance(missileLocation) <= 0.7)
							{
								// If it is in their body height
								if (Math.abs((eLoc.getY() + (ent.getEyeHeight() / 1.5)) - missileLocation.getY()) <= ent
										.getEyeHeight() / 2)
								{
									burst();
									return;
								}
							}
						}

						if (UtilBlock.solid(missileLocation.getBlock()))
						{
							burst();
							return;
						}

						playParticle(missileLocation, previousLocation);
						previousLocation = missileLocation.clone();

						dingsDone++;
					}

					missileLocation.getWorld().playSound(missileLocation, Sound.ORB_PICKUP, 0.7F, 0);
				}
			}
		}.runTaskTimer(Wizards.Manager.getPlugin(), 0, 0);

		charge(player);
	}

	private void playParticle(Location start, Location end)
	{
		final ArrayList<Location> locations = UtilShapes.getLinesDistancedPoints(start, end, 0.1);

		new BukkitRunnable()
		{
			int timesRan;

			public void run()
			{
				for (Location loc : locations)
				{
					UtilParticle.PlayParticle(ParticleType.RED_DUST, loc, -1, 1, 1, 1, 0,
							ViewDist.LONG, UtilServer.getPlayers());
				}

				if (timesRan++ > 1)
				{
					cancel();
				}
			}
		}.runTaskTimer(Wizards.getArcadeManager().getPlugin(), 0, 0);
	}
}
