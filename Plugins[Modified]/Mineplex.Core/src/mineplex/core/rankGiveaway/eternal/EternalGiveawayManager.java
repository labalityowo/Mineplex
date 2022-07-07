package mineplex.core.rankGiveaway.eternal;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.rankGiveaway.redis.EternalGiveawayMessage;
import mineplex.core.rankGiveaway.redis.GiveawayMessageHandler;
import mineplex.core.status.ServerStatusManager;
import mineplex.serverdata.Region;
import mineplex.serverdata.commands.ServerCommandManager;

public class  EternalGiveawayManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		ETERNAL_COMMAND,
	}

	private static final double RANK_FIND_CHANCE = 0.001;
	
	private final PermissionGroup _group;
	
	private EternalGiveawayRepository _repository;
	private CoreClientManager _clientManager;
	private ServerStatusManager _statusManager;
	private Random _random;

	public EternalGiveawayManager(JavaPlugin plugin, CoreClientManager clientManager, ServerStatusManager statusManager)
	{
		super("Eternal Giveaway", plugin);

		_repository = new EternalGiveawayRepository(plugin);
		_clientManager = clientManager;
		_statusManager = statusManager;
		_random = new Random();

		_group = PermissionGroup.ETERNAL;

		ServerCommandManager.getInstance().registerCommandType("EternalGiveawayMessage", EternalGiveawayMessage.class, new GiveawayMessageHandler(plugin));
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.LT.setPermission(Perm.ETERNAL_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new EternalCommand(this));
	}

	public void openPumpkin(final Player player, final Runnable onSuccess)
	{
		double rand = _random.nextDouble();
		if (!hasEternal(player) && rand < RANK_FIND_CHANCE)
		{
			attemptToGiveEternal(player, onSuccess);
		}
	}

	protected void attemptToGiveEternal(Player player, Runnable onSuccess)
	{
		final int accountId = _clientManager.getAccountId(player);
		final Region region = getRegion();
		final String serverName = getServerName();

		if (accountId == -1) return;

		// Need to check database that we can give away a rank
		runAsync(() ->
		{
			final boolean pass = _repository.canGiveaway(region);

			if (pass && _repository.addEternal(accountId, region, serverName))
			{
				runSync(() -> giveRank(player, () ->
				{
					EternalGiveawayMessage message = new EternalGiveawayMessage(player.getName(), _repository.getEternalCount() + 1);
					message.publish();
					if (onSuccess != null)
					{
						onSuccess.run();
					}
				}));
			}
		});
	}

	/**
	 * Confirm that the player doesn't already have ETERNAL rank
	 */
	private boolean hasEternal(Player player)
	{
		return _clientManager.Get(player).getPrimaryGroup().inheritsFrom(PermissionGroup.ETERNAL);
	}

	public Region getRegion()
	{
		return _statusManager.getRegion();
	}

	public String getServerName()
	{
		return _statusManager.getCurrentServerName();
	}

	private void giveRank(Player player, Runnable after)
	{
		_clientManager.setPrimaryGroup(player, _group, after);
	}
}