package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.attackenemy;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import mineplex.core.common.util.UtilBlock;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.DefaultHashMap;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.tutorial.TutorialRegion;
import mineplex.game.clans.tutorial.TutorialSession;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;
import mineplex.game.clans.tutorial.tutorials.clans.objective.AttackEnemyObjective;

public class StealEnemyPotatoesGoal extends ObjectiveGoal<AttackEnemyObjective>
{
	private DefaultHashMap<UUID, AtomicInteger> _playersMap = new DefaultHashMap<>(uuid -> new AtomicInteger());

	public StealEnemyPotatoesGoal(AttackEnemyObjective objective)
	{
		super(
				objective,
				"Steal Potatoes",
				"Steal potatoes from the Enemy Clan’s base",
				"Raiding enemy bases is one of the best parts of Clans! There's nothing better than looting Legendary weapons from enemies!",
				DyeColor.PURPLE
		);
	}

	@Override
	public String getDescription(Player player)
	{
		int count = _playersMap.get(player.getUniqueId()).get();
		return "Steal potatoes from the Enemy Clan’s base " + count + "/10";
	}

	@Override
	protected void customStart(Player player)
	{
		_playersMap.put(player.getUniqueId(), new AtomicInteger(0));

		TutorialSession session = getObjective().getPlugin().getTutorialSession(player);
		session.setMapTargetLocation(getObjective().getPlugin().getCenter(session.getRegion(), ClansMainTutorial.Bounds.ENEMY_LAND));

		UtilBlock.getInRadius(
				getObjective().getPlugin().getRegion(player).getLocationMap().getSpongeLocations(DyeColor.MAGENTA).get(0), 5).
				keySet().stream().filter(block -> block.getType().name().contains("IRON_DOOR")).forEach(block ->
			block.setType(Material.AIR)
		);
	}

	@Override
	protected void customFinish(Player player)
	{
	}

	@EventHandler
	public void onEntityInteract(EntityInteractEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}
		
		if (!getObjective().getPlugin().isInTutorial((Player) event.getEntity()))
		{
			return;
		}
		
		if (event.getBlock().getType() == Material.SOIL && event.getEntity() instanceof Creature)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockDamageEvent event)
	{
		if (!contains(event.getPlayer()) || event.getBlock().getType() != Material.POTATO)
			return;

		TutorialRegion region = getObjective().getPlugin().getRegion(event.getPlayer());
		if (getObjective().getPlugin().isIn(event.getBlock().getLocation().add(0, 1, 0), region, ClansMainTutorial.Bounds.ENEMY_LAND))
		{
			event.setCancelled(true);
			event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().add(0.5, 0.5, 0.5), new ItemStack(Material.POTATO_ITEM));
			event.getBlock().setType(Material.AIR);

			Bukkit.getServer().getScheduler().runTaskLater(getObjective().getJavaPlugin(), new Runnable()
			{
				@Override
				public void run()
				{
					if (contains(event.getPlayer()))
					{
						event.getBlock().setType(Material.POTATO);
					}
				}
			}, 20 * 10);
		}
	}

	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event)
	{
		if (!contains(event.getPlayer()))
			return;

		if (event.getItem().getItemStack().getType() == Material.POTATO_ITEM)
		{
			int count = _playersMap.get(event.getPlayer().getUniqueId()).incrementAndGet();
			if (count == 10)
				finish(event.getPlayer());
		}
	}
}
