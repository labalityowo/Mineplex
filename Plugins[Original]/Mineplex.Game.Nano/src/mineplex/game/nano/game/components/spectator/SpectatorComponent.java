package mineplex.game.nano.game.components.spectator;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface SpectatorComponent
{

	void addSpectator(Player player, boolean teleport, boolean out);

	Location getSpectatorLocation();

}
