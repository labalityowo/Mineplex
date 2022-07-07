package mineplex.game.clans.clans.worldevent.raid.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.worldevent.raid.RaidManager;
import mineplex.game.clans.clans.worldevent.raid.RaidType;

public class StartRaidCommand extends CommandBase<RaidManager>
{
	public StartRaidCommand(RaidManager plugin)
	{
		super(plugin, RaidManager.Perm.START_RAID_COMMAND, "startraid");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 1)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Usage: /startraid <RaidType>"));
			return;
		}
		try
		{
			RaidType type = RaidType.valueOf(args[0]);
			Plugin.startRaid(caller, type);
		}
		catch (Exception e)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "That is not an existing raid type. Raids:"));
			for (RaidType type : RaidType.values())
			{
				UtilPlayer.message(caller, C.cGray + " - " + C.cYellow + type.name());
			}
		}
	}
}