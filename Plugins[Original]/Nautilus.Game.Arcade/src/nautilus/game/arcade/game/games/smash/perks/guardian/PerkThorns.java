package nautilus.game.arcade.game.games.smash.perks.guardian;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.C;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.kit.Perk;

public class PerkThorns extends Perk
{

	private int _maxHealth;
	private double _damageDecrease;
	private double _knockbackDecrease;
	
	public PerkThorns()
	{
		super("Thorns", new String[] { C.cGray + "Takes 66% less damage and knockback from projectiles", C.cGray + "when under 10 health."});
	}

	@Override
	public void setupValues()
	{
		_maxHealth = getPerkInt("Max Health");
		_damageDecrease = getPerkPercentage("Damage Decrease");
		_knockbackDecrease = getPerkPercentage("Knockback Decrease");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void damage(CustomDamageEvent event)
	{
		if (event.GetDamagerPlayer(true) == null || event.GetCause() == DamageCause.ENTITY_ATTACK)
		{
			return;
		}
		
		Player player = event.GetDamageePlayer();
		
		
		if (!hasPerk(player))
		{
			return;
		}
				
		if (player.getHealth() < _maxHealth)
		{
			event.AddMult(GetName(), null, _damageDecrease, false);
			event.AddKnockback(GetName(), _knockbackDecrease);
		}
	}
}
