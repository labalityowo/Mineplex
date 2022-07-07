package mineplex.core.thereallyoldscoreboardapiweshouldremove.elements;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.ScoreboardManager;

public class ScoreboardElementRank implements ScoreboardElement
{
	@Override
	public List<String> getLines(ScoreboardManager manager, Player player, List<String> out)
	{
		List<String> output = new ArrayList<>();
		
		PermissionGroup group = manager.getClients().Get(player).getRealOrDisguisedPrimaryGroup();
		String display = group.getDisplay(false, false, false, false);
		
		if (!display.isEmpty())
		{
			output.add(display);
		}
		else if (manager.getDonation().Get(player).ownsUnknownSalesPackage("SuperSmashMobs ULTRA") ||
				manager.getDonation().Get(player).ownsUnknownSalesPackage("Survival Games ULTRA") ||
				manager.getDonation().Get(player).ownsUnknownSalesPackage("Minigames ULTRA") ||
				manager.getDonation().Get(player).ownsUnknownSalesPackage("CastleSiege ULTRA") ||
				manager.getDonation().Get(player).ownsUnknownSalesPackage("Champions ULTRA"))
		{
			output.add("Single Ultra");
		}
		else
		{
			output.add("No Rank");
		}
		
		return output;
	}
}