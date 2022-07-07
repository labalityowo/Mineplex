package mineplex.hub.gimmicks.staffbuild;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.HoverEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.hub.gimmicks.staffbuild.StaffBuild.Perm;

public class BuildHistoryCommand extends CommandBase<StaffBuild>
{

	BuildHistoryCommand(StaffBuild plugin)
	{
		super(plugin, Perm.BUILD_HISTORY, "buildhistory");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Map<Block, String> buildHistory = Plugin.getBuildHistory();

		if (args.length < 1)
		{
			caller.sendMessage(F.main(Plugin.getName(), "Build History:"));

			buildHistory.values().stream()
					.distinct()
					.forEach(playerName ->
					{
						new JsonMessage(C.cYellowB + "[VIEW] " + F.name(playerName))
								.hover(HoverEvent.SHOW_TEXT, C.cGray + "Click to show all blocks placed by " + F.name(playerName) + ".")
								.click(ClickEvent.RUN_COMMAND, "/" + _aliasUsed + " " + playerName)
								.sendToPlayer(caller);
					});

			new JsonMessage(C.cGreenB + "[RESET]")
					.hover(HoverEvent.SHOW_TEXT, C.cGray + "Resets your view of the staff build area.")
					.click(ClickEvent.RUN_COMMAND, "/" + _aliasUsed + " reset")
					.sendToPlayer(caller);
		}
		else
		{
			String playerToCheck = args[0];

			if (playerToCheck.equals("reset"))
			{
				buildHistory.forEach((block, playerName) -> caller.sendBlockChange(block.getLocation(), block.getType(), block.getData()));
				caller.sendMessage(F.main(Plugin.getName(), "Reset your view of the staff build area."));
				return;
			}

			if (!buildHistory.containsValue(playerToCheck))
			{
				caller.sendMessage(F.main(Plugin.getName(), F.name(playerToCheck) + " has never placed any blocks in the staff build area."));
				return;
			}

			buildHistory.forEach((block, playerName) ->
			{
				if (playerToCheck.equals(playerName))
				{
					caller.sendBlockChange(block.getLocation(), block.getType(), block.getData());
				}
				else
				{
					caller.sendBlockChange(block.getLocation(), Material.AIR, (byte) 0);
				}
			});

			caller.sendMessage(F.main(Plugin.getName(), "Viewing all blocks placed by " + F.name(playerToCheck) + "."));
		}
	}
}
