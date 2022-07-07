package nautilus.game.arcade.game.games.christmasnew;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.treasure.types.TreasureType;

import nautilus.game.arcade.game.modules.gamesummary.GameSummaryComponent;
import nautilus.game.arcade.game.modules.gamesummary.GameSummaryComponentType;

public class ChristmasSummaryComponent extends GameSummaryComponent<Boolean>
{

	private static final List<String> HOVER_TEXT = Arrays.asList(
			C.cGray + "- " + C.cGold + "Pumpkin Shield Particle Effect",
			C.cGray + "- " + C.cRed + "Santa's Helper Title",
			C.cGray + "- " + C.cYellow + "1 " + TreasureType.GINGERBREAD.getName()

	);

	ChristmasSummaryComponent(Function<Player, Boolean> getFunction)
	{
		super(GameSummaryComponentType.CUSTOM_REWARD, getFunction);
	}

	@Override
	public String getMainText(Boolean data)
	{
		return data ? C.cAquaB + "UNLOCKED CHRISTMAS REWARDS" : null;
	}

	@Override
	public List<String> getHoverText(Boolean data)
	{
		return HOVER_TEXT;
	}
}
