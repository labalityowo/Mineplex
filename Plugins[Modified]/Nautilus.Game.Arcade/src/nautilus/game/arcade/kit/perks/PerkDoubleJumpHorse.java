package nautilus.game.arcade.kit.perks;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkDoubleJumpHorse extends Perk
{
	public PerkDoubleJumpHorse() 
	{
		super("Jumper", new String[] 
				{
				C.cYellow + "Tap Jump Twice" + C.cGray + " to " + C.cGreen + "Double Jump"
				});
	}

	@EventHandler
	public void FlightHop(PlayerToggleFlightEvent event)
	{
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		if (Manager.isSpectator(player))
			return;
		
		if (player.getGameMode() == GameMode.CREATIVE)
			return;
		
		event.setCancelled(true);
		player.setFlying(false);
		
		//Disable Flight
		player.setAllowFlight(false);
		
		//Velocity
		if (player.getVehicle() == null)
			UtilAction.velocity(player, player.getLocation().getDirection(), 1.2, true, 1.2, 0, 1.2, true);
		else
			UtilAction.velocity(player.getVehicle(), player.getLocation().getDirection(), 0.8, true, 0.8, 0, 0.8, true);

		//Sound
		player.getWorld().playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 0);
	}
	
	@EventHandler
	public void FlightUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (Manager.isSpectator(player))
				continue;
			
			if (!Kit.HasKit(player))
				continue;
			
			if (player.getVehicle() == null)
			{
				if (UtilEnt.isGrounded(player) || UtilBlock.solid(player.getLocation().getBlock().getRelative(BlockFace.DOWN)))
					player.setAllowFlight(true);
			}
			else 
			{
				if (UtilEnt.isGrounded(player.getVehicle()) || UtilBlock.solid(player.getVehicle().getLocation().getBlock().getRelative(BlockFace.DOWN)))
					player.setAllowFlight(true);
			}
		}
	}
}
