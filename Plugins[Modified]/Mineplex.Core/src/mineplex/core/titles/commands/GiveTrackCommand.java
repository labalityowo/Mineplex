package mineplex.core.titles.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.titles.tracks.ItemizedTrack;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackManager;

public class GiveTrackCommand extends CommandBase<TrackManager>
{
	public GiveTrackCommand(TrackManager plugin)
	{
		super(plugin, TrackManager.Perm.GIVE_TRACK_COMMAND, "givetrack");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.help("/givetrack <player> <trackid>", "Give a player an unlockable track", ChatColor.DARK_RED));
			return;
		}

		String player = args[0];
		String track = args[1];

		Track trackObj = Plugin.getTrackById(track);

		if (trackObj == null)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "That is not a track" ));
			return;
		}

		if (!(trackObj instanceof ItemizedTrack))
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "That is not an unlockable track" ));
			return;
		}

		Plugin.unlockTrack(player, track, result ->
		{
			switch (result)
			{
				case PLAYER_NOT_FOUND:
					UtilPlayer.message(caller, F.main(Plugin.getName(), "That player has never joined Mineplex!"));
					break;
				case UNKNOWN_ERROR:
					UtilPlayer.message(caller, F.main(Plugin.getName(), "Oops. Something has gone wrong while giving " + F.elem(player) + " the track " + F.elem(track) + "!"));
					break;
				case SUCCESS:
					UtilPlayer.message(caller, F.main(Plugin.getName(), "Successfully gave " + F.elem(player) + " the track " + F.elem(track) + "!"));
					break;
			}
		});
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args)
	{
		if (args.length == 1)
		{
			return tabCompletePlayerNames(sender, args);
		}
		if (args.length == 2)
		{
			return getMatches(args[1], Plugin.getAllTracks().stream().filter(track -> track instanceof ItemizedTrack).map(Track::getId).collect(Collectors.toList()));
		}
		return null;
	}
}