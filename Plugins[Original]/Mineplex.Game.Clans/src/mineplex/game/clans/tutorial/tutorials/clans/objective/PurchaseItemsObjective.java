package mineplex.game.clans.tutorial.tutorials.clans.objective;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.game.clans.tutorial.TutorialSession;
import mineplex.game.clans.tutorial.objective.UnorderedObjective;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.shops.PurchaseGoal;

import java.util.List;
import java.util.UUID;

public class PurchaseItemsObjective extends UnorderedObjective<ClansMainTutorial>
{
	public PurchaseItemsObjective(ClansMainTutorial clansMainTutorial, JavaPlugin javaPlugin)
	{
		super(clansMainTutorial, javaPlugin, "Purchase Items Tutorial", "Purchase Items from Shop");

		addGoal(new PurchaseGoal(
				this,
				Material.IRON_HELMET,
				"Purchase Iron Helmet",
				"Buy an Iron Helmet from the PvP Gear NPC",
				"The shops sell everything you could ever need and more. Buy armour from the " + C.cYellow + "PvP NPC."
		));
		addGoal(new PurchaseGoal(this, Material.IRON_CHESTPLATE, "Purchase Iron Chestplate",
				"Buy an Iron Chestplate"));
		addGoal(new PurchaseGoal(this, Material.IRON_LEGGINGS, "Purchase Iron Leggings",
				"Buy Iron Leggings"));
		addGoal(new PurchaseGoal(this, Material.IRON_BOOTS, "Purchase Iron Boots",
				"Buy Iron Boots"));
		addGoal(new PurchaseGoal(this, Material.IRON_AXE, "Purchase Iron Axe",
				"Buy an Iron Axe"));
//		addGoal(new PurchaseGoal(this, Material.IRON_PICKAXE, "Purchase Iron Pickaxe", "Talk to the Pvp Gear NPC and purchase an Iron Pickaxe"));

		setStartMessageDelay(60);
	}

	@Override
	protected void customStart(Player player)
	{
		super.customStart(player);

		TutorialSession session = getPlugin().getTutorialSession(player);
		session.setMapTargetLocation(getPlugin().getPoint(session.getRegion(), ClansMainTutorial.Point.PVP_SHOP));
	}

	@Override
	protected void customFinish(Player player)
	{
	}

	@EventHandler
	public void update(UpdateEvent event) {
		if(!event.getType().equals(UpdateType.SEC_05)) return;

		for (Player player : getActivePlayers())
		{
			if (player == null || !player.isOnline()) continue;
			List<Location> locations = getPlugin().getRegion(player).getLocationMap().getSpongeLocations(DyeColor.BROWN);
			if (locations == null) continue;
			for(Location loc : locations)
			{
				UtilFirework.playFirework(loc, FireworkEffect.Type.BURST, Color.AQUA, true, true);
			}
		}
	}
}
