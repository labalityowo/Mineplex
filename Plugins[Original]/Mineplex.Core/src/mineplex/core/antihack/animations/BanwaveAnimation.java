package mineplex.core.antihack.animations;

import mineplex.core.antihack.AntiHack;
import org.bukkit.entity.Player;

public interface BanwaveAnimation
{
	void run(Player player, Runnable after);
}
