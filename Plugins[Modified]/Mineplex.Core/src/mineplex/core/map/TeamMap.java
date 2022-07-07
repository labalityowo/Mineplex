package mineplex.core.map;

import java.util.ArrayList;
import java.util.List;

import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;

public class TeamMap
{
	List<Player> _players;
	final ItemStack _map;
	
	public TeamMap(List<Player> display, Location loc, Scale scale)
	{
		_players = display;
		MapView view = Bukkit.createMap(loc.getWorld());
		view.setCenterX(loc.getBlockX());
		view.setCenterZ(loc.getBlockZ());
		view.setScale(scale);
		
		_map = new ItemStack(Material.MAP);
		_map.setDurability(view.getId());
	}
	
	public void update(Player... forceRemove)
	{
		if (forceRemove != null)
		{
			for (Player remove : forceRemove)
			{
				_players.remove(remove);
			}
		}
		
		List<Player> confirmed = new ArrayList<Player>();
		
		for (Player check : _players)
		{
			if (!UtilPlayer.isOnline(check.getName()))
				continue;
			
			if (UtilPlayer.isSpectator(check))
				continue;
			
			confirmed.add(check);
		}
		
		_players.clear();
		for (Player add : confirmed)
			_players.add(add);
	}
	
	public void giveMaps()
	{
		update(null);
		
		for (Player player : _players)
		{
			UtilInv.insert(player, _map);
			UtilInv.Update(player);
		}
	}
	
	public void giveMap(Player player)
	{
		giveMap(player, true, "");
	}
	
	public void giveMap(Player player, boolean add, String displayName, String... lore)
	{
		if (!_players.contains(player))
		{
			if (add)
			{
				_players.add(player);
			}
		}
		
		ItemStack map = _map.clone();
		ItemMeta im = map.getItemMeta();
		if (!displayName.equalsIgnoreCase(""))
			im.setDisplayName(displayName);
		
		List<String> lores = new ArrayList<String>();
		for (String s : lore)
			lores.add(s);
		
		if (!lores.isEmpty())
			im.setLore(lores);
		
		map.setItemMeta(im);
		
		UtilInv.insert(player, map);
		UtilInv.Update(player);
	}
}
