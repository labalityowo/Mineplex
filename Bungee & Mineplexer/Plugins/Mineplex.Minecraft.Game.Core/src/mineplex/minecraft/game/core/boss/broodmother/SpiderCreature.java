package mineplex.minecraft.game.core.boss.broodmother;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

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
import mineplex.minecraft.game.core.boss.broodmother.attacks.SpiderCeilingCling;
import mineplex.minecraft.game.core.boss.broodmother.attacks.SpiderEggScatter;
import mineplex.minecraft.game.core.boss.broodmother.attacks.SpiderEggplosm;
import mineplex.minecraft.game.core.boss.broodmother.attacks.SpiderPoisonBarrage;
import mineplex.minecraft.game.core.boss.broodmother.attacks.SpiderWebBarrage;
import mineplex.minecraft.game.core.boss.broodmother.attacks.SpiderWebStomp;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class SpiderCreature extends EventCreature<Spider>
{
	private ArrayList<BossAbility> _currentAbilities = new ArrayList<BossAbility>();
	private int _lastAbility;
	private long _lastWalked;
	private long _reverseWalk;
	private Location _standing;
	private Vector _afkWalk = new Vector();
	private HashMap<Class, Long> _cooldowns = new HashMap<Class, Long>();
	private long _lastAttack;

	public SpiderCreature(SpiderBoss boss, Location location, double maxHealth)
	{
		super(boss, location, "Brood Mother", true, maxHealth, Spider.class);

		spawnEntity();
	}

	@Override
	protected void spawnCustom()
	{
		UtilEnt.vegetate(getEntity());
	}

	@Override
	public void dieCustom()
	{
		/*for (Block block : Webs)
		{
			block.setType(Material.AIR);
		}*/
	}

	@EventHandler
	public void onTick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
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
				System.out.print("Unregistered spider ability " + ability.getClass().getSimpleName());

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
				{// Eggsplosm
					ArrayList<Player> players = getPlayers(dist, 30);

					if (getEvent().getCreatures().size() < 5 && !players.isEmpty())
					{
						weight.put(SpiderEggplosm.class, 4);
					}
				}

				{ // Ceiling Cling
					if (getEvent().getCreatures().size() < 5)
					{
						weight.put(SpiderCeilingCling.class, 2);
					}
				}

				{ // Poison barrage
					if (!getPlayers(dist, 10, 30).isEmpty())
					{
						weight.put(SpiderPoisonBarrage.class, 4);
					}
				}

				{// Spider egg scatter
					if (getEvent().getCreatures().size() < 5 && !getPlayers(dist, 20, 40).isEmpty())
					{
						weight.put(SpiderEggScatter.class, 4);
					}
				}

				{ // Spider web barrage
					if (!getPlayers(dist, 30).isEmpty())
					{
						weight.put(SpiderWebBarrage.class, 4);
					}
				}

				{ // Spider web stomp
					if (!getPlayers(dist, 20).isEmpty())
					{
						weight.put(SpiderWebStomp.class, 4);
					}
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

					for (Entry<Class, Integer> entry : weight.entrySet())
					{
						luckyNumber -= entry.getValue();

						if (luckyNumber <= 0)
						{
							try
							{
								ability = (BossAbility) entry.getKey().getConstructor(SpiderCreature.class).newInstance(this);

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

				System.out.print("Spider boss is using " + ability.getClass().getSimpleName());

				_currentAbilities.add(ability);
			}

			_lastAbility = 10;
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
				e.printStackTrace();
			}

			if (!ability.canMove())
			{
				canMove = false;
			}
		}

		if (canMove && System.currentTimeMillis() % 20000 > 2000)
		{
			for (double x = -.5; x <= .5; x += 1)
			{
				for (double y = 0; y <= 1; y += 1)
				{
					for (double z = -.5; z <= .5; z += 1)
					{
						Block block = getEntity().getLocation().add(x, y, z).getBlock();

						if (block.getType() == Material.WEB)
						{
							block.setType(Material.AIR);
						}
					}
				}
			}

			if (UtilBlock.solid(getEntity().getLocation().getBlock().getRelative(BlockFace.DOWN))
					|| getEntity().getVelocity().length() > 0.1)
			{

				Player target = null;
				double dist = 0;
				boolean canAttack = UtilTime.elapsed(_lastAttack, 1300);

				for (Player player : UtilPlayer.getNearby(getEntity().getLocation(), 50, true))
				{
					if (!player.hasLineOfSight(getEntity()))
					{
						continue;
					}

					double d = player.getLocation().distance(getEntity().getLocation());

					if ((canAttack || d > 1.5) && (d < 7 || d > 15) && (target == null || (d < 50 && dist > d)))
					{
						target = player;
						dist = d;
					}
				}

				if (target != null && dist < 1.5 && canAttack)
				{
					// UtilEnt.CreatureMoveFast(getEntity(), target.getLocation(), 1F);

					_lastAttack = System.currentTimeMillis();
					Vector vec = UtilAlg.getTrajectory2d(getEntity(), target);

					vec.multiply(0.5).setY(0.4);

					getEntity().setVelocity(vec);

					getEvent().getCondition().Factory()
							.Confuse("Brood Mother Bite", target, getEntity(), 4, 0, false, true, true);
					getEvent().getCondition().Factory()
							.Blind("Brood Mother Bite", target, getEntity(), 1.5, 0, false, true, true);
					getEvent().getCondition().Factory()
							.Slow("Brood Mother Bite", target, getEntity(), 4, 1, false, true, true, true);

					getEvent().getDamageManager().NewDamageEvent(target, getEntity(), null, DamageCause.ENTITY_ATTACK,
							2 * getDifficulty(), true, false, false, "Brood Mother Attack", "Brood Mother Attack");
					return;
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
						UtilEnt.CreatureMoveFast(getEntity(), getEntity().getLocation().add(vec), (target != null ? 1.8F : 1.1F)
								+ (superWalk ? 0.4F : 0));
					}
				}
			}

			_standing = getEntity().getLocation();
		}
		else if (_standing != null)
		{
			Location l = getEntity().getLocation();

			_standing.setYaw(l.getYaw());
			_standing.setPitch(l.getPitch());
			_standing.setY(l.getY());

			getEntity().teleport(_standing);
		}
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
	public void noFallDamage(CustomDamageEvent event)
	{
		if (getEntity() == null)
			return;

		if (!event.GetDamageeEntity().equals(getEntity()))
			return;

		DamageCause cause = event.GetCause();

		if (cause == DamageCause.FALL)
			event.SetCancelled("Cancel");
	}
}