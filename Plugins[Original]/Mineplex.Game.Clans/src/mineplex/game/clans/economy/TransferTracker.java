package mineplex.game.clans.economy;

import org.bukkit.entity.Player;

import mineplex.serverdata.Region;
import mineplex.serverdata.data.DataRepository;
import mineplex.serverdata.redis.RedisDataRepository;
import mineplex.serverdata.servers.ServerManager;

/**
 * Tracks when a player last converted gems into coins.
 * @author MrTwiggy
 *
 */
public class TransferTracker 
{
	public static final int TRANSFER_TIMEOUT = 24 * 60 * 60;	// Time before transfer entry expires from DB (in seconds)
	private DataRepository<GemTransfer> _repository;
	
	public TransferTracker()
	{
		_repository = new RedisDataRepository<GemTransfer>(ServerManager.getMasterConnection(), ServerManager.getSlaveConnection(),
						Region.currentRegion(), GemTransfer.class, "GemTransfers");
	}
	
	public void insertTransfer(Player player)
	{
		GemTransfer transfer = new GemTransfer(player.getName());
		_repository.addElement(transfer, TRANSFER_TIMEOUT);
	}
	
	public boolean hasTransferredToday(Player player)
	{
		GemTransfer transfer = _repository.getElement(player.getName());
		return transfer != null && transfer.transferWasToday();
	}
}