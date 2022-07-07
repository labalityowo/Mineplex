package mineplex.game.clans.items.attributes.armor;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.generation.ValueDistribution;

public class PaddedAttribute extends FlatReductionAttribute
{	
	private static ValueDistribution reductionGen = generateDistribution(1.0d, 4.0d);
	private static ReductionConfig config = new ReductionConfig(DamageCause.FALL);
	
	public PaddedAttribute()
	{
		super(AttributeType.PREFIX, reductionGen, config);
	}

	@Override
	public String getDisplayName() 
	{
		return "Padded";
	}
	
	@Override
	public String getDescription()
	{
		return String.format("-%.1f damage taken from falls", getFlatReduction());
	}
}
