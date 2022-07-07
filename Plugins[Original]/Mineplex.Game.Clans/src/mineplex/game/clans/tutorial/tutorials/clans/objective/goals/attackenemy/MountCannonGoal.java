package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.attackenemy;

import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.siege.events.MountSiegeWeaponEvent;
import mineplex.game.clans.clans.siege.weapon.Cannon;
import mineplex.game.clans.tutorial.TutorialRegion;
import mineplex.game.clans.tutorial.TutorialSession;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial.Point;
import mineplex.game.clans.tutorial.tutorials.clans.objective.AttackEnemyObjective;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class MountCannonGoal extends ObjectiveGoal<AttackEnemyObjective>
{
	private ClansManager _clansManager;
	
	public MountCannonGoal(AttackEnemyObjective objective, ClansManager clansManager)
	{
		super(
				objective,
				"Get on the Cannon",
				"Right-Click on the Cannon",
				"You cannot break blocks in enemy territory, however you can blow them up! "
				+ "TNT Cannons are the best way to do destroy enemy bases!",
				DyeColor.BLACK
		);
		
		_clansManager = clansManager;
	}
	
	@Override
	protected void customStart(Player player)
	{
		getObjective().getCannons().put(player.getName(), _clansManager.getSiegeManager().spawnCannon(player, getObjective().getPlugin().getPoint(getObjective().getPlugin().getRegion(player), Point.CANNON), false));
		getObjective().getCannons().get(player.getName()).SetForcedVelocity(0.44, 2.45);
		getObjective().getCannons().get(player.getName()).setInvincible(true);
		
		getObjective().getCannons().get(player.getName()).LockYaw(-193, -173);

		TutorialSession session = getObjective().getPlugin().getTutorialSession(player);
		session.setMapTargetLocation(getObjective().getPlugin().getPoint(session.getRegion(), ClansMainTutorial.Point.CANNON));
	}
	
	@Override
	protected void clean(Player player, TutorialRegion region)
	{
		// This cannon could be removed from the tutorial already in BlowUpWallGoal, we need to check if its null
		Cannon cannon = getObjective().getCannons().remove(player.getName());
		if (cannon != null)
			cannon.kill();
	}

	@Override
	protected void customFinish(Player player)
	{
	}
	
	@EventHandler
	public void onSiegeWeaponMount(MountSiegeWeaponEvent event)
	{
		if (!contains(event.getPlayer()))
		{
			return;
		}
		
		finish(event.getPlayer());
	}
}
