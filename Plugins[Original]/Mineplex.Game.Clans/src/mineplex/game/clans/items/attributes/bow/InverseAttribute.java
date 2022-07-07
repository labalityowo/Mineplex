package mineplex.game.clans.items.attributes.bow;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.generation.ValueDistribution;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class InverseAttribute extends ItemAttribute
{
private static ValueDistribution knockbackGen = generateDistribution(0.d, 1.d);	
	
	private double _knockbackModifier;
	
	public InverseAttribute()
	{
		super(AttributeType.PREFIX);
		
		_knockbackModifier = knockbackGen.generateValue();
	}

	@Override
	public String getDisplayName() 
	{
		return "Inverse";	
	}
	
	@Override
	public String getDescription()
	{
		return String.format("Reverse knockback and modify amount to %.2f percent", (.5d + _knockbackModifier) * 100d);
	}
	
	@Override
	public void onAttack(CustomDamageEvent event)
	{
		event.AddKnockback("InverseAttribute", _knockbackModifier);
		event.invertKnockback();
	}
}
