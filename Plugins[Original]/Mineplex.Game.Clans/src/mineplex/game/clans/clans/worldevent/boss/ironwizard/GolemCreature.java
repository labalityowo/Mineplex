package mineplex.game.clans.clans.worldevent.boss.ironwizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.worldevent.api.BossAbility;
import mineplex.game.clans.clans.worldevent.api.EventCreature;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities.GolemBlockHail;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities.GolemBlockShot;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities.GolemDeadlyTremor;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities.GolemEarthquake;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities.GolemExplodingAura;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities.GolemIronHook;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities.GolemMeleeAttack;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities.GolemRupture;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities.GolemSlam;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities.GolemSpike;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class GolemCreature extends EventCreature<IronGolem>
{
	private int _lastAbility;
	private long _lastWalked;
	private Location _standing;
	private long _spawnDelay = System.currentTimeMillis();
	private long _reverseWalk;
	private Map<Class<? extends BossAbility<GolemCreature, IronGolem>>, Class<?>[]> _preferredCombos = new HashMap<>();
	private Class<? extends BossAbility<GolemCreature, IronGolem>> _lastAttack;
	private boolean _usedFinalAttack;
	private List<BossAbility<GolemCreature, IronGolem>> _currentAbilities = new ArrayList<>();
	private double _canDeadlyTremor = 225;
	private Vector _afkWalk = new Vector();
	private long _lastSlam;

	public GolemCreature(GolemBoss boss, Location location)
	{
		super(boss, location, "Iron Wizard", true, 3000, 30, true, IronGolem.class);

		spawnEntity();

		_preferredCombos.put(GolemEarthquake.class, new Class[]
			{
					GolemBlockHail.class, GolemRupture.class
			});
		_preferredCombos.put(GolemMeleeAttack.class, new Class[]
			{
					GolemEarthquake.class, GolemRupture.class
			});
		_preferredCombos.put(GolemDeadlyTremor.class, new Class[]
			{
					GolemMeleeAttack.class
			});
		_preferredCombos.put(GolemBlockShot.class, new Class[]
			{
					GolemEarthquake.class
			});
		_preferredCombos.put(GolemRupture.class, new Class[]
			{
					GolemBlockShot.class
			});
		_preferredCombos.put(GolemBlockHail.class, new Class[]
			{
				GolemDeadlyTremor.class
			});
	}
	
	private boolean hasFurther(Map<Player, Double> distances, double range)
	{
		for (Player player : distances.keySet())
		{
			if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
			{
				continue;
			}
			
			Double dist = distances.get(player);
			if (dist >= range)
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	protected void spawnCustom()
	{
		UtilEnt.vegetate(getEntity());
		_standing = getEntity().getLocation();
	}

	@SuppressWarnings("unchecked")
	@EventHandler
	public void abilityTick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		if (!UtilTime.elapsed(_spawnDelay, 5000))
		{
			_standing = getEntity().getLocation();
			return;
		}
		
		Iterator<BossAbility<GolemCreature, IronGolem>> itel = _currentAbilities.iterator();
		boolean canDoNew = _currentAbilities.size() < 3;

		while (itel.hasNext())
		{
			BossAbility<GolemCreature, IronGolem> ability = itel.next();

			if (ability.hasFinished())
			{
				itel.remove();
				ability.setFinished();
				_lastAbility = 20;

				HandlerList.unregisterAll(ability);
				if (DEBUG_MODE)
				{
					System.out.print("Unregistered golem ability " + ability.getClass().getSimpleName());
				}
			}
			else if (!ability.inProgress())
			{
				canDoNew = false;
			}
		}

		if (_lastAbility-- <= 0 && canDoNew && UtilBlock.solid(getEntity().getLocation().getBlock().getRelative(BlockFace.DOWN)))
		{
			Map<Class<? extends BossAbility<GolemCreature, IronGolem>>, Integer> weight = new HashMap<>();
			Map<Player, Double> dist = new HashMap<>();

			for (Player player : UtilPlayer.getNearby(getEntity().getLocation(), 50, true))
			{
				if (player.hasLineOfSight(getEntity()))
				{
					dist.put(player, player.getLocation().distance(getEntity().getLocation()));
				}
			}

			if (!dist.isEmpty())
			{
				double hp = getHealthPercent();

				{ // Melee
					List<Player> players = getPlayers(dist, UtilMath.r(10) == 0 ? 4 : 3);

					if (!players.isEmpty())
					{
						if (players.size() >= 4)
						{
							weight.put(GolemEarthquake.class, 999);
						}
						else
						{
							weight.put(GolemMeleeAttack.class, 999);
						}
					}
				}
				
				if (hasFurther(dist, 15))
				{ // Iron Hook
					weight.put(GolemIronHook.class, 6); //6
				}

				if (hp < 0.7)
				{ // Earthquake
					List<Player> players = getPlayers(dist, 10);

					double score = 0;

					for (Player player : players)
					{
						score += (8 - dist.get(player)) / 2;
					}

					if (players.size() >= 3)
					{
						score += 7;
					}

					if (score > 0)
					{
						weight.put(GolemEarthquake.class, (int) Math.ceil(score));
					}
				}

				{ // Wall explode
					if (!getPlayers(dist, 20).isEmpty() && getPlayers(dist, 4).isEmpty())
					{
						weight.put(GolemSpike.class, 8);
					}
				}

				{ // Block Shot
					List<Player> players = getPlayers(dist, 30);

					for (Player player : players)
					{
						if (dist.get(player) > 4)
						{
							weight.put(GolemBlockShot.class, 6);
							break;
						}
					}
				}

				{ // Rupture
					List<Player> players = getPlayers(dist, 30);

					if (!players.isEmpty())
					{
						weight.put(GolemRupture.class, (int) Math.min(5, dist.get(players.get(0))));
					}
				}

				{ // Slam
					List<Player> players = getPlayers(dist, 30);

					if (!players.isEmpty() && UtilTime.elapsed(_lastSlam, 20000))
					{
						weight.put(GolemSlam.class, 6);
					}
				}

				if (_canDeadlyTremor <= 0) // Deadly Tremor
				{
					List<Player> players = getPlayers(dist, 80);

					for (BossAbility<GolemCreature, IronGolem> ability : _currentAbilities)
					{
						if (ability instanceof GolemExplodingAura)
						{
							players.clear();
						}
					}

					if (!players.isEmpty())
					{
						weight.put(GolemDeadlyTremor.class, (int) 30);
					}
				}

				{// Block Hail
					List<Player> players = getPlayers(dist, 30);

					if (!players.isEmpty())
					{
						int we = _lastAttack == GolemEarthquake.class ? 20 : UtilMath.r(15) - 2;

						if (we > 0)
						{
							weight.put(GolemBlockHail.class, we);
						}
					}
				}

				if (!_usedFinalAttack && getHealth() < 90)
				{
					_usedFinalAttack = true;
					weight.clear();

					weight.put(GolemExplodingAura.class, 999);
				}

				if (_lastAttack != null && _preferredCombos.containsKey(_lastAttack))
				{
					weight.remove(_lastAttack);

					for (Class<?> c : _preferredCombos.get(_lastAttack))
					{
						if (weight.containsKey(c))
						{
							weight.put((Class<? extends BossAbility<GolemCreature, IronGolem>>)c, weight.get(c) * 4);
						}
					}
				}

				for (BossAbility<GolemCreature, IronGolem> ability : _currentAbilities)
				{
					weight.remove(ability.getClass());
				}

				BossAbility<GolemCreature, IronGolem> ability = null;

				if (!weight.isEmpty())
				{
					int i = 0;

					for (Integer entry : weight.values())
					{
						i += entry;
					}

					for (int a = 0; a < 10; a++)
					{
						int luckyNumber = UtilMath.r(i);

						for (Entry<Class<? extends BossAbility<GolemCreature, IronGolem>>, Integer> entry : weight.entrySet())
						{
							luckyNumber -= entry.getValue();

							if (luckyNumber <= 0)
							{
								try
								{
									ability = entry.getKey().getConstructor(GolemCreature.class).newInstance(this);

									if (ability.getTarget() == null)
									{
										ability = null;
									}
									else
									{
										break;
									}
								}
								catch (Exception ex)
								{
									ex.printStackTrace();
								}

								break;
							}
						}
					}
				}

				if (ability != null && ability.getTarget() != null)
				{
					_lastAttack = (Class<? extends BossAbility<GolemCreature, IronGolem>>) ability.getClass();

					if (ability instanceof GolemDeadlyTremor)
					{
						_canDeadlyTremor = 225;
					}
					else if (ability instanceof GolemSlam)
					{
						_lastSlam = System.currentTimeMillis();
					}

					Bukkit.getPluginManager().registerEvents(ability, UtilServer.getPlugin());
					
					if (DEBUG_MODE)
					{
						System.out.print("Golem boss is using " + ability.getClass().getSimpleName());
					}

					_currentAbilities.add(ability);
				}

				_lastAbility = 10;
			}

			_lastAttack = null;
		}

		boolean canMove = true;

		for (BossAbility<GolemCreature, IronGolem> ability : _currentAbilities)
		{
			try
			{
				ability.tick();
			}
			catch (Exception e)
			{
				e.printStackTrace(); //Keeps the boss from getting stuck if one of the moves throws an error in progression
			}

			if (!ability.canMove())
			{
				canMove = false;
			}
		}

		if (canMove)
		{
			Player target = null;
			double dist = 0;

			for (Player player : UtilPlayer.getNearby(getEntity().getLocation(), 50, true))
			{
				if (!player.hasLineOfSight(getEntity()))
				{
					continue;
				}

				double d = player.getLocation().distance(getEntity().getLocation());

				if (d > 1.5 && (d < 7 || d > 15) && (target == null || (d < 50 && dist > d)))
				{
					target = player;
					dist = d;
				}
			}

			Vector vec = null;
			boolean superWalk = false;

			if (target != null)
			{
				vec = target.getLocation().subtract(getEntity().getLocation()).toVector();
				vec.setY(getEntity().getLocation().getY());

				double len = vec.length();

				vec.setX(vec.getX() * (UtilMath.random.nextDouble() / 3D));
				vec.setZ(vec.getZ() * (UtilMath.random.nextDouble() / 3D));

				vec.multiply(len);

				if (target != null && dist < 8)
				{
					vec.multiply(-1);
					superWalk = true;
				}

				if (!UtilAlg.HasSight(getEntity().getLocation(),
						getEntity().getLocation().add(vec.clone().normalize().multiply(2))))
				{
					_reverseWalk = System.currentTimeMillis();
				}

				if (!UtilTime.elapsed(_reverseWalk, 4000))
				{
					vec.multiply(-1);
				}

			}
			else if (!UtilTime.elapsed(_lastWalked, 7000))
			{
				vec = _afkWalk;
			}
			else if (UtilTime.elapsed(_lastWalked, 12000))
			{
				_afkWalk = new Vector();

				for (int i = 0; i < 10; i++)
				{
					Vector vector = new Vector(UtilMath.r(20) - 10, 0, UtilMath.r(20) - 10);

					if (UtilAlg.HasSight(getEntity().getLocation(),
							getEntity().getLocation().add(vector.clone().normalize().multiply(2))))
					{
						vec = _afkWalk = vector;
						break;
					}
				}

				_lastWalked = System.currentTimeMillis();
			}

			if (vec != null)
			{
				{
					UtilEnt.CreatureMoveFast(getEntity(), getEntity().getLocation().add(vec),
							(target != null ? 1.8F : 1.1F) + (superWalk ? 0.4F : 0));
				}
			}

			_standing = getEntity().getLocation();
		}
		else
		{
			Location l = getEntity().getLocation();

			_standing.setYaw(l.getYaw());
			_standing.setPitch(l.getPitch());
			_standing.setY(l.getY());

			getEntity().teleport(_standing);
		}
	}

	private List<Player> getPlayers(final Map<Player, Double> map, double maxDist)
	{
		List<Player> list = new ArrayList<Player>();

		for (Player p : map.keySet())
		{
			if (map.get(p) <= maxDist)
			{
				list.add(p);
			}
		}

		Collections.sort(list, (o1, o2) ->
		{
			return Double.compare(map.get(o2), map.get(o1));
		});

		return list;
	}

	@EventHandler
	public void onGolemDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity().equals(getEntity()))
		{
			event.AddKnockback("Heavy Golem", 0.3);
		}
	}

	@EventHandler
	public void onRangedAttack(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity().equals(getEntity()))
		{
			if (event.GetDamageePlayer() != null)
			{
				if (event.GetProjectile() != null && event.GetProjectile() instanceof Arrow)
				{
					if (new Random().nextDouble() <= .5)
					{
						event.SetCancelled("Iron Skin Reflection");
						getEntity().getWorld().playSound(getEntity().getLocation(), Sound.ZOMBIE_METAL, 0.5f, 1.6f);
						return;
					}
				}
				
				double dist = event.GetDamageePlayer().getLocation().distance(getEntity().getLocation());

				double maxRange = _usedFinalAttack ? 20 : 45;

				double modifier = (maxRange - dist) / maxRange;

				if (modifier > 0)
				{
					event.AddMod("Ranged Resistance", 1 - modifier);
				}
				else
				{
					event.SetCancelled("Range too far");
				}
			}
		}
	}

	@Override
	public void dieCustom()
	{
		endAbility();
	}

	private void endAbility()
	{
		for (BossAbility<GolemCreature, IronGolem> ability : _currentAbilities)
		{
			ability.setFinished();
			HandlerList.unregisterAll(ability);
		}

		_currentAbilities.clear();
	}

	@Override
	public void setHealth(double health)
	{
		_canDeadlyTremor -= getHealth() - health;
		super.setHealth(health);
		if (getHealth() <= 100 && !_usedFinalAttack)
		{
			endAbility();
		}
	}
}