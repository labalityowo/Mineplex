package mineplex.gemhunters.progression;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.C;

import java.util.Arrays;
import java.util.List;

@ReflectivelyCreateMiniPlugin
public class ProgressionModule extends MiniPlugin
{

	private static final List<ProgressionTitle> TITLE_LIST = Arrays.asList(
			new ProgressionTitle(C.cGray + "Bankrupt", 0),
			new ProgressionTitle(C.cAqua + "Beggar", 100),
			new ProgressionTitle(C.cGreen + "Poor", 250),
			new ProgressionTitle(C.cGreen + "MiddleClass", 500),
			new ProgressionTitle(C.cGold + "Wealthy", 750),
			new ProgressionTitle(C.cGold + "Loaded", 1000),
			new ProgressionTitle(C.cRed + "Millionaire", 5000)
	);

	public ProgressionModule()
	{
		super("Progression");
	}

	public ProgressionTitle getTitle(int gems)
	{
		for (ProgressionTitle title : TITLE_LIST)
		{
			if (title.getRequiredGems() >= gems)
			{
				return title;
			}
		}

		return TITLE_LIST.get(TITLE_LIST.size() - 1);
	}
}
