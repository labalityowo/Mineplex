package nautilus.game.arcade.game.modules.gamesummary.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.bukkit.entity.Player;

import mineplex.core.common.util.C;

import nautilus.game.arcade.game.GemData;
import nautilus.game.arcade.game.modules.gamesummary.GameSummaryComponent;
import nautilus.game.arcade.game.modules.gamesummary.GameSummaryComponentType;

public class GemSummaryComponent extends GameSummaryComponent<Map<String, GemData>>
{

	private final String _colour;
	private final String _currency;

	public GemSummaryComponent(Function<Player, Map<String, GemData>> getFunction, String colour, String currency)
	{
		super(GameSummaryComponentType.GEMS, getFunction);

		_colour = colour;
		_currency = currency;
	}

	@Override
	public String getMainText(Map<String, GemData> data)
	{
		AtomicInteger totalGems = new AtomicInteger();

		data.values().forEach(gemData -> totalGems.getAndAdd((int) gemData.Gems));

		return C.cGray + "+" + _colour + totalGems.get() + C.cGray + " " + _currency;
	}

	@Override
	public List<String> getHoverText(Map<String, GemData> data)
	{
		List<String> text = new ArrayList<>();

		data.forEach((reason, gemData) -> text.add(C.cGray + "+" + _colour + (int) gemData.Gems + C.cGray + " for " + C.cYellow + (gemData.Amount > 0 ? gemData.Amount + " " : "") + reason));

		return text;
	}

}
