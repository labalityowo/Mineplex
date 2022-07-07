package mineplex.core.antihack.guardians;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.bukkit.entity.Player;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import mineplex.core.MiniPlugin;
import mineplex.core.PlayerSelector;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.UtilLambda;

@ReflectivelyCreateMiniPlugin
public class GuardianManager extends MiniPlugin
{
	private static final int MAX_STALKED_PLAYERS = 3;
	private static final int STALK_COOLDOWN_TIME_SECONDS = 5;

	private static final int MIN_STALK_TIME = 10 * 20;
	private static final int MAX_STALK_TIME = 20 * 20;
	private static final int MAX_MIN_DIFF = MAX_STALK_TIME - MIN_STALK_TIME;
	private static final Function<Integer, Double> STALK_END_PROBABILITY_EQUATION = x ->
	{
		return 1.0/ MAX_MIN_DIFF * x; // linear equation with points (0, 0) and (diff, 1)
	};

	private final Cache<UUID, Boolean> _stalkingCooldown = CacheBuilder.newBuilder()
			.concurrencyLevel(1)
			.expireAfterWrite(STALK_COOLDOWN_TIME_SECONDS, TimeUnit.SECONDS)
			.build();
	private final List<UUID> _stalking = new ArrayList<>();
	private List<AntiHackGuardian> _guardians = new ArrayList<>();

	private GuardianManager()
	{
		super("GuardianManager");

		_plugin.getServer().getScheduler().runTaskTimer(_plugin, () ->
		{
			for (AntiHackGuardian guardian : _guardians)
			{
				if (guardian.getTarget() != null && !guardian.getTarget().isOnline())
				{
					_stalking.remove(guardian.getTarget().getUniqueId());
					guardian.stopTargeting();
				}
				else if (guardian.getTargetingTime() > MIN_STALK_TIME)
				{
					double threshold = STALK_END_PROBABILITY_EQUATION.apply(guardian.getTargetingTime() - MIN_STALK_TIME);
					if (Math.random() <= threshold)
					{
						_stalking.remove(guardian.getTarget().getUniqueId());
						_stalkingCooldown.put(guardian.getTarget().getUniqueId(), true);
						guardian.stopTargeting();
					}
				}
				guardian.tick();
			}
		}, 0L, 1L);

		_plugin.getServer().getScheduler().runTaskTimer(_plugin, () ->
		{
			if (_stalking.size() >= MAX_STALKED_PLAYERS)
			{
				return;
			}

			if (_guardians.size() == 0)
			{
				return;
			}

			List<Player> targets = PlayerSelector.selectPlayers(
					UtilLambda.and(
							PlayerSelector.NOT_VANISHED,
							PlayerSelector.hasAnyRank(false,
									PermissionGroup.PLAYER,
									PermissionGroup.ULTRA,
									PermissionGroup.HERO,
									PermissionGroup.LEGEND,
									PermissionGroup.TITAN,
									PermissionGroup.TWITCH,
									PermissionGroup.YT,
									PermissionGroup.YOUTUBE,
									PermissionGroup.ADMIN,
									PermissionGroup.DEV,
									PermissionGroup.LT,
									PermissionGroup.OWNER
							),
							player -> !_stalking.contains(player.getUniqueId()),
							player -> _stalkingCooldown.getIfPresent(player.getUniqueId()) == null
					));

			while (_stalking.size() < MAX_STALKED_PLAYERS && targets.size() > 0)
			{
				Player target = targets.remove(ThreadLocalRandom.current().nextInt(targets.size()));

				int start = ThreadLocalRandom.current().nextInt(_guardians.size());

				for (int i = start, j = 0; j < _guardians.size(); i++, j++)
				{
					if (i >= _guardians.size())
					{
						i -= _guardians.size();
					}
					AntiHackGuardian guardian = _guardians.get(i);
					if (!guardian.isTargeting())
					{
						guardian.target(target);
						_stalking.add(target.getUniqueId());
						break;
					}
				}
			}
		}, 0L, 20L);
	}

	public void registerGuardian(AntiHackGuardian guardian)
	{
		_guardians.add(guardian);
	}
}