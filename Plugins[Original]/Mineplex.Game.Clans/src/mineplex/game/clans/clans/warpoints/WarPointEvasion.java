package mineplex.game.clans.clans.warpoints;

import com.google.common.collect.Maps;
import mineplex.core.MiniPlugin;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.event.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class WarPointEvasion extends MiniPlugin
{
    private HashMap<Chunk, Long> _chunkCooldown;
    private HashMap<UUID, Long> _playerCooldown;

    private final long COOLDOWN_TIME = 1000 * 60 * 10;

    public WarPointEvasion(JavaPlugin plugin) 
    {
        super("WP Evasion", plugin);
        
        _chunkCooldown = Maps.newHashMap();
        _playerCooldown = Maps.newHashMap();
    }

    @EventHandler
    public void updateCooldown(UpdateEvent event) 
    {
        if(!event.getType().equals(UpdateType.SEC)) return;

        for (Iterator<Chunk> chunkIterator = _chunkCooldown.keySet().iterator(); chunkIterator.hasNext();)
        {
        	Chunk chunk = chunkIterator.next();
        	
        	if (UtilTime.elapsed(_chunkCooldown.get(chunk), COOLDOWN_TIME))
        		chunkIterator.remove();
        }

        
        for (Iterator<UUID> uuidIterator = _playerCooldown.keySet().iterator(); uuidIterator.hasNext();)
        {
        	UUID uuid = uuidIterator.next();
        	
        	if (UtilTime.elapsed(_playerCooldown.get(uuid), COOLDOWN_TIME))
        	{
        		uuidIterator.remove();
        		
        		Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline())
                {
                    if(ClansManager.getInstance().getClan(player) == null) 
                    {
                    	player.sendMessage(F.main("Clans", "You can now create a clan."));
                    }
                }
        	}
        }
    }

    @EventHandler
    public void onClaim(PlayerPreClaimTerritoryEvent event) 
    {
        Chunk chunk = event.getClaimedChunk();
        
        if (_chunkCooldown.containsKey(chunk))
        {
            event.setCancelled(true);
            event.getClaimer().sendMessage(F.main("Clans", "You cannot claim this chunk for another " + UtilTime.convertString(COOLDOWN_TIME - (System.currentTimeMillis() -  _chunkCooldown.get(chunk)), 1, UtilTime.TimeUnit.MINUTES)));
        }
    }

    @EventHandler
    public void onunClaim(PlayerUnClaimTerritoryEvent event) 
    {
        _chunkCooldown.put(event.getUnClaimedChunk(), System.currentTimeMillis());
    }

    @EventHandler
    public void onClanDisband(ClanDisbandedEvent event) 
    {
        _playerCooldown.put(event.getDisbander().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onClanLeave(ClanLeaveEvent event) 
    {
        _playerCooldown.put(event.getPlayer().getUuid(), System.currentTimeMillis());
    }


    @EventHandler
    public void onClanCreate(ClanCreatedEvent event) 
    {
        if (event.getFounder() == null)
            return;

        if (_playerCooldown.containsKey(event.getFounder().getUniqueId())) 
        {
            event.setCancelled(true);
            event.getFounder().sendMessage(F.main("Clans", "You cannot create a clan for another " + UtilTime.convertString(COOLDOWN_TIME - (System.currentTimeMillis() - _playerCooldown.get(event.getFounder().getUniqueId())), 1, UtilTime.TimeUnit.MINUTES)));
        }
    }

    public void resetCooldown(UUID uuid)
    {
        _playerCooldown.remove(uuid);
    }
}
