 package mineplex.game.clans.items.attributes.armor;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.generation.ValueDistribution;

public class ReinforcedAttribute extends FlatReductionAttribute
{
	private static ValueDistribution reductionGen = generateDistribution(0.5d, 1.0d);
	private static ReductionConfig config = new ReductionConfig(DamageCause.ENTITY_ATTACK);
	
	public ReinforcedAttribute()
	{
		super(AttributeType.PREFIX, reductionGen, config);
	}

	@Override
	public String getDisplayName() 
	{
		return "Reinforced";
	}

	@Override
	public String getDescription()
	{
		return String.format("-%.1f damage taken from melee", getFlatReduction());
	}
}
