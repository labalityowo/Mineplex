package mineplex.game.clans.clans;

import java.util.EnumMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClanTips.TipType;

public class ClanEnergyTracker extends MiniPlugin
{
	private ClansManager _clans;
	private EnumMap<UpdateType, Triple<Long, Long, String[]>> _updateMap;
	
	public ClanEnergyTracker(JavaPlugin plugin, ClansManager clans)
	{
		super("Clan Energy Tracker", plugin);
		_clans = clans;
		
		_updateMap = new EnumMap<>(UpdateType.class);
		_updateMap.put(UpdateType.MIN_05, Triple.of(0L, TimeUnit.HOURS.toMillis(1), new String[] { C.cRed + "Urgent", "Clan Energy is almost depleted" }));
		_updateMap.put(UpdateType.MIN_10, Triple.of(TimeUnit.HOURS.toMillis(1), TimeUnit.HOURS.toMillis(4), new String[] { C.cGold + "Warning", "Clan Energy is low" }));
		_updateMap.put(UpdateType.MIN_30, Triple.of(TimeUnit.HOURS.toMillis(4), TimeUnit.DAYS.toMillis(1), new String[] { C.cGold + "Notice", "Clan Energy is running low" }));
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (_updateMap.containsKey(event.getType()))
		{
			for (ClanInfo clan : _clans.getClanMap().values())
			{
				display(clan, event.getType());
			}
		}
	}
	
	private void display(ClanInfo clan, UpdateType type)
	{
		// No point in doing anything if nobody is online!
		if (!clan.isOnlineNow())
		{
			return;
		}
		
		// Avoid divide by 0
		if (clan.getEnergyCostPerMinute() <= 0)
		{
			return;
		}
		
		// Energy Remaining in milliseconds
		long energyRemaining = (clan.getEnergy() / clan.getEnergyCostPerMinute()) * 60000L;
		
		Triple<Long, Long, String[]> energyBounds = _updateMap.get(type);
		
		if (energyBounds != null && energyRemaining > energyBounds.getLeft() && energyRemaining < energyBounds.getMiddle())
		{
			_clans.middleTextClan(clan, energyBounds.getRight()[0], energyBounds.getRight()[1], 20, 200, 80);
			_clans.sendTipToClan(clan, TipType.ENERGY);
		}
	}
}
