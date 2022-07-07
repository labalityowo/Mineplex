package nautilus.game.arcade.managers.voting.command;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.jsonchat.HoverEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.game.GameDisplay;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.managers.voting.VoteRating;
import nautilus.game.arcade.managers.voting.VotingManager;
import nautilus.game.arcade.managers.voting.VotingManager.Perm;

public class MapRatingsCommand extends CommandBase<ArcadeManager>
{

	private static final String[] EXPLANATION =
			{
					"When map voting has completed the " + F.name("Winning") + " map",
					"gains " + F.count("+x") + " rating, where " + F.count("x") + " is the number of other",
					"maps in the voting pool. All " + F.name("Remaining") + " maps lose",
					F.count("1")  + " rating."
			};

	public MapRatingsCommand(ArcadeManager plugin)
	{
		super(plugin, Perm.MAP_RATINGS_COMMAND, "mapratings");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 1)
		{
			caller.sendMessage(F.main(Plugin.getName(), "/" + _aliasUsed + " <game>"));
			return;
		}

		String gameString = args[0];
		GameDisplay gameDisplay;

		try
		{
			gameDisplay = GameDisplay.valueOf(gameString);
		}
		catch (IllegalArgumentException e)
		{
			caller.sendMessage(F.main(Plugin.getName(), F.name(gameString) + " is not a game."));
			caller.sendMessage(F.main(Plugin.getName(), "Valid games: " + Arrays.toString(GameDisplay.values())));
			return;
		}

		VotingManager manager = Plugin.GetGameCreationManager().getVotingManager();

		manager.getMapRatings(ratings ->
		{
			caller.sendMessage(F.main(Plugin.getName(), "Map Ratings for " + F.name(gameString) + ":"));
			ratings.sort(Comparator.comparingInt(VoteRating::getRating).reversed());
			ratings.forEach(rating -> caller.sendMessage(C.mBody + " - " + F.name(rating.getMapName()) + " - " + F.count(rating.getRating())));

			new JsonMessage(F.main(Plugin.getName(), "Hover over for details on what these mean."))
					.hover(HoverEvent.SHOW_TEXT, C.mBody + Arrays.stream(EXPLANATION).collect(Collectors.joining("\n" + C.mBody)))
					.sendToPlayer(caller);
		}, gameDisplay);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args)
	{
		if (args.length > 1)
		{
			return null;
		}

		Stream<String> nameStream = Arrays.stream(GameDisplay.values())
				.map(GameDisplay::name);

		if (args.length == 1)
		{
			nameStream = nameStream.filter(name -> name.startsWith(args[0]));
		}

		return nameStream.collect(Collectors.toList());
	}
}
