package mineplex.game.nano.game.games.quick.challenges;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengeMaths extends Challenge
{

	private int _solution;

	public ChallengeMaths(Quick game)
	{
		super(game, ChallengeType.MATHS_QUESTION);
	}

	@Override
	public void challengeSelect()
	{
		_game.getManager().runSyncLater(() ->
		{
			int a = UtilMath.rRange(3, 10), b = UtilMath.rRange(3, 10);
			_solution = a * b;

			UtilTextMiddle.display(C.cYellow + a + " x " + b, "Type your answer in chat!", 0, 80, 0, UtilServer.getPlayers());
			_game.announce(F.main(_game.getManager().getName(), "Solve " + F.elem(a + " x " + b) + "!"), Sound.NOTE_SNARE_DRUM);
		}, 40);
	}

	@Override
	public void disable()
	{

	}

	@EventHandler
	public void playerChat(AsyncPlayerChatEvent event)
	{
		if (_solution == 0)
		{
			return;
		}

		Player player = event.getPlayer();

		if (event.getMessage().equals(String.valueOf(_solution)))
		{
			completePlayer(player, false);
			event.setCancelled(true);
		}
		else
		{
			failPlayer(player, false);
		}
	}
}
