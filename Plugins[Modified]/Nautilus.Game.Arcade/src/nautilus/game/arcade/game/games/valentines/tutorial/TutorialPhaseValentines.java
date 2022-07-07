package nautilus.game.arcade.game.games.valentines.tutorial;

import org.bukkit.Sound;

import mineplex.core.common.util.C;
import nautilus.game.arcade.gametutorial.TutorialPhase;
import nautilus.game.arcade.gametutorial.TutorialText;

public class TutorialPhaseValentines extends TutorialPhase
{

	public TutorialPhaseValentines() 
	{
		super(new TutorialText[]
				{
					new TutorialText(C.cRed + "Moolanie", "Calvin! Did you forget Valentines Day again?!", 1, Sound.COW_IDLE),
					new TutorialText(C.cGreen + "Calvin", "Of course not!!!", 2, Sound.COW_IDLE),
					new TutorialText(C.cGreen + "Calvin", "I promise this year will be amazing!", 3, Sound.COW_IDLE),
					new TutorialText(C.cRed + "Moolanie", "It better be, or we're finished...", 4, Sound.COW_IDLE),
					new TutorialText(C.cGreen + "Calvin", "Good thing I prepared this year!", 5, Sound.COW_IDLE),
					new TutorialText(C.cGreen + "Calvin", "WHAT?!", 6, Sound.COW_IDLE),
					new TutorialText(C.cGreen + "Calvin", "NO!!", 7, Sound.COW_IDLE),
					new TutorialText(C.cGreen + "Calvin", "SOMEBODY HELP ME!", 8, Sound.COW_IDLE),
					new TutorialText(C.cGreen + "Calvin", "I'M BEING ROBBED!", 9, Sound.COW_IDLE),
					
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

	}
}
