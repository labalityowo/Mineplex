package mineplex.cache.player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import mineplex.serverdata.Region;
import mineplex.serverdata.redis.RedisDataRepository;
import mineplex.serverdata.redis.atomic.RedisStringRepository;
import mineplex.serverdata.servers.ServerManager;

public enum PlayerCache
{
	INSTANCE;

	public static PlayerCache getInstance()
	{
		return INSTANCE;
	}

	private final RedisDataRepository<PlayerInfo> _playerInfoRepository;
	private final RedisStringRepository _accountIdRepository;

	PlayerCache()
	{
		_playerInfoRepository = new RedisDataRepository<PlayerInfo>(
				ServerManager.getMasterConnection(),
				ServerManager.getSlaveConnection(),
				Region.ALL,
				PlayerInfo.class,
				"playercache");

		_accountIdRepository = new RedisStringRepository(
				ServerManager.getMasterConnection(),
				ServerManager.getSlaveConnection(),
				Region.ALL,
				"accountid",
				(int) TimeUnit.HOURS.toSeconds(6)
		);
	}

	public void addPlayer(PlayerInfo player)
	{
		try
		{
			_playerInfoRepository.addElement(player, 60 * 60 * 6);  // 6 Hours
		}
		catch (Exception exception)
		{
			System.out.println("Error adding player info in PlayerCache : " + exception.getMessage());
			exception.printStackTrace();
		}
	}

	public PlayerInfo getPlayer(UUID uuid)
	{
		try
		{
			PlayerInfo playerInfo = _playerInfoRepository.getElement(uuid.toString());
			return playerInfo;
		}
		catch (Exception exception)
		{
			System.out.println("Error retrieving player info in PlayerCache : " + exception.getMessage());
			exception.printStackTrace();
		}

		return null;
	}

	/**
	 * Attempts to grab a player's account ID from the cache
	 *
	 * @param uuid Minecraft Account UUID
	 * @return The account id of the player, or -1 if the player is not in the cache
	 */
	public int getAccountId(UUID uuid)
	{
		String accountIdStr = _accountIdRepository.get(uuid.toString());

		if (accountIdStr == null)
			return -1;

		try
		{
			int accountId = Integer.parseInt(accountIdStr);
			if (accountId <= 0)
			{
				// remove invalid account id
				_accountIdRepository.del(uuid.toString());
				return -1;
			}
			return accountId;
		}
		catch (NumberFormatException ex)
		{
			// remove invalid account id
			_accountIdRepository.del(uuid.toString());
			return -1;
		}
	}

	public void updateAccountId(UUID uuid, int newId)
	{
		_accountIdRepository.set(uuid.toString(), String.valueOf(newId));
	}

	public void clean()
	{
		_playerInfoRepository.clean();
	}
}
