package mineplex.core.sound;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;
import com.mineplex.spigot.MissingSoundEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.UtilServer;
import mineplex.core.slack.SlackAPI;
import mineplex.core.slack.SlackMessage;
import mineplex.core.slack.SlackTeam;
import mineplex.serverdata.Region;
import mineplex.serverdata.redis.atomic.RedisStringRepository;
import mineplex.serverdata.servers.ServerManager;

/**
 * Notifies #pc-sound-logging when we detect a bad sound mapping
 * @author Dan
 */
@ReflectivelyCreateMiniPlugin
public class SoundNotifier extends MiniPlugin
{
	private final RedisStringRepository _repo;
	private final Set<String> _messages = Sets.newConcurrentHashSet();

	private SoundNotifier()
	{
		super("Sound Notifier");

		_repo = new RedisStringRepository(
				ServerManager.getMasterConnection(),
				ServerManager.getSlaveConnection(),
				Region.ALL,
				"missingSounds"
		);

		// run task every 10 minutes
		long delay = 20 * 60 * 10;

		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (!_messages.isEmpty())
				{
					Set<String> messages;

					synchronized (_messages)
					{
						messages = new HashSet<>(_messages);
						_messages.clear();
					}

					Iterator<String> iter = messages.iterator();
					while (iter.hasNext())
					{
						String message = iter.next();
						if (_repo.get(message) == null)
						{
							sendSlackMessage(message);
							_repo.set(message, "sent");
						}

						iter.remove();
					}
				}
			}
		}.runTaskTimerAsynchronously(_plugin, delay, delay);
	}

	@EventHandler
	public void onMissingSound(MissingSoundEvent event)
	{
		final String message = event.getMessage();

		synchronized (_messages)
		{
			_messages.add(message);
		}
	}

	private void sendSlackMessage(String message)
	{
		message = String.format("[%s] %s", UtilServer.getServerName(), message);
		SlackMessage slackMsg = new SlackMessage("Sound Bot", "notes", message);
		SlackAPI.getInstance().sendMessage(SlackTeam.DEVELOPER, "#pc-sound-logging", slackMsg, true);
	}
}
