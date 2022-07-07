package nautilus.game.arcade.game.games.cakewars.kits.perk;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import mineplex.core.common.util.UtilPlayer;

import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.kit.Perk;

public class PerkLifeSteal extends Perk
{

	private final double _increase;

	public PerkLifeSteal(double increase)
	{
		super("Lifesteal");

		_increase = increase;
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		Player killer = player.getKiller();

		if (killer != null && hasPerk(killer))
		{
			CakeWars game = (CakeWars) Manager.GetGame();
			double increase = Math.max(0, _increase - game.getDeathsInLastMinute(player) * 2);

			UtilPlayer.health(killer, increase);
		}
	}
}
