package mineplex.game.clans.items.attributes.armor;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.generation.ValueDistribution;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public abstract class ReductionAttribute extends ItemAttribute
{

	private ReductionConfig _config;
	
	public ReductionAttribute(AttributeType type, ReductionConfig config)
	{
		super(type);
		
		_config = config;
	}
	
	@Override
	public void onAttacked(CustomDamageEvent event)
	{
		DamageCause cause = event.GetCause();
		Entity attacker = event.GetDamagerEntity(true);
		
		if (reducesDamage(cause, attacker))
		{
			double damage = event.GetDamage();
			double reduction = getDamageReduction(damage);
			event.AddMod("Reduction Armor", -reduction);
//			System.out.println("Reduced damage by " + reduction);
		}
		else
		{
//			System.out.println("Armor doesn't reduce " + cause);
		}
	}
	
	public boolean reducesDamage(DamageCause cause, Entity attacker)
	{
		return _config.reducesDamage(cause, attacker);
	}
	
	public abstract double getDamageReduction(double originalDamage);
}
