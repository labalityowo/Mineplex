package mineplex.game.clans.items.attributes.bow;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.attributes.DamageAttribute;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.generation.ValueDistribution;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;

public class SlayingAttribute extends DamageAttribute
{
	private static ValueDistribution attackGen = generateDistribution(2, 12);	
	
	public SlayingAttribute()
	{
		super(AttributeType.SUFFIX, attackGen);
	}

	@Override
	public String getDisplayName() 
	{
		return "Slaying";	
	}
	
	@Override
	public String getDescription()
	{
		return String.format("Increase damage by %.2f half-hearts against mobs & bosses", getBonusDamage());
	}
	
	@Override
	public boolean grantBonusDamage(Entity defender)
	{
		return defender instanceof Monster;	// TODO: Check to see if defender is also a WorldEvent boss?
	}
}
