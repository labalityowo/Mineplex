package mineplex.game.clans.clans.worldevent.raid.wither.creature.wither;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityWither;
import net.minecraft.server.v1_8_R3.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_8_R3.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_8_R3.PathfinderGoalRandomStroll;

import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.com.google.common.base.Predicate;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftWither;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.worldevent.api.BossPassive;
import mineplex.game.clans.clans.worldevent.raid.RaidChallenge;
import mineplex.game.clans.clans.worldevent.raid.RaidCreature;
import mineplex.game.clans.clans.worldevent.raid.wither.WitherRaid;
import mineplex.game.clans.clans.worldevent.raid.wither.creature.wither.ai.PathfinderGoalCustomFloat;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class CharlesWitherton extends RaidCreature<Wither>
{
	private RaidChallenge<WitherRaid> _challenge;
	private CharlesSkulls _passive;
	private List<BossPassive<CharlesWitherton, Wither>> _abilities = new ArrayList<>();
	
	public boolean Flying = false;

	public CharlesWitherton(RaidChallenge<WitherRaid> challenge, Location location)
	{
		super(challenge.getRaid(), location, "Charles Witherton", true, 5000, 15000, true, Wither.class);
		
		_challenge = challenge;
		spawnEntity();
		_passive = new CharlesSkulls(this);
		_abilities.add(new LifeSap(this));
		_abilities.add(new Flight(this));
		_abilities.add(new Decay(this));
		_abilities.add(new WitherWave(this));
		_abilities.add(new SummonUndead(this));
		_abilities.add(new SummonCorpse(this));
		_abilities.add(new BlackHole(this));
		_abilities.add(new SummonMinions(this));
	}
	
	protected RaidChallenge<WitherRaid> getChallenge()
	{
		return _challenge;
	}
	
	protected List<Location> getCustomLocs(String id)
	{
		return _challenge.getRaid().getWorldData().getCustomLocs(id);
	}

	@Override
	protected void spawnCustom()
	{
		UtilEnt.vegetate(getEntity());
		CraftWither cw = (CraftWither)getEntity();
		EntityWither wither = cw.getHandle();
		wither.setVegetated(false);
		wither.goalSelector.a(0, new PathfinderGoalCustomFloat(this, wither));
		wither.goalSelector.a(5, new PathfinderGoalRandomStroll(wither, 1.0D));
		wither.goalSelector.a(6, new PathfinderGoalLookAtPlayer(wither, EntityHuman.class, 8.0F));
		wither.goalSelector.a(7, new PathfinderGoalRandomLookaround(wither));
		wither.targetSelector.a(1, new PathfinderGoalHurtByTarget(wither, false, new Class[0]));
		wither.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityInsentient>(wither, EntityInsentient.class, 0, false, false, new Predicate<Entity>()
		{
			public boolean a(Entity entity)
			{
				return entity instanceof EntityHuman;
			}
			
			public boolean apply(Entity entity)
			{
				return this.a(entity);
			}
		}));
	}

	@Override
	public void dieCustom()
	{
		endAbility();
	}

	private void endAbility()
	{
		for (BossPassive<CharlesWitherton, Wither> ability : _abilities)
		{
			HandlerList.unregisterAll(ability);
		}
		_abilities.clear();
		_passive.setFinished();
		HandlerList.unregisterAll(_passive);
		_passive = null;
	}
	
	@EventHandler
	public void onTick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		if (_challenge.isComplete())
		{
			remove();
			return;
		}
		
		for (Block b : UtilBlock.getBlocksInRadius(getEntity().getLocation(), 4))
		{
			if (b.getType() == Material.ICE)
			{
				if (ClansManager.getInstance().getBlockRestore().contains(b))
				{
					ClansManager.getInstance().getBlockRestore().restore(b);
				}
				else
				{
					b.setType(Material.AIR);
				}
			}
		}
		
		_passive.tick();
		_abilities.forEach(BossPassive::tick);
		
		if (Flying)
		{
			getEntity().setHealth(500);
		}
		else
		{
			getEntity().setHealth(100);
		}
	}
	
	@EventHandler
	public void handleBlockChange(EntityChangeBlockEvent event)
	{
		if (event.getEntity().getEntityId() == getEntity().getEntityId())
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onLaunch(ProjectileLaunchEvent event)
	{
		if (event.getEntity() instanceof WitherSkull && event.getEntity().getShooter() instanceof Wither)
		{
			if (((Wither)event.getEntity().getShooter()).getEntityId() == getEntity().getEntityId())
			{
				if (((WitherSkull)event.getEntity()).isCharged())
				{
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void allyDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity().getEntityId() == getEntity().getEntityId())
		{
			LivingEntity damager = event.GetDamagerEntity(event.GetCause() == DamageCause.PROJECTILE);
			if (damager != null && !(damager instanceof Player))
			{
				event.SetCancelled("Allied Damage");
			}
		}
	}

	@Override
	public void handleDeath(Location location) {}
}