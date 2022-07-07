package mineplex.game.clans.items.attributes.armor;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.generation.ValueDistribution;

public abstract class PercentReductionAttribute extends ReductionAttribute
{
	private double _reductionPercent;
	public double getReductionPercent() { return _reductionPercent; }
	
	public PercentReductionAttribute(AttributeType type, ValueDistribution reductionGen, ReductionConfig config)
	{
		super(type, config);
		
		_reductionPercent = reductionGen.generateValue();
	}
	
	@Override
	public double getDamageReduction(double originalDamage) 
	{
		return originalDamage * _reductionPercent;
	}

}
