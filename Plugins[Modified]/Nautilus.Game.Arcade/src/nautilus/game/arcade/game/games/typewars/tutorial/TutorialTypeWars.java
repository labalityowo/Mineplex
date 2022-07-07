package nautilus.game.arcade.game.games.typewars.tutorial;

import java.util.ArrayList;

import mineplex.core.common.util.UtilShapes;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.typewars.Minion;
import nautilus.game.arcade.game.games.typewars.TypeWars;
import nautilus.game.arcade.gametutorial.GameTutorial;
import nautilus.game.arcade.gametutorial.TutorialPhase;

import org.bukkit.Location;

public class TutorialTypeWars extends GameTutorial
{

	private ArcadeManager _manager;
	private TypeWars _typeWars;
	
	public TutorialTypeWars(ArcadeManager manager)
	{
		super(manager, new TutorialPhase[]{new TutorialPhaseTypeWars()});
		
		TutorialNotification = true;
	}

	@Override
	public void onStart()
	{
		_manager = Manager;
		_typeWars = (TypeWars) Manager.GetGame();
		for(GameTeam team : Manager.GetGame().GetTeamList())
		{
			if(team != getTeam())
			{	
				ArrayList<Location> locations = UtilShapes.getLinesDistancedPoints(_typeWars.getMinionSpawns().get(getTeam()).get(4), _typeWars.getMinionSpawns().get(team).get(4), 1);
				_manager.GetCreature().SetForce(true);
				_manager.GetGame().CreatureAllowOverride = true;
				Minion minion = new Minion(_manager, locations.get(locations.size() - 35), _typeWars.getMinionSpawns().get(team).get(4), getTeam(), 4);
				minion.changeName("Fishing");
				((TypeWars) _manager.GetGame()).getActiveMinions().add(minion);
				_manager.GetGame().CreatureAllowOverride = false;
				_manager.GetCreature().SetForce(false);	
			}
		}
	}

}
