package mineplex.core.mission.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.mission.MissionContext;
import mineplex.core.mission.MissionLength;
import mineplex.core.mission.MissionManager;
import mineplex.core.mission.MissionManager.Perm;

public class SetMissionsCommand extends CommandBase<MissionManager>
{

	public SetMissionsCommand(MissionManager plugin)
	{
		super(plugin, Perm.SET_MISSIONS_COMMAND, "setmissions");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 2)
		{
			caller.sendMessage(F.main(Plugin.getName(), "/" + _aliasUsed + " <daily> <weekly>"));
			return;
		}

		Map<MissionLength, List<MissionContext<?>>> missions = new HashMap<>();

		missions.put(MissionLength.DAY, getFromArg(args[0], caller));
		missions.put(MissionLength.WEEK, getFromArg(args[1], caller));

		if (args.length > 2)
		{
			caller.sendMessage(F.main(Plugin.getName(), "Overriding for all players..."));

			for (Player player : UtilServer.getPlayersCollection())
			{
				Plugin.setMissions(player, missions);
			}
		}
		else
		{
			Plugin.setMissions(caller, missions);
		}
	}

	private List<MissionContext<?>> getFromArg(String arg, Player caller)
	{
		String[] split = arg.split(",");
		List<MissionContext<?>> missions = new ArrayList<>(split.length);

		for (String idString : split)
		{
			try
			{
				int id = Integer.parseInt(idString);
				MissionContext<?> context = Plugin.getMission(id);

				if (context == null)
				{
					continue;
				}

				missions.add(context);
			}
			catch (NumberFormatException ex)
			{
				caller.sendMessage(F.main(Plugin.getName(), F.elem(idString) + " is not a number."));
			}
		}

		return missions;
	}
}
