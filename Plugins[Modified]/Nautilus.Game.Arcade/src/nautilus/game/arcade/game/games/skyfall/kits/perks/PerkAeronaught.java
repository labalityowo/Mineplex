package nautilus.game.arcade.game.games.skyfall.kits.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.kit.Perk;

/**
 * Perk that increases the attack damage
 * while the attacker is flying.
 *
 * @author xXVevzZXx
 */
public class PerkAeronaught extends Perk
{
	private double _strength;
	
	/**
	 * Standard Constructor for PerkAeronaught
	 * 
	 * @param name of the perk
	 * @param strength multiplicator of damage
	 */
	public PerkAeronaught(String name, double strength)
	{
		super(name, new String[]
		{
			C.cWhite + "Deal " + C.cGreen + "+45%" + C.cWhite + " damage while flying"
		});
		
		_strength = strength;
	}

	@EventHandler
	public void addDamage(CustomDamageEvent event)
	{
		if (Manager.GetGame() == null)
			return;
		
		Player player = event.GetDamagerPlayer(true);

		if (!hasPerk(player))
			return;

		if (!UtilPlayer.isGliding(player))
			return;

		double damage = event.GetDamageInitial() * _strength;
		event.AddMod(player.getName(), "Kit Damage", damage, true);
	}

}
