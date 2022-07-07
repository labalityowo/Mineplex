package nautilus.game.arcade.game.modules;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import mineplex.core.common.MinecraftVersion;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;

/**
 * This module functions as a checkpoint for any client connecting a game
 * server. Making sure they have the minimum client version for your game.
 * 
 * @see MinecraftVersion
 */
public class VersionModule extends Module
{
	private final MinecraftVersion _minecraftVersion;
	
	public VersionModule(MinecraftVersion minecraftVersion)
	{
		_minecraftVersion = minecraftVersion;
		
		for (Player player : UtilServer.getPlayers())
		{
			if (UtilPlayer.getVersion(player) != _minecraftVersion)
			{
				UtilPlayer.message(player, C.cGold + C.Bold + "Please use Minecraft " + _minecraftVersion.friendlyName() + " or newer to play this game!");
				Portal.getInstance().sendPlayerToGenericServer(player, GenericServer.HUB, Intent.KICK);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event)
	{
		if (MinecraftVersion.fromInt(event.spigot().getVersion()) != _minecraftVersion)
		{
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, C.cGold + C.Bold + "Please use Minecraft " + _minecraftVersion.friendlyName() + " or newer to play this game!");
		}
	}
}