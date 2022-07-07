package nautilus.game.arcade.game.games.halloween2016.tutorial;

import org.bukkit.Location;

import mineplex.core.common.animation.Animator;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.halloween2016.Halloween2016;
import nautilus.game.arcade.gametutorial.GameTutorial;
import nautilus.game.arcade.gametutorial.TutorialPhase;

public class TutorialHalloween2016 extends GameTutorial
{
	
	private Halloween2016 _host;
	
	public TutorialHalloween2016(Halloween2016 host, Animator animator, GameTeam team, Location start, int duration)
	{
		super(host.Manager, new TutorialPhase[]{
				new TutorialPhaseHalloween(duration, animator, team, start)
		});
		_host = host;
		
		SetTutorialPositions = false;
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		_host.Manager.getCosmeticManager().setHideParticles(true);
	}
	
	@Override
	public void onEnd()
	{
		super.onEnd();
		_host.AllowParticles = true;
		
		_host.Manager.getCosmeticManager().setHideParticles(false);
	}

}
