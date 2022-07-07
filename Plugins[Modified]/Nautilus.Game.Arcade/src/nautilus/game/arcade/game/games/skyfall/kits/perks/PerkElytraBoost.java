package nautilus.game.arcade.game.games.skyfall.kits.perks;

import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;

/**
 * Perk that allows people 
 * to double tap space to 
 * get a huge boost of
 * momentum.
 *
 * @author xXVevzZXx
 */
public class PerkElytraBoost extends Perk
{

	private int _cooldown;
	
	/**
	 * Standard Constructor for PerkElytraboost
	 * 
	 * @param name of the perk
	 * @param cooldown of the perk
	 */
	public PerkElytraBoost(String name, int cooldown)
	{
		super(name, new String[]
		{
			C.cYellow + "Double tap jump" + C.cWhite + " to use your " + C.cGreen + "Elytra Boost"
		});
		
		_cooldown = cooldown;
	}
	
	@EventHandler
	public void enableFlight(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (Manager.GetGame() == null)
			return;
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!hasPerk(player))
				continue;

			if (!UtilPlayer.isGliding(player) || !Recharge.Instance.usable(player, GetName()))
			{
				player.setAllowFlight(false);
				player.setFlying(false);
				return;
			}
			player.setAllowFlight(true);
		}
	}
	
	@EventHandler
	public void boost(PlayerToggleFlightEvent event)
	{
		Player player = event.getPlayer();
		
		if (!hasPerk(player))
			return;
		
		if (!Recharge.Instance.use(player, GetName(), _cooldown * 1000, false, true))
			return;
		
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
		
		player.setFlying(false);
		Manager.runSyncLater(new Runnable()
		{
			@Override
			public void run()
			{
				int i = 0;
				while (i < 10 && !UtilPlayer.isGliding(player))				
				{
					UtilPlayer.setGliding(player, true);
					i++;
				}
				
				Vector vec = player.getEyeLocation().getDirection();
				UtilAction.velocity(player, vec.multiply(2.5));
				
				UtilFirework.playFirework(player.getEyeLocation(), Type.BALL_LARGE, Color.BLUE, true, false);
			}
		}, 4);
	}
	
}
