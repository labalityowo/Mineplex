package mineplex.core.thereallyoldscoreboardapiweshouldremove.elements;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.ScoreboardManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardElementCoinCount implements ScoreboardElement
{
	@Override
	public List<String> getLines(ScoreboardManager manager, Player player, List<String> out)
	{
		List<String> output = new ArrayList<String>();
		output.add(manager.getDonation().Get(player).getBalance(GlobalCurrency.TREASURE_SHARD) + "");
		return output;
	}
}
