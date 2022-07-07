package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.clan;

import mineplex.core.common.util.UtilBlock;
import net.minecraft.server.v1_8_R3.EnumDirection;
import org.bukkit.DyeColor;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilWorld;
import mineplex.game.clans.clans.event.ClansCommandPreExecutedEvent;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;
import mineplex.game.clans.tutorial.tutorials.clans.objective.ClanObjective;

public class SetHomeGoal extends ObjectiveGoal<ClanObjective>
{
	public SetHomeGoal(ClanObjective objective)
	{
		super(
				objective,
				"Set Clan Home",
				"Type '/c sethome' to set your Clan's Home",
				"Your Clan Home is a special place in your base that you can teleport " +
						"to from " + F.elem("Spawn Island") + " or at any time by typing " + F.elem("/c home") + ".",
				DyeColor.ORANGE
		);

		setDisplayFinishMessage(false);
	}

	@Override
	protected void customStart(Player player)
	{
	}

	@Override
	protected void customFinish(Player player)
	{
	}

	@EventHandler
	public void onSetHome(ClansCommandPreExecutedEvent event)
	{
		if (contains(event.getPlayer()) && event.getArguments().length == 1 && event.getArguments()[0].equalsIgnoreCase("sethome"))
		{
			event.setCancelled(true); //before checking if bed placed

			if (getObjective().getPlugin().isIn(event.getPlayer(), ClansMainTutorial.Bounds.LAND_CLAIM))
			{
				boolean bedPlaced = UtilBlock.placeBed(event.getPlayer().getLocation(), BlockFace.valueOf(EnumDirection.fromAngle(event.getPlayer().getLocation().getYaw()).name()), false, false);

				if (!bedPlaced)
				{
					UtilPlayer.message(event.getPlayer(), F.main("Clans", "This is not a suitable place for a bed."));
					return;
				}

				// we need to save this for later when the player teleports home!
				getObjective().getPlugin().getTutorialSession(event.getPlayer()).setHomeLocation(event.getPlayer().getLocation());

				finish(event.getPlayer());

				UtilPlayer.message(event.getPlayer(), F.main("Clans", "You have successfully set your Clan's Home to " + UtilWorld.locToStrClean(event.getPlayer().getLocation()) + "."));
			}
			else
			{
				UtilPlayer.message(event.getPlayer(), F.main("Tutorial", "You must set your home in your own land claim."));
			}

		}
	}
}
