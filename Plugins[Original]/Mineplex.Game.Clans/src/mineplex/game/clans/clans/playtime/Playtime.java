package mineplex.game.clans.clans.playtime;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.MiniClientPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.stats.StatsManager;
import mineplex.core.task.TaskManager;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.ClansPlayerStats;
import mineplex.game.clans.clans.playtime.command.PlayTimeCommand;

public class Playtime extends MiniClientPlugin<PlayingClient>
{
	public enum Perm implements Permission
	{
		CLANS_TIME_COMMAND,
	}

	private StatsManager _statsManager;
	
	public Playtime(ClansManager clans, StatsManager statsManager)
	{
		super("Clans Play Time Tracker", clans.getPlugin());
		
		_statsManager = statsManager;
		
		addCommand(new PlayTimeCommand(_statsManager, this));
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.MOD.setPermission(Perm.CLANS_TIME_COMMAND, true, true);
	}
	
	@Override
	public void disable()
	{
		for (Player player : Bukkit.getOnlinePlayers())
		{
			save(player);
		}
	}
	
	// Seconds
	public long getPlaytime(Player player)
	{
		return _statsManager.Get(player).getStat(ClansPlayerStats.PLAY_TIME.id());
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Set(event.getPlayer(), addPlayer(event.getPlayer().getUniqueId()));
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		save(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerKicked(PlayerKickEvent event)
	{
		save(event.getPlayer());
	}
	
	private void save(Player player)
	{
		long timePlaying = getUnsavedPlaytime(player);

		_statsManager.incrementStat(player, ClansPlayerStats.PLAY_TIME.id(), timePlaying);
		
		// Increment main time in game as well
		_statsManager.incrementStat(player, "Global.TimeInGame", timePlaying);
		
		Get(player).StartTime = System.currentTimeMillis();
	}

	@Override
	protected PlayingClient addPlayer(UUID uuid)
	{
		return new PlayingClient(Bukkit.getPlayer(uuid), TaskManager.Instance);
	}
	
	// Seconds
	public long getUnsavedPlaytime(Player player)
	{
		return (System.currentTimeMillis() - Get(player).StartTime) / 1000;
	}
}