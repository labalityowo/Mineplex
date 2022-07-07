package mineplex.gemhunters.death.quitnpc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.gemhunters.death.event.QuitNPCDespawnEvent;
import mineplex.gemhunters.death.event.QuitNPCSpawnEvent;
import mineplex.gemhunters.economy.CashOutModule;

@ReflectivelyCreateMiniPlugin
public class QuitNPCModule extends MiniPlugin
{
	private static final long LOG_OUT_TIME = TimeUnit.SECONDS.toMillis(60);

	private final CashOutModule _cashOut;

	private final Map<UUID, QuitNPC> _npcs;
	
	private final String _serverName;
	private final QuitNPCRepository _repo;

	private QuitNPCModule()
	{
		super("Quit NPC");

		_cashOut = require(CashOutModule.class);

		_npcs = new HashMap<>();
		_serverName = UtilServer.getRegion().name() + ":" + UtilServer.getServerName();
		_repo = new QuitNPCRepository();
	}
	
	public void spawnNpc(Player player)
	{
		log("Attempting to spawn quit npc for " + player.getName());
	
		if (player.getGameMode() != GameMode.SURVIVAL)
		{
			log(player.getName() + " was not in survival");
			return;
		}
		
		if (_cashOut.isAboutToCashOut(player))
		{
			log(player.getName() + " was cashing out");
			return;
		}
		
		// Event
		QuitNPCSpawnEvent event = new QuitNPCSpawnEvent(player);
		
		UtilServer.CallEvent(event);
		
		if (event.isCancelled())
		{
			log("Event cancelled for " + player.getName());
			return;
		}
		
		_npcs.put(player.getUniqueId(), new QuitNPC(player, LOG_OUT_TIME));
		_repo.insertNpc(player.getUniqueId(), _serverName);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void playerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		
		if (UtilPlayer.isSpectator(player))
		{
			return;
		}
		
		spawnNpc(player);
	}
	
	@EventHandler
	public void npcDespawn(QuitNPCDespawnEvent event)
	{
		log("Despawning npc for " + _npcs.remove(event.getNpc().getUniqueId()).getName());
		_repo.deleteNpc(event.getNpc().getUniqueId());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin(AsyncPlayerPreLoginEvent event)
	{
		try
		{
			String npcServer = _repo.loadNpcServer(event.getUniqueId()).join();
			if (npcServer == null || npcServer.isEmpty())
			{
				return;
			}
			if (npcServer.equals(_serverName))
			{
				return;
			}
			
			event.disallow(Result.KICK_OTHER, C.cRed + "You have a combat logger alive on " + npcServer + "! Either wait for it to despawn or join that server directly!");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public QuitNPC getNPC(Player player)
	{
		return _npcs.get(player.getUniqueId());
	}
	
	public boolean hasNPC(Player player)
	{
		return _npcs.containsKey(player.getUniqueId());
	}
}