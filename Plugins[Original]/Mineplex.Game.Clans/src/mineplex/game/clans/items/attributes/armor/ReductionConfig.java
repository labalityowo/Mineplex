package mineplex.game.clans.items.attributes.armor;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class ReductionConfig 
{

	private Set<EntityType> _reducedAttackers;	// EntityTypes whose attacks are reduced by this attribute
	private Set<DamageCause> _reducedCauses;	// DamageCauses that are reduced by this attribute
	
	public ReductionConfig()
	{
		_reducedAttackers = new HashSet<EntityType>();
		_reducedCauses = new HashSet<DamageCause>();
	}
	
	public ReductionConfig(DamageCause... reducedCauses)
	{
		this();
		
		for (DamageCause cause : reducedCauses)
		{
			_reducedCauses.add(cause);
		}
	}
	
	public ReductionConfig(EntityType... reducedAttackers)
	{
		this();
		
		for (EntityType attacker : reducedAttackers)
		{
			_reducedAttackers.add(attacker);
		}
	}
	
	public boolean reducesDamage(DamageCause cause, Entity attacker)
	{
		return _reducedCauses.contains(cause) || (attacker != null && _reducedAttackers.contains(attacker.getType()));
	}
}
