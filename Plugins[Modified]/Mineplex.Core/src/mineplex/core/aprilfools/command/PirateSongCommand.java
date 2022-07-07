package mineplex.core.aprilfools.command;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.aprilfools.AprilFoolsManager;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.UtilTextMiddle;

public class PirateSongCommand extends CommandBase<AprilFoolsManager>
{
	private static final String[] PIRATE_SONG =
	{
		"Are you ready kids?",
		"Aye aye captain!",
		"I can't hear you!",
		"AYE AYE CAPTAIN!",
		"OOOOOOOOOOOOH",
		"Who lives in a pineapple under the sea?",
		"Spongebob Squarepants!"
	};

	public PirateSongCommand(AprilFoolsManager plugin)
	{
		super(plugin, AprilFoolsManager.Perm.PIRATE_SONG_COMMAND, "piratesong");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.runSyncTimer(new BukkitRunnable()
		{
			int index = 0;

			@Override
			public void run()
			{
				if (index == PIRATE_SONG.length)
				{
					cancel();
					return;
				}

				UtilTextMiddle.display("", PIRATE_SONG[index++], 10, 60, 10);
			}
		}, 20, 100);
	}
}