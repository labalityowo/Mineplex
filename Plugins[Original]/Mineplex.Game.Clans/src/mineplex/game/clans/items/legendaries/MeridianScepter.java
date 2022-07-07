package mineplex.game.clans.items.legendaries;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.RGBData;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilCollections;
import mineplex.core.common.util.UtilColor;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.game.clans.clans.ClansManager;

public class MeridianScepter extends LegendaryItem
{
	private static final RGBData[] COLORS = { UtilColor.RgbPurple, UtilColor.RgbPurple.Lighten(), UtilColor.RgbPurple.Darken() };

	private long _interactWait;
	
	private transient Map<AttackAnimation, Integer> _animations = new HashMap<>();
	
	public MeridianScepter()
	{
		super("Meridian Scepter", UtilText.splitLinesToArray(new String[]
		{
			C.cWhite + "Legend says that this scepter was retrieved from the deepest trench in all of Minecraftia. It is said that he who wields this scepter holds the power of Poseidon himself.",
			" ",
			"#" + C.cYellow + "Right-Click" + C.cWhite + " to use " + C.cGreen + "Meridian Beam"
		}, LineFormat.LORE), Material.RECORD_6);
	}
	
	@Override
	public void update(Player wielder)
	{
		if (timeSinceLastBlock() < 98 && (System.currentTimeMillis() - _interactWait) >= 98)
		{
			if (ClansManager.getInstance().hasTimer(wielder))
			{
				UtilPlayer.message(wielder, F.main("Clans", "You are not allowed to fire the Meridian Scepter whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
				return;
			}
			
			if (ClansManager.getInstance().getClanUtility().getClaim(wielder.getLocation()) != null && ClansManager.getInstance().getClanUtility().getClaim(wielder.getLocation()).isSafe(wielder.getLocation()))
			{
				UtilPlayer.message(wielder, F.main("Clans", "You are not allowed to fire the " + F.elem("Meridian Scepter") + " whilst in a safe zone."));
				return;
			}
			
			if (Recharge.Instance.use(wielder, "Meridian Scepter", 2000, true, true))
			{
				fire(wielder);
				
				_interactWait = System.currentTimeMillis();
			}
		}
	}
	
	private void fire(final Player shooter)
	{
		final Location projectile = shooter.getEyeLocation();
		final Location origin = shooter.getEyeLocation();
		final Vector direction = shooter.getEyeLocation().getDirection().normalize().multiply(0.25);
		final int maxRange = 50;
		final int maxDings = maxRange * 4;
		
		UtilServer.repeat(new BukkitRunnable()
		{
			private int dingsDone;
			private Location previousLocation = projectile;

			private void burst()
			{
				for (Entity cur : projectile.getWorld().getEntities())
				{
					if (cur == shooter || !(cur instanceof LivingEntity) || (cur instanceof Player && UtilPlayer.isSpectator(cur)) || UtilEnt.hasFlag(cur, "LegendaryAbility.IgnoreMe"))
						continue;

					LivingEntity ent = (LivingEntity) cur;

					// If they are less than 0.5 blocks away
					if (ent.getEyeLocation().subtract(0, .3, 0).distance(projectile) <= 2)
					{						
						AttackAnimation aa = new AttackAnimation(ent, shooter);
						int i = UtilServer.getServer().getScheduler().scheduleSyncRepeatingTask(UtilServer.getPlugin(), () ->
						{
							aa.update();
						}, 0, 1);
						_animations.put(aa, i);
						
						UtilPlayer.message(ent, F.main("Clans", F.elem(shooter.getName()) + " hit you with a " +  F.elem("Meridian Scepter") + C.mBody + "."));
						UtilPlayer.message(shooter, F.main("Clans", "You hit " + F.elem(ent.getName()) + " with your " + F.elem("Meridian Scepter") + C.mBody + "."));
					}
				}

				playParticle(projectile, previousLocation);

				cancel();
			}

			public void run()
			{
				if (dingsDone >= maxDings || !shooter.isOnline())
				{
					burst();
				}
				else
				{
					for (int i = 0; i < 2; i++)
					{
						Player closestPlayer = null;
						double dist = 0;

						for (Player closest : UtilServer.getPlayers())
						{
							if (!closest.getWorld().equals(projectile.getWorld()))
							{
								continue;
							}
							
							if (ClansManager.getInstance().hasTimer(closest))
							{
								continue;
							}
							
							if (ClansManager.getInstance().isInClan(shooter) && ClansManager.getInstance().getClan(shooter).isMember(closest))
							{
								continue;
							}
							
							if (shooter.getGameMode().equals(GameMode.CREATIVE) || shooter.getGameMode().equals(GameMode.SPECTATOR))
							{
								continue;
							}
							
							if (closest.getGameMode().equals(GameMode.CREATIVE) || closest.getGameMode().equals(GameMode.SPECTATOR))
							{
								continue;
							}
							
							if (ClansManager.getInstance().getIncognitoManager().Get(closest).Hidden)
							{
								continue;
							}
							
							if (ClansManager.getInstance().isInClan(shooter) && ClansManager.getInstance().getClan(shooter).isAlly(ClansManager.getInstance().getClan(closest)))
							{
								continue;
							}
							
							if (ClansManager.getInstance().getClanUtility().getClaim(closest.getLocation()) != null && ClansManager.getInstance().getClanUtility().getClaim(closest.getLocation()).isSafe(closest.getLocation()))
							{
								continue;
							}
							
							Location loc = closest.getLocation();

							if (closest != shooter)
							{
								double dist1 = loc.distance(origin);
								if (dist1 < maxRange + 10)
								{
									double dist2 = projectile.distance(loc);
									if (closestPlayer == null || dist2 < dist)
									{
										double dist3 = projectile.clone().add(direction).distance(loc);

										if (dist3 < dist2)
										{
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
									.subtract(projectile.toVector());

							direction.add(newDirection.normalize().multiply(0.02)).normalize().multiply(0.25);
						}
						
						projectile.add(direction);
						
						for (Entity cur : projectile.getWorld().getEntities())
						{
							if (cur == shooter || !(cur instanceof LivingEntity)
									|| (cur instanceof Player && UtilPlayer.isSpectator(cur))
									|| UtilEnt.hasFlag(cur, "LegendaryAbility.IgnoreMe"))
								continue;
							
							LivingEntity ent = (LivingEntity) cur;

							Location eLoc = ent.getLocation();

							// If they are less than 0.5 blocks away
							if (eLoc.clone().add(0, projectile.getY() - eLoc.getY(), 0).distance(projectile) <= 0.7)
							{
								// If it is in their body height
								if (Math.abs((eLoc.getY() + (ent.getEyeHeight() / 1.5)) - projectile.getY()) <= ent.getEyeHeight() / 2)
								{
									burst();
									return;
								}
							}
						}

						if (UtilBlock.solid(projectile.getBlock()))
						{
							burst();
							return;
						}

						playParticle(projectile, previousLocation);
						previousLocation = projectile.clone();

						dingsDone++;
					}

					projectile.getWorld().playSound(projectile, Sound.BLAZE_BREATH, 0.2F, 1f);
				}
			}
		}, 0);
	}
	
	private void playParticle(Location start, Location end)
	{
		for (Location loc : UtilShapes.getLinesDistancedPoints(start, end, 0.06))
		{
			UtilParticle.PlayParticleToAll(ParticleType.RED_DUST, loc, UtilCollections.random(COLORS).ToVector(), 1f, 0, ViewDist.LONG);
		}
	}
	
	private class AttackAnimation
	{
		private LivingEntity _hit;
		private Player _shooter;
		private double _step;
		private double _radius;
		private long _start, _lastStepIncrease;
		
		public AttackAnimation(LivingEntity hit, Player shooter)
		{
			_step = 0;
			_start = System.currentTimeMillis();
			_lastStepIncrease = System.currentTimeMillis();
			_hit = hit;
			_shooter = shooter;
			_radius = 2;
		}
		
		public void update()
		{
			if (_hit == null || !_hit.isValid() || _hit.isDead() || ((_hit instanceof Player) && !((Player)_hit).isOnline()))
			{
				end();
				return;
			}
			if (UtilTime.elapsed(_lastStepIncrease, 500))
			{
				_step++;
				_lastStepIncrease = System.currentTimeMillis();
			}
			drawHelix();
			
			if (UtilTime.elapsed(_start, 2000))
			{
				_hit.getWorld().strikeLightningEffect(_hit.getLocation());
				ClansManager.getInstance().getDamageManager().NewDamageEvent(_hit, _shooter, null, 
						DamageCause.CUSTOM, 8, false, true, true,
						_shooter.getName(), "Meridian Scepter");
				ClansManager.getInstance().getCondition().Factory().Blind("Meridian Scepter", _hit, _shooter, 3, 0, true, true, false);
				end();
				return;
			}
		}
		
		private void end()
		{
			int id = _animations.remove(this);
			Bukkit.getScheduler().cancelTask(id);
		}
		
		private void drawHelix()
		{
			double height = Math.min(_step * 2, 8D);
			
			for (double y = 0; y <= height; y += .5)
			{
				double x = _radius * Math.cos(y);
				double z = _radius * Math.sin(y);
				Location play = _hit.getLocation().add(x, y, z);
				
				UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, play, null, 0, 3, ViewDist.MAX);
			}
		}
	}
}