package nautilus.game.arcade.game.games.smash.perks.pig;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;

public class SmashPig extends SmashUltimate
{

	public SmashPig()
	{
		super("Pig Stink", new String[]{}, Sound.PIG_IDLE, 0);
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);

		GameTeam team = Manager.GetGame().GetTeam(player);

		for (Player other : Manager.GetGame().GetPlayers(true))
		{
			if (player.equals(other) || team.HasPlayer(player))
			{
				continue;
			}

			Manager.GetCondition().Factory().Confuse(GetName() + " " + player.getName(), other, player, getLength() / 1000, 0, false, false, false);
		}
	}
}
