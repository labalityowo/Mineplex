package mineplex.core.treasure.animation;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.TreasureSession;
import mineplex.core.treasure.animation.event.TreasureAnimationFinishEvent;
import mineplex.core.treasure.types.Treasure;

public abstract class TreasureAnimation implements Runnable
{

	private final Treasure _treasure;
	private final TreasureLocation _treasureLocation;

	protected final List<LivingEntity> _entities;
	protected final List<ArmorStand> _stands;
	protected final List<Item> _items;

	private int _tick;
	private boolean _running;

	public TreasureAnimation(Treasure treasure, TreasureLocation treasureLocation)
	{
		_treasure = treasure;
		_treasureLocation = treasureLocation;

		_entities = new ArrayList<>();
		_stands = new ArrayList<>();
		_items = new ArrayList<>();
	}

	protected abstract void onStart();

	@Override
	public void run()
	{
		onTick();
		_tick++;

		_items.forEach(item ->
		{
			if (!UtilEnt.hasFlag(item, UtilEnt.FLAG_NO_REMOVE) && item.getTicksLived() > 20)
			{
				item.remove();
			}
		});
	}

	public abstract void onTick();

	protected abstract void onFinish();

	public void cleanup()
	{
		_entities.forEach(Entity::remove);
		_entities.clear();
		_stands.forEach(Entity::remove);
		_stands.clear();
		_items.forEach(Entity::remove);
		_items.clear();
	}

	protected <T extends LivingEntity> T spawnEntity(Location location, Class<T> clazz)
	{
		return spawnEntity(location, clazz, false);
	}

	protected <T extends LivingEntity> T spawnEntity(Location location, Class<T> clazz, boolean ai)
	{
		T entity = location.getWorld().spawn(location, clazz);

		if (!ai)
		{
			UtilEnt.vegetate(entity);
		}

		UtilEnt.ghost(entity, true, false);
		UtilEnt.CreatureLook(entity, location.getPitch(), location.getYaw());
		UtilEnt.addFlag(entity, UtilEnt.FLAG_ENTITY_COMPONENT);
		_entities.add(entity);

		return entity;
	}

	protected void disguise(DisguiseBase disguise)
	{
		_treasureLocation.getManager().getDisguiseManager().disguise(disguise);
	}

	protected ArmorStand spawnArmourStand(Location location)
	{
		float yaw = Math.round(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, _treasureLocation.getChest())) / 90F) * 90F;
		location.setYaw(yaw);

		location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, _treasureLocation.getChest())));
		ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);

		stand.setVisible(false);
		stand.setGravity(false);
		_stands.add(stand);

		return stand;
	}

	protected void shakeArmourStand(ArmorStand stand, double magnitude)
	{
		stand.setHeadPose(
				new EulerAngle(
						UtilMath.random(-magnitude, magnitude),
						UtilMath.random(-magnitude, magnitude),
						UtilMath.random(-magnitude, magnitude)
				)
		);
	}

	protected void resetArmourStand(ArmorStand stand)
	{
		stand.setHeadPose(EulerAngle.ZERO);
	}

	protected Item spawnItem(Location location, ItemStack itemStack, boolean temporary)
	{
		Item item = location.getWorld().dropItemNaturally(location, itemStack);

		if (!temporary)
		{
			UtilEnt.addFlag(item, UtilEnt.FLAG_NO_REMOVE);
		}

		item.setPickupDelay(Integer.MAX_VALUE);
		_items.add(item);

		return item;
	}

	public Treasure getTreasure()
	{
		return _treasure;
	}

	public TreasureLocation getTreasureLocation()
	{
		return _treasureLocation;
	}

	public int getTicks()
	{
		return _tick;
	}

	public List<LivingEntity> getEntities()
	{
		return _entities;
	}

	public boolean isRunning()
	{
		return _running;
	}

	public void setRunning(boolean running)
	{
		_running = running;

		if (running)
		{
			onStart();
		}
		else
		{
			onFinish();
			TreasureSession session = _treasureLocation.getSession();
			if (session != null)
				UtilServer.CallEvent(new TreasureAnimationFinishEvent(session.getPlayer(), session, this));
		}
	}
}
