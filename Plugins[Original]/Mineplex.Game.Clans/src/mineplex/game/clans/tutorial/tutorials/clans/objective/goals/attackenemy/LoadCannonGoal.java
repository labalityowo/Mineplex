package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.attackenemy;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilInv;
import mineplex.game.clans.clans.siege.events.LoadSiegeWeaponEvent;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.AttackEnemyObjective;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

public class LoadCannonGoal extends ObjectiveGoal<AttackEnemyObjective>
{
	public LoadCannonGoal(AttackEnemyObjective objective)
	{
		super(
				objective,
				"Load the Cannon",
				"Right-Click while on the Cannon, and insert your TNT",
				"TNT Cannons require TNT to be able to shoot. You can also change the range your cannon fires in the Cannon Menu.",
				null
		);
	}

	@Override
	protected void customStart(Player player)
	{
		UtilInv.give(player, Material.TNT);
	}
	
	@Override
	protected void customFinish(Player player)
	{
	}
	
	@EventHandler
	public void onSiegeWeaponLoad(LoadSiegeWeaponEvent event)
	{
		if (!contains(event.getPlayer()))
		{
			return;
		}
		
		finish(event.getPlayer());
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (!contains(event.getPlayer()))
		{
			return;
		}
		UtilPlayer.message(event.getPlayer(), F.main("Clans", "Are you sure? That's the only TNT you have!"));
		event.setCancelled(true);
	}
}
