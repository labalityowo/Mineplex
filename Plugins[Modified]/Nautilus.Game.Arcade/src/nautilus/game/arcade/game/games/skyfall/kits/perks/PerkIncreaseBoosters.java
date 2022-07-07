package nautilus.game.arcade.game.games.skyfall.kits.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;

import nautilus.game.arcade.game.games.skyfall.BoosterRing;
import nautilus.game.arcade.game.games.skyfall.PlayerBoostRingEvent;
import nautilus.game.arcade.kit.Perk;

/**
 * Perk that increases the
 * momentum which Players get 
 * from {@link BoosterRing}.
 *
 * @author xXVevzZXx
 */
public class PerkIncreaseBoosters extends Perk
{
	private double _strength;
	
	public PerkIncreaseBoosters(String name, double strength)
	{
		super(name, new String[]
		{
			C.cWhite + "Boost rings give " + C.cGreen + "+10%" + C.cWhite + " extra boost"
		});
		_strength = strength;
	}
	
	@EventHandler
	public void increaseBoost(PlayerBoostRingEvent event)
	{
		Player player = event.getPlayer();
		
		if (!hasPerk(player))
			return;
		
		event.multiplyStrength(_strength);
	}
}
