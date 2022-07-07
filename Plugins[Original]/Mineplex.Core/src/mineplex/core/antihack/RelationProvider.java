package mineplex.core.antihack;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface RelationProvider
{
	boolean canDamage(Player player, Entity target);
}
