package nautilus.game.arcade.kit.perks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.DyeColor;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftWolf;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.SpigotUtil;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.events.PlayerStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.GameTeam.PlayerState;
import nautilus.game.arcade.kit.Perk;

public class PerkWolfPet extends Perk
{
	private HashMap<Player, ArrayList<Wolf>> _petMap = new HashMap<>();

	private HashMap<Wolf, Long> _tackle = new HashMap<>();

	private int _spawnRate;
	private int _max;
	private boolean _baby;
	private boolean _name;

	public PerkWolfPet(int spawnRate, int max, boolean baby, boolean name)
	{
		super("Wolf Master", new String[] 
				{
				C.cGray + "Spawn 1 Wolf every " + spawnRate + " seconds. Maximum of " + max + ".",
				C.cYellow + "Right-Click" + C.cGray + " with Sword/Axe to use " + C.cGreen + "Cub Strike",
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
	}

	@EventHandler
	public void cubSpawn(UpdateEvent event)
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

			if (!_petMap.containsKey(cur))
				_petMap.put(cur, new ArrayList<>());

			if (_petMap.get(cur).size() >= _max)
				continue;

			Manager.GetGame().CreatureAllowOverride = true;
			Wolf wolf = cur.getWorld().spawn(cur.getLocation(), Wolf.class);
			SpigotUtil.setOldOwner_RemoveMeWhenSpigotFixesThis(wolf, cur);
			wolf.setOwner(cur);
			wolf.playEffect(EntityEffect.WOLF_HEARTS);
			wolf.setAngry(true);
			wolf.setMaxHealth(14);
			wolf.setHealth(wolf.getMaxHealth());

			GameTeam team = Manager.GetGame().GetTeam(cur);

			if (team != null)
			{
				wolf.setCollarColor(team.getDyeColor());
			}
			else
			{
				wolf.setCollarColor(DyeColor.GREEN);
			}

			if (_baby)
				wolf.setBaby();

			if (_name)
			{
				wolf.setCustomName(cur.getName() + "'s Wolf");
			}
			_petMap.get(cur).add(wolf);
			Manager.GetGame().CreatureAllowOverride = false;
			cur.playSound(cur.getLocation(), Sound.WOLF_HOWL, 1f, 1f);
		}
	}

	@EventHandler
	public void cubTargetCancel(EntityTargetEvent event)
	{
		if (!(event.getTarget() instanceof Player) || !(event.getEntity() instanceof Wolf))
		{
			return;
		}

		if (_petMap.containsKey(event.getTarget()) && _petMap.get(event.getTarget()).contains(event.getEntity()))
		{
			event.setCancelled(true);
			return;
		}

		for (Player owner : _petMap.keySet())
		{
			for (Wolf wolf : _petMap.get(owner))
			{
				if (event.getEntity().equals(wolf))
				{
					if (!Manager.canHurt(owner, (Player) event.getTarget()))
					{
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}

	// Note: Why are there 4 different update event handlers?
	@EventHandler
	public void cubUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player player : _petMap.keySet())
		{
			Iterator<Wolf> wolfIterator = _petMap.get(player).iterator();

			while (wolfIterator.hasNext())
			{
				Wolf wolf = wolfIterator.next();

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
					((CraftWolf) wolf).getHandle().setGoalTarget(null);
				}
									
				//Return to Owner
				double range = 0.5;
				if (wolf.getTarget() != null)
					range = 9;
				
				Location target = player.getLocation().add(player.getLocation().getDirection().multiply(3));
				target.setY(player.getLocation().getY());
				
				if (UtilMath.offset(wolf.getLocation(), target) > range)
				{
					float speed = 1.2f;
					if (player.isSprinting())
						speed = 1.6f;

					wolf.setTarget(null);
					wolf.setAngry(false);
					UtilEnt.CreatureMove(wolf, target, speed);
				}
				else
				{
					wolf.setAngry(true);
				}
			}
		}
	}

	@EventHandler
	public void cubStrikeTrigger(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		Player player = event.getPlayer();

		if (!UtilItem.isAxe(player.getItemInHand()) && !UtilItem.isSword(player.getItemInHand()))
		{
			return;
		}

		if (!Kit.HasKit(player))
			return;
		
		if (!_petMap.containsKey(player) || _petMap.get(player).isEmpty())
		{
			UtilPlayer.message(player, F.main("Game", "You have no Wolf Cubs."));
			return;
		}

		if (!Recharge.Instance.use(player, "Cub Strike", 8000, true, true))
			return;

		Wolf wolf = _petMap.get(player).get(UtilMath.r(_petMap.get(player).size()));
		
		UtilAction.velocity(wolf, player.getLocation().getDirection(), 1.4, false, 0, 0.2, 1.2, true);
		
		wolf.playEffect(EntityEffect.WOLF_SMOKE);
		
		player.getWorld().playSound(wolf.getLocation(), Sound.WOLF_BARK, 1f, 1.2f);

		//Record
		_tackle.put(wolf, System.currentTimeMillis());

		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill("Cub Strike") + "."));
	}

	@EventHandler
	public void cubStrikeEnd(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		//Collide
		Iterator<Wolf> wolfIterator = _tackle.keySet().iterator();

		while (wolfIterator.hasNext())
		{
			Wolf wolf = wolfIterator.next();

			for (Player other : Manager.GetGame().GetPlayers(true))
				if (!Manager.isSpectator(other))
					if (UtilEnt.hitBox(wolf.getLocation(), other, 2, null))
					{
						if (other.equals(wolf.getOwner()))
							continue;
						
						cubStrikeHit((Player)wolf.getOwner(), other, wolf);
						wolfIterator.remove();
						return;
					}
			
			if (!UtilEnt.isGrounded(wolf))
				continue;

			if (!UtilTime.elapsed(_tackle.get(wolf), 1000))  
				continue;

			wolfIterator.remove();	
		}	
	}
	
	public void cubStrikeHit(Player damager, LivingEntity damagee, Wolf wolf)
	{
		//Damage Event
		((CraftWolf)wolf).getHandle().setGoalTarget(((CraftLivingEntity)damagee).getHandle(), EntityTargetEvent.TargetReason.CUSTOM, false);

		//Sound
		damagee.getWorld().playSound(damagee.getLocation(), Sound.WOLF_BARK, 1.5f, 1.5f);

		//Slow
		Manager.GetCondition().Factory().Slow(GetName(), damagee, damager, 4, 1, false, false, true, false);

		//Inform
		UtilPlayer.message(damager, F.main("Game", "You hit " + F.name(UtilEnt.getName(damagee)) + " with " + F.skill("Wolf Tackle") + "."));
		UtilPlayer.message(damagee, F.main("Game", F.name(damager.getName()) + " hit you with " + F.skill("Wolf Tackle") + "."));
	}
	
	@EventHandler
	public void cubHeal(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (ArrayList<Wolf> wolves : _petMap.values())
		{
			for (Wolf wolf : wolves)
			{
				if (wolf.getHealth() > 0)
					wolf.setHealth(Math.min(wolf.getMaxHealth(), wolf.getHealth()+1));
			}
		}
	}
	
	@EventHandler
	public void outOfGame(PlayerStateChangeEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
		
		if (event.GetState() == PlayerState.OUT)
		{
			despawnWolf(event.GetPlayer());
		}
	}
	
	@EventHandler
	public void PlayerDeath(PlayerDeathEvent event)
	{
		despawnWolf(event.getEntity());
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		despawnWolf(event.getPlayer());
	}
	
	private void despawnWolf(Player player)
	{
		ArrayList<Wolf> wolves = _petMap.remove(player);
		
		if (wolves == null)
			return;
		
		for (Wolf wolf : wolves)
			wolf.remove();
		
		wolves.clear();
	}
	
	public boolean isMinion(Entity ent)
	{
		for (ArrayList<Wolf> minions : _petMap.values())
		{
			for (Wolf minion : minions)
			{
				if (ent.equals(minion))
				{
					return true;
				}
			}
		}

		return false;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		LivingEntity damagee = event.GetDamageeEntity();
		LivingEntity damager = event.GetDamagerEntity(true);

		if (isMinion(damagee) && event.GetCause() == DamageCause.FALL)
		{
			event.SetCancelled("Wolf Fall Damage");
		}
		else if (damager != null && isMinion(damager))
		{
			if (event.GetDamageePlayer() != null)
			{
				event.GetDamageePlayer().playSound(event.GetDamageeEntity().getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
			}

			event.AddMod("Wolf Minion", "Damage", -event.GetDamageInitial() + 3, false);
		}
	}
}
