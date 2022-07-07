package nautilus.game.arcade.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilTime;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.PlayerPrepareTeleportEvent;
import nautilus.game.arcade.kit.Kit;

/**
 * Wrapper for team games to implement for simple universal use of Elo and Ranked methods.
 */
public abstract class RankedTeamGame extends TeamGame
{
	public int MaxPlayers = -1;
	public int MaxPerTeam = -1;
	
	public RankedTeamGame(ArcadeManager manager, GameType gameType, Kit[] kits, String[] gameDesc)
	{
		super(manager, gameType, kits, gameDesc);
		
		this.EloRanking = true;
		this.SpectatorAllowed = false;
	}
	
	private int getAccountId(UUID uuid)
	{
		final List<Integer> id = new ArrayList<>();
		Manager.GetClients().getRepository().getAccountId(uuid, data ->
		{
			id.add(data);
		});
		
		if (id.isEmpty())
		{
			id.add(0);
		}
		
		return id.get(0);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTeleport(PlayerPrepareTeleportEvent event)
	{
		if (GetTeam(event.GetPlayer()).GetPlayers(true).size() > MaxPerTeam)
		{
			if (MaxPerTeam != -1)
			{
				disqualify(event.GetPlayer());
				event.GetPlayer().sendMessage(F.main("Game", "This game has reached maximum capacity!"));
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin(AsyncPlayerPreLoginEvent event)
	{
		long expiry = Manager.getEloManager().getRankBanExpiry(getAccountId(event.getUniqueId()));
		if (Manager.getEloManager().checkRankBanned(getAccountId(event.getUniqueId())))
		{
			String time = UtilTime.MakeStr(expiry - System.currentTimeMillis());
			String message = ChatColor.RED + "You cannot join a ranked match yet because you left your last one early! Your ban expires in " + time;
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, message);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin(PlayerLoginEvent event)
	{
		if (MaxPlayers == -1)
		{
			return;
		}
		if (Bukkit.getOnlinePlayers().size() < MaxPlayers)
		{
			return;
		}
		String message = ChatColor.RED + "This game has reached maximum capacity!";
		if (Manager.GetClients().Get(event.getPlayer().getUniqueId()).hasPermission(ArcadeManager.Perm.INFORM_RANKED_MODERATION_POTENTIAL))
		{
			if (InProgress())
			{
				return;
			}
			else
			{
				message += " You may join for moderation purposes after the game has started!";
			}
		}
		event.disallow(Result.KICK_OTHER, message);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onQuit(PlayerQuitEvent event)
	{
		if (InProgress())
		{
			if (Manager.hasBeenPlaying(event.getPlayer()))
			{
				Manager.getEloManager().banFromRanked(getAccountId(event.getPlayer().getUniqueId()));
			}
		}
	}
}