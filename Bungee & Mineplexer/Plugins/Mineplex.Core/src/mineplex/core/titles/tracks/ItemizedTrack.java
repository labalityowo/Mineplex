package mineplex.core.titles.tracks;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.inventory.InventoryManager;

public class ItemizedTrack extends Track
{
	private final InventoryManager _inventoryManager = Managers.require(InventoryManager.class);

	public ItemizedTrack(String trackId, String shortName, String description, boolean hideIfUnowned)
	{
		super(trackId, shortName, description, hideIfUnowned);
		special();
	}

	public ItemizedTrack(String trackId, ChatColor color, String shortName, String longName, String description)
	{
		super(trackId, color, shortName, longName, description);
		special();
	}

	public ItemizedTrack(String trackId, ChatColor color, String shortName, String longName, String description, boolean hideIfUnowned)
	{
		super(trackId, color, shortName, longName, description, hideIfUnowned);
		special();
	}

	public boolean owns(Player player) {
		return _inventoryManager.Get(player).getItemCount("track." + getId()) > 0;
	}
}
