package mineplex.game.clans.items.attributes.weapon;

import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.generation.ValueDistribution;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Frosted attribute to be added onto custom armor. Applies a slowing effect to players that 
 * attack the wearer of a Frosted {@link CustomItem}.
 * @author MrTwiggy
 *
 */
public class FrostedAttribute extends ItemAttribute
{
	private static ValueDistribution amountGen = generateDistribution(0, 3);	// Value generator for slow amount range
	private static ValueDistribution durationGen = generateDistribution(20, 60);	// Value generator for slow duration range
	
	private int _slowAmount;		// The slowness level/amplifier
	private int _slowDuration;		// The duration (in ticks) of slow effect
	
	/**
	 * Class constructor
	 */
	public FrostedAttribute()
	{
		super(AttributeType.SUPER_PREFIX);
		
		_slowAmount = amountGen.generateIntValue();
		_slowDuration = durationGen.generateIntValue();
	}
	
	@Override
	public String getDisplayName()
	{
		return "Frosted";
	}
	
	@Override
	public String getDescription()
	{
		return String.format("Apply slowness %s for %d ticks to enemies", amplifierToRoman(_slowAmount), _slowDuration);
	}
	
	@Override
	public void onAttack(CustomDamageEvent event)
	{
		Player victim = event.GetDamageePlayer();
		
		if (victim != null)
		{
			if (isTeammate(event.GetDamagerPlayer(true), victim)) return;
			victim.addPotionEffect(generateSlowEffect());	// Slow attacking player
		}
	}
	
	private PotionEffect generateSlowEffect()
	{
		return new PotionEffect(PotionEffectType.SLOW, _slowDuration, _slowAmount);
	}
}
