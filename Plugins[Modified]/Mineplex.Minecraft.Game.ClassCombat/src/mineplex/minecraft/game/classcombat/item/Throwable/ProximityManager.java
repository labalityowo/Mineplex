package mineplex.minecraft.game.classcombat.item.Throwable;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.item.event.ProximityUseEvent;

public class ProximityManager implements Listener
{
	private int _proxyLimit = 6;

	private Map<Player, List<Entry<String, Entity>>> _proxyMap = new HashMap<>();
	
	public void setProxyLimit(int limit)
	{
		_proxyLimit = limit;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void proximityThrownEvent(ProximityUseEvent event)
	{
		//Don't bother if the proxy has been disallowed
		if (!event.getEntity().isValid())
			return;
		
		if (!_proxyMap.containsKey(event.getPlayer()))
			_proxyMap.put(event.getPlayer(), new ArrayList<Entry<String,Entity>>());
		
		List<Entry<String, Entity>> proxies = _proxyMap.get(event.getPlayer());
		
		//Store New
		proxies.add(new AbstractMap.SimpleEntry<String, Entity>(event.getItemType().GetName(), event.getEntity()));
		
		//Clean Excess
		while (proxies.size() > _proxyLimit)
		{
			Entry<String, Entity> entry = proxies.remove(0);
			UtilPlayer.message(event.getPlayer(), F.main("Game", "Your old " + entry.getKey() + " was removed. Limit of " + _proxyLimit + "."));	
			entry.getValue().remove();
		}
	}

	@EventHandler
	public void clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		clean(null, 0);
	}

	public void clean(Location loc, int radius) 
	{
		Iterator<Player> playerIter = _proxyMap.keySet().iterator();
		
		while (playerIter.hasNext())
		{
			Player player = playerIter.next();
			
			//Clean Offline Players
			if (!player.isOnline())
			{
				playerIter.remove();
				continue;
			}
			
			List<Entry<String,Entity>> proxies = _proxyMap.get(player);
			
			Iterator<Entry<String,Entity>> proxyIter = proxies.iterator();
			
			//Clean Dead Proxies
			while (proxyIter.hasNext())
			{
				Entry<String,Entity> proxy = proxyIter.next();
				
				if (!proxy.getValue().isValid() ||													//Dead
				(loc != null && UtilMath.offset(proxy.getValue().getLocation(), loc) < radius))		//Around Radius
				{
					proxy.getValue().remove();
					proxyIter.remove();
				}
			}
			
			//Clean Empty Entries
			if (proxies.isEmpty())
				playerIter.remove();
		}
	}
}