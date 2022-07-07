package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.clan;

import java.util.List;

import mineplex.core.common.util.C;
import mineplex.game.clans.clans.gui.events.ClansButtonClickEvent;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.event.PlayerPreClaimTerritoryEvent;
import mineplex.game.clans.tutorial.TutorialRegion;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;
import mineplex.game.clans.tutorial.tutorials.clans.objective.ClanObjective;
import org.bukkit.event.EventPriority;

public class ClaimLandGoal extends ObjectiveGoal<ClanObjective>
{
	public ClaimLandGoal(ClanObjective objective)
	{
		super(
				objective,
				"Claim Land",
				"Type '/c' to Claim Land using the Clan Menu",
				"Clans are able to claim land for themselves. "
				+ "Once claimed, no one else can break or place blocks there! " 
				+ "You must be inside the " + C.cAqua + "blue" + C.cGray + " outline to claim land.",
				DyeColor.ORANGE
		);
	}

	@Override
	protected void customStart(Player player)
	{
	}

	@Override
	protected void customFinish(Player player)
	{
		TutorialRegion region = getObjective().getPlugin().getRegion(player);
		List<Location> blocks = region.getLocationMap().getGoldLocations(ClansMainTutorial.Bounds.LAND_CLAIM.getDataLocColor());
		UtilAlg.getBox(blocks.get(0).getBlock(), blocks.get(1).getBlock()).stream().filter(block -> block.getType() == Material.WOOL)
				.forEach(block -> block.setType(Material.GLOWSTONE));
	}

	@EventHandler
	public void onClaim(PlayerPreClaimTerritoryEvent event)
	{
		if (contains(event.getClaimer()))
		{
			if (getObjective().getPlugin().isIn(event.getClaimer(), ClansMainTutorial.Bounds.LAND_CLAIM))
			{
				finish(event.getClaimer());
			}
			else
			{
				UtilPlayer.message(event.getClaimer(), F.main("Tutorial", "You must claim the land inside the blue outline"));
			}

			event.setCancelled(true);
		}
	}


	@EventHandler (priority = EventPriority.HIGHEST)
	public void onClick(ClansButtonClickEvent event) {
		if(contains(event.getPlayer()) && event.getButtonType().equals(ClansButtonClickEvent.ButtonType.Territory))
			event.setCancelled(false);
	}
}
