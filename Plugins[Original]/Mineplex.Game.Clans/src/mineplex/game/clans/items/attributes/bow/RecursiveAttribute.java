package mineplex.game.clans.items.attributes.bow;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.attributes.DamageAttribute;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.generation.ValueDistribution;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.entity.Entity;

public class RecursiveAttribute extends DamageAttribute
{
	private static ValueDistribution attackGen = generateDistribution(2, 6);	
	
	public RecursiveAttribute()
	{
		super(AttributeType.PREFIX, attackGen);
	}

	@Override
	public String getDisplayName() 
	{
		return "Recursive";	
	}
	
	@Override
	public String getDescription()
	{
		return String.format("Increase damage by %.2f half-hearts", getBonusDamage());
	}
	
	@Override
	public boolean grantBonusDamage(Entity defender)
	{
		return true;
	}
}
