package mineplex.core.movement;

import mineplex.core.MiniClientPlugin;
import mineplex.core.common.util.UtilMath;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class Movement extends MiniClientPlugin<ClientMovement>
{
	public Movement(JavaPlugin plugin)
	{
		super("Movement", plugin);
	}
	
	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
		{
			for (Player cur : getPlugin().getServer().getOnlinePlayers())
			{	
				ClientMovement player = Get(cur);
				
				if (player.LastLocation != null)
					if (UtilMath.offset(player.LastLocation, cur.getLocation()) > 0)
						player.LastMovement = System.currentTimeMillis();
				
				player.LastLocation = cur.getLocation();
				
				//Save Grounded
				if (((CraftPlayer)cur).getHandle().onGround)
					player.LastGrounded = System.currentTimeMillis();
			}
		}
	}

	@Override
	protected ClientMovement addPlayer(UUID uuid)
	{
		return new ClientMovement();
	}
}
