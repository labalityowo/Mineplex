package mineplex.game.clans.items.attributes.armor;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.generation.ValueDistribution;

public abstract class FlatReductionAttribute extends ReductionAttribute
{

	private double _reduction;
	public double getFlatReduction() { return _reduction; }
	
	public FlatReductionAttribute(AttributeType type, ValueDistribution reductionGen, ReductionConfig config)
	{
		super(type, config);
		
		_reduction = reductionGen.generateValue();
	}
	
	@Override
	public double getDamageReduction(double originalDamage) 
	{
		return _reduction;
	}

}
