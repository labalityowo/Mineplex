package mineplex.game.clans.items.attributes.weapon;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.attributes.DamageAttribute;
import mineplex.game.clans.items.generation.ValueDistribution;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ConqueringAttribute extends DamageAttribute
{
	private static ValueDistribution damageGen = generateDistribution(1.0d, 4.0d);		
	
	public ConqueringAttribute()
	{
		super(AttributeType.SUFFIX, damageGen);
	}

	@Override
	public String getDisplayName() 
	{
		return "Conquering";	
	}
	
	@Override
	public String getDescription()
	{
		return String.format("%.2f bonus damage against mobs & bosses", getBonusDamage());
	}
	
	@Override
	public boolean grantBonusDamage(Entity entity)
	{
		return !(entity instanceof Player);	// TODO: Check to see if entity is mob and/or a boss!
	}

}
