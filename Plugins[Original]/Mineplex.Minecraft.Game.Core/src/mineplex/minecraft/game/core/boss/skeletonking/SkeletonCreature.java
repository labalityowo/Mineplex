package mineplex.minecraft.game.core.boss.skeletonking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
import mineplex.minecraft.game.core.boss.skeletonking.abilities.SkeletonArcherShield;
import mineplex.minecraft.game.core.boss.skeletonking.abilities.SkeletonPassive;
import mineplex.minecraft.game.core.boss.skeletonking.abilities.SkeletonPulse;
import mineplex.minecraft.game.core.boss.skeletonking.abilities.SkeletonSmite;
import mineplex.minecraft.game.core.boss.skeletonking.abilities.SkeletonStrike;
import mineplex.minecraft.game.core.boss.skeletonking.abilities.SkeletonWraithSummon;
import mineplex.minecraft.game.core.boss.skeletonking.minion.UndeadArcherCreature;
import mineplex.minecraft.game.core.boss.skeletonking.minion.UndeadWarriorCreature;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

public class SkeletonCreature extends EventCreature<Skeleton>
{
	private ArrayList<BossAbility> _currentAbilities = Lists.newArrayList();
	private SkeletonPassive _passive;
	private int _lastAbility;
	private HashMap<Class, Long> _cooldowns = new HashMap<>();
	private LinkedList<Double> _wraithTriggers = new LinkedList<>();
	private List<Location> _movePoints = Lists.newArrayList();
	private Location _movingTo;
	private boolean _moving;
	private long _lastMoved;
	private long _lastUsedPassive;
	public List<UndeadArcherCreature> Archers = Lists.newArrayList();
	public List<UndeadWarriorCreature> Warriors = Lists.newArrayList();

	public SkeletonCreature(SkeletonBoss boss, Location location, double maxHealth)
	{
		super(boss, location, "Skeleton King", true, maxHealth, Skeleton.class);

		spawnEntity();
		_passive = new SkeletonPassive(this);
		_wraithTriggers.add(1500D);
		_wraithTriggers.add(1000D);
		_wraithTriggers.add(500D);
		_wraithTriggers.add(100D);
		getEntity().getWorld().setThunderDuration(10000000);
		getEntity().getWorld().setThundering(true);
	}

	@Override
	protected void spawnCustom()
	{
		UtilEnt.vegetate(getEntity());
		getEntity().setSkeletonType(SkeletonType.WITHER);
		getEntity().getEquipment().setItemInHand(new ItemStack(Material.RECORD_6)); //Meridian Scepter
		getEntity().getEquipment().setItemInHandDropChance(0.f);
	}

	@Override
	public void dieCustom()
	{
		HandlerList.unregisterAll(_passive);
		_passive = null;
		endAbility();
		getEntity().getWorld().setThunderDuration(0);
		getEntity().getWorld().setThundering(false);
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

	@EventHandler
	public void onTick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		if (_passive != null && ((SkeletonBoss)getEvent()).canMove)
		{
			if (UtilTime.elapsed(_lastUsedPassive, _passive.getCooldown() * 20) || _passive.isProgressing())
			{
				_lastUsedPassive = System.currentTimeMillis();
				_passive.tick();
			}
		}

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
				System.out.print("Unregistered necromancer ability " + ability.getClass().getSimpleName());

				_cooldowns.put(ability.getClass(), System.currentTimeMillis() + (ability.getCooldown() * 1000));
			}
			else if (ability.inProgress())
			{
				canDoNew = false;
				_lastAbility = 20;// _currentAbility.getCooldown();
			}
		}

		if (_lastAbility-- <= 0 && canDoNew && UtilBlock.solid(getEntity().getLocation().getBlock().getRelative(BlockFace.DOWN)))
		{
			HashMap<Class<? extends BossAbility>, Integer> weight = new HashMap<>();
			HashMap<Player, Double> dist = new HashMap<>();

			for (Player player : UtilPlayer.getNearby(getEntity().getLocation(), 50, true))
			{
				if (player.hasLineOfSight(getEntity()))
				{
					dist.put(player, player.getLocation().distance(getEntity().getLocation()));
				}
			}

			if (!dist.isEmpty())
			{
				{// Pulse & Strike
					ArrayList<Player> players = getPlayers(dist, UtilMath.r(10) == 0 ? 25 : 20);
					ArrayList<Player> near = getPlayers(dist, 5);
					
					if (!players.isEmpty())
					{
						if (!near.isEmpty() && near.size() >= 4 && new Random().nextDouble() <= .45)
						{
							weight.put(SkeletonPulse.class, 999);
						}
						else
						{
							weight.put(SkeletonStrike.class, 6);
						}
					}
				}
				{// Smite
					ArrayList<Player> players = getPlayers(dist, 15);

					if (!players.isEmpty())
					{
						weight.put(SkeletonSmite.class, 6);
					}
				}
				{//Archer Shield
					ArrayList<Player> players = getPlayers(dist, 20);
					double score = 0;
					for (Player player : players)
					{
						score += (18 - dist.get(player)) / 2;
					}
					if (players.size() >= 4)
					{
						score += 17;
					}
					if (score > 0)
					{
						weight.put(SkeletonArcherShield.class, (int) Math.ceil(score));
					}
				}
				Double wraithUse = null;
				for (Double test : _wraithTriggers)
				{
					if (wraithUse == null)
					{
						if (getHealth() <= test)
						{
							wraithUse = test;
							break;
						}
					}
				}
				if (wraithUse != null)
				{// Wraith Summon
					_wraithTriggers.remove(wraithUse);
					weight.clear();
					weight.put(SkeletonWraithSummon.class, 999);
				}
			}

			for (BossAbility ability : _currentAbilities)
			{
				weight.remove(ability.getClass());
			}

			for (Class c : _cooldowns.keySet())
			{
				if (_cooldowns.get(c) > System.currentTimeMillis())
				{
					weight.remove(c);
				}
			}

			if (_moving)
			{
				Iterator<Class<? extends BossAbility>> trying = weight.keySet().iterator();
				while (trying.hasNext())
				{
					Class<? extends BossAbility> abilityClass = trying.next();

					try
					{
						BossAbility ability = abilityClass.newInstance();
						if (!ability.canMove())
						{
							trying.remove();
						}
					}
					catch (Exception e) {}
				}
			}

			BossAbility ability = null;

			if (!weight.isEmpty())
			{
				int i = 0;

				for (Integer entry : weight.values())
				{
					i += entry;
				}

				loop: for (int a = 0; a < 10; a++)
				{
					int luckyNumber = UtilMath.r(i);

					for (Entry<Class<? extends BossAbility>, Integer> entry : weight.entrySet())
					{
						luckyNumber -= entry.getValue();

						if (luckyNumber <= 0)
						{
							try
							{
								ability = entry.getKey().getConstructor(SkeletonCreature.class).newInstance(this);

								if (ability.getTarget() == null || ability.hasFinished())
								{
									ability = null;
								}
								else
								{
									break loop;
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

				Bukkit.getPluginManager().registerEvents(ability, getEvent().getPlugin());

				System.out.print("Necromancer is using " + ability.getClass().getSimpleName());

				_currentAbilities.add(ability);
			}

			_lastAbility = 10;
		}
		
		for (BossAbility ability : _currentAbilities)
		{
			try
			{
				ability.tick();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		boolean canMove = true;
		for (BossAbility ability : _currentAbilities)
		{
			if (!ability.canMove())
			{
				canMove = false;
			}
		}
		if (!((SkeletonBoss)getEvent()).canMove)
		{
			return;
		}
		
		if (_moving)
		{
			if (_movingTo == null)
			{
				_movingTo = selectWalkTarget();
			}
			if (UtilMath.offset(getEntity().getLocation(), _movingTo) <= 1.3)
			{
				_lastMoved = System.currentTimeMillis();
				_movingTo = null;
				_moving = false;
				return;
			}
			UtilEnt.LookAt(getEntity(), _movingTo);
			Vector walk = UtilAlg.getTrajectory(getEntity().getLocation(), _movingTo);
			walk.multiply(walk.length());
			walk.multiply(.2);
			getEntity().setVelocity(walk);
		}
		else
		{
			if (!UtilTime.elapsed(_lastMoved, 7000) || !canMove)
			{
				return;
			}
			_movingTo = selectWalkTarget();
			_moving = true;
		}
	}
	
	private Location selectWalkTarget()
	{
		if (_movePoints.isEmpty())
		{
			Location base = getSpawnLocation().clone();
			base.setY(getEntity().getLocation().getY());
			generateWalkPoints(base);
		}
		Location selected = _movePoints.get(new Random().nextInt(_movePoints.size()));
		_movePoints.remove(selected);
		
		return selected;
	}
	
	private void generateWalkPoints(Location base)
	{
		_movePoints.add(base.clone().add(5 + UtilMath.random(1, 3), 0, 5 + UtilMath.random(1, 3)));
		_movePoints.add(base.clone().add(-5 + UtilMath.random(1, 3), 0, 5 + UtilMath.random(1, 3)));
		_movePoints.add(base.clone().add(5 + UtilMath.random(1, 3), 0, -5 + UtilMath.random(1, 3)));
		_movePoints.add(base.clone().add(-5 + UtilMath.random(1, 3), 0, -5 + UtilMath.random(1, 3)));
	}

	private ArrayList<Player> getPlayers(HashMap<Player, Double> map, double maxDist)
	{
		return getPlayers(map, 0, maxDist);
	}

	private ArrayList<Player> getPlayers(final HashMap<Player, Double> map, double minDist, double maxDist)
	{
		ArrayList<Player> list = new ArrayList<Player>();

		for (Player p : map.keySet())
		{
			if (map.get(p) >= minDist && map.get(p) <= maxDist)
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
	public void onSkeletonDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity().getEntityId() == getEntity().getEntityId())
		{
			event.SetKnockback(false);
		}
	}

	@EventHandler
	public void noFallDamage(CustomDamageEvent event)
	{
		if (getEntity() == null)
			return;

		if (event.GetDamageeEntity().getEntityId() != getEntity().getEntityId())
			return;

		DamageCause cause = event.GetCause();

		if (cause == DamageCause.FALL)
		{
			event.SetCancelled("Boss Invulnerability");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void protect(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}
		
		LivingEntity damagee = event.GetDamageeEntity();
		LivingEntity damager = event.GetDamagerEntity(event.GetCause() == DamageCause.PROJECTILE);
		
		if (damagee == null)
		{
			return;
		}
		
		if (damager == null)
		{
			return;
		}
		
		if (getEntity().equals(damagee))
		{
			if (!(damager instanceof Player))
			{
				event.SetCancelled("Allied Attacker");
			}
		}
	}
}