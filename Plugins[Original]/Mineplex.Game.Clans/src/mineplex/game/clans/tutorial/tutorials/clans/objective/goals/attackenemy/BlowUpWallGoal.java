package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.attackenemy;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.fallingblock.FallingBlocks;
import mineplex.game.clans.clans.siege.events.SiegeWeaponExplodeEvent;
import mineplex.game.clans.tutorial.TutorialRegion;
import mineplex.game.clans.tutorial.TutorialSession;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;
import mineplex.game.clans.tutorial.tutorials.clans.objective.AttackEnemyObjective;

public class BlowUpWallGoal extends ObjectiveGoal<AttackEnemyObjective>
{
	public BlowUpWallGoal(AttackEnemyObjective objective)
	{
		super(
				objective,
				"Blow up the Enemy Base",
				"Left-Click to shoot TNT at the Enemy Base",
				"TNT Cannons will rotate to the direction you are looking. Simply look at the Enemy Base, wait for it to rotate, and then FIRE!",
				DyeColor.MAGENTA
		);
	}

	@Override
	protected void customStart(Player player)
	{
		TutorialSession session = getObjective().getPlugin().getTutorialSession(player);
		session.setMapTargetLocation(getObjective().getPlugin().getCenter(session.getRegion(), ClansMainTutorial.Bounds.ENEMY_ATTACK_AREA));
	}
	
	@EventHandler
	public void siegeWeaponExplode(SiegeWeaponExplodeEvent event)
	{
		Player shooter = event.getProjectile().getShooter();

		if (!contains(shooter))
		{
			if (getObjective().getPlugin().isInTutorial(shooter))
			{
				UtilPlayer.message(shooter, F.main("Clans", "No cheating! (:"));
				event.setCancelled(true);
			}
			
			return;
		}

		Location center = event.getProjectile().getLocation();
		
		TutorialRegion region = getObjective().getPlugin().getRegion(shooter);
		
		double radius = 5.2;
		
		Map<Block, Double> blockList = new HashMap<>();
		int iR = (int) radius + 1;
		
		for (int x = -iR; x <= iR; x++)
		{
			for (int z = -iR; z <= iR; z++)
			{
				for (int y = -iR; y <= iR; y++)
				{
					Block curBlock = center.getBlock().getRelative(x, y, z);
					
					double offset = UtilMath.offset(center, curBlock.getLocation());
					
					if (offset <= radius)
					{
						blockList.put(curBlock, Double.valueOf(offset));
					}
				}
			}
		}
		
		blockList.forEach((block, dist) -> {
			
			if (block.getType() == Material.SMOOTH_BRICK
		     || block.getType() == Material.SMOOTH_STAIRS
		     || block.getType() == Material.IRON_DOOR_BLOCK)
			
			if (Math.random() < 0.2 + (dist / 2.55) || dist < 1.75)
			{
				block.setType(Material.AIR, false);
				
				if (block.getType() != Material.IRON_DOOR_BLOCK && block.getType().name().endsWith("BANNER"))
					FallingBlocks.Instance.Spawn(block.getLocation(), block.getType(), block.getData(), center);
			}
		});

		event.setCancelled(true);
		finish(shooter);
	}
	
	@Override
	protected void customFinish(Player player)
	{
		getObjective().getCannons().remove(player.getName()).kill(); //Kill cannon after goal complete
		
		getObjective().getShooters().get(player.getName()).forEach(Zombie::remove);
		
		getObjective().getShooters().get(player.getName()).clear();
	}
}
