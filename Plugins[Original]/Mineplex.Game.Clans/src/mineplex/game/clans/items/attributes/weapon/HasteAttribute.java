package mineplex.game.clans.items.attributes.weapon;

import mineplex.game.clans.items.attributes.AttackAttribute;
import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.generation.ValueDistribution;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class HasteAttribute extends AttackAttribute
{
	private static ValueDistribution attackGen = generateDistribution(2, 4);		
	private static ValueDistribution speedGen = generateDistribution(0, 2);	
	private static ValueDistribution durationGen = generateDistribution(60, 120);	
	
	private int _speedAmount;
	private int _speedDuration;
	
	public HasteAttribute()
	{
		super(AttributeType.SUFFIX, attackGen.generateIntValue());
		
		_speedAmount = speedGen.generateIntValue();
		_speedDuration = durationGen.generateIntValue();
	}

	@Override
	public String getDisplayName() 
	{
		return "Haste";	
	}
	
	@Override
	public String getDescription()
	{
		return String.format("Every %d attacks gives you Speed %s for %.1f seconds", getAttackLimit(), amplifierToRoman(_speedAmount), (_speedDuration / 20f));
	}
	
	@Override
	public void triggerAttack(Entity attacker, Entity defender)
	{
		if (isTeammate(attacker, defender)) return;
		if (attacker instanceof Player)
		{
			Player player = (Player) attacker;
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, _speedDuration, _speedAmount));
		}
	}

}
