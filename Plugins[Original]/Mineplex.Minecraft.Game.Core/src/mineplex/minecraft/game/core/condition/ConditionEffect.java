package mineplex.minecraft.game.core.condition;

import java.util.Iterator;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutRelEntityMove;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilTime;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.visibility.VisibilityManager;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class ConditionEffect implements Listener, IPacketHandler
{

	protected final ConditionManager Manager;

	public ConditionEffect(ConditionManager manager) 
	{
		Manager = manager;
		Manager.getPlugin().getServer().getPluginManager().registerEvents(this, Manager.getPlugin());
		Managers.require(PacketHandler.class).addPacketHandler(this,
				PacketPlayOutEntityLook.class,
				PacketPlayOutRelEntityMove.class,
				PacketPlayOutRelEntityMoveLook.class,
				PacketPlayOutEntityTeleport.class
		);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void Invulnerable(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		LivingEntity ent = event.GetDamageeEntity();
		if (ent == null)	return;

		if (!Manager.IsInvulnerable(ent))
			return;

		//Set Damage
		event.SetCancelled("Invulnerable");
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void Cloak(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		LivingEntity ent = event.GetDamageeEntity();
		if (ent == null)	return;

		if (!Manager.IsCloaked(ent) && !Manager.isUntrueCloaked(ent))
			return;

		//Set Damage
		event.SetCancelled("Cloak");
	}

	@EventHandler
	public void Cloak(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		VisibilityManager vm = Managers.require(VisibilityManager.class);

		for (LivingEntity ent : Manager.GetActiveConditions().keySet())
		{
			if (!(ent instanceof Player))
				continue;

			Player player = (Player)ent;

			//Hide
			if (Manager.IsCloaked(ent))
			{
				for (Player other : Bukkit.getOnlinePlayers())
				{
					vm.hidePlayer(other, player, "Cloaking");
				}
			}
			//Show
			else
			{
				for (Player other : Bukkit.getOnlinePlayers())
				{
					vm.showPlayer(other, player, "Cloaking");
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void Cloak(EntityTargetEvent event)
	{
		if (!(event.getTarget() instanceof Player))
			return;
		
		if (!Manager.HasCondition((LivingEntity)event.getTarget(), ConditionType.CLOAK, null))
			return;
		
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Protection(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		Player damagee = event.GetDamageePlayer();
		if (damagee == null)	return;

		if (!damagee.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE))
			return;

		mineplex.minecraft.game.core.condition.Condition cond = Manager.GetActiveCondition(damagee, ConditionType.DAMAGE_RESISTANCE);
		if (cond == null)		return;

		event.AddMod(UtilEnt.getName(cond.GetSource()), cond.GetReason(), -1 * (cond.GetMult()+1), false);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void VulnerabilityDamagee(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		Player damagee = event.GetDamageePlayer();
		if (damagee == null)	return;

		if (!damagee.hasPotionEffect(PotionEffectType.WITHER))
			return;

		mineplex.minecraft.game.core.condition.Condition cond = Manager.GetActiveCondition(damagee, ConditionType.WITHER);
		if (cond == null)		return;

		event.AddMod(UtilEnt.getName(cond.GetSource()), cond.GetReason(), cond.GetMult()+1, false);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void VulnerabilityDamager(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;

		if (!damager.hasPotionEffect(PotionEffectType.WITHER))
			return;

		mineplex.minecraft.game.core.condition.Condition cond = Manager.GetActiveCondition(damager, ConditionType.WITHER);
		if (cond == null)		return;

		event.AddMod(UtilEnt.getName(cond.GetSource()), cond.GetReason(), -1 * (cond.GetMult()+1), false);
	}

	@EventHandler
	public void VulnerabilityEffect(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		for (LivingEntity ent : Manager.GetActiveConditions().keySet())
		{
			if (ent.isDead())
				continue;
			
			if (!ent.hasPotionEffect(PotionEffectType.WITHER))
				continue;
			
			if (Manager.HasCondition(ent, ConditionType.CLOAK, null))
				continue;
			
			ent.getWorld().playEffect(ent.getLocation(), Effect.SMOKE, 1);
			ent.getWorld().playEffect(ent.getLocation(), Effect.SMOKE, 3);
			ent.getWorld().playEffect(ent.getLocation(), Effect.SMOKE, 5);
			ent.getWorld().playEffect(ent.getLocation(), Effect.SMOKE, 7);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void VulnerabilityWitherCancel(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() == DamageCause.WITHER)
			event.SetCancelled("Vulnerability Wither");
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Strength(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		Player damager = event.GetDamagerPlayer(true);
		if (damager == null)	return;

		if (!damager.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE))
			return;

		mineplex.minecraft.game.core.condition.Condition cond = Manager.GetActiveCondition(damager, ConditionType.INCREASE_DAMAGE);
		if (cond == null)		return;

		event.AddMod(damager.getName(), cond.GetReason(), cond.GetMult() + 1, true);
	}

	@EventHandler
	public void Shock(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (LivingEntity ent : Manager.GetActiveConditions().keySet())
			for (ConditionActive ind : Manager.GetActiveConditions().get(ent))
				if (ind.GetCondition().GetType() == ConditionType.SHOCK)
					ent.playEffect(EntityEffect.HURT);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void Lightning(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.LIGHTNING)
			return;

		LivingEntity ent = event.GetDamageeEntity();
		if (ent == null)	return;

		Condition condition = Manager.GetActiveCondition(ent, ConditionType.LIGHTNING);
		if (condition == null)	return;
		
		if (event.GetDamageePlayer() != null)
		{
			if (!Recharge.Instance.use(event.GetDamageePlayer(), "Lightning by " + UtilEnt.getName(condition.GetSource()), 1000, false, false))
			{
				event.SetCancelled("Lightning Rate");
				return;
			}
		}

		//Damage
		event.SetDamager(condition.GetSource());
		event.AddMod(UtilEnt.getName(condition.GetSource()), condition.GetReason(), 0, true);

		if (condition.GetMult() != 0)
			event.AddMod("Lightning Modifier", UtilEnt.getName(condition.GetSource()), condition.GetMult(), false);

		event.SetKnockback(false);
	}

	@EventHandler
	public void Explosion(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.ENTITY_EXPLOSION && event.GetCause() != DamageCause.BLOCK_EXPLOSION)
			return;

		LivingEntity ent = event.GetDamageeEntity();
		if (ent == null)	return;

		Condition condition = Manager.GetActiveCondition(ent, ConditionType.EXPLOSION);
		if (condition == null)	return;

		//Damage
		event.SetDamager(condition.GetSource());

		event.AddMod("Negate", condition.GetReason(), -event.GetDamageInitial(), false);
		event.AddMod(UtilEnt.getName(condition.GetSource()), condition.GetReason(), Math.min(event.GetDamageInitial(), condition.GetMult()), true);

		event.SetKnockback(false);
	}

	@EventHandler
	public void Fire(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.FIRE_TICK)
			return;

		LivingEntity ent = event.GetDamageeEntity();
		if (ent == null)	return;

		//Limit
		if (ent.getFireTicks() > 160)
			ent.setFireTicks(160);

		Condition condition = Manager.GetActiveCondition(ent, ConditionType.BURNING);
		if (condition == null)	return;

		//Damage
		event.SetDamager(condition.GetSource());
		event.AddMod(UtilEnt.getName(condition.GetSource()), condition.GetReason(), 0, true);
		event.SetIgnoreArmor(true);
		event.SetKnockback(false);
	}

	@EventHandler
	public void FireDouse(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		for (LivingEntity ent : Manager.GetActiveConditions().keySet())
			if (ent.getFireTicks() <= 0)
				Manager.EndCondition(ent, ConditionType.BURNING, null);
	}

	@EventHandler
	public void Poison(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.POISON)
			return;

		LivingEntity ent = event.GetDamageeEntity();
		if (ent == null)	return;
		
		//Ignore Poison Shock
		Condition poisonShock = Manager.GetActiveCondition(ent, ConditionType.POISON_SHOCK);
		if (poisonShock != null)	
		{
			event.SetCancelled("Poison Shock - Poison Cancel");
			return;
		}
		
		Condition condition = Manager.GetActiveCondition(ent, ConditionType.POISON);
		if (condition == null)	
			return;		
		//Damage
		event.SetDamager(condition.GetSource());
		event.AddMod(UtilEnt.getName(condition.GetSource()), condition.GetReason(), 0, true);
		event.SetIgnoreArmor(true);
		event.SetKnockback(false);
	}
	
	@EventHandler
	public void PoisonShock(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<LivingEntity> ents = Manager.GetActiveConditions().keySet().iterator();
		while (ents.hasNext())
		{
			LivingEntity ent = ents.next();
			Condition condition = Manager.GetActiveCondition(ent, ConditionType.POISON_SHOCK);
			if (condition == null || condition.GetSource() == null)	
				continue;
			
			try
			{
				Manager.getDamagerManager().NewDamageEvent(ent, condition.GetSource(), null, 
						DamageCause.CUSTOM, 0.1, false, true, false,
						condition.GetSource() != null ? condition.GetSource().getName() : "The Mighty Defek7", "Poison");
			}
			catch (Exception exception)
			{
				System.out.println("__+Poison error+__");
				System.out.println("Manager null? : " + Manager == null);
				System.out.println("Manager.getDamagerManager null? : " + Manager.getDamagerManager() == null);
				System.out.println("condition.GetSource() null? : " + condition.GetSource() == null);
				System.out.println("condition.GetSource().getName() null? : " + condition.GetSource().getName() == null);
				
				throw exception;
			}
		}
	}

	@EventHandler
	public void Fall(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.FALL)
			return;

		LivingEntity ent = event.GetDamageeEntity();
		if (ent == null)	return;

		Condition condition = Manager.GetActiveCondition(ent, ConditionType.FALLING);
		if (condition == null)	return;

		//Damage
		event.SetDamager(condition.GetSource());
		event.AddMod(UtilEnt.getName(condition.GetSource()), condition.GetReason(), 0, true);
		event.SetIgnoreArmor(true);
		event.SetKnockback(false);
	}

	@EventHandler
	public void Fall(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (LivingEntity ent : Manager.GetActiveConditions().keySet())
		{
			if (!UtilEnt.isGrounded(ent))
				continue;

			Condition condition = Manager.GetActiveCondition(ent, ConditionType.FALLING);
			if (condition == null)	return;

			if (!UtilTime.elapsed(condition.GetTime(), 250))
				continue;

			Manager.EndCondition(ent, ConditionType.FALLING , null);
		}
	}

	@Override
	public void handle(PacketInfo packetInfo)
	{
		Player player = packetInfo.getPlayer();
		Packet packet = packetInfo.getPacket();
		Player packetPlayer = null;

		if (packet instanceof PacketPlayOutEntity)
		{
			PacketPlayOutEntity entityPacket = (PacketPlayOutEntity) packet;

			for (Player other : player.getWorld().getPlayers())
			{
				if (entityPacket.a == other.getEntityId())
				{
					packetPlayer = other;
					break;
				}
			}

			if (packetPlayer == null || !Manager.isUntrueCloaked(packetPlayer))
			{
				return;
			}

			packetInfo.setCancelled(true);
		}
		else if (packet instanceof PacketPlayOutEntityTeleport)
		{
			PacketPlayOutEntityTeleport entityPacket = (PacketPlayOutEntityTeleport) packet;

			for (Player other : player.getWorld().getPlayers())
			{
				if (entityPacket.a == other.getEntityId())
				{
					packetPlayer = other;
					break;
				}
			}

			if (packetPlayer == null || !Manager.isUntrueCloaked(packetPlayer))
			{
				return;
			}

			packetInfo.setCancelled(true);
		}
	}
}
