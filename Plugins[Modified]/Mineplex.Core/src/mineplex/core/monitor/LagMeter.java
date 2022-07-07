package mineplex.core.monitor;

import java.util.HashSet;

import net.minecraft.server.v1_8_R3.MinecraftServer;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.Managers;
import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.communities.CommunityManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class LagMeter extends MiniPlugin
{
	public enum Perm implements Permission
	{
		VERSIONS_COMMAND,
		LAG_COMMAND,
		MONITOR_COMMAND,
	}
	
	private CoreClientManager _clientManager;
	private CommunityManager _communities;

    private long _lastRun = -1;
    private int _count;
    private double _ticksPerSecond;
    private double _ticksPerSecondAverage;
    private long _lastAverage;
    
    private HashSet<Player> _monitoring = new HashSet<Player>();

    public LagMeter(JavaPlugin plugin, CoreClientManager clientManager)
    {
    	super("LagMeter", plugin);
    	
    	_clientManager = clientManager;
        _lastRun = System.currentTimeMillis();
        _lastAverage = System.currentTimeMillis();
        
        generatePermissions();
    }
    
    private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.VERSIONS_COMMAND, true, true);
		PermissionGroup.QA.setPermission(Perm.VERSIONS_COMMAND, true, true);
		PermissionGroup.MOD.setPermission(Perm.LAG_COMMAND, true, true);
		PermissionGroup.MOD.setPermission(Perm.MONITOR_COMMAND, true, true);
	}

    @Override
    public void addCommands()
    {
    	addCommand(new VersionsCommand(this));
    }

    @EventHandler
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event)
    {
    	if (event.getMessage().trim().equalsIgnoreCase("/lag") && _clientManager.Get(event.getPlayer()).hasPermission(Perm.LAG_COMMAND))
    	{
    		sendUpdate(event.getPlayer());
    		event.setCancelled(true);
    		return;
    	}
    	if (event.getMessage().trim().equalsIgnoreCase("/monitor") && _clientManager.Get(event.getPlayer()).hasPermission(Perm.MONITOR_COMMAND))
    	{
    		if (_monitoring.contains(event.getPlayer()))
    		{
    			_monitoring.remove(event.getPlayer());
    		}
    		else
    		{
    			_monitoring.add(event.getPlayer());
    		}

    		event.setCancelled(true);
    		return;
    	}
    }
    
    @EventHandler
    public void playerQuit(PlayerQuitEvent event)
    {
    	_monitoring.remove(event.getPlayer());
    }
    
    @EventHandler
    public void update(UpdateEvent event)
    {
    	if (event.getType() != UpdateType.SEC)
    		return;

    	long now = System.currentTimeMillis();
    	_ticksPerSecond = 1000D / (now - _lastRun) * 20D;

    	sendUpdates();
        
        if (_count % 30 == 0)
        {
        	_ticksPerSecondAverage = 30000D / (now - _lastAverage) * 20D;
        	_lastAverage = now;
        }
        
        _lastRun = now;
        
        _count++;
    }
    
    public double getTicksPerSecond()
    {
        return _ticksPerSecond;
    }
    
    public double getRecentTicksPercentageAverage()
    {
        return _ticksPerSecondAverage;
    }
    
    private void sendUpdates()
    {
    	for (Player player : _monitoring)
    	{
    		sendUpdate(player);
    	}
    }

	private String getPrefix(double value)
	{
		if (value >= 18.0)
			return C.cGreen;
		else if (value >= 15)
			return C.cYellow;
		else
			return C.cRed;
	}
    
    private void sendUpdate(Player player)
    {
	    double[] tps = MinecraftServer.getServer().recentTps;
	    StringBuilder mcString = new StringBuilder();
	    for (int i = 0; i < tps.length; i++)
	    {
		    mcString.append(getPrefix(tps[i])).append((double) Math.round(tps[i] * 100.0D) / 100.0D);
		    if (i < tps.length - 1) mcString.append(C.cWhite).append(", ");
	    }

	    player.sendMessage(" ");
	    player.sendMessage(" ");
	    player.sendMessage(" ");
	    player.sendMessage(" ");
	    player.sendMessage(" ");
	    player.sendMessage(F.main(getName(), ChatColor.GRAY + "Live-------" + ChatColor.YELLOW + String.format("%.00f", _ticksPerSecond)));
	    player.sendMessage(F.main(getName(), ChatColor.GRAY + "Avg--------" + ChatColor.YELLOW + String.format("%.00f", _ticksPerSecondAverage * 20)));
	    player.sendMessage(F.main(getName(), ChatColor.YELLOW + "MC Timings (5,10,15 min avg)"));
	    player.sendMessage(F.main(getName(), ChatColor.GRAY + mcString.toString()));
	    player.sendMessage(F.main(getName(), ChatColor.YELLOW + "Memory"));
	    player.sendMessage(F.main(getName(), ChatColor.GRAY + "Free-------" + ChatColor.YELLOW + (Runtime.getRuntime().freeMemory() / 1048576) + "MB"));
	    player.sendMessage(F.main(getName(), ChatColor.GRAY + "Max--------" + ChatColor.YELLOW + (Runtime.getRuntime().maxMemory() / 1048576)) + "MB");

	    player.sendMessage(" ");
	    player.sendMessage(F.main(getName(), ChatColor.GRAY + "In Memory -----"));

	    player.sendMessage(F.main(getName(), ChatColor.YELLOW + String.valueOf(player.getWorld().getLoadedChunks().length) + ChatColor.GRAY + " chunks loaded"));

	    if (_communities == null)
	    {
			_communities = Managers.get(CommunityManager.class);
		}

	    player.sendMessage(F.main(getName(), ChatColor.YELLOW + String.valueOf(_communities.getCount()) + ChatColor.GRAY + " communities loaded"));
    }
}