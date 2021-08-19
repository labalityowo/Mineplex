package nautilus.game.arcade.stats;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;

public class WinWithoutWearingArmorStatTracker extends StatTracker<Game>
{
	private final Set<String> _wearing = new HashSet<String>();

	public WinWithoutWearingArmorStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.getType() == UpdateType.TICK)
		{
			for (Player player : getGame().GetPlayers(true))
			{
				if (_wearing.contains(player.getUniqueId().toString()))
				{
					continue;
				}

				for (ItemStack armor : player.getInventory().getArmorContents())
				{
					if (armor != null && armor.getType() != Material.AIR && armor.getType() != Material.ELYTRA)
					{
						_wearing.add(player.getUniqueId().toString());
						break;
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == Game.GameState.End)
		{
			List<Player> winners = getGame().getWinners();

			if (winners != null)
			{
				for (Player winner : winners)
				{
					if (!_wearing.contains(winner.getUniqueId().toString()))
					{
						addStat(winner, "NoArmor", 1, true, false);
					}
				}
			}
		}
	}
}
