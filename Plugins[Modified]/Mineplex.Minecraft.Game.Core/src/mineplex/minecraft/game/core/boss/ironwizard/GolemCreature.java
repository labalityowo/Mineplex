package mineplex.minecraft.game.core.boss.ironwizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.boss.BossAbility;
import mineplex.minecraft.game.core.boss.EventCreature;
import mineplex.minecraft.game.core.boss.ironwizard.abilities.GolemBlockHail;
import mineplex.minecraft.game.core.boss.ironwizard.abilities.GolemBlockShot;
import mineplex.minecraft.game.core.boss.ironwizard.abilities.GolemDeadlyTremor;
import mineplex.minecraft.game.core.boss.ironwizard.abilities.GolemEarthquake;
import mineplex.minecraft.game.core.boss.ironwizard.abilities.GolemExplodingAura;
import mineplex.minecraft.game.core.boss.ironwizard.abilities.GolemIronHook;
import mineplex.minecraft.game.core.boss.ironwizard.abilities.GolemMeleeAttack;
import mineplex.minecraft.game.core.boss.ironwizard.abilities.GolemRupture;
import mineplex.minecraft.game.core.boss.ironwizard.abilities.GolemSlam;
import mineplex.minecraft.game.core.boss.ironwizard.abilities.GolemSpike;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

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

public class GolemCreature extends EventCreature<IronGolem>
{
	//private static final double HEALTH_PER_REGEN = 5;
	//private static final long HEALTH_REGEN_DELAY = 1000;
	//private static final long REGEN_SAFE_TIME_NEED = 10000;
	//private static final double CROWD_CONTROL_DAMAGE_REDUCTION = .70;
	//private final double MAX_HEALTH;
	private GolemBoss _boss;
	// private GolemAbility _currentAbility;
	private int _lastAbility;
	private long _lastWalked;
	private Location _standing;
	private long _spawnDelay = System.currentTimeMillis();
	private long _reverseWalk;
	private HashMap<Class, Class[]> _preferedCombos = new HashMap<Class, Class[]>();
	private Class _lastAttack;
	private boolean _usedFinalAttack;
	private ArrayList<BossAbility> _currentAbilities = new ArrayList<BossAbility>();
	private double _canDeadlyTremor = 225;
	private Vector _afkWalk = new Vector();
	private long _lastSlam;
	//private long _lastHit;
	//private long _lastRegenerate;

	public GolemCreature(GolemBoss boss, Location location, double maxHealth)
	{
		super(boss, location, "Iron Wizard", true, maxHealth, IronGolem.class);
		//MAX_HEALTH = maxHealth;
		//_lastRegenerate = System.currentTimeMillis() + 20000;
		_boss = boss;

		spawnEntity();

		_preferedCombos.put(GolemEarthquake.class, new Class[]
			{
					GolemBlockHail.class, GolemRupture.class
			});
		_preferedCombos.put(GolemMeleeAttack.class, new Class[]
			{
					GolemEarthquake.class, GolemRupture.class
			});
		_preferedCombos.put(GolemDeadlyTremor.class, new Class[]
			{
					GolemMeleeAttack.class
			});
		_preferedCombos.put(GolemBlockShot.class, new Class[]
			{
					GolemEarthquake.class
			});
		_preferedCombos.put(GolemRupture.class, new Class[]
			{
					GolemBlockShot.class
			});
		_preferedCombos.put(GolemBlockHail.class, new Class[]
			{
				GolemDeadlyTremor.class
			});
	}
	
	private boolean hasFurther(HashMap<Player, Double> distances, double range)
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
		// EntityInsentient creature = (EntityInsentient) ((CraftEntity) getEntity()).getHandle();

		// creature.Vegetated = false;

		_standing = getEntity().getLocation();
	}

	@EventHandler
	public void abilityTick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (!UtilTime.elapsed(_spawnDelay, 5000))
		{
			_standing = getEntity().getLocation();
			return;
		}
		
		/*if (UtilTime.elapsed(_lastHit, REGEN_SAFE_TIME_NEED) && UtilTime.elapsed(_lastRegenerate, HEALTH_REGEN_DELAY))
		{
			_lastRegenerate = System.currentTimeMillis();
			super.setHealth(Math.min(MAX_HEALTH, getHealth() + HEALTH_PER_REGEN));
			setHealth(getHealth() + HEALTH_PER_REGEN);
		}*/

		// if (_currentAbility == null || _currentAbility.hasFinished())
		// {
		Iterator<BossAbility> itel = _currentAbilities.iterator();
		boolean canDoNew = _currentAbilities.size() < 3;

		while (itel.hasNext())
		{
			BossAbility ability = itel.next();

			if (ability.hasFinished())
			{
				itel.remove();
				ability.setFinished();
				_lastAbility = 20;// _currentAbility.getCooldown();

				HandlerList.unregisterAll(ability);
				System.out.print("Unregistered golem ability " + ability.getClass().getSimpleName());
			}
			else if (!ability.inProgress())
			{
				canDoNew = false;
			}
		}

		if (_lastAbility-- <= 0 && canDoNew && UtilBlock.solid(getEntity().getLocation().getBlock().getRelative(BlockFace.DOWN)))
		{
			HashMap<Class, Integer> weight = new HashMap<Class, Integer>();
			HashMap<Player, Double> dist = new HashMap<Player, Double>();

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
					ArrayList<Player> players = getPlayers(dist, UtilMath.r(10) == 0 ? 4 : 3);

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
					ArrayList<Player> players = getPlayers(dist, 10);

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
					ArrayList<Player> players = getPlayers(dist, 30);

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
					ArrayList<Player> players = getPlayers(dist, 30);

					if (!players.isEmpty())
					{
						weight.put(GolemRupture.class, (int) Math.min(5, dist.get(players.get(0))));
					}
				}

				{ // Slam
					ArrayList<Player> players = getPlayers(dist, 30);

					if (!players.isEmpty() && UtilTime.elapsed(_lastSlam, 20000))
					{
						weight.put(GolemSlam.class, 6);
					}
				}

				if (_canDeadlyTremor <= 0) // Deadly Tremor
				{
					ArrayList<Player> players = getPlayers(dist, 80);

					for (BossAbility ability : _currentAbilities)
					{
						if (ability instanceof GolemExplodingAura)
						{
							players.clear();
						}
					}

					if (!players.isEmpty())
					{
						// weight.put(GolemCaveIn.class, (int) Math.min(players.size() * 2, 7));
						weight.put(GolemDeadlyTremor.class, (int) 30);
					}
				}

				{// Block Hail
					ArrayList<Player> players = getPlayers(dist, 30);

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

				if (_lastAttack != null && _preferedCombos.containsKey(_lastAttack))
				{
					weight.remove(_lastAttack);

					for (Class c : _preferedCombos.get(_lastAttack))
					{
						if (weight.containsKey(c))
						{
							weight.put(c, weight.get(c) * 4);
						}
					}
				}

				for (BossAbility ability : _currentAbilities)
				{
					weight.remove(ability.getClass());
				}

				BossAbility ability = null;

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

						for (Entry<Class, Integer> entry : weight.entrySet())
						{
							luckyNumber -= entry.getValue();

							if (luckyNumber <= 0)
							{
								try
								{
									ability = (BossAbility) entry.getKey().getConstructor(GolemCreature.class).newInstance(this);

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
					_lastAttack = ability.getClass();

					if (ability instanceof GolemDeadlyTremor)
					{
						_canDeadlyTremor = 225;
					}
					else if (ability instanceof GolemSlam)
					{
						_lastSlam = System.currentTimeMillis();
					}

					Bukkit.getPluginManager().registerEvents(ability, _boss.getPlugin());

					// Bukkit.broadcastMessage("Prepare fair maidens for " + ability.getClass().getSimpleName() + "!");

					System.out.print("Golem boss is using " + ability.getClass().getSimpleName());

					_currentAbilities.add(ability);
				}

				_lastAbility = 10;
			}

			_lastAttack = null;
		}

		boolean canMove = true;

		for (BossAbility ability : _currentAbilities)
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
				// if (vec.length() > 1)
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

	private ArrayList<Player> getPlayers(final HashMap<Player, Double> map, double maxDist)
	{
		ArrayList<Player> list = new ArrayList<Player>();

		for (Player p : map.keySet())
		{
			if (map.get(p) <= maxDist)
			{
				list.add(p);
			}
		}

		Collections.sort(list, new Comparator<Player>()
		{

			@Override
			public int compare(Player o1, Player o2)
			{
				return Double.compare(map.get(o2), map.get(o1));
			}
		});

		return list;
	}

	@EventHandler
	public void onGolemDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity().equals(getEntity()))
		{
			event.AddKnockback("Heavy Golem", 0.3);
			//_lastHit = System.currentTimeMillis();
			/*if (UtilPlayer.getInRadius(getEntity().getLocation(), 10).size() >= 3)
			{
				event.AddMult(getEntity().getName(), "Level Field", CROWD_CONTROL_DAMAGE_REDUCTION, false);
			}*/
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
		for (BossAbility ability : _currentAbilities)
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
