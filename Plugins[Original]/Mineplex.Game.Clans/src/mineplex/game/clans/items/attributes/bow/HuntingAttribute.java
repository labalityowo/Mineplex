package mineplex.game.clans.items.attributes.bow;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.generation.ValueDistribution;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HuntingAttribute extends ItemAttribute
{
	private static ValueDistribution amountGen = generateDistribution(0, 2);	// Value generator for slow amount range
	private static ValueDistribution durationGen = generateDistribution(1, 4);	// Value generator for slow duration range
	
	private int _slowAmount;		// The slowness level/amplifier
	private double _slowDuration;	// The duration (in ticks) of slow effect
	
	public HuntingAttribute()
	{
		super(AttributeType.PREFIX);
		
		_slowAmount = amountGen.generateIntValue();
		_slowDuration = durationGen.generateValue();
	}

	@Override
	public String getDisplayName() 
	{
		return "Hunting";	
	}
	
	@Override
	public String getDescription()
	{
		return String.format("Damaged enemies receive slowness %s for %.2f seconds", amplifierToRoman(_slowAmount), _slowDuration);
	}
	
	@Override
	public void onAttacked(CustomDamageEvent event)
	{
		Player damager = event.GetDamagerPlayer(true);
		
		if (damager != null)
		{
			damager.addPotionEffect(generateSlowEffect());	// Slow attacking player
		}
	}
	
	private PotionEffect generateSlowEffect()
	{
		return new PotionEffect(PotionEffectType.SLOW, (int) (_slowDuration * 20), _slowAmount);
	}
}
