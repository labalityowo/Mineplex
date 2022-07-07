package mineplex.core.resourcepack;

import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Objects;

import mineplex.core.MiniPlugin;
import mineplex.core.common.MinecraftVersion;
import mineplex.core.common.Pair;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;
import mineplex.core.resourcepack.redis.RedisUnloadResPack;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommandManager;

public class ResourcePackManager extends MiniPlugin implements CommandCallback<RedisUnloadResPack>
{
	private Pair<MinecraftVersion, String>[] _resourcePackUrls;
	private boolean _resourcePackRequired;
	private NautHashMap<String, Boolean> _resourcePackUsers = new NautHashMap<>();
	private NautHashMap<String, Long> _resourcePackNoResponse = new NautHashMap<>();
	private final Portal _portal;

	public ResourcePackManager(JavaPlugin plugin, Portal portal)
	{
		super("Resource Pack Manager", plugin);

		_portal = portal;

		ServerCommandManager.getInstance().registerCommandType("RedisUnloadResPack", RedisUnloadResPack.class, this);
	}
	
	public void setPlayerPack(Player player)
	{
		MinecraftVersion version = UtilPlayer.getVersion(player);
		
		if (_resourcePackUrls == null || _resourcePackUrls.length == 0)
			return;
		
		int lastOrdinal = -1;
		String pack = null;
		
		for (Pair<MinecraftVersion, String> entry : _resourcePackUrls)
		{
			if (lastOrdinal != -1 && entry.getLeft().ordinal() > lastOrdinal)
			{
				continue;
			}
			
			if (version.atOrAbove(entry.getLeft()))
			{
				lastOrdinal = entry.getLeft().ordinal();
				pack = entry.getRight();
			}
		}
		
		if (pack != null)
		{
			player.setResourcePack(pack);
		}
		else
		{
			player.setResourcePack(_resourcePackUrls[0].getRight());
		}
	}

	@EventHandler
	public void ResourcePackJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		if (_resourcePackUrls == null)
		{
			return;
		}

		if (_resourcePackRequired)
		{
			_resourcePackNoResponse.put(player.getName(), System.currentTimeMillis());
		}

		_resourcePackUsers.put(player.getName(), false);
		
		setPlayerPack(player);
	}

	@EventHandler
	public void onSecond(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}
		
		Iterator<Entry<String, Long>> itel = _resourcePackNoResponse.entrySet().iterator();

		while (itel.hasNext())
		{
			Entry<String, Long> entry = itel.next();

			if (!UtilTime.elapsed(entry.getValue(), 20000))
			{
				continue;
			}

			Player player = Bukkit.getPlayerExact(entry.getKey());

			if (player == null)
			{
				itel.remove();
				continue;
			}

			// Send it again, enforce it!
			_resourcePackNoResponse.put(player.getName(), System.currentTimeMillis());

			setPlayerPack(player);
		}
	}

	@EventHandler
	public void onResourcePackStatus(PlayerResourcePackStatusEvent event)
	{
		if (_resourcePackUrls == null)
		{
			return;
		}

		Player player = event.getPlayer();

		if (_resourcePackRequired)
		{
			if (event.getStatus() == Status.ACCEPTED)
			{
				_resourcePackNoResponse.remove(player.getName());
			}
			else if (event.getStatus() == Status.DECLINED)
			{
				_resourcePackNoResponse.remove(player.getName());

				UtilPlayer.message(player, "  ");
				JsonMessage message = new JsonMessage("").color("gold").bold()
						.extra("You need to accept the resource pack!\n" + "Click me for instructions on how to fix this!")

						.click(ClickEvent.OPEN_URL,

								"http://mineplex.com/forums/m/11929946/viewthread/21554536-wizards-resource-pack-help");

				message.sendToPlayer(player);
				UtilPlayer.message(player, "  ");

				returnHubNoResPack(player);
			}
			else if (event.getStatus() == Status.FAILED_DOWNLOAD)
			{
				_resourcePackNoResponse.remove(player.getName());

				returnHubNoResPack(player, "Failed to download resource pack!");

				return;
			}
		}

		if (event.getStatus() == Status.ACCEPTED || event.getStatus() == Status.SUCCESSFULLY_LOADED)
		{
			_resourcePackUsers.put(player.getName(), true);
		}
		else
		{
			_resourcePackUsers.remove(player.getName());
		}
	}

	@EventHandler
	public void ResourcePackQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		if (!_resourcePackUsers.containsKey(player.getName()) || !_resourcePackUsers.get(player.getName()))
		{
			return;
		}

		new RedisUnloadResPack(player.getName()).publish();

		_resourcePackUsers.remove(player.getName());
	}

	private void returnHubNoResPack(Player player)
	{
		player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 10f, 1f);
		_portal.sendPlayerToGenericServer(player, GenericServer.HUB, Intent.KICK);
	}

	private void returnHubNoResPack(Player player, String message)
	{
		UtilPlayer.message(player, "  ");
		UtilPlayer.message(player, C.cGold + C.Bold + message);
		UtilPlayer.message(player, "  ");

		returnHubNoResPack(player);
	}

	public void setResourcePack(Pair<MinecraftVersion, String>[] resourcePack, boolean forceResourcePack)
	{
		if (Objects.equal(resourcePack, _resourcePackUrls) && forceResourcePack == _resourcePackRequired)
		{
			return;
		}

		_resourcePackNoResponse.clear();
		_resourcePackUsers.clear();
		_resourcePackUrls = resourcePack == null || (resourcePack.length == 0) ? null : resourcePack;
		_resourcePackRequired = forceResourcePack;

		if (_resourcePackUrls == null || _resourcePackUrls.length == 0)
		{
			_resourcePackRequired = false;

			for (Player player : Bukkit.getOnlinePlayers())
			{
				//player.setResourcePack("http://file.mineplex.com/ResReset.zip");
				player.setResourcePack("http://198.20.72.74/ResReset.zip");
			}
		}
		else
		{
			for (Player player : Bukkit.getOnlinePlayers())
			{
				if (_resourcePackRequired)
				{
					_resourcePackNoResponse.put(player.getName(), System.currentTimeMillis());
				}

				_resourcePackUsers.put(player.getName(), false);
				
				setPlayerPack(player);
			}
		}
	}

	@Override
	public void run(RedisUnloadResPack command)
	{
		Player player = Bukkit.getPlayerExact(command.getPlayer());
		
		if (player == null)
		{
			return;
		}
		
		if (_resourcePackUsers.containsKey(player.getName()))
		{
			return;
		}
		
		//player.setResourcePack("http://file.mineplex.com/ResReset.zip");
		player.setResourcePack("http://198.20.72.74/ResReset.zip");
	}
}