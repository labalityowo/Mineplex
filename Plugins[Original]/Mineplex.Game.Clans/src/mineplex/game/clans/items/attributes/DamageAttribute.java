package mineplex.game.clans.items.attributes;

import mineplex.game.clans.items.generation.ValueDistribution;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public abstract class DamageAttribute extends ItemAttribute
{
	private double _bonusDamage;
	public double getBonusDamage() { return _bonusDamage; }
	
	public DamageAttribute(AttributeType type, ValueDistribution damageGen) 
	{
		super(type);
		
		_bonusDamage = damageGen.generateValue();
	}

	@Override
	public void onAttack(CustomDamageEvent event)
	{
		if (grantBonusDamage(event.GetDamageeEntity()))
		{
			event.AddMod("Damage Attribute", _bonusDamage);
		}
	}
	
	public abstract boolean grantBonusDamage(Entity defender);
}
