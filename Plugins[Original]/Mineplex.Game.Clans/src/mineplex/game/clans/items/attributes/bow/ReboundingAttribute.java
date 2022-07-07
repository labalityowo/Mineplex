package mineplex.game.clans.items.attributes.bow;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.generation.ValueDistribution;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.entity.Entity;

public class ReboundingAttribute extends ItemAttribute
{
	private static ValueDistribution attackGen = generateDistribution(2, 4);	
	private static ValueDistribution fireGen = generateDistribution(60, 120);	
	
	
	public ReboundingAttribute()
	{
		super(AttributeType.SUPER_PREFIX);
	}

	@Override
	public String getDisplayName() 
	{
		return "";	
	}
	
	@Override
	public String getDescription()
	{
		return "";
	}
	
	@Override
	public void onAttack(CustomDamageEvent event)
	{

	}
}
