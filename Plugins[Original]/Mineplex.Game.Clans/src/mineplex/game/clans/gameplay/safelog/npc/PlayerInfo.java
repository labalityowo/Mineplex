package mineplex.game.clans.gameplay.safelog.npc;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerInfo
{
	private String _playerName;
	private UUID _playerUuid;
	private Set<ItemStack> _items;
	
	public PlayerInfo(Player player)
	{
		_playerName = player.getName();
		_playerUuid = player.getUniqueId();
		_items = fetchItems(player.getInventory());
	}
	
	public void dropItems(Location location)
	{
		World world = location.getWorld();
		for (ItemStack item : _items)
		{
			world.dropItemNaturally(location, item);
		}
	}
	
	public String getPlayerName()
	{
		return _playerName;
	}
	
	public Set<ItemStack> getItems()
	{
		return _items;
	}
	
	public UUID getUniqueId()
	{
		return _playerUuid;
	}
	
	public String getPlayerUuid()
	{
		return _playerUuid.toString();
	}
	
	public Player getPlayer()
	{
		return Bukkit.getPlayerExact(_playerName);
	}
	
	private Set<ItemStack> fetchItems(PlayerInventory inventory)
	{
		Set<ItemStack> items = new HashSet<ItemStack>();
		
		addItems(items, inventory.getArmorContents());
		addItems(items, inventory.getContents());
		
		return items;
	}
	
	private void addItems(Set<ItemStack> items, ItemStack[] itemsToAdd)
	{
		for (ItemStack item : itemsToAdd)
		{
			if (item != null && item.getType() != Material.AIR)
			{
				items.add(item);
			}
		}
	}
}
