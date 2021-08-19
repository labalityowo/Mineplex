package mineplex.core.gadget.gadgets.mount;

import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SingleEntityMountData<T extends Entity> extends MountData
{
	
	protected T Entity;

	public SingleEntityMountData(Player player, T ent)
	{
		super(player);
		Entity = ent;
	}

	@Override
	public List<Entity> getEntityParts()
	{
		return Collections.singletonList(Entity);
	}
	
	public T getEntity()
	{
		return Entity;
	}

}
