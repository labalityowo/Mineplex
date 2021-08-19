package mineplex.core.gadget.gadgets.mount;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public abstract class MountData
{
	
	protected Player _owner;
	
	public MountData(Player player)
	{
		_owner = player;
	}

	public boolean isPartOfMount(Entity ent)
	{
		return getEntityParts().contains(ent);
	}
	
	public abstract List<Entity> getEntityParts();
	
	public boolean ownsMount(Player p)
	{
		return _owner.equals(p);
	}
	
	public Player getOwner() 
	{
		return _owner;
	}
	
	public void remove()
	{
		for(Entity e : getEntityParts())
		{
			e.remove();
		}
	}
	 
}
