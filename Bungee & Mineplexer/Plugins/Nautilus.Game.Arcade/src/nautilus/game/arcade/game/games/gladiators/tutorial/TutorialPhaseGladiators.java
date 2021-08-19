package nautilus.game.arcade.game.games.gladiators.tutorial;

import nautilus.game.arcade.gametutorial.TutorialPhase;
import nautilus.game.arcade.gametutorial.TutorialText;

/**
 * Created by William (WilliamTiger).
 * 10/12/15
 */
public class TutorialPhaseGladiators extends TutorialPhase
{

	public TutorialPhaseGladiators()
	{
		super(new TutorialText[]{
				new TutorialText("Defeat your opponent!", 20 * 4, 1),
				new TutorialText("", 20 * 2, 2),
		});
	}

	@Override
	public int ID()
	{
		return 1;
	}
}