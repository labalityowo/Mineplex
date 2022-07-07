package mineplex.game.clans.items.attributes.bow;

import mineplex.core.common.util.UtilMath;
import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.generation.ValueDistribution;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class LeechingAttribute extends ItemAttribute
{
	private static ValueDistribution healGen = generateDistribution(5, 15);
	
	private int _healPercent;
	
	public LeechingAttribute()
	{
		super(AttributeType.SUPER_PREFIX);
		
		_healPercent = healGen.generateIntValue();
	}

	@Override
	public String getDisplayName() 
	{
		return "Leeching";
	}
	
	@Override
	public String getDescription()
	{
		return String.format("Heal for %d percentage of damage dealt", _healPercent);
	}
	
	@Override
	public void onAttack(CustomDamageEvent event)
	{
		Player damager = event.GetDamagerPlayer(true);
		
		double damage = event.GetDamage();
		double healAmount = damage * (_healPercent / 100d);
		heal(damager, healAmount);
	}

	private void heal(Player player, double healAmount)
	{
		player.setHealth(UtilMath.clamp(player.getHealth() + healAmount, 0, 20));
	}
}
