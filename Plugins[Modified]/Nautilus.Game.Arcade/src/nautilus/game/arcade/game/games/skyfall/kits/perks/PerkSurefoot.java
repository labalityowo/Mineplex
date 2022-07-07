package nautilus.game.arcade.game.games.skyfall.kits.perks;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;

/**
 * Perk that reduces the momentum
 * of a Player while landing on
 * an Island with an Elytra.
 *
 * @author xXVevzZXx
 */
public class PerkSurefoot extends Perk
{
	private final static long MIN_AIRTIME = 2000;
	
	private HashMap<UUID, Long> _lastGround = new HashMap<>();

	private double _strength;
	
	/**
	 * Standard Constructor for PerkSurefoot
	 * 
	 * @param name of the perk
	 * @param strength of the reduced momentum
	 */
	public PerkSurefoot(String name, double strength)
	{
		super(name, new String[]
		{
			C.cGreen + "Reduced momentum" + C.cWhite + " when landing"
		});
		
		_strength = strength;
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (Manager.GetGame() == null)
			return;
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!hasPerk(player))
				continue;

			if (!UtilPlayer.isGliding(player))
			{
				_lastGround.put(player.getUniqueId(), System.currentTimeMillis());
				continue;
			}
			
			if (_lastGround.containsKey(player.getUniqueId()) && !UtilTime.elapsed(_lastGround.get(player.getUniqueId()), MIN_AIRTIME))
				continue;
			
			if (UtilPlayer.isInAir(player, 1, Material.STAINED_CLAY))
				continue;
			
			UtilAction.zeroVelocity(player);
			
			Vector vec = Manager.GetGame().getMovement(player, 5);
			
			UtilAction.velocity(player, vec.multiply(_strength/100));
			
			UtilPlayer.setGliding(player, false);
		}
	}
}
