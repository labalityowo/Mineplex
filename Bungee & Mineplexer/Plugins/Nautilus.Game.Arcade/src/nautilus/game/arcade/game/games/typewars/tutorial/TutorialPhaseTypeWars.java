package nautilus.game.arcade.game.games.typewars.tutorial;

import nautilus.game.arcade.game.games.typewars.Minion;
import nautilus.game.arcade.game.games.typewars.TypeWars;
import nautilus.game.arcade.gametutorial.TutorialPhase;
import nautilus.game.arcade.gametutorial.TutorialText;

public class TutorialPhaseTypeWars extends TutorialPhase
{

	public TutorialPhaseTypeWars()
	{
		super(new TutorialText[]
		{
			new TutorialText("This is your giant!", 1),
			new TutorialText("Protect him from evil minions!", 2),
			new TutorialText("Type the name above their head to kill them", 3),
			new TutorialText("F_", 7, 4),
			new TutorialText("Fi_", 7, 5),
			new TutorialText("Fis_", 7, 6),
			new TutorialText("Fish_", 7, 7),
			new TutorialText("Fishi_", 7, 8),
			new TutorialText("Fishin_", 7, 9),
			new TutorialText("Fishing_", 30, 10),
			new TutorialText("Kill your enemy's giant before they kill yours.", 11)
		});
	}

	@Override
	public int ID()
	{
		return 1;
	}
	
	@Override
	public void onStart()
	{
		
	}
	
	@Override
	public void onEnd()
	{
		
	}
	
	@Override
	public void onMessageDisplay(TutorialText text)
	{	
		if(text.ID() == 10)
		{
			for(Minion minion : ((TypeWars) getTutorial().Manager.GetGame()).getActiveMinions())
			{
				if(minion.getTeam() == getTutorial().getTeam())
				{
					minion.despawn(null, false);
				}
			}
		}
	}

}
