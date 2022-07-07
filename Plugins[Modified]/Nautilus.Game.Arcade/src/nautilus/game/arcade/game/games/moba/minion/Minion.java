package nautilus.game.arcade.game.games.moba.minion;

import mineplex.core.common.util.UtilEnt;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

public class Minion
{

	private static final int HEALTH = 10;
	private static final float HIT_BOX = 2F;
	private static final ItemStack[] SUPER_ARMOUR = {
			new ItemStack(Material.IRON_BOOTS),
			new ItemStack(Material.IRON_LEGGINGS),
			new ItemStack(Material.IRON_CHESTPLATE),
			new ItemStack(Material.IRON_HELMET)
	};

	private final LivingEntity _entity;

	private Location _target;
	private int _targetIndex;

	public Minion(Location spawn, Class<? extends LivingEntity> clazz, boolean superMinion)
	{
		_target = spawn;

		LivingEntity entity = spawn.getWorld().spawn(spawn, clazz);
		_entity = entity;
		entity.setMaxHealth(HEALTH);
		entity.setRemoveWhenFarAway(false);

		if (entity instanceof Zombie)
		{
			((Zombie) entity).setBaby(true);
		}

		if (superMinion)
		{
			entity.getEquipment().setArmorContents(SUPER_ARMOUR);
		}

		UtilEnt.vegetate(entity);
		UtilEnt.silence(entity, true);
		UtilEnt.setBoundingBox(entity, HIT_BOX, HIT_BOX);

		entity.setCustomNameVisible(true);
		updateDisplay(entity.getMaxHealth());
	}

	public void updateDisplay(double health)
	{
		_entity.setCustomName(MobaUtil.getHealthBar(_entity, health, 10));
	}

	public LivingEntity getEntity()
	{
		return _entity;
	}

	public void setTarget(Location location)
	{
		// Keep the Y constant
		location.setY(_target.getY());

		_target = location;
	}

	public Location getTarget()
	{
		return _target;
	}

	public void setTargetIndex(int index)
	{
		_targetIndex = index;
	}

	public int getTargetIndex()
	{
		return _targetIndex;
	}
}
