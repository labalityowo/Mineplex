package nautilus.game.arcade.kit.perks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftWolf;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;
import net.minecraft.server.v1_8_R3.EntityCreature;
import net.minecraft.server.v1_8_R3.Navigation;
import net.minecraft.server.v1_8_R3.NavigationAbstract;

public class PerkWolfPack extends Perk
{
	private HashMap<Player, ArrayList<Wolf>> _wolfMap = new HashMap<Player, ArrayList<Wolf>>();

	private HashMap<Wolf, Long> _strike = new HashMap<Wolf, Long>();
	private HashMap<Player, Long> _tackle = new HashMap<Player, Long>();
	
	private HashMap<Wolf, Long> _useDelay = new HashMap<Wolf, Long>();

	private int _spawnRate;
	private int _max;
	private boolean _baby;
	private boolean _name;

	public PerkWolfPack(int spawnRate, int max, boolean baby, boolean name) 
	{
		super("Wolf Master", new String[] 
				{
				C.cYellow + "Tap Jump Twice" + C.cGray + " to " + C.cGreen + "Double Jump",
				C.cGray + "Spawn 1 Wolf Cub every " + spawnRate + " seconds. Maximum of " + max + ".",
				C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Cub Strike",
				C.cYellow + "Right-Click" + C.cGray + " with Spade to use " + C.cGreen + "Pack Leap",
				C.cYellow + "Crouch" + C.cGray + " to use " + C.cGreen + "Cub Return",
				});

		_spawnRate = spawnRate;
		_max = max;
		_baby = baby;
		_name = name;
	}

	@Override
	public void Apply(Player player) 
	{
		Recharge.Instance.use(player, GetName(), _spawnRate*1000, false, false);
		
		if (_wolfMap.containsKey(player))
		{
			for (Wolf wolf : _wolfMap.get(player))
				wolf.remove();
			
			_wolfMap.get(player).clear();
			
			_wolfMap.remove(player);
		}
	}

	@EventHandler
	public void DoubleJump(PlayerToggleFlightEvent event)
	{
		final Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (Manager.isSpectator(player))
			return;

		event.setCancelled(true);
		player.setFlying(false);

		//Disable Flight
		player.setAllowFlight(false);

		//Velocity
		UtilAction.velocity(player, player.getLocation().getDirection(), 1, true, 1, 0, 1, true);

		//Wolves Velocity
		if (_wolfMap.containsKey(player))
		{
			for (final Wolf wolf : _wolfMap.get(player))
			{
				Manager.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
				{
					public void run()
					{
						//Trajectory to ahead of player (try to land on same land)
						Vector velocity = UtilAlg.getTrajectory(player.getLocation(), 
								player.getLocation().add(player.getLocation().getDirection().setY(0).multiply(6).add(new Vector(0,6,0))));
						
						//Power Adjust
						double power = 1.2;
						if (player.isSprinting())	
							power = 1.6;
						
						//Vel
						UtilAction.velocity(wolf, velocity, power, true, 1, 0, 1, true);
						
						//Sound
						wolf.getWorld().playEffect(wolf.getLocation(), Effect.BLAZE_SHOOT, 0);
					}
				}, UtilMath.r(10));
			}
		}

		//Sound
		player.playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 0);
	}

	@EventHandler
	public void DoubleJumpUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (Manager.isSpectator(player))
				continue;

			if (!Kit.HasKit(player))
				continue;

			if (UtilEnt.isGrounded(player) || UtilBlock.solid(player.getLocation().getBlock().getRelative(BlockFace.DOWN)))
				player.setAllowFlight(true);
		}
	}

	@EventHandler
	public void MinionSpawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player cur : UtilServer.getPlayers())
		{
			if (!Kit.HasKit(cur))
				continue;

			if (!Manager.GetGame().IsAlive(cur))
				continue;

			if (!Recharge.Instance.use(cur, GetName(), _spawnRate*1000, false, false))
				continue;

			if (!_wolfMap.containsKey(cur))
			{
				_wolfMap.put(cur, new ArrayList<Wolf>());
				
				while (_wolfMap.get(cur).size() < _max)
					MinionSpawn(cur);
				
				continue;
			}
				
			if (_wolfMap.get(cur).size() >= _max)
				continue;

			MinionSpawn(cur);
		}
	}
	
	public void MinionSpawn(Player cur)
	{
		Manager.GetGame().CreatureAllowOverride = true;
		Wolf wolf = cur.getWorld().spawn(cur.getLocation(), Wolf.class);
		Manager.GetGame().CreatureAllowOverride = false;

		//wolf.setOwner(cur);
		//wolf.setCollarColor(DyeColor.GREEN);
		wolf.playEffect(EntityEffect.WOLF_HEARTS);

		wolf.setMaxHealth(30);
		wolf.setHealth(wolf.getMaxHealth());

		if (_baby)
			wolf.setBaby();

		if (_name)
		{
			wolf.setCustomName(cur.getName() + "'s Wolf");
			wolf.setCustomNameVisible(true);
		}

		_wolfMap.get(cur).add(wolf);
	}

	@EventHandler
	public void MinionTargetCancel(EntityTargetEvent event)
	{
		if (!_wolfMap.containsKey(event.getTarget()))
			return;

		if (_wolfMap.get(event.getTarget()).contains(event.getEntity()))
			event.setCancelled(true);
	}

	@EventHandler
	public void MinionUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player player : _wolfMap.keySet())
		{
			Iterator<Wolf> wolfIterator = _wolfMap.get(player).iterator();

			while (wolfIterator.hasNext())
			{
				Wolf wolf = wolfIterator.next();

				if (!Manager.GetGame().IsAlive(player))
				{
					wolf.remove();
					wolfIterator.remove();
					continue;
				}
				
				//Dead
				if (!wolf.isValid())
				{
					wolf.getWorld().playSound(wolf.getLocation(), Sound.WOLF_DEATH, 1f, 1f);
					Recharge.Instance.useForce(player, GetName(), _spawnRate*1000);
					wolfIterator.remove();
					continue;
				}	

				if (player.isSneaking())
				{
					((CraftWolf)wolf).getHandle().setGoalTarget(null);
					wolf.setAngry(false);
				}

				//Return to Owner
				double range = 0.5;
				if (wolf.getTarget() != null)
					range = 12;

				Location target = player.getLocation().add(player.getLocation().getDirection().multiply(3));
				target.setY(player.getLocation().getY());

				if (UtilMath.offset(wolf.getLocation(), target) > range)
				{
					float speed = 1f;
					if (player.isSprinting())
						speed = 1.4f;

					//Leap
					if (UtilEnt.isGrounded(wolf) && UtilMath.offset(target, wolf.getLocation()) > 6 && !_useDelay.containsKey(wolf))
					{
						Vector vel = UtilAlg.getTrajectory(wolf, player);
						if (vel.getY() < 0.2)
							vel.setY(0.2);
						
						UtilAction.velocity(wolf, vel, 1.2, false, 1, 0.2, 1, true);		
						_useDelay.put(wolf, (long) (System.currentTimeMillis() + 500 + (500 * Math.random())));
					}
					
					//Shorten Target Location
					if (UtilMath.offset(target, wolf.getLocation()) > 16)
						target = wolf.getLocation().add(UtilAlg.getTrajectory(wolf.getLocation(), target));
					
					//Move
					EntityCreature ec = ((CraftCreature)wolf).getHandle();
					NavigationAbstract nav = ec.getNavigation();
					nav.a(target.getX(), target.getY(), target.getZ(), speed);

					wolf.setTarget(null);
				}
			}
			
			//Use Delay Clear
			wolfIterator = _useDelay.keySet().iterator();

			while (wolfIterator.hasNext())
			{
				Wolf wolf = wolfIterator.next();
				
				if (System.currentTimeMillis() > _useDelay.get(wolf))
				{
					wolfIterator.remove();
				}
			}
		}
	}

	@EventHandler
	public void CubStrikeTrigger(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (event.getPlayer().getItemInHand() == null)
			return;

		if (!event.getPlayer().getItemInHand().getType().toString().contains("_AXE"))
			return; 

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		//Get Nearest Wolf
		Wolf wolf = null;
		double best = 999;

		for (Wolf other : _wolfMap.get(player))
		{
			if (_useDelay.containsKey(other))
				continue;
			
			double dist = UtilMath.offset(other.getLocation(), player.getEyeLocation().add(player.getLocation().getDirection().setY(0).multiply(1)));

			if (dist > 4)
				continue;

			if (wolf == null || dist < best)
			{
				wolf = other;
				best = dist;
			}
		}

		if (wolf == null)
		{
			UtilPlayer.message(player, F.main("Game", "You have no nearby Wolf Cubs."));
			return;
		}

		UtilAction.velocity(wolf, player.getLocation().getDirection(), 1.6, false, 0, 0.2, 1.2, true);

		wolf.playEffect(EntityEffect.WOLF_SMOKE);

		player.getWorld().playSound(wolf.getLocation(), Sound.WOLF_BARK, 1f, 1.8f);

		//Record
		_strike.put(wolf, System.currentTimeMillis());
		_useDelay.put(wolf, System.currentTimeMillis() + 1000);

		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill("Cub Strike") + "."));
	}

	@EventHandler
	public void CubStrikeEnd(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		//Collide
		Iterator<Wolf> wolfIterator = _strike.keySet().iterator();

		while (wolfIterator.hasNext())
		{
			Wolf wolf = wolfIterator.next();

			for (Player other : Manager.GetGame().GetPlayers(true))
				if (Manager.isSpectator(other))
					if (UtilEnt.hitBox(wolf.getLocation(), other, 2, null))
					{
						if (other.equals(GetOwner(wolf)))
							continue;

						CubStrikeHit(GetOwner(wolf), other, wolf);
						wolfIterator.remove();
						return;
					}

			if (!UtilEnt.isGrounded(wolf))
				continue;

			if (!UtilTime.elapsed(_strike.get(wolf), 1000))  
				continue;

			wolfIterator.remove();	
		}	
	}
	
	

	public void CubStrikeHit(Player damager, LivingEntity damagee, Wolf wolf)
	{
		if (damager == null)
			return;
		
		//Damage
		Manager.GetDamage().NewDamageEvent(damagee, damager, null, 
				DamageCause.CUSTOM, 5, true, true, false,
				damager.getName(), "Cub Strike");	

		//Target
		((CraftWolf)wolf).getHandle().setGoalTarget(((CraftLivingEntity)damagee).getHandle());

		//Sound
		damagee.getWorld().playSound(damagee.getLocation(), Sound.WOLF_BARK, 1.5f, 2f);

		//Inform
		UtilPlayer.message(damager, F.main("Game", "You hit " + F.name(UtilEnt.getName(damagee)) + " with " + F.skill("Cub Strike") + "."));
		UtilPlayer.message(damagee, F.main("Game", F.name(damager.getName()) + " hit you with " + F.skill("Cub Strike") + "."));
	}

	@EventHandler
	public void CubHeal(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (ArrayList<Wolf> wolves : _wolfMap.values())
		{
			for (Wolf wolf : wolves)
			{
				if (wolf.getHealth() > 0)
					wolf.setHealth(Math.min(wolf.getMaxHealth(), wolf.getHealth()+1));
			}
		}
	}

	@EventHandler
	public void TackleTrigger(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (event.getPlayer().getItemInHand() == null)
			return;

		if (!event.getPlayer().getItemInHand().getType().toString().contains("_SPADE"))
			return; 

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (!Recharge.Instance.use(player, "Pack Leap", 4000, true, true))
			return;

		//Velocity
		UtilAction.velocity(player, player.getLocation().getDirection(), 1.6, false, 1, 0.2, 1.4, true);

		//Wolves Velocity
		if (_wolfMap.containsKey(player))
		{
			for (Wolf wolf : _wolfMap.get(player))
			{
				UtilAction.velocity(wolf, player.getLocation().getDirection(), 1.6, false, 1, 0.2, 1.4, true);		
			}
		}

		//Record
		_tackle.put(player, System.currentTimeMillis());

		player.getWorld().playSound(player.getLocation(), Sound.WOLF_BARK, 1f, 1.2f);

		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill("Tackle Leap") + "."));
	}

	@EventHandler
	public void TackleEnd(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		//Collide
		Iterator<Player> playerIterator = _tackle.keySet().iterator();

		while (playerIterator.hasNext())
		{
			Player player = playerIterator.next();

			for (Player other : Manager.GetGame().GetPlayers(true))
				if (!player.equals(other))
					if (!Manager.isSpectator(other))
						if (UtilEnt.hitBox(player.getLocation(), other, 2, null))
						{
							TackleHit(player, other);
							playerIterator.remove();
							return;
						}

			if (!UtilEnt.isGrounded(player))
				continue;

			if (!UtilTime.elapsed(_tackle.get(player), 1000))  
				continue;

			playerIterator.remove();	
		}	
	}

	public void TackleHit(Player damager, LivingEntity damagee)
	{
		UtilAction.zeroVelocity(damager);
		
		Manager.GetDamage().NewDamageEvent(damagee, damager, null, 
				DamageCause.CUSTOM, 7, false, true, false,
				damager.getName(), "Tackle Leap");	

		//Wolves Target
		if (_wolfMap.containsKey(damager))
		{
			for (Wolf wolf : _wolfMap.get(damager))
			{
				//Target
				((CraftWolf)wolf).getHandle().setGoalTarget(((CraftLivingEntity)damagee).getHandle());
			}
		}

		//Sound
		damagee.getWorld().playSound(damagee.getLocation(), Sound.WOLF_BARK, 1.5f, 1.5f);

		//Slow
		Manager.GetCondition().Factory().Slow(GetName(), damagee, damager, 6, 2, false, false, true, false);

		//Inform
		UtilPlayer.message(damager, F.main("Game", "You hit " + F.name(UtilEnt.getName(damagee)) + " with " + F.skill("Tackle Leap") + "."));
		UtilPlayer.message(damagee, F.main("Game", F.name(damager.getName()) + " hit you with " + F.skill("Tackle Leap") + "."));
	}

	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() != null && event.GetReason().contains("Cub Strike"))
		{
			event.AddKnockback(GetName(), 3);
		}
		
		if (event.GetDamagerEntity(false) != null && event.GetDamagerEntity(false) instanceof Wolf)
		{
			event.AddKnockback(GetName(), 3);
		}
	}
	
	public Player GetOwner(Wolf wolf)
	{
		for (Player player : _wolfMap.keySet())
		{
			if (_wolfMap.get(player).contains(wolf))
				return player;
		}
		
		return null;
	}
}
