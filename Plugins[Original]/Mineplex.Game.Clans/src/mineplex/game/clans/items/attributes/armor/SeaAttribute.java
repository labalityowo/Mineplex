package mineplex.game.clans.items.attributes.armor;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.generation.ValueDistribution;

public class SeaAttribute extends ItemAttribute
{
	// TODO: Replace with your generators
	private static ValueDistribution healGen = generateDistribution(4, 12);		// Value generator for heal amount
	
	private int _healPercent;
	
	public SeaAttribute()
	{
		super(AttributeType.SUFFIX);
		
		_healPercent = healGen.generateIntValue();
	}

	@Override
	public String getDisplayName() 
	{
		return "";	// TODO: Fill in name
	}

}
