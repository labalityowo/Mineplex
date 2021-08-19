package nautilus.game.arcade.game.modules.gamesummary.components;

import java.util.List;

import nautilus.game.arcade.game.modules.gamesummary.GameSummaryComponent;
import nautilus.game.arcade.game.modules.gamesummary.GameSummaryComponentType;

public class ComplexSummaryComponent extends GameSummaryComponent<Object>
{

	public ComplexSummaryComponent(GameSummaryComponentType type)
	{
		super(type, null);
	}

	@Override
	public String getMainText(Object data)
	{
		return null;
	}

	@Override
	public List<String> getHoverText(Object data)
	{
		return null;
	}
}
