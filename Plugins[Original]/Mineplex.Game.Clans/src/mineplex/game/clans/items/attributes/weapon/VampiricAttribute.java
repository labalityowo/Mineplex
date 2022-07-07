package mineplex.game.clans.items.attributes.weapon;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.generation.ValueDistribution;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class VampiricAttribute extends ItemAttribute
{
	private static ValueDistribution healGen = generateDistribution(4, 12);
	
	private int _healPercent;
	
	public VampiricAttribute()
	{
		super(AttributeType.SUPER_PREFIX);
		
		_healPercent = healGen.generateIntValue();
	}

	@Override
	public String getDisplayName() 
	{
		return "Vampiric";
	}
	
	@Override
	public String getDescription()
	{
		return String.format("Heal yourself for %d% of damage dealt", _healPercent);
	}
	
	@Override
	public void onAttack(CustomDamageEvent event)
	{
		Player damager = event.GetDamagerPlayer(false);

		if (isTeammate(damager, event.GetDamageePlayer())) return;
		
		double damage = event.GetDamage();
		double healAmount = damage * (_healPercent / 100d);
		heal(damager, healAmount);
	}

	private void heal(Player player, double healAmount)
	{
		player.setHealth(player.getHealth() + healAmount);
	}
}
