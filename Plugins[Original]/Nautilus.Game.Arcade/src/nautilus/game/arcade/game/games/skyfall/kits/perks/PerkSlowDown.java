package nautilus.game.arcade.game.games.skyfall.kits.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.kit.Perk;

/**
 * Perk that reduces the
 * momentum of the using Player.
 *
 * @author xXVevzZXx
 */
public class PerkSlowDown extends Perk
{
	private int _cooldown;
	
	/**
	 * Standard Constructor for PerkSlowDown
	 * 
	 * @param name of the perk
	 * @param cooldown of the perk usage
	 */
	public PerkSlowDown(String name, int cooldown)
	{
		super(name, new String[]
		{
			C.cWhite + "Press " + C.cYellow + "shift" + C.cWhite + " to slow down"
		});
		
		_cooldown = cooldown;
	}
	
	@EventHandler
	public void slowDown(PlayerToggleSneakEvent event)
	{
		Player player = event.getPlayer();
		
		if (!hasPerk(player))
			return;
		
		if (!UtilPlayer.isGliding(player))
			return;
		
		if (player.isSneaking())
			return;
		
		if (!Recharge.Instance.use(player, GetName(), _cooldown * 1000, false, true))
			return;
		
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
		
		UtilAction.velocity(player, player.getEyeLocation().getDirection().multiply(-0.001));
		
		Vector vec = player.getEyeLocation().getDirection();
		double blocks = Manager.GetGame().getSpeed(player, 20);
		double mult = blocks/40;
		vec.multiply(mult);
		
		UtilAction.velocity(player, vec);
	}

}
