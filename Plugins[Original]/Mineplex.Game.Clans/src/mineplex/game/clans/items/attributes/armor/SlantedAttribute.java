package mineplex.game.clans.items.attributes.armor;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.generation.ValueDistribution;

public class SlantedAttribute extends FlatReductionAttribute
{
	private static ValueDistribution reductionGen = generateDistribution(0.5d, 1.5d);
	private static ReductionConfig config = new ReductionConfig(DamageCause.PROJECTILE);
	
	public SlantedAttribute()
	{
		super(AttributeType.PREFIX, reductionGen, config);
	}

	@Override
	public String getDisplayName() 
	{
		return "Slanted";
	}

	@Override
	public String getDescription()
	{
		return String.format("-%.1f damage taken from projectiles", getFlatReduction());
	}
}
