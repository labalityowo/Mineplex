package nautilus.game.arcade.game.games.moba.kit.hp;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.moba.Moba;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffectType;

public class HPManager implements Listener
{

	// Health per 5 seconds.
	private static final double HP5 = 0.66;
	private static final double HP_KILL_FACTOR = 0.25;

	private final Moba _host;

	public HPManager(Moba host)
	{
		_host = host;
	}

	@EventHandler
	public void regeneration(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC_05 || !_host.IsLive())
		{
			return;
		}

		for (Player player : _host.GetPlayers(true))
		{
			if (UtilPlayer.isSpectator(player))
			{
				continue;
			}

			MobaHPRegenEvent regenEvent = new MobaHPRegenEvent(player, null, HP5, true);
			UtilServer.CallEvent(regenEvent);

			if (regenEvent.isCancelled())
			{
				continue;
			}

			player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + regenEvent.getHealth()));
		}
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		Player killer = event.getEntity().getKiller();

		if (killer == null)
		{
			return;
		}

		killer.setHealth(Math.min(killer.getHealth() + killer.getMaxHealth() * HP_KILL_FACTOR, killer.getMaxHealth()));
	}

	@EventHandler
	public void preventHungerRegeneration(EntityRegainHealthEvent event)
	{
		if (event.getRegainReason() == RegainReason.SATIATED)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void preventRegenerationWither(MobaHPRegenEvent event)
	{
		if (event.getPlayer().hasPotionEffect(PotionEffectType.WITHER))
		{
			event.setCancelled(true);
		}
	}
}
