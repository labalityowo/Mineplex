package nautilus.game.arcade.game.modules;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/*
 * This module will prevent spectators from picking up xp orbs
 *
 * fixme: there are still some graphical glitches
 */
public class AntiExpOrbModule extends Module
{
	private Set<UUID> _playersLastUpdated = new HashSet<>();

	@EventHandler
	public void on(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Game game = getGame();

		for (Player player : Bukkit.getOnlinePlayers())
		{
			boolean disallow = game.GetState() != Game.GameState.Live || !game.IsAlive(player);

			if (disallow)
			{
				updateTimer(player, 86000);
				_playersLastUpdated.add(player.getUniqueId());
			}
			else
			{
				if (_playersLastUpdated.remove(player.getUniqueId()))
				{
					updateTimer(player, 2);
				}
			}
		}
	}

	@EventHandler
	public void on(EntityTargetLivingEntityEvent event)
	{
		if (event.getEntity() instanceof ExperienceOrb)
		{
			if (event.getTarget() instanceof Player)
			{
				Player player = (Player) event.getTarget();

				Game game = getGame();

				if (game.GetState() != Game.GameState.Live || !game.IsAlive(player))
				{
					// Setting the target to null sets the actual target in EntityExperienceOrb to null
					event.setTarget(null);
					// Cancelling the event is a sanity check
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void on(PlayerQuitEvent event)
	{
		_playersLastUpdated.remove(event.getPlayer().getUniqueId());
	}

	@Override
	public void cleanup()
	{
		for (UUID uuid : _playersLastUpdated)
		{
			Player player = Bukkit.getPlayer(uuid);
			if (player != null)
			{
				updateTimer(player, 0);
			}
		}
		_playersLastUpdated.clear();
	}

	private void updateTimer(Player player, int ticksToWait)
	{
		((CraftPlayer) player).getHandle().bp = ticksToWait;
	}
}
