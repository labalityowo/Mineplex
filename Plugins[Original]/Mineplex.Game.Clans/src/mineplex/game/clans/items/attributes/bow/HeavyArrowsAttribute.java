package mineplex.game.clans.items.attributes.bow;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.generation.ValueDistribution;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.entity.Entity;

public class HeavyArrowsAttribute extends ItemAttribute
{
	private static ValueDistribution knockbackGen = generateDistribution(25, 75);	
	
	private double _knockbackPercent;
	
	public HeavyArrowsAttribute()
	{
		super(AttributeType.PREFIX);
		
		_knockbackPercent = knockbackGen.generateValue();
	}

	@Override
	public String getDisplayName() 
	{
		return "Heavy";	
	}
	
	@Override
	public String getDescription()
	{
		return String.format("Increase knockback by %.2f%%", _knockbackPercent);
	}
	
	@Override
	public void onAttack(CustomDamageEvent event)
	{
		double knockback = (_knockbackPercent / 100d) * 6;
		event.AddKnockback("Heavy Attribute", knockback);
	}
}
