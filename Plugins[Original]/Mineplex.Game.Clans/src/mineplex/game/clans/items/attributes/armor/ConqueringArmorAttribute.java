package mineplex.game.clans.items.attributes.armor;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.generation.ValueDistribution;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

// A.K.A Conquering for Armor
public class ConqueringArmorAttribute extends ItemAttribute
{
	private static ValueDistribution reductionGen = generateDistribution(2.5d, 6.25d);
	private double _reduction;
	
	public ConqueringArmorAttribute()
	{
		super(AttributeType.SUFFIX);
		_reduction = reductionGen.generateValue() / 100;
	}

	@Override
	public String getDisplayName() 
	{
		return "Conquering";
	}
	
	@Override
	public void onAttacked(CustomDamageEvent event)
	{
		DamageCause cause = event.GetCause();
		Entity attacker = event.GetDamagerEntity(true);
		
		if (reducesDamage(cause, attacker))
		{
			event.AddMult("Conquering Armor", new String(), 1 - _reduction, false);
//			System.out.println("Reduced damage by " + reduction);
		}
		else
		{
//			System.out.println("Armor doesn't reduce " + cause);
		}
	}

	@Override
	public String getDescription()
	{
		return String.format("%.1f%% damage taken from mobs & bosses", (1 - _reduction) * 100);
	}
	
	public boolean reducesDamage(DamageCause cause, Entity attacker)
	{
		return !(attacker instanceof Player);	// Reduces damage from all entities
	}
}
