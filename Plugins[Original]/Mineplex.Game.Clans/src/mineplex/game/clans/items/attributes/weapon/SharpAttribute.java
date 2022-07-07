package mineplex.game.clans.items.attributes.weapon;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.attributes.DamageAttribute;
import mineplex.game.clans.items.generation.ValueDistribution;

import org.bukkit.entity.Entity;

public class SharpAttribute extends DamageAttribute
{
	private static ValueDistribution damageGen = generateDistribution(0.5d, 1.5d);
	
	public SharpAttribute()
	{
		super(AttributeType.PREFIX, damageGen);
	}

	@Override
	public String getDisplayName() 
	{
		return "Sharp";
	}
	
	@Override
	public String getDescription()
	{
		return String.format("%.2f bonus damage", getBonusDamage());
	}
	
	@Override
	public boolean grantBonusDamage(Entity defender)
	{
		return true;
	}

}
