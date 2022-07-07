package nautilus.game.arcade.game.games.skyfall.kits.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

/**
 * Perk that allows Players to
 * deal a increased amount of
 * knockback to others Players
 * while flying.
 *
 * @author xXVevzZXx
 */
public class PerkElytraKnockback extends Perk
{	
	/**
	 * Standard Constructor for PerkElytraKnockback
	 * 
	 * @param name of the perk
	 */
	public PerkElytraKnockback(String name)
	{
		super(name, new String[]
		{
			C.cYellow + "Hitting Players " + C.cWhite + "will deal " + C.cGreen + "+100%" + C.cWhite + " Knockback"
		});
	}
	
	@EventHandler
	public void addKnockback(CustomDamageEvent event)
	{
		if (Manager.GetGame() == null)
			return;
		
		Player player = event.GetDamagerPlayer(true);
		
		if (!hasPerk(player))
			return;
		
		if (!UtilPlayer.isGliding(player))
			return;
		
		if (Manager.GetGame().TeamMode && Manager.GetGame().GetTeam(player) == Manager.GetGame().GetTeam(event.GetDamageePlayer()))
			return;
		
		event.AddKnockback("Kit Effect", event.getKnockbackValue()*2);
	}

}
