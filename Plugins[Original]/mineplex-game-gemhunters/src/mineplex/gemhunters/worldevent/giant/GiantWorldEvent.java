package mineplex.gemhunters.worldevent.giant;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.loot.rewards.LootChestReward;
import mineplex.gemhunters.worldevent.WorldEvent;
import mineplex.gemhunters.worldevent.WorldEventState;
import mineplex.gemhunters.worldevent.WorldEventType;

public class GiantWorldEvent extends WorldEvent
{

	private static final double START_CHANCE = 0.01;
	private static final int MINI_ZOMBIES = 10;
	private static final int MINI_ZOMBIES_MAX_DISTANCE_SQUARED = 2500;
	private static final long MAX_TIME = TimeUnit.MINUTES.toMillis(5);
	private static final long CASH_OUT_DELAY = TimeUnit.MINUTES.toMillis(10);
	
	private CustomGiant _giant;

	public GiantWorldEvent()
	{
		super(WorldEventType.GIANT);
	}

	@EventHandler
	public void trigger(PlayerDeathEvent event)
	{
		if (Math.random() < START_CHANCE)
		{
			setEventState(WorldEventState.WARMUP);
		}
	}

	@Override
	public void onStart()
	{
		_giant = new CustomGiant(_worldData.getCustomLocation("GIANT_SPAWN").get(0));
		addEntity(_giant.getGiant());

		_worldEvent.registerEvents(_giant);

		setEventState(WorldEventState.LIVE);
	}

	@Override
	public boolean checkToEnd()
	{
		return UtilTime.elapsed(_start, MAX_TIME) || _giant.getGiant().isDead() || !_giant.getGiant().isValid();
	}

	@Override
	public void onEnd()
	{
		ItemStack itemStack = SkinData.OMEGA_CHEST.getSkull(C.cAqua + "Omega Chest", new ArrayList<>());
		LootChestReward reward = new LootChestReward(CASH_OUT_DELAY, itemStack, "Omega", 1);
		
		_worldData.World.dropItemNaturally(getEventLocations()[0], itemStack);
		
		_loot.addItemReward(reward);
		UtilServer.broadcast(F.main(_worldEvent.getName(), "The Giant has been killed! And has dropped loot!"));
	
		UtilServer.Unregister(_giant);
		_giant = null;
	}

	@Override
	public Location[] getEventLocations()
	{
		return new Location[] { _giant.getGiant().getLocation() };
	}

	@Override
	public double getProgress()
	{
		LivingEntity giant = _giant.getGiant();
		
		return giant.getHealth() / giant.getMaxHealth();
	}
	
	@EventHandler
	public void zombieCombust(EntityCombustEvent event)
	{
		if (_entities.contains(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || _giant == null)
		{
			return;
		}

		for (Entity entity : _entities)
		{
			if (UtilMath.offsetSquared(entity, _giant.getGiant()) > MINI_ZOMBIES_MAX_DISTANCE_SQUARED)
			{
				entity.teleport(_giant.getGiant());
			}
		}

		// -1 for the giant
		if (_entities.size() - 1 < MINI_ZOMBIES)
		{
			Zombie zombie = _worldData.World.spawn(_giant.getGiant().getLocation(), Zombie.class);

			zombie.setRemoveWhenFarAway(false);
			zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));

			addEntity(zombie);
		}
	}

}
