package mineplex.core.bonuses.redis;

import org.bukkit.entity.Player;

import mineplex.core.bonuses.BonusManager;
import mineplex.core.common.util.UtilPlayer;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

public class VoteHandler implements CommandCallback
{
	private BonusManager _bonusManager;

	public VoteHandler(BonusManager bonusManager)
	{
		_bonusManager = bonusManager;
	}

	@Override
	public void run(ServerCommand command)
	{
		VotifierCommand v = ((VotifierCommand) command);

		Player player = UtilPlayer.searchExact(v.getPlayerName());

		if (player != null)
		{
			_bonusManager.handleVote(player, v.getRewardReceived(), v.isClans());
		}
	}
}