package nautilus.game.arcade.game.games.skyfall.kits.perks;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.kit.Perk;

/**
 * Perk that removes attacked
 * Players Elytra for the
 * specified amount of
 * time in milliseconds.
 *
 * @author xXVevzZXx
 */
public class PerkRemoveElytra extends Perk
{

	private HashMap<UUID, Long> _disabled = new HashMap<>();
	
	private long _duration;
	
	/**
	 * Standard Constructor for PerkRemoveElytra
	 * 
	 * @param name of the perk
	 * @param duration of the removal
	 */
	public PerkRemoveElytra(String name, long duration)
	{
		super(name, new String[]
		{
			C.cWhite + "Dealing damage to flying enemies " + C.cGreen + "disables elytra" + C.cWhite + " for " + C.cGreen + (duration/1000) + " seconds"
		});
		
		_duration = duration;
	}
	
	@EventHandler
	public void stunEnemy(CustomDamageEvent event)
	{	
		if (Manager.GetGame() == null)
			return;
		
		Player player = event.GetDamagerPlayer(true);
		
		if (!hasPerk(player))
			return;
		
		if (Manager.GetGame().TeamMode && Manager.GetGame().GetTeam(player) == Manager.GetGame().GetTeam(event.GetDamageePlayer()))
			return;
		
		Recharge.Instance.useForce(event.GetDamageePlayer(), "Elytra Removal", _duration, true);
		_disabled.put(event.GetDamageePlayer().getUniqueId(), System.currentTimeMillis() + _duration);
	}
	
	@EventHandler
	public void updateElytras(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (Manager.GetGame() == null)
			return;
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (_disabled.containsKey(player.getUniqueId()))
			{
				if (System.currentTimeMillis() > _disabled.get(player.getUniqueId()))
				{
					player.getInventory().setChestplate(new ItemStack(Material.ELYTRA));
					_disabled.remove(player.getUniqueId());
				}
				else
				{
					if (player.getInventory().getChestplate() != null)
					{
						UtilPlayer.message(player, F.main("Game", C.cRed + "Your Elytra is disabled!"));
						player.getInventory().setChestplate(null);
					}
				}
			}
		}
	}

}
