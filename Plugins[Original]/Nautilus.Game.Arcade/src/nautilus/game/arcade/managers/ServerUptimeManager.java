package nautilus.game.arcade.managers;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.portal.Intent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;

/**
 * ServerUptimeManager
 *
 * @author xXVevzZXx
 */
public class ServerUptimeManager implements Listener
{
	private static final long SHUTDOWN_DELAY = 10000;
	private static final long KICK_AFTER_SENDING = 20*10; // 10 Seconds

	public ArcadeManager Manager;

	private HashMap<Date, Date> _dates;

	private long _informed;

	private boolean _closeServer;

	private boolean _enabled;

	private boolean _closed;

	public ServerUptimeManager(ArcadeManager manager)
	{
		Manager = manager;

		_dates = new HashMap<>();
		_enabled = true;
		updateDates();

		Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
	}

	@EventHandler
	public void updateDates(UpdateEvent event)
	{
		if (!_enabled)
			return;

		if (event.getType() != UpdateType.MIN_30)
			return;
		
		if (isOpen())
			return;

		updateDates();
	}

	@EventHandler
	public void changeState(UpdateEvent event)
	{
		if (!_enabled)
			return;

		if (event.getType() != UpdateType.SLOWEST)
			return;

		if (isOpen())
		{
			if (!_closed)
				return;

			System.out.println("Opening server!");
			UtilServer.getServer().setWhitelist(false);
			_closed = false;
		}
		else
		{
			if (_closed)
				return;

			System.out.println("Closing server!");
			_informed = System.currentTimeMillis();
			_closeServer = true;
			UtilServer.getServer().setWhitelist(true);

			UtilServer.broadcast(F.main("Server", "Servertime expired! I will shutdown soon."));
		}
	}

	@EventHandler
	public void closeServer(UpdateEvent event)
	{
		if (!_enabled)
			return;

		if (event.getType() != UpdateType.SLOWER)
			return;

		if (!_closeServer)
			return;

		if (Manager.GetGame() == null)
			return;

		if (!Manager.GetGame().inLobby())
			return;

		if (!UtilTime.elapsed(Manager.GetGame().GetStateTime(), SHUTDOWN_DELAY)
				|| !UtilTime.elapsed(_informed, SHUTDOWN_DELAY))
			return;

		if (_closed)
			return;

		_closeServer = false;

		for (Player player : UtilServer.getPlayers())
		{
			Manager.GetPortal().sendToHub(player, "Servertime has expired!", Intent.KICK);
		}

		Manager.runSyncLater(new Runnable()
		{
			@Override
			public void run()
			{
				while (UtilServer.getPlayers().length > 0)
				{
					UtilServer.getPlayers()[0].kickPlayer(F.main("Server", "Servertime has expired!"));
				}
			}
		}, KICK_AFTER_SENDING);
		
		_closed = true;

	}

	public boolean isOpen()
	{
		for (Date start : _dates.keySet())
		{
			Date end = _dates.get(start);
			Date current = new Date(System.currentTimeMillis());
			
			if (current.before(end) && current.after(start))
				return true;
		}
		return false;
	}

	public void updateDates()
	{
		updateDates(Manager.GetServerConfig().Uptimes);
	}

	public void updateDates(String uptimes)
	{
		System.out.println(uptimes);
		if (Manager.GetServerConfig().Uptimes.isEmpty())
		{
			_enabled = false;
			return;
		}
		_dates.clear();

		String[] pairs = uptimes.split(",");

		for (String pair : pairs)
		{
			GregorianCalendar firstDate = new GregorianCalendar();
			GregorianCalendar secondDate = new GregorianCalendar();
			
			if (firstDate.get(GregorianCalendar.DAY_OF_WEEK) == 1)
			{
				int week = firstDate.get(GregorianCalendar.WEEK_OF_YEAR) - 1;
				
				if (week == 0)
				{
					week = Calendar.getInstance().getMaximum(Calendar.WEEK_OF_YEAR);
				}
				firstDate.set(GregorianCalendar.WEEK_OF_YEAR, week);
				secondDate.set(GregorianCalendar.WEEK_OF_YEAR, week);
			}

			int i = 0;
			for (String part : pair.split("-"))
			{
				i++;
				GregorianCalendar current = i == 1 ? firstDate : secondDate;

				String[] date = part.split(":");

				for (String token : date)
				{
					if (token.endsWith("Y"))
					{
						current.set(GregorianCalendar.YEAR,
								Integer.parseInt(token.substring(0, token.length() - 1)));
					}
					else if (token.endsWith("MO"))
					{
						current.set(GregorianCalendar.MONTH,
								Integer.parseInt(token.substring(0, token.length() - 2)));
					}
					else if (token.endsWith("W"))
					{
						current.set(GregorianCalendar.WEEK_OF_MONTH,
								Integer.parseInt(token.substring(0, token.length() - 1)));
					}
					else if (token.endsWith("D"))
					{
						current.set(GregorianCalendar.DAY_OF_WEEK,
								Integer.parseInt(token.substring(0, token.length() - 1)));
					}
					else if (token.endsWith("H"))
					{
						current.set(GregorianCalendar.HOUR_OF_DAY,
								Integer.parseInt(token.substring(0, token.length() - 1)));
					}
					else if (token.endsWith("M"))
					{
						current.set(GregorianCalendar.MINUTE,
								Integer.parseInt(token.substring(0, token.length() - 1)));
					}
					else if (token.endsWith("S"))
					{
						current.set(GregorianCalendar.SECOND,
								Integer.parseInt(token.substring(0, token.length() - 1)));
					}
				}

			}
			int[] types =
			{ GregorianCalendar.SECOND, GregorianCalendar.MINUTE, GregorianCalendar.HOUR_OF_DAY,
					GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.WEEK_OF_MONTH,
					GregorianCalendar.MONTH, GregorianCalendar.YEAR };

			int e = 0;
			for (int type : types)
			{
				e++;
				if (e == 7)
					e = 6;

				if (firstDate.get(type) > secondDate.get(type))
				{
					int time = secondDate.get(types[e]) + 1;
					secondDate.set(types[e], time);
				}

				if (secondDate.get(type) > Calendar.getInstance().getMaximum(type))
				{
					secondDate.set(type, secondDate.get(type - 1));
					int time = secondDate.get(types[e]) + 1;
					secondDate.set(types[e], time);
				}

			}

			_dates.put(firstDate.getTime(), secondDate.getTime());
		}
	}

}