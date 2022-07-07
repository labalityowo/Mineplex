package nautilus.game.arcade.game.games.smash.perks;

import org.bukkit.entity.Player;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.smash.TeamSuperSmash;
import nautilus.game.arcade.kit.Perk;

public class SmashPerk extends Perk
{

	public SmashPerk(String name, String[] perkDesc)
	{
		super(name, perkDesc);
	}

	/**
	 * @param player
	 *            The player that you need to check.
	 * 
	 * @return true when the player is currently using a SmashUltimate.
	 */
	public boolean isSuperActive(Player player)
	{
		if (Kit instanceof SmashKit)
		{
			SmashKit kit = (SmashKit) Kit;

			if (kit.isSmashActive(player))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks to see if an attack would count as team damage.
	 * Always returns false if the game is not {@link TeamSuperSmash}.
	 * 
	 * @param player1
	 * @param player2
	 * @return true when the team of player1 equals the team of player2.
	 * @see GameTeam
	 */
	public boolean isTeamDamage(Player player1, Player player2)
	{
		Game game = Manager.GetGame();

		if (!(game instanceof TeamSuperSmash))
		{
			return false;
		}

		if (game.GetTeam(player1).equals(game.GetTeam(player2)))
		{
			return true;
		}
		
		return false;
	}

}
