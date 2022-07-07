package mineplex.game.clans.gameplay.safelog;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.Managers;
import mineplex.core.MiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.ClanTips.TipType;
import mineplex.game.clans.clans.freeze.ClansFreezeManager;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.worldevent.raid.RaidManager;
import mineplex.game.clans.gameplay.safelog.npc.NPCManager;
import mineplex.game.clans.restart.RestartManager;

public class SafeLog extends MiniPlugin
{
	public static final long REJOIN_TIME = 60000;
	
	// Track Offline Players
	private ClansManager _clansManager;
	
	public SafeLog(JavaPlugin plugin, ClansManager clansManager)
	{
		super("SafeLog", plugin);
		
		_clansManager = clansManager;
		
		new File(clansManager.UserDataDir).mkdir();
	}
	
	@Override
	public void disable()
	{
		NPCManager.getInstance().disable();
	}
	
	public void onPlayerQuit(Player player)
	{
		boolean isSafeLog = false;
		
		if (_clansManager.getClanUtility().isSafe(player))
		{
			isSafeLog = true;
		}
		
		if (_flying.contains(player.getName()))
		{
			_flying.remove(player.getName());
			return;
		}
		
		if (Managers.get(ClansFreezeManager.class).isFrozen(player))
		{
			isSafeLog = true;
		}
		
		if (Managers.get(ClansFreezeManager.class).isPanicking(player))
		{
			isSafeLog = true;
		}
		
		if (Managers.get(RestartManager.class).isRestarting())
		{
			isSafeLog = true;
		}
		
		if (Managers.get(RaidManager.class).isInRaid(player.getLocation()))
		{
			isSafeLog = true;
		}
		
		if (_clansManager.hasTimer(player))
		{
			isSafeLog = true;
		}
		
		if (_clansManager.getIncognitoManager().Get(player).Status)
		{
			isSafeLog = true;
		}
		
		if (!isSafeLog)
		{
			NPCManager.getInstance().spawnLogoutNpc(player);
		}
	}
	
	public void onPlayerJoin(final Player player)
	{
		// Despawn the player's logout NPC if they have one existing on login
		if (NPCManager.getInstance().hasLogoutNpc(player))
		{
			NPCManager.getInstance().despawnLogoutNpc(player);
		}
		
		File deathFile = new File(_clansManager.UserDataDir + String.format("DEATH_%s.dat", player.getUniqueId().toString()));
		
		if (deathFile.exists())
		{
			try
			{
				DataInputStream stream = new DataInputStream(new FileInputStream(deathFile));
				
				final long time = stream.readLong();
				final int length = stream.readInt();
				final StringBuilder killerName = new StringBuilder();
				
				for (int i = 0; i < length; i++)
				{
					killerName.append((char) stream.readByte());
				}
				
				stream.close();
				
				UtilServer.getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () ->
				{
					_clansManager.ClanTips.displayTip(TipType.NPC_RIPPARONI, player);
					UtilPlayer.message(player, F.main("SafeLog", "You were killed by " + F.elem(killerName) + " when you logged out! This happened about " + F.time(UtilTime.MakeStr(System.currentTimeMillis() - time))) + " ago.");
					UtilTextMiddle.display("Offline Death", "Log out in a safer place next time!", 15, 80, 40, player);
				}, 15);
				
				deathFile.delete();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private List<String> _flying = new ArrayList<>();

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
  		onPlayerQuit(event.getPlayer());
	}
			
	@EventHandler(priority = EventPriority.HIGHEST)
	public void flyCheck(PlayerKickEvent event)
	{
		if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getItemMeta() != null && (C.cGold + "Wind Blade").equals(event.getPlayer().getItemInHand().getItemMeta().getDisplayName()))
			if (event.getReason().contains("flying is not enabled"))
				_flying.add(event.getPlayer().getName());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerKicked(PlayerKickEvent event)
	{
		if (event.isCancelled()) return;
		if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getItemMeta() != null && (C.cGold + "Wind Blade").equals(event.getPlayer().getItemInHand().getItemMeta().getDisplayName()))
			if (event.getReason().contains("flying is not enabled"))
				return;
		
		onPlayerQuit(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		onPlayerJoin(event.getPlayer());
	}
}