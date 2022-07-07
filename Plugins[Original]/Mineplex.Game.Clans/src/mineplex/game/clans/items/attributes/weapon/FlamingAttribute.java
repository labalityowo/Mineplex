package mineplex.game.clans.items.attributes.weapon;

import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.gui.page.ClanMainPage;
import mineplex.game.clans.items.attributes.AttackAttribute;
import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.generation.ValueDistribution;

import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class FlamingAttribute extends AttackAttribute
{
	private static ValueDistribution attackGen = generateDistribution(2, 4);	
	private static ValueDistribution fireGen = generateDistribution(60, 120);	
	
	private int _fireDuration;
	
	public FlamingAttribute()
	{
		super(AttributeType.SUPER_PREFIX, attackGen.generateIntValue());
		_fireDuration = fireGen.generateIntValue();
	}

	@Override
	public String getDisplayName() 
	{
		return "Flaming";
	}
	
	@Override
	public String getDescription()
	{
		return String.format("Every %d attacks gives Fire for %.1f seconds", getAttackLimit(), (_fireDuration / 20d));
	}
	
	@Override
	public void triggerAttack(Entity attacker, Entity defender)
	{
		if(attacker instanceof Player && ClansManager.getInstance().isSafe((Player) attacker)) return;
		if(defender instanceof Player && ClansManager.getInstance().isSafe((Player) defender)) return;
		if(attacker instanceof Player && ((Player)attacker).getGameMode().equals(GameMode.CREATIVE)) return;
		if (isTeammate(attacker, defender)) return;
		defender.setFireTicks(_fireDuration);
	}

}
