package nautilus.game.arcade.game.games.moba.overtime;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.boss.MobaBoss;
import nautilus.game.arcade.game.games.moba.boss.wither.WitherBoss;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.concurrent.TimeUnit;

public class OvertimeManager implements Listener
{

	private static final long OVERTIME = TimeUnit.MINUTES.toMillis(15);

	private final Moba _host;
	private boolean _enabled;
	private boolean _overtime;

	public OvertimeManager(Moba host)
	{
		_host = host;
		_enabled = true;
	}

	public void disableOvertime()
	{
		_enabled = false;
	}

	@EventHandler
	public void updateOvertime(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !_host.IsLive() || !UtilTime.elapsed(_host.GetStateTime(), OVERTIME) || _overtime || !_enabled)
		{
			return;
		}

		_overtime = true;
		UtilTextMiddle.display(C.cRedB + "OVERTIME", "Victory or Death, Withers are moving to the center!");
		_host.Announce(F.main("Game", "Victory or Death, Withers are moving to the center!"), false);

		for (WitherBoss boss : _host.getBossManager().getWitherBosses())
		{
			boss.setDamageable(true);
		}

		for (Player player : Bukkit.getOnlinePlayers())
		{
			player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1.2F);
		}
	}

	public boolean isOvertime()
	{
		return _enabled && _overtime;
	}
}
