package mineplex.core.titles.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.titles.Titles;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackManager;

public class TrackCommand extends CommandBase<Titles>
{
	private final TrackManager _trackManager;

	public TrackCommand(Titles plugin)
	{
		super(plugin, TrackManager.Perm.TRACK_COMMAND, "track");

		_trackManager = Managers.require(TrackManager.class);
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			if (Plugin.getActiveTrack(caller) == null)
			{
				UtilPlayer.message(caller, F.main("Track", "You must specify the ID of a track to enable"));
			}
			else
			{
				Plugin.setOrToggleTrackForPlayer(caller, Plugin.getActiveTrack(caller), false);
			}
			return;
		}
		Track track = _trackManager.getTrackById(args[0]);
		if (track == null)
		{
			UtilPlayer.message(caller, F.main("Track", "That is not a valid track"));
			return;
		}
		if (track.getRequirements().getTier(caller) == null)
		{
			UtilPlayer.message(caller, F.main("Track", "You have not unlocked any tiers on that track"));
			return;
		}
		Plugin.setOrToggleTrackForPlayer(caller, track, args.length > 1);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args)
	{
		if (args.length == 1)
		{
			if (sender instanceof Player)
			{
				return getMatches(args[0], _trackManager.getAllTracks().stream().filter(track -> track.getRequirements().getTier((Player) sender) != null).map(Track::getId));
			}
		}
		return null;
	}
}