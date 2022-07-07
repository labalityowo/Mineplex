package mineplex.game.clans.items.attributes;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Represents an attribute that triggers a special ability after a specified number
 * of attacks with a weapon possessing the attribute.
 * @author MrTwiggy
 *
 */
public abstract class AttackAttribute extends ItemAttribute 
{

	private int _attackLimit;
	public int getAttackLimit() { return _attackLimit; }
	
	private int _attackCount;
	
	public AttackAttribute(AttributeType type, int attackLimit)
	{
		super(type);
		
		_attackLimit = attackLimit;
		_attackCount = 0;
	}
	
	@Override
	public void onAttack(CustomDamageEvent event)
	{
		if(event.IsCancelled() || event.isCancelled()) return;
		_attackCount++;
//		System.out.println("Attack count " + _attackCount + " - " + _attackLimit);
		if (_attackCount >= _attackLimit)
		{
			_attackCount = 0;
			triggerAttack(event.GetDamagerEntity(true), event.GetDamageeEntity());
		}
	}
	
	public abstract void triggerAttack(Entity attacker, Entity defender);
}
