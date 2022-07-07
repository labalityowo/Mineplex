package mineplex.game.clans.items.attributes.weapon;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.generation.ValueDistribution;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class HeavyAttribute extends ItemAttribute
{
	private static ValueDistribution knockbackGen = generateDistribution(25, 75);		// Value generator for knockback % boost.
	
	private double _knockbackBoost;
	
	public HeavyAttribute()
	{
		super(AttributeType.PREFIX);
		
		_knockbackBoost = knockbackGen.generateValue();
	}

	@Override
	public String getDisplayName() 
	{
		return "Heavy";
	}
	
	@Override
	public String getDescription()
	{
		return String.format("%.1f%% additional knockback", _knockbackBoost);
	}

	@Override
	public void onAttack(CustomDamageEvent event)
	{
		double knockback = (_knockbackBoost / 100d) * event.getKnockbackValue();
		event.AddKnockback("Heavy Attribute", knockback);
	}
}
