package nautilus.game.arcade.game.games.uhc.stat;

import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.uhc.UHC;
import nautilus.game.arcade.stats.StatTracker;

public class LuckyMinerStat extends StatTracker<UHC>
{

	private static final long BEFORE_TIME = TimeUnit.MINUTES.toMillis(10);
		
	public LuckyMinerStat(UHC game)
	{
		super(game);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC_05 || UtilTime.elapsed(getGame().GetStateTime(), BEFORE_TIME))
		{
			return;
		}
		
		playerLoop : for (Player player : getGame().GetPlayers(true))
		{
			for (ItemStack itemStack : player.getInventory().getArmorContents())
			{
				if (!UtilItem.isIronProduct(itemStack))
				{
					continue playerLoop;
				}
			}
			
			getGame().addUHCAchievement(player, "Miner");
		}
	}
	
}
