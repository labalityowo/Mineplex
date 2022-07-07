package nautilus.game.arcade.game.games.cakewars.shop;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import mineplex.core.common.Pair;

public interface CakeTeamItem extends CakeItem
{

	void apply(Player player, int level, Location cake);

	String getName();

	String[] getDescription(int level);

	Pair<String, Integer>[] getLevels();

}
