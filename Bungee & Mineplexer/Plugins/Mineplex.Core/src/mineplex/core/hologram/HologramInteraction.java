package mineplex.core.hologram;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

@FunctionalInterface
public interface HologramInteraction
{
	void onClick(Player player, ClickType clickType);
}
