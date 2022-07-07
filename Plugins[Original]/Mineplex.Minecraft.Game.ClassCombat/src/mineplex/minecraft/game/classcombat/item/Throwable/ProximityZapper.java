package mineplex.minecraft.game.classcombat.item.Throwable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.item.ItemFactory;
import mineplex.minecraft.game.classcombat.item.ItemUsable;
import mineplex.minecraft.game.classcombat.item.event.ProximityUseEvent;

public class ProximityZapper extends ItemUsable
{
	private Map<Entity, LivingEntity> _armed = new HashMap<>();

	public ProximityZapper(ItemFactory factory, Material type,
			int amount, boolean canDamage, int gemCost, int tokenCost,
			ActionType useAction, boolean useStock, long useDelay,
			int useEnergy, ActionType throwAction, boolean throwStock,
			long throwDelay, int throwEnergy, float throwPower, 
			long throwExpire, boolean throwPlayer, boolean throwBlock, boolean throwIdle, boolean throwPickup) 
	{
		super(factory, "Proximity Zapper", new String[] { 
				"Thrown Item:", 
				"Activates after 4 seconds.", 
				"Detonates on player proximity;",
				"* Lightning strikes the Zapper" ,
				"* Silence for 6 seconds" ,
				"* Shock for 6 seconds" ,
				"* Slow IV for 6 seconds"
				}, type, amount, canDamage, gemCost, tokenCost,
				useAction, useStock, useDelay, useEnergy, throwAction, throwStock,
				throwDelay, throwEnergy, throwPower, 
				throwExpire, throwPlayer, throwBlock, throwIdle, throwPickup);
	}

	@Override
	public void UseAction(PlayerInteractEvent event) 
	{

	}
	
	@Override
	public void ThrowCustom(PlayerInteractEvent event, org.bukkit.entity.Item ent) 
	{
		ProximityUseEvent useEvent = new ProximityUseEvent(event.getPlayer(), this, ent);
		Bukkit.getServer().getPluginManager().callEvent(useEvent);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		if (UtilEnt.hasFlag(target, UtilEnt.FLAG_ENTITY_COMPONENT))
		{
			return;
		}

		//Arm
		_armed.put(data.getThrown(), data.getThrower());

		//Effect
		data.getThrown().getWorld().playEffect(data.getThrown().getLocation(), Effect.STEP_SOUND, 7);
		data.getThrown().getWorld().playSound(data.getThrown().getLocation(), Sound.NOTE_PLING, 0.5f, 2f);
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		//Arm
		_armed.put(data.getThrown(), data.getThrower());

		//Effect
		data.getThrown().getWorld().playEffect(data.getThrown().getLocation(), Effect.STEP_SOUND, 7);
		data.getThrown().getWorld().playSound(data.getThrown().getLocation(), Sound.NOTE_PLING, 0.5f, 2f);
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		//Arm
		_armed.put(data.getThrown(), data.getThrower());

		//Effect
		data.getThrown().getWorld().playEffect(data.getThrown().getLocation(), Effect.STEP_SOUND, 7);
		data.getThrown().getWorld().playSound(data.getThrown().getLocation(), Sound.NOTE_PLING, 0.5f, 2f);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void Pickup(PlayerPickupItemEvent event)
	{
		if (UtilPlayer.isSpectator(event.getPlayer()))
			return;
		
		if (((CraftPlayer)event.getPlayer()).getHandle().spectating)
			return;

		if (_armed.containsKey(event.getItem()))
		{
			event.setCancelled(true);
			Detonate(event.getItem(), event.getPlayer());
		}			
	}
	
	@EventHandler
	public void HopperPickup(InventoryPickupItemEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (_armed.containsValue(event.getItem()))
			event.setCancelled(true);
	}

	public void Detonate(Entity ent, Player player)
	{
		//Remove
		ent.remove();
		LivingEntity thrower = _armed.remove(ent);

		//Stun
		if (player != null)
		{
			//Inform
			UtilPlayer.message(player, F.main(GetName(), F.name(UtilEnt.getName(thrower)) +" hit you with " + F.item(GetName()) + "."));
		
			Factory.Condition().Factory().Silence(GetName(), player, thrower, 6, false, true);	
			Factory.Condition().Factory().Shock(GetName(), player, thrower, 6, false, false);
			Factory.Condition().Factory().Slow(GetName(), player, thrower, 6, 3, false, true, true, true);
		}

		//Blast
		ent.getWorld().strikeLightning(ent.getLocation());

		//Effect
		ent.getWorld().playSound(ent.getLocation(), Sound.NOTE_PLING, 0.5f, 0.5f);
		UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, ent.getLocation(), 0, 0.5f, 0, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
	}

	@EventHandler
	public void Clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		Iterator<Entity> armedIter = _armed.keySet().iterator();
		
		while (armedIter.hasNext())
		{
			Entity ent = armedIter.next();
			
			if (ent.isDead() || !ent.isValid() || ent.getTicksLived() >= 3600)
			{
				UtilParticle.PlayParticle(ParticleType.EXPLODE, ent.getLocation(), 0.1f, 0.1f, 0.1f, 0, 10,
						ViewDist.MAX, UtilServer.getPlayers());
				
				ent.remove();
				armedIter.remove();
			}
		}	
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void chunkUnload(ChunkUnloadEvent event)
	{
		for (Entity e : event.getChunk().getEntities())
		{
			if (_armed.remove(e) != null)
			{
				e.remove();
			}
		}
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
}