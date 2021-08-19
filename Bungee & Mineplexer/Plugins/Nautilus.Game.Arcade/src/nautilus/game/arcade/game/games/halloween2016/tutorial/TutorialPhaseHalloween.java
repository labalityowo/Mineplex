package nautilus.game.arcade.game.games.halloween2016.tutorial;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import mineplex.core.common.animation.Animator;
import mineplex.core.common.util.C;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.gametutorial.TutorialPhase;
import nautilus.game.arcade.gametutorial.TutorialText;

public class TutorialPhaseHalloween extends TutorialPhase
{
	
	private Animator _animator;
	private GameTeam _team;
	private Location _start;

	public TutorialPhaseHalloween(int duration, Animator animator, GameTeam team, Location start)
	{
		super(new TutorialText[]
				{
						new TutorialText(C.cGold + "Legend says that many years ago", 5 * 20, 1),
						new TutorialText(C.cGold + "A group of heroes defeated the Pumpkin King", 4 * 20, 2),
						new TutorialText(C.cGold + "Since then the world has known peace.", 3 * 20, 3),
						new TutorialText("", 2 * 20, 4),
						new TutorialText(C.cGold + "My family has guarded this tomb for generations.", 4 * 20, 5),
						new TutorialText(C.cGold + "All I know is we must not let it fall.", 4 * 20, 6),
						new TutorialText("", 2 * 20, 7),
						new TutorialText("", Math.max((duration-(24*20)), 8))
				});
		
		_animator = animator;
		_team = team;
		_start = start;
	}
	
	
	@Override
	public void onStart()
	{
		_animator.start(_start);
	}
	
	
	
	@Override
	public void onEnd()
	{
		for(Player p : _team.GetPlayers(false))
		{
			p.setGameMode(GameMode.SURVIVAL);
		}
		_animator.stop();
	}

	@Override
	public int ID()
	{
		return 1;
	}
	
	

}
