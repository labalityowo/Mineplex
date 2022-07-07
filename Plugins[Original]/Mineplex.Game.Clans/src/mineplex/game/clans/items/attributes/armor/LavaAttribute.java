package mineplex.game.clans.items.attributes.armor;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.generation.ValueDistribution;

public class LavaAttribute extends PercentReductionAttribute
{	
	private static ValueDistribution reductionGen = generateDistribution(0.2d, 1.0d);		// Value generator for heal amount
	private static ReductionConfig lavaConfig = new ReductionConfig(DamageCause.FIRE, DamageCause.LAVA, DamageCause.FIRE_TICK);
	
	public LavaAttribute()
	{
		super(AttributeType.SUPER_PREFIX, reductionGen, lavaConfig);
	}

	@Override
	public String getDisplayName() 
	{
		return "Lava Forged";
	}
	
	@Override
	public String getDescription()
	{
		return String.format("Reduces damage from fire and lava by %.1f%%", getReductionPercent() * 100d);
	}
}