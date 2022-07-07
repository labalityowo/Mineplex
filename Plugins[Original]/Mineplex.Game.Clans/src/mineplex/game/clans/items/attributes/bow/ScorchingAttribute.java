package mineplex.game.clans.items.attributes.bow;

import mineplex.game.clans.items.attributes.AttackAttribute;
import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.generation.ValueDistribution;

import org.bukkit.entity.Entity;

public class ScorchingAttribute extends AttackAttribute
{
	private static ValueDistribution fireGen = generateDistribution(2, 6);	
	
	private double _fireDuration;
	
	public ScorchingAttribute()
	{
		super(AttributeType.SUPER_PREFIX, 1);	// Activates every hit
		_fireDuration = fireGen.generateValue();
	}

	@Override
	public String getDisplayName() 
	{
		return "Scorching";
	}
	
	@Override
	public String getDescription()
	{
		return String.format("Struck enemies catch fire for %.2f seconds", _fireDuration);
	}
	
	@Override
	public void triggerAttack(Entity attacker, Entity defender)
	{
		if (isTeammate(attacker, defender))
		{
			return;
		}
		defender.setFireTicks((int) (_fireDuration * 20));
	}

}
