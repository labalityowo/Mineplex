package nautilus.game.arcade.game;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface DebugCommandExecutor
{
	void execute(Player player, String[] args);
}
