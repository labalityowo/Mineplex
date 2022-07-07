package mineplex.game.clans.clans;

import com.mineplex.clansqueue.common.ClansQueueMessenger;
import com.mineplex.clansqueue.common.messages.QueuePauseUpdateMessage;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.game.clans.core.ClaimLocation;
import mineplex.game.clans.shop.energy.EnergyShop;

public class ClanEnergyManager extends MiniPlugin implements Runnable
{

	private final ClansManager _clansManager;
	private volatile boolean _paused;
	private int tickCount;

	public ClanEnergyManager(ClansManager clansManager)
	{
		super("Clan Energy");

		_clansManager = clansManager;

		new EnergyShop(this, clansManager.getClientManager(), clansManager.getDonationManager());

		// Wait 5 seconds and then tick every 60 seconds
		_plugin.getServer().getScheduler().runTaskTimer(_plugin, this, 20 * 5, 20 * 60);
		ClansQueueMessenger.getMessenger(UtilServer.getServerName()).registerListener(QueuePauseUpdateMessage.class, (pause, origin) ->
		{
			if (pause.ServerName.equals(UtilServer.getServerName()))
			{
				_paused = pause.Paused;
			}
		});
	}

	@Override
	public void run()
	{
		if (_paused)
		{
			return;
		}

		tickCount++;

		for (final ClanInfo clanInfo : _clansManager.getClanMap().values())
		{
			if (clanInfo.isAdmin())
				continue;

			int energyPerMinute = clanInfo.getEnergyCostPerMinute();
			int currentEnergy = clanInfo.getEnergy();

			if (currentEnergy < energyPerMinute)
			{
				for (ClaimLocation chunk : clanInfo.getClaimSet())
				{
					_clansManager.getClanDataAccess().unclaimSilent(chunk, true);
				}
				_clansManager.messageClan(clanInfo, F.main("Clans", "Your clan has ran out of energy. Land claims have been removed"));
			}
			else
			{
				clanInfo.adjustEnergy(-energyPerMinute);
				if (tickCount % 5 == 0 && energyPerMinute > 0)
				{
					runAsync(() -> _clansManager.getClanDataAccess().updateEnergy(clanInfo));
				}
			}
		}
	}

	public ClansManager getClansManager()
	{
		return _clansManager;
	}

	public int convertEnergyToGold(int energy)
	{
		return (energy / 8) + (energy % 8 == 0 ? 0 : 1);
	}
}
